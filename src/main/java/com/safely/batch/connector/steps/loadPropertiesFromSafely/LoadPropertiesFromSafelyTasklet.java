package com.safely.batch.connector.steps.loadPropertiesFromSafely;

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

public class LoadPropertiesFromSafelyTasklet implements Tasklet {

  private static final Logger log = LoggerFactory.getLogger(LoadPropertiesFromSafelyTasklet.class);

  @Autowired
  public JobContext jobContext;

  @Autowired
  private SafelyConnectorPropertiesService propertiesService;

  @Override
  public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext)
      throws Exception {

    Organization organization = jobContext.getOrganization();
    JWTToken token = jobContext.getSafelyToken();

    List<Property> currentSafelyProperties = propertiesService
        .getAll(token.getIdToken(), organization.getEntityId());
    log.info("Loaded {} Safely properties for organization: {} - ({}, {})",
        currentSafelyProperties.size(), organization.getName(), organization.getEntityId(),
        organization.getLegacyOrganizationId());

    jobContext.setCurrentSafelyProperties(currentSafelyProperties);

    return RepeatStatus.FINISHED;
  }
}
