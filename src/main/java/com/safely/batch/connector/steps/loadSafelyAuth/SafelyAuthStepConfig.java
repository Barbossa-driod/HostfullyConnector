package com.safely.batch.connector.steps.loadSafelyAuth;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SafelyAuthStepConfig {

  @Autowired
  private StepBuilderFactory stepBuilderFactory;

  @Bean
  public SafelyAuthTasklet getSafelyAuthTasklet() {
    return new SafelyAuthTasklet();
  }

  @Bean
  @Qualifier("safelyAuth")
  public Step getSafelyAuthStep(SafelyAuthTasklet safelyAuthTasklet) {
    return stepBuilderFactory.get("SafelyAuth")
        .tasklet(safelyAuthTasklet)
        .build();
  }
}
