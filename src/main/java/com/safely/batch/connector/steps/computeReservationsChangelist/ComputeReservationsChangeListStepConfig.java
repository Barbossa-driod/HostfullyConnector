package com.safely.batch.connector.steps.computeReservationsChangelist;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ComputeReservationsChangeListStepConfig {

  @Autowired
  private StepBuilderFactory stepBuilderFactory;

  @Bean
  public ComputeReservationsChangeListTasklet getComputeReservationsChangeListTasklet() {
    return new ComputeReservationsChangeListTasklet();
  }

  @Bean
  @Qualifier("computeReservationsChangeList")
  public Step getComputeReservationsChangeListStep(
      ComputeReservationsChangeListTasklet computeReservationsChangeListTasklet) {
    return stepBuilderFactory.get("ComputeReservationsChangeList")
        .tasklet(computeReservationsChangeListTasklet)
        .build();
  }
}
