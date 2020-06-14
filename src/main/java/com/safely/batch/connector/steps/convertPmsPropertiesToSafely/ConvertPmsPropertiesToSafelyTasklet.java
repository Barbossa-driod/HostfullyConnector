package com.safely.batch.connector.steps.convertPmsPropertiesToSafely;

import com.safely.api.domain.Organization;
import com.safely.api.domain.Property;
import com.safely.api.domain.PropertyPhoto;
import com.safely.api.domain.enumeration.ConnectorOperationMode;
import com.safely.api.domain.enumeration.PhotoType;
import com.safely.api.domain.enumeration.PropertyStatus;
import com.safely.api.domain.enumeration.PropertyType;
import com.safely.batch.connector.pms.property.PmsProperty;
import com.safely.batch.connector.pms.property.PmsPropertyPhoto;
import com.safely.batch.connector.steps.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConvertPmsPropertiesToSafelyTasklet implements Tasklet {

  private static final Logger log = LoggerFactory
      .getLogger(ConvertPmsPropertiesToSafelyTasklet.class);

  @Autowired
  public JobContext jobContext;

  @Override
  public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext)
      throws Exception {

    Organization organization = jobContext.getOrganization();
    if (organization.getConnectorOperationMode() != ConnectorOperationMode.PROPERTIES
        && organization.getConnectorOperationMode() != ConnectorOperationMode.ALL) {
      log.info("Skipping convert properties from PMS to Safely. Connector Operation Mode: {}",
          organization.getConnectorOperationMode().name());
      return RepeatStatus.FINISHED;
    }

    List<PmsProperty> pmsProperties = jobContext.getPmsProperties();
    //Map<Integer, List<PmsPhoto>> propertyImages = jobContext.getPmsPropertyPhotos();

    List<Property> pmsConvertedProperties = new ArrayList<>();

    for (PmsProperty pmsProperty : pmsProperties) {
      // TODO: Implement any custom logic for converting a PMS property to a Safely property

      List<PmsPropertyPhoto> images = new ArrayList<>();
      images = pmsProperty.getPhotos();
//      if (propertyImages != null && propertyImages.containsKey(pmsProperty.getId())) {
//        images.addAll(propertyImages.get(pmsProperty.getId()));
//      }
      Property property = convertToSafelyProperty(organization, pmsProperty, images);
      pmsConvertedProperties.add(property);
    }

    jobContext.setPmsSafelyProperties(pmsConvertedProperties);

    return RepeatStatus.FINISHED;
  }

  protected Property convertToSafelyProperty(Organization organization, PmsProperty pmsProperty,
      List<PmsPropertyPhoto> propertyImages) {

    // TODO: Implement custom logic to convert the PMS Property to a SafelyProperty.

    Property safelyProperty = new Property();
    safelyProperty.setOrganizationId(organization.getId());
    safelyProperty.setReferenceId(String.valueOf(pmsProperty.getUid()));
    safelyProperty.setName(pmsProperty.getName());

    safelyProperty.setDescription(pmsProperty.getShortDescription());

    safelyProperty.setAccomodates(pmsProperty.getMaximumGuests());
    safelyProperty.setAccomodatesAdults(pmsProperty.getMaximumGuests());

    // only using full baths for current calculation
    safelyProperty.setBathRooms(String.valueOf(pmsProperty.getBathrooms()));
    safelyProperty.setBedRooms(String.valueOf(pmsProperty.getBedrooms()));

    //Address
    safelyProperty.setStreetLine1(pmsProperty.getAddress1());
    safelyProperty.setStreetLine2(pmsProperty.getAddress2());
    safelyProperty.setCity(pmsProperty.getCity());
    safelyProperty.setPostalCode(pmsProperty.getPostalCode().toString());
    //TODO: Update to use consistent logic with other connectors
    safelyProperty.setStateCode(pmsProperty.getState());
    //note: we get the 2 letter country code abbreviation
    safelyProperty.setCountryCode(pmsProperty.getCountryCode());

    setPropertyType(pmsProperty, safelyProperty, organization);
    setPropertyStatus(pmsProperty, safelyProperty);

    setPropertyImages(pmsProperty, propertyImages, safelyProperty);

    //PMS object details
    // hashcode for comparison
    safelyProperty.setPmsObjectHashcode(pmsProperty.hashCode());

    //PMS created date
    // TODO: Revert when we move to >= JAVA 11
    //if (pmsProperty.getCreatedAt() != null && !pmsProperty.getCreatedAt().isBlank()) {

      safelyProperty
          .setPmsCreateDate(pmsProperty.getCreatedDate());


    //PMS updated date
    // TODO: Revert when we move to >= JAVA 11
    //if (pmsProperty.getUpdatedAt() != null && !pmsProperty.getUpdatedAt().isBlank()) {
    //if (pmsProperty.getUpdatedAt() != null && !pmsProperty.getUpdatedAt().isEmpty())

    //TODO: hostfully does not have an updatedAt so I do not know what to do about that
    //OffsetDateTime updatedDate = OffsetDateTime.parse(pmsProperty.getUpdatedAt());
//      safelyProperty
//          .setPmsUpdateDate(updatedDate.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());


    return safelyProperty;
  }

  private void setPropertyImages(PmsProperty pmsProperty, List<PmsPropertyPhoto> propertyImages,
      Property safelyProperty) {

    // TODO: Implement custom logic to handle PMS photos

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

    // TODO: Implement custom logic to handle property types

    String typeCode = null;

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
          log.error(ex.getMessage());
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
