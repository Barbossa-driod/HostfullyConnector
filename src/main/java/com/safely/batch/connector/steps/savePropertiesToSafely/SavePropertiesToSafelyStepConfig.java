package com.safely.batch.connector.steps.savePropertiesToSafely;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SavePropertiesToSafelyStepConfig {

  @Autowired
  private StepBuilderFactory stepBuilderFactory;

  @Bean
  public SavePropertiesToSafelyTasklet getSavePropertiesToSafelyTasklet() {
    return new SavePropertiesToSafelyTasklet();
  }

  @Bean
  @Qualifier("savePropertiesToSafely")
  public Step getSavePropertiesToSafelyStep(
      SavePropertiesToSafelyTasklet savePropertiesToSafelyTasklet) {
    return stepBuilderFactory.get("savePropertiesToSafely")
        .tasklet(savePropertiesToSafelyTasklet)
        .build();
  }
}
