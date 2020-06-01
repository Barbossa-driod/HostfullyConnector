package com.safely.batch.connector.steps.loadReservationsFromSafely;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadReservationsFromSafelyStepConfig {

  @Autowired
  private StepBuilderFactory stepBuilderFactory;

  @Bean
  public LoadReservationsFromSafelyTasklet getLoadReservationsFromSafelyTasklet() {
    return new LoadReservationsFromSafelyTasklet();
  }

  @Bean
  @Qualifier("loadReservationsFromSafely")
  public Step getLoadReservationsFromSafelyStep(
      LoadReservationsFromSafelyTasklet loadReservationsFromSafelyTasklet) {
    return stepBuilderFactory.get("loadReservationsFromSafely")
        .tasklet(loadReservationsFromSafelyTasklet)
        .build();
  }
}
