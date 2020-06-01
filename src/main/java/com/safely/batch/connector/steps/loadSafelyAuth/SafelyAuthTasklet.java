package com.safely.batch.connector.steps.loadSafelyAuth;

import com.safely.batch.connector.common.client.aws.SecretsManagerClient;
import com.safely.batch.connector.common.client.safely.SafelyAuthClient;
import com.safely.batch.connector.common.domain.safely.auth.JWTToken;
import com.safely.batch.connector.SafelyPropertiesConfig;
import com.safely.batch.connector.common.services.safely.SafelyAuthenticationService;
import com.safely.batch.connector.steps.JobContext;
import java.util.Optional;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SafelyAuthTasklet implements Tasklet {

  private static final Logger log = LoggerFactory.getLogger(SafelyAuthTasklet.class);

  @Autowired
  public JobContext jobContext;

  @Autowired
  public SafelyAuthenticationService safelyAuthenticationService;

  @Autowired
  public SecretsManagerClient secretsManagerClient;

  @Autowired
  public SafelyPropertiesConfig safelyPropertiesConfig;

  @Override
  public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext)
      throws Exception {

    String secretName = safelyPropertiesConfig.getApiAccountSecretName();
    JSONObject secretResponse = secretsManagerClient.getSecret(secretName);

    String username = secretResponse.getString("username");
    String password = secretResponse.getString("password");
    Optional<JWTToken> maybeToken = safelyAuthenticationService.authenticate(username, password);

    if (maybeToken.isPresent()) {
      log.info("Authentication token for Safely API found.");
      jobContext.setSafelyToken(maybeToken.get());
    } else {
      log.error("No authentication token retrieved from Safely API.");
      throw new Exception("Authentication unsuccessful.");
    }
    return RepeatStatus.FINISHED;
  }
}