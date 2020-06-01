package com.safely.batch.connector.steps.computePropertiesChangelist;

import com.safely.api.domain.Organization;
import com.safely.api.domain.Property;
import com.safely.api.domain.enumeration.ConnectorOperationMode;
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

  @Override
  public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext)
      throws Exception {

    Organization organization = jobContext.getOrganization();
    if (organization.getConnectorOperationMode() != ConnectorOperationMode.PROPERTIES
        && organization.getConnectorOperationMode() != ConnectorOperationMode.ALL) {
      log.info("Skipping compute property changes. Connector Operation Mode: {}",
          organization.getConnectorOperationMode().name());
      return RepeatStatus.FINISHED;
    }

    log.info("Processing properties to find changes for organization: {} - ({})",
        organization.getName(), organization.getId());
    processProperties(jobContext);

    return RepeatStatus.FINISHED;
  }

  protected JobContext processProperties(JobContext jobContext) throws Exception {

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
      Property safelyProperty = safelyPropertyLookup.get(pmsProperty.getReferenceId());

      // TODO: Implement any custom logic for this PMS around new and updated properties

      // if the current PMS property does not exist in the Safely list, it is new
      if (safelyProperty == null) {
        newProperties.add(pmsProperty);
      } else {
        // PMS publishes an updatedAt date, for now we will use the updatedAt and pmsObjectHashcode to check for changes
        if (safelyProperty.getPmsUpdateDate() != null && !safelyProperty.getPmsUpdateDate()
            .equals(pmsProperty.getPmsUpdateDate())) {
          updateProperty(safelyProperty, pmsProperty);
          updatedProperties.add(safelyProperty);
        } else if (!safelyProperty.getPmsObjectHashcode()
            .equals(pmsProperty.getPmsObjectHashcode())) {
          updateProperty(safelyProperty, pmsProperty);
          updatedProperties.add(safelyProperty);
        }
      }
    }

    //Find all deleted Properties.
    List<Property> removedProperties = new ArrayList<>();
    for (Property safelyProperty : safelyProperties) {
      Property pmsProperty = pmsPropertyLookup.get(safelyProperty.getReferenceId());

      // TODO: Implement any custon logic to deal with deleted or missing properties from PMS.

      // if we don't see the property in the data pull from PMS, then we will mark the property with PMS Status inactive
      if (pmsProperty == null) {
        safelyProperty.setPmsStatus(PropertyStatus.INACTIVE);
        updatedProperties.add(safelyProperty);
      }
    }

    jobContext.setNewProperties(newProperties);
    jobContext.setUpdatedProperties(updatedProperties);
    jobContext.setRemovedProperties(removedProperties);

    return jobContext;
  }

  protected Property updateProperty(Property safelyProperty, Property pmsProperty) {

    safelyProperty.setName(pmsProperty.getName());
    safelyProperty.setDescription(pmsProperty.getDescription());
    safelyProperty.setAccomodates(pmsProperty.getAccomodates());
    safelyProperty.setAccomodatesAdults(pmsProperty.getAccomodatesAdults());
    safelyProperty.setBathRooms(String.valueOf(pmsProperty.getBathRooms()));
    safelyProperty.setBedRooms(String.valueOf(pmsProperty.getBedRooms()));
    safelyProperty.setPropertyType(pmsProperty.getPropertyType());

    safelyProperty.setStatus(pmsProperty.getStatus());

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