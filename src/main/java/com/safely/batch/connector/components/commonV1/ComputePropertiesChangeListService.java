package com.safely.batch.connector.components.commonV1;

import com.safely.api.domain.Organization;
import com.safely.api.domain.Property;
import com.safely.api.domain.enumeration.PropertyStatus;
import com.safely.batch.connector.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ComputePropertiesChangeListService {

    private static final Logger log = LoggerFactory.getLogger(ComputePropertiesChangeListService.class);
    private static final String UPDATED = "updated";
    private static final String CREATED = "created";
    private static final String DELETED = "deleted";
    private static final String FAILED = "failed";
    private static final String FAILED_IDS = "failed_ids";
    private static final String PROCESSED = "processed";
    private static final String STEP_NAME = "compute_properties_change_list";

    public void execute(JobContext jobContext) {

        Organization organization = jobContext.getOrganization();

        log.info("OrganizationId: {}. Processing properties to find changes for organization with name: {}",
                organization.getEntityId(), organization.getName());

        Map<String, Object> stepStatistics = new HashMap<>();

        List<Property> safelyProperties = jobContext.getCurrentSafelyProperties();
        List<Property> pmsProperties = jobContext.getPmsSafelyProperties();

        //Add all the safely properties to a Map for easy lookup by Reference Id
        Map<String, Property> safelyPropertyLookup = new HashMap<>();
        for (Property safelyProperty : safelyProperties) {
            safelyPropertyLookup.put(safelyProperty.getReferenceId(), safelyProperty);
        }

        //Add all the PMS properties to a Map for easy lookup by Reference Id
        Map<String, Property> pmsPropertyLookup = new HashMap<>();
        for (Property pmsProperty : pmsProperties) {
            pmsPropertyLookup.put(pmsProperty.getReferenceId(), pmsProperty);
        }

        //Find all new properties
        List<Property> newProperties = new ArrayList<>();
        List<Property> updatedProperties = new ArrayList<>();
        List<String> erroredProperties = new ArrayList<>();

        for (Property pmsProperty : pmsProperties) {
            try {
                Property safelyProperty = safelyPropertyLookup.get(pmsProperty.getReferenceId());

                // if the current PMS property does not exist in the Safely list, it is new
                if (safelyProperty == null) {
                    newProperties.add(pmsProperty);
                } else if (!safelyProperty.equals(pmsProperty)) {
                    updateProperty(safelyProperty, pmsProperty);
                    updatedProperties.add(safelyProperty);
                }
            } catch (Exception e) {
                String message = String.format("OrganizationId: %s. Failed to compute changes for property with referenceId %s",
                        jobContext.getOrganizationId(), pmsProperty.getReferenceId());
                log.error(message, e);
                erroredProperties.add(pmsProperty.getReferenceId());
            }
        }

        log.info("OrganizationId: {}. Found {} new properties.", jobContext.getOrganizationId(), newProperties.size());
        log.info("OrganizationId: {}. Found {} updated properties.", jobContext.getOrganizationId(), updatedProperties.size());

        int successfullyDeleted = 0;

        //Find all deleted Properties.
        for (Property safelyProperty : safelyProperties) {
            try {
                Property pmsProperty = pmsPropertyLookup.get(safelyProperty.getReferenceId());

                // if we don't see the property in the data pull from PMS, then we will mark the property with PMS Status inactive
                if (pmsProperty == null) {
                    log.warn("OrganizationId: {}. Property that previously existed was not found! Property referenceId: {}, name: {}",
                            jobContext.getOrganizationId(), safelyProperty.getReferenceId(), safelyProperty.getName());
                    safelyProperty.setPmsStatus(PropertyStatus.INACTIVE);
                    updatedProperties.add(safelyProperty);
                    successfullyDeleted++;
                }
            } catch (Exception e) {
                String message = String.format("OrganizationId: %s. Failed to compute if property with referenceId %s has been deleted",
                        jobContext.getOrganizationId(), safelyProperty.getReferenceId());
                log.error(message, e);
                erroredProperties.add(safelyProperty.getReferenceId());
            }
        }

        log.info("OrganizationId: {}. Found {} deleted properties.", jobContext.getOrganizationId(), successfullyDeleted);

        jobContext.setNewProperties(newProperties);
        jobContext.setUpdatedProperties(updatedProperties);

        stepStatistics.put(CREATED, newProperties.size());
        stepStatistics.put(UPDATED, updatedProperties.size() - successfullyDeleted);
        stepStatistics.put(DELETED, successfullyDeleted);
        stepStatistics.put(PROCESSED, pmsProperties.size());
        stepStatistics.put(FAILED, erroredProperties.size());
        stepStatistics.put(FAILED_IDS, erroredProperties);
        jobContext.getJobStatistics().put(STEP_NAME, stepStatistics);
    }

    protected Property updateProperty(Property safelyProperty, Property pmsProperty) {

        safelyProperty.setLegacyOrganizationId(pmsProperty.getLegacyOrganizationId());
        safelyProperty.setReferenceId(pmsProperty.getReferenceId());

        safelyProperty.setName(pmsProperty.getName());
        safelyProperty.setDescription(pmsProperty.getDescription());
        safelyProperty.setAccomodates(pmsProperty.getAccomodates());
        safelyProperty.setAccomodatesAdults(pmsProperty.getAccomodatesAdults());
        safelyProperty.setBathRooms(String.valueOf(pmsProperty.getBathRooms()));
        safelyProperty.setBedRooms(String.valueOf(pmsProperty.getBedRooms()));
        safelyProperty.setPropertyType(pmsProperty.getPropertyType());

        safelyProperty.setStatus(pmsProperty.getStatus());
        safelyProperty.setPmsStatus(pmsProperty.getPmsStatus());

        // images
        safelyProperty.setPropertyPhotos(pmsProperty.getPropertyPhotos());
        safelyProperty.setPropertyThumbnails(pmsProperty.getPropertyThumbnails());

        //Address
        safelyProperty.setStreetLine1(pmsProperty.getStreetLine1());
        safelyProperty.setStreetLine2(pmsProperty.getStreetLine2());
        safelyProperty.setCity(pmsProperty.getCity());
        safelyProperty.setPostalCode(String.valueOf(pmsProperty.getPostalCode()));
        safelyProperty.setStateCode(pmsProperty.getStateCode());
        safelyProperty.setCountryCode(pmsProperty.getCountryCode());

        //PMS object details
        safelyProperty.setPmsObjectHashcode(pmsProperty.getPmsObjectHashcode());
        safelyProperty.setPmsUpdateDate(pmsProperty.getPmsUpdateDate());

        return safelyProperty;
    }
}