package com.safely.batch.connector;

import com.safely.batch.connector.common.client.aws.SecretsManagerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AWSConfiguration {

  @Bean
  public SecretsManagerClient getSecretsManagerClient() {
    return new SecretsManagerClient();
  }

}
