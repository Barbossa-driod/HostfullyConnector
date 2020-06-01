package com.safely.batch.connector;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "safely")
@Data
public class SafelyPropertiesConfig {

  private String baseUrl;

  private String apiAccountSecretName;

  private Integer pmsRateLimitPerMinute;
  private Integer safelyRateLimitPerMinute;
}
