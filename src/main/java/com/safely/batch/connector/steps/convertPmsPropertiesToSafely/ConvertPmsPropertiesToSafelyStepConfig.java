package com.safely.batch.connector.steps.convertPmsPropertiesToSafely;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConvertPmsPropertiesToSafelyStepConfig {

  @Autowired
  private StepBuilderFactory stepBuilderFactory;

  @Bean
  public ConvertPmsPropertiesToSafelyTasklet getConvertPmsPropertiesToSafelyTasklet() {
    return new ConvertPmsPropertiesToSafelyTasklet();
  }

  @Bean
  @Qualifier("convertPmsPropertiesToSafely")
  public Step getConvertPmsPropertiesToSafelyStep(
      ConvertPmsPropertiesToSafelyTasklet convertPmsPropertiesToSafelyTasklet) {
    return stepBuilderFactory.get("ConvertPmsPropertiesToSafelyTasklet")
        .tasklet(convertPmsPropertiesToSafelyTasklet)
        .build();
  }
}
