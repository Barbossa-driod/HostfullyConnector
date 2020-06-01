package com.safely.batch.connector.steps.loadPropertiesFromSafely;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadPropertiesFromSafelyStepConfig {

  @Autowired
  private StepBuilderFactory stepBuilderFactory;

  @Bean
  public LoadPropertiesFromSafelyTasklet getLoadPropertiesFromSafelyTasklet() {
    return new LoadPropertiesFromSafelyTasklet();
  }

  @Bean
  @Qualifier("loadPropertiesFromSafely")
  public Step getLoadPropertiesFromSafelyStep(
      LoadPropertiesFromSafelyTasklet loadPropertiesFromSafelyTasklet) {
    return stepBuilderFactory.get("loadPropertiesFromSafely")
        .tasklet(loadPropertiesFromSafelyTasklet)
        .build();
  }
}
