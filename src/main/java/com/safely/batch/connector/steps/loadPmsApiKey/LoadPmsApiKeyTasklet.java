package com.safely.batch.connector.steps.loadPmsApiKey;

import com.safely.batch.connector.SafelyPropertiesConfig;
import com.safely.batch.connector.common.client.aws.SecretsManagerClient;
import com.safely.batch.connector.common.domain.safely.auth.JWTToken;
import com.safely.batch.connector.common.services.safely.SafelyAuthenticationService;
import com.safely.batch.connector.steps.JobContext;
import java.util.Optional;
import org.checkerframework.checker.units.qual.A;
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
public class LoadPmsApiKeyTasklet implements Tasklet {
  private static final Logger log = LoggerFactory.getLogger(LoadPmsApiKeyTasklet.class);

  @Autowired
  public JobContext jobContext;

  @Autowired
  public SecretsManagerClient secretsManagerClient;

  @Autowired
  public SafelyPropertiesConfig safelyPropertiesConfig;

  @Override
  public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext)
      throws Exception {
    String secretName = safelyPropertiesConfig.getPmsPartnerApiKey();
    JSONObject secretResponse = secretsManagerClient.getSecret(secretName);

    String apiKey = secretResponse.getString("api_key");

    if (apiKey != null && !apiKey.equals("")){
      jobContext.setHostfullyApiKey(apiKey);
    } else {
      log.error("No API key was retrieved for the Hostfully API.");
      throw new Exception("Authentication API key retrieval was unsuccessful from AWS secrets manager.");
    }
    return RepeatStatus.FINISHED;
  }
}
