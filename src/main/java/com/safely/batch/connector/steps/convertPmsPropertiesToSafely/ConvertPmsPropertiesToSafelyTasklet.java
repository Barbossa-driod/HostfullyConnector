package com.safely.batch.connector.steps.convertPmsPropertiesToSafely;

import com.safely.api.domain.Organization;
import com.safely.api.domain.Property;
import com.safely.api.domain.PropertyPhoto;
import com.safely.api.domain.enumeration.PhotoType;
import com.safely.api.domain.enumeration.PropertyStatus;
import com.safely.api.domain.enumeration.PropertyType;
import com.safely.batch.connector.pms.property.PmsProperty;
import com.safely.batch.connector.pms.property.PmsPropertyPhoto;
import com.safely.batch.connector.steps.JobContext;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;


public class ConvertPmsPropertiesToSafelyTasklet implements Tasklet {

  private static final Logger log = LoggerFactory
      .getLogger(ConvertPmsPropertiesToSafelyTasklet.class);

  @Autowired
  public JobContext jobContext;

  private static final String CONVERTED= "converted";
  private static final String PROCESSED = "processed";
  private static final String FAILED = "failed";
  private static final String FAILED_IDS = "failed_ids";
  private static final String STEP_NAME = "convert_pms_properties_to_safely";

  @Override
  public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext)
      throws Exception {
    Map<String, Object> stepStatistics = new HashMap<>();
    Organization organization = jobContext.getOrganization();

    List<PmsProperty> pmsProperties = jobContext.getPmsProperties();

    List<Property> pmsConvertedProperties = new ArrayList<>();

    List<String> failedPropertyUids = new ArrayList<>();

    for (PmsProperty pmsProperty : pmsProperties) {
      try{
        List<PmsPropertyPhoto> images = pmsProperty.getPhotos();

        Property property = convertToSafelyProperty(organization, pmsProperty, images);
        pmsConvertedProperties.add(property);
      } catch(Exception e){
        String message = String
            .format("Failed to convert property with Uid %s", pmsProperty.getUid());
        log.error(message, e);
        failedPropertyUids.add(pmsProperty.getUid());
        Exception wrapperException = new Exception(message, e);
        chunkContext.getStepContext().getStepExecution().addFailureException(wrapperException);
      }
    }
    jobContext.setPmsSafelyProperties(pmsConvertedProperties);

    stepStatistics.put(CONVERTED, pmsConvertedProperties.size());
    stepStatistics.put(PROCESSED, pmsProperties.size());
    stepStatistics.put(FAILED, failedPropertyUids.size());
    stepStatistics.put(FAILED_IDS, failedPropertyUids);

    jobContext.getJobStatistics().put(STEP_NAME, stepStatistics);

    return RepeatStatus.FINISHED;
  }

  protected Property convertToSafelyProperty(Organization organization, PmsProperty pmsProperty,
      List<PmsPropertyPhoto> propertyImages) {

    Property safelyProperty = new Property();
    safelyProperty.setOrganizationId(organization.getEntityId());
    safelyProperty.setLegacyOrganizationId(organization.getLegacyOrganizationId());
    safelyProperty.setReferenceId(String.valueOf(pmsProperty.getUid()));
    safelyProperty.setName(pmsProperty.getName());

    safelyProperty.setDescription(pmsProperty.getShortDescription());

    safelyProperty.setAccomodates(pmsProperty.getMaximumGuests());
    safelyProperty.setAccomodatesAdults(pmsProperty.getMaximumGuests());

    // only using full baths for current calculation
    safelyProperty.setBathRooms(pmsProperty.getBathrooms());
    safelyProperty.setBedRooms(String.valueOf(pmsProperty.getBedrooms()));

    //Address
    safelyProperty.setStreetLine1(pmsProperty.getAddress1());
    safelyProperty.setStreetLine2(pmsProperty.getAddress2());
    safelyProperty.setCity(pmsProperty.getCity());
    safelyProperty.setPostalCode(pmsProperty.getPostalCode().toString());
    safelyProperty.setStateCode(pmsProperty.getState());


    if(pmsProperty.getCountryCode() != null && pmsProperty.getCountryCode().length() > 0) {
      //note: we get the 2 letter country code abbreviation
      safelyProperty.setCountryCode(pmsProperty.getCountryCode());
    } else {
      safelyProperty.setCountryCode("US");
    }

    setPropertyType(pmsProperty, safelyProperty, organization);
    setPropertyStatus(pmsProperty, safelyProperty);

    setPropertyImages(pmsProperty, propertyImages, safelyProperty);

    //PMS object details
    // hashcode for comparison
    safelyProperty.setPmsObjectHashcode(pmsProperty.hashCode());

    //PMS created date
    if(pmsProperty.getCreatedDate() != null) {
      safelyProperty.setPmsCreateDate(LocalDateTime.parse(pmsProperty.getCreatedDate(),
              DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")));
    }
    return safelyProperty;
  }

  private void setPropertyImages(PmsProperty pmsProperty, List<PmsPropertyPhoto> propertyImages,
      Property safelyProperty) {

    List<PropertyPhoto> propertyPhotos = new ArrayList<>();
    for (PmsPropertyPhoto image : propertyImages) {
      PropertyPhoto photo = new PropertyPhoto();
      photo.setId(String.valueOf(image.getUid()));
      photo.setCaption(image.getDescription());
      photo.setActive(Boolean.TRUE);
      photo.setUrl(image.getUrl());
      photo.setType(PhotoType.IMAGE);
      photo.setDisplayOrder(image.getDisplayOrder());
      if (pmsProperty.getPicture().equalsIgnoreCase(image.getUrl())) {
        photo.setPrimary(Boolean.TRUE);
      } else {
        photo.setPrimary(Boolean.FALSE);
      }

      propertyPhotos.add(photo);
    }

    ensurePrimaryPhotoSet(propertyPhotos);
    safelyProperty.setPropertyPhotos(propertyPhotos);
  }

  private void setPropertyStatus(PmsProperty pmsProperty, Property safelyProperty) {
    //Property Status
    PropertyStatus status = null;
    if (pmsProperty.getIsActive()) {
      status = PropertyStatus.ACTIVE;
    } else {
      status = PropertyStatus.INACTIVE;
    }
    safelyProperty.setStatus(status);
    safelyProperty.setPmsStatus(status);
  }

  private void setPropertyType(PmsProperty pmsProperty, Property safelyProperty,
      Organization organization) {
    Map<String, String> propertyTypeMap = organization.getPmsPropertyTypesToSafelyTypesMapping();
    PropertyType propertyType = PropertyType.OTHER;

    String typeCode = pmsProperty.getType();

    if (typeCode != null) {
      if (propertyTypeMap != null) {
        String safelyPropertyType = PropertyType.OTHER.name();
        //check the map for the type and provide the mapped type, otherwise provide OTHER
        if (propertyTypeMap.containsKey(typeCode)) {
          safelyPropertyType = propertyTypeMap.get(typeCode);
        } else {
          log.warn("No property type mapping found for PMS value {} for client {} ({})", typeCode,
              organization.getName(), organization.getId());
        }

        // convert to the enum, if the conversion fails, fallback to OTHER
        try {
          propertyType = PropertyType.valueOf(safelyPropertyType);
        } catch (Exception ex) {
          log.error(
              "Failed to convert Property Type string to enum. PMS value: {}. Safely value: {} for client {} ({})",
              typeCode, safelyPropertyType, organization.getName(), organization.getId());
          log.error(ex.getMessage(), ex);
          propertyType = PropertyType.OTHER;
        }
      } else {
        log.warn("No property mappings setup for client {} ({})", organization.getName(),
            organization.getId());
        propertyType = PropertyType.OTHER;
      }
    }
    safelyProperty.setPropertyType(propertyType);
  }

  private void ensurePrimaryPhotoSet(List<PropertyPhoto> propertyPhotos) {
    if (propertyPhotos.size() > 0) {
      // check to ensure there is an image set as primary
      int primaryIndex = 0;
      for (PropertyPhoto photo : propertyPhotos) {
        if (photo.getPrimary()) {
          break;
        }
        primaryIndex++;
      }
      if (primaryIndex == propertyPhotos.size()) {
        // no primary photo set, set the first one
        propertyPhotos.get(0).setPrimary(Boolean.TRUE);
      }
    }
  }
}
