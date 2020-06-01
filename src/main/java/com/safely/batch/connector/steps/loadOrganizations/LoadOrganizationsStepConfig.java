package com.safely.batch.connector.steps.loadOrganizations;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadOrganizationsStepConfig {

  @Autowired
  private StepBuilderFactory stepBuilderFactory;

  @Bean
  public LoadOrganizationsTasklet getLoadOrganizationsTasklet() {
    return new LoadOrganizationsTasklet();
  }

  @Bean
  @Qualifier("loadOrganizations")
  public Step getLoadOrganizationsStep(LoadOrganizationsTasklet loadOrganizationsTasklet) {
    return stepBuilderFactory.get("LoadOrganizations")
        .tasklet(loadOrganizationsTasklet)
        .build();
  }
}
