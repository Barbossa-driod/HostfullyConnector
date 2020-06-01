package com.safely.batch.connector.steps.computePropertiesChangelist;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ComputePropertiesChangeListStepConfig {

  @Autowired
  private StepBuilderFactory stepBuilderFactory;

  @Bean
  public ComputePropertiesChangeListTasklet getComputePropertiesChangeListTasklet() {
    return new ComputePropertiesChangeListTasklet();
  }

  @Bean
  @Qualifier("computePropertiesChangeList")
  public Step getComputePropertiesChangeListStep(
      ComputePropertiesChangeListTasklet computePropertiesChangeListTasklet) {
    return stepBuilderFactory.get("ComputePropertiesChangeList")
        .tasklet(computePropertiesChangeListTasklet)
        .build();
  }
}
