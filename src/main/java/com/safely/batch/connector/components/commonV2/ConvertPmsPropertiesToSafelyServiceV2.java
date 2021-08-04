package com.safely.batch.connector.components.commonV2;


import com.safely.api.domain.Organization;
import com.safely.api.domain.Property;
import com.safely.api.domain.PropertyPhoto;
import com.safely.api.domain.enumeration.PhotoType;
import com.safely.api.domain.enumeration.PropertyStatus;
import com.safely.api.domain.enumeration.PropertyType;
import com.safely.batch.connector.JobContext;
import com.safely.batch.connector.pmsV2.property.PmsPropertyPhotoV2;
import com.safely.batch.connector.pmsV2.property.PmsPropertyV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class ConvertPmsPropertiesToSafelyServiceV2 {

    private static final Logger log = LoggerFactory.getLogger(ConvertPmsPropertiesToSafelyServiceV2.class);

    private static final String CONVERTED = "converted";
    private static final String PROCESSED = "processed";
    private static final String FAILED = "failed";
    private static final String FAILED_IDS = "failed_ids";
    private static final String STEP_NAME = "convert_pms_properties_to_safely";

    public void execute(JobContext jobContext) {
        Map<String, Object> stepStatistics = new HashMap<>();
        Organization organization = jobContext.getOrganization();

        log.info("OrganizationId: {}. Convert PMS properties to Safely structure.", jobContext.getOrganizationId());

        List<PmsPropertyV2> pmsProperties = jobContext.getPmsPropertiesV2();

        List<Property> pmsConvertedProperties = new ArrayList<>();

        List<String> failedPropertyUids = new ArrayList<>();

        for (PmsPropertyV2 pmsPropertyV2 : pmsProperties) {
            try {
                List<PmsPropertyPhotoV2> images = pmsPropertyV2.getPhotos();

                Property property = convertToSafelyProperty(organization, pmsPropertyV2, images);
                pmsConvertedProperties.add(property);
            } catch (Exception e) {
                String message = String.format("OrganizationId: %s. Failed to convert property with Id %s",
                        jobContext.getOrganizationId(), pmsPropertyV2.getUid());
                log.error(message, e);
                failedPropertyUids.add(pmsPropertyV2.getUid());
            }
        }
        jobContext.setPmsSafelyProperties(pmsConvertedProperties);
        log.info("OrganizationId: {}. Converted properties count: {}", jobContext.getOrganizationId(), pmsConvertedProperties.size());

        stepStatistics.put(CONVERTED, pmsConvertedProperties.size());
        stepStatistics.put(PROCESSED, pmsProperties.size());
        stepStatistics.put(FAILED, failedPropertyUids.size());
        stepStatistics.put(FAILED_IDS, failedPropertyUids);

        jobContext.getJobStatistics().put(STEP_NAME, stepStatistics);
    }

    protected Property convertToSafelyProperty(Organization organization, PmsPropertyV2 pmsPropertyV2,
                                               List<PmsPropertyPhotoV2> propertyImages) {

        Property safelyProperty = new Property();
        safelyProperty.setOrganizationId(organization.getEntityId());
        safelyProperty.setLegacyOrganizationId(organization.getLegacyOrganizationId());
        safelyProperty.setReferenceId(String.valueOf(pmsPropertyV2.getUid()));
        safelyProperty.setName(pmsPropertyV2.getName());

        safelyProperty.setDescription(pmsPropertyV2.getShortDescription());

        safelyProperty.setAccomodates(pmsPropertyV2.getMaximumGuests());
        safelyProperty.setAccomodatesAdults(pmsPropertyV2.getMaximumGuests());

        // only using full baths for current calculation
        safelyProperty.setBathRooms(pmsPropertyV2.getBathrooms());
        safelyProperty.setBedRooms(String.valueOf(pmsPropertyV2.getBedrooms()));

        //Address
        safelyProperty.setStreetLine1(pmsPropertyV2.getAddress1());
        safelyProperty.setStreetLine2(pmsPropertyV2.getAddress2());
        safelyProperty.setCity(pmsPropertyV2.getCity());
        if (pmsPropertyV2.getPostalCode() != null) {
            safelyProperty.setPostalCode(pmsPropertyV2.getPostalCode());
        }
        safelyProperty.setStateCode(pmsPropertyV2.getState());


        if (pmsPropertyV2.getCountryCode() != null && pmsPropertyV2.getCountryCode().length() > 0) {
            //note: we get the 2 letter country code abbreviation
            safelyProperty.setCountryCode(pmsPropertyV2.getCountryCode());
        } else {
            safelyProperty.setCountryCode("US");
        }

        setPropertyType(pmsPropertyV2, safelyProperty, organization);
        setPropertyStatus(pmsPropertyV2, safelyProperty);

        setPropertyImages(pmsPropertyV2, propertyImages, safelyProperty);

        //PMS object details
        // hashcode for comparison
        safelyProperty.setPmsObjectHashcode(pmsPropertyV2.hashCode());

        //PMS created date
        if (pmsPropertyV2.getCreatedDate() != null) {
            safelyProperty.setPmsCreateDate(LocalDateTime.parse(pmsPropertyV2.getCreatedDate(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")));
        }
        return safelyProperty;
    }

    private void setPropertyImages(PmsPropertyV2 pmsPropertyV2, List<PmsPropertyPhotoV2> propertyImages,
                                   Property safelyProperty) {

        List<PropertyPhoto> propertyPhotos = new ArrayList<>();
        for (PmsPropertyPhotoV2 image : propertyImages) {
            PropertyPhoto photo = new PropertyPhoto();
            photo.setId(String.valueOf(image.getUid()));
            photo.setCaption(image.getDescription());
            photo.setActive(Boolean.TRUE);
            photo.setUrl(image.getUrl());
            photo.setType(PhotoType.IMAGE);
            photo.setDisplayOrder(image.getDisplayOrder());
            if (pmsPropertyV2.getPicture().equalsIgnoreCase(image.getUrl())) {
                photo.setPrimary(Boolean.TRUE);
            } else {
                photo.setPrimary(Boolean.FALSE);
            }

            propertyPhotos.add(photo);
        }

        ensurePrimaryPhotoSet(propertyPhotos);
        safelyProperty.setPropertyPhotos(propertyPhotos);
    }

    private void setPropertyStatus(PmsPropertyV2 pmsPropertyV2, Property safelyProperty) {
        //Property Status
        PropertyStatus status;
        if (pmsPropertyV2.getIsActive()) {
            status = PropertyStatus.ACTIVE;
        } else {
            status = PropertyStatus.INACTIVE;
        }
        safelyProperty.setStatus(status);
        safelyProperty.setPmsStatus(status);
    }

    private void setPropertyType(PmsPropertyV2 pmsPropertyV2, Property safelyProperty,
                                 Organization organization) {
        Map<String, String> propertyTypeMap = organization.getPmsPropertyTypesToSafelyTypesMapping();
        PropertyType propertyType = PropertyType.OTHER;

        String typeCode = pmsPropertyV2.getType();

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
