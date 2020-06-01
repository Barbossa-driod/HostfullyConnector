package com.safely.batch.connector.steps.saveReservationsToSafely;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SaveReservationsToSafelyStepConfig {

  @Autowired
  private StepBuilderFactory stepBuilderFactory;

  @Bean
  public SaveReservationsToSafelyTasklet getSaveReservationsToSafelyTasklet() {
    return new SaveReservationsToSafelyTasklet();
  }

  @Bean
  @Qualifier("saveReservationsToSafely")
  public Step getSaveReservationsToSafelyStep(
      SaveReservationsToSafelyTasklet saveReservationsToSafelyTasklet) {
    return stepBuilderFactory.get("saveReservationsToSafely")
        .tasklet(saveReservationsToSafelyTasklet)
        .build();
  }
}
