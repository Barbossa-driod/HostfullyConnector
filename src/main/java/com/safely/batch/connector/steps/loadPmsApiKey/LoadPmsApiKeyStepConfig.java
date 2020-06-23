package com.safely.batch.connector.steps.loadPmsApiKey;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadPmsApiKeyStepConfig {

  @Autowired
  private StepBuilderFactory stepBuilderFactory;

  @Bean
  public LoadPmsApiKeyTasklet getPmsApiKeyTasklet() {
    return new LoadPmsApiKeyTasklet();
  }

  @Bean
  @Qualifier("loadPmsApiKey")
  public Step getPmsApiKeyStep(LoadPmsApiKeyTasklet loadPmsApiKeyTasklet) {
    return stepBuilderFactory.get("LoadPmsApiKey")
        .tasklet(loadPmsApiKeyTasklet)
        .build();
  }
}
