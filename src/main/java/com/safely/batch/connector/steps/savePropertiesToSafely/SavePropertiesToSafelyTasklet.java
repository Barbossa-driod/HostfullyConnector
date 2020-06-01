package com.safely.batch.connector.steps.savePropertiesToSafely;

import com.safely.api.domain.Organization;
import com.safely.api.domain.Property;
import com.safely.batch.connector.common.domain.safely.auth.JWTToken;
import com.safely.batch.connector.common.services.safely.SafelyConnectorPropertiesService;
import com.safely.batch.connector.steps.JobContext;
import java.util.List;
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

  public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext)
      throws Exception {

    Organization organization = jobContext.getOrganization();
    JWTToken token = jobContext.getSafelyToken();

    List<Property> newProperties = jobContext.getNewProperties();
    log.info("Writing {} new properties for organization: {} - ({})", newProperties.size(),
        organization.getName(), organization.getEntityId());
    for (Property property : newProperties) {
      propertiesService.create(token.getIdToken(), property);
    }

    List<Property> updatedProperties = jobContext.getUpdatedProperties();
    log.info("Writing {} updated properties for organization: {} - ({})", updatedProperties.size(),
        organization.getName(), organization.getEntityId());
    for (Property property : updatedProperties) {
      propertiesService.save(token.getIdToken(), property);
    }
    return RepeatStatus.FINISHED;
  }
}