package com.safely.batch.connector.steps.computePropertiesChangelist;

import com.safely.api.domain.Organization;
import com.safely.api.domain.Property;
import com.safely.api.domain.enumeration.PropertyStatus;
import com.safely.batch.connector.steps.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComputePropertiesChangeListTasklet implements Tasklet {

  private static final Logger log = LoggerFactory
      .getLogger(ComputePropertiesChangeListTasklet.class);

  @Autowired
  public JobContext jobContext;

  private static final String UPDATED = "updated";
  private static final String CREATED = "created";
  private static final String DELETED = "deleted";
  private static final String PROCESSED = "processed";
  private static final String STEP_NAME = "compute_properties_change_list";

  @Override
  public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext)
      throws Exception {
    Organization organization = jobContext.getOrganization();

    log.info("Processing properties to find changes for organization: {} - ({})",
        organization.getName(), organization.getEntityId());

    processProperties(jobContext, chunkContext);

    return RepeatStatus.FINISHED;
  }

  protected JobContext processProperties(JobContext jobContext, ChunkContext chunkContext) throws Exception {

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

    for (Property pmsProperty : pmsProperties) {
      try {
        Property safelyProperty = safelyPropertyLookup.get(pmsProperty.getReferenceId());

        // if the current PMS property does not exist in the Safely list, it is new
        if (safelyProperty == null) {
          newProperties.add(pmsProperty);
        } else {
          // pmsObjectHashcode to check for changes
          if (!safelyProperty.getPmsObjectHashcode()
              .equals(pmsProperty.getPmsObjectHashcode())) {
            updateProperty(safelyProperty, pmsProperty);
            updatedProperties.add(safelyProperty);
          }
        }
      } catch (Exception e) {
        String message = String
            .format("failed to compute changes for PMS property with referenceId %s",
                pmsProperty.getReferenceId());
        log.error(message);
        Exception wrapperException = new Exception(message, e);
        chunkContext.getStepContext().getStepExecution().addFailureException(wrapperException);
      }

    }

    int successfullyDeleted = 0;

    //Find all deleted Properties.
    for (Property safelyProperty : safelyProperties) {
      try{
        Property pmsProperty = pmsPropertyLookup.get(safelyProperty.getReferenceId());

        //I do not think we need to implement any logic here.

        // if we don't see the property in the data pull from PMS, then we will mark the property with PMS Status inactive
        if (pmsProperty == null) {
          safelyProperty.setPmsStatus(PropertyStatus.INACTIVE);
          updatedProperties.add(safelyProperty);
          successfullyDeleted++;
        }
      } catch(Exception e) {
        String message = String
            .format("Failed to compute if property with referenceId %s has been deleted",
                safelyProperty.getReferenceId());
        log.error(message);
        Exception wrapperException = new Exception();
        chunkContext.getStepContext().getStepExecution().addFailureException(wrapperException);
      }

    }

    jobContext.setNewProperties(newProperties);
    jobContext.setUpdatedProperties(updatedProperties);

    stepStatistics.put(CREATED, newProperties.size());
    stepStatistics.put(UPDATED, updatedProperties.size() - successfullyDeleted);
    stepStatistics.put(DELETED, successfullyDeleted);
    stepStatistics.put(PROCESSED, pmsProperties.size());
    jobContext.getJobStatistics().put(STEP_NAME,stepStatistics);

    return jobContext;
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