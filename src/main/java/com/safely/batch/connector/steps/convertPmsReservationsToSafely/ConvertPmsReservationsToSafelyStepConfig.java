package com.safely.batch.connector.steps.convertPmsReservationsToSafely;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConvertPmsReservationsToSafelyStepConfig {

  @Autowired
  private StepBuilderFactory stepBuilderFactory;

  @Bean
  public ConvertPmsReservationsToSafelyTasklet getConvertPmsReservationsToSafelyTasklet() {
    return new ConvertPmsReservationsToSafelyTasklet();
  }

  @Bean
  @Qualifier("convertPmsReservationsToSafely")
  public Step getConvertPmsReservationsToSafelyStep(
      ConvertPmsReservationsToSafelyTasklet convertPmsReservationsToSafelyTasklet) {
    return stepBuilderFactory.get("ConvertPmsReservationsToSafely")
        .tasklet(convertPmsReservationsToSafelyTasklet)
        .build();
  }
}
