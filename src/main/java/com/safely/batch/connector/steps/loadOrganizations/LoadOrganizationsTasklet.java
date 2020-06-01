package com.safely.batch.connector.steps.loadOrganizations;

import com.safely.api.domain.Organization;
import com.safely.batch.connector.common.domain.safely.auth.JWTToken;
import com.safely.batch.connector.common.services.safely.SafelyConnectorOrganizationsService;
import com.safely.batch.connector.steps.JobContext;
import java.util.Optional;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
@StepScope
public class LoadOrganizationsTasklet implements Tasklet {

  private static final Logger log = LoggerFactory.getLogger(LoadOrganizationsTasklet.class);

  @Value("${organizationId}")
  String organizationId;

  @Autowired
  private JobContext jobContext;

  @Autowired
  private SafelyConnectorOrganizationsService connectorOrganizationsService;

  @Override
  public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext)
      throws Exception {

    JWTToken token = jobContext.getSafelyToken();
    Optional<Organization> maybeOrganization = connectorOrganizationsService
        .getById(token.getIdToken(), organizationId);

    if (maybeOrganization.isPresent()) {
      log.info("Organization found: {}", maybeOrganization.get());
      jobContext.setOrganization(maybeOrganization.get());
    } else {
      throw new Exception("Could not load Organization with id: " + organizationId);
    }

    return RepeatStatus.FINISHED;
  }
}
