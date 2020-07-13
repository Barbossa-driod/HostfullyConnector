package com.safely.batch.connector.steps.savePropertiesToSafely;

import com.safely.api.domain.Organization;
import com.safely.api.domain.Property;
import com.safely.batch.connector.common.domain.safely.auth.JWTToken;
import com.safely.batch.connector.common.services.safely.SafelyConnectorPropertiesService;
import com.safely.batch.connector.steps.JobContext;
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

public class SavePropertiesToSafelyTasklet implements Tasklet {

  private static final Logger log = LoggerFactory.getLogger(SavePropertiesToSafelyTasklet.class);
  @Autowired
  public JobContext jobContext;

  @Autowired
  private SafelyConnectorPropertiesService propertiesService;

  private static final String UPDATED = "updated";
  private static final String CREATED = "created";
  private static final String FAILED = "failed";
  private static final String PROCESSED = "processed";
  private static final String FAILED_IDS = "failed_ids";
  private static final String STEP_NAME = "save_properties_to_safely";

  public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext)
      throws Exception {

    Map<String, Object> stepStatistics = new HashMap<>();

    Organization organization = jobContext.getOrganization();
    JWTToken token = jobContext.getSafelyToken();

    List<Property> newProperties = jobContext.getNewProperties();

    List<String> failedIds = new ArrayList<>();

    log.info("Writing {} new properties for organization: {} - ({})", newProperties.size(),
        organization.getName(), organization.getEntityId());
    int successfullyCreated = 0;

    for (Property property : newProperties) {
      try{
        propertiesService.create(token.getIdToken(), property);
        successfullyCreated++;
      } catch(Exception e){
        log.error("Failed to create property with ReferenceID {}", property.getReferenceId());
        failedIds.add(property.getReferenceId());
      }
    }

    List<Property> updatedProperties = jobContext.getUpdatedProperties();

    int updatedSuccessfully = 0;
    log.info("Writing {} updated properties for organization: {} - ({})", updatedProperties.size(),
        organization.getName(), organization.getEntityId());
    for (Property property : updatedProperties) {
      try{
        propertiesService.save(token.getIdToken(), property);
        updatedSuccessfully++;
      } catch (Exception e) {
        log.error("Failed to save updates for property with referenceId {}",
            property.getReferenceId());
        failedIds.add(property.getReferenceId());
      }
    }
    stepStatistics.put(CREATED, successfullyCreated);
    stepStatistics.put(UPDATED, updatedSuccessfully);
    stepStatistics.put(FAILED, failedIds.size());
    stepStatistics.put(PROCESSED, newProperties.size() + updatedProperties.size());
    stepStatistics.put(FAILED_IDS, failedIds);

    jobContext.getJobStatistics().put(STEP_NAME, stepStatistics);
    return RepeatStatus.FINISHED;
  }
}