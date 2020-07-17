package com.safely.batch.connector;

import com.safely.batch.connector.steps.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

  private static final Logger log = LoggerFactory
      .getLogger(JobCompletionNotificationListener.class);

  @Autowired
  public JobBuilderFactory jobBuilderFactory;

  @Autowired
  public StepBuilderFactory stepBuilderFactory;

  @Autowired
  public SafelyPropertiesConfig safelyPropertiesConfig;

  @Autowired
  public JobContext jobContext;

  @Qualifier("safelyAuth")
  @Autowired
  private Step safelyAuth;

  @Qualifier("loadOrganizations")
  @Autowired
  private Step loadOrganizations;

  @Qualifier("loadPmsProperties")
  @Autowired
  private Step loadPmsProperties;

  @Qualifier("loadPmsReservations")
  @Autowired
  private Step loadPmsReservations;

  @Qualifier("convertPmsPropertiesToSafely")
  @Autowired
  private Step convertPmsPropertiesToSafely;

  @Qualifier("convertPmsReservationsToSafely")
  @Autowired
  private Step convertPmsReservationsToSafely;

  @Qualifier("loadPropertiesFromSafely")
  @Autowired
  private Step loadPropertiesFromSafely;

  @Qualifier("loadReservationsFromSafely")
  @Autowired
  private Step loadReservationsFromSafely;

  @Qualifier("computePropertiesChangeList")
  @Autowired
  private Step computePropertiesChangeList;

  @Qualifier("computeReservationsChangeList")
  @Autowired
  private Step computeReservationsChangeList;

  @Qualifier("savePropertiesToSafely")
  @Autowired
  private Step savePropertiesToSafely;

  @Qualifier("saveReservationsToSafely")
  @Autowired
  private Step saveReservationsToSafely;

  @Qualifier("loadPmsApiKey")
  @Autowired
  private Step loadPmsApiKey;

  @Bean
  public Job pmsConnectorJob(JobCompletionNotificationListener listener) {
    return jobBuilderFactory.get("pmsConnector")
        .incrementer(new RunIdIncrementer())
        .listener(listener)
        .flow(safelyAuth)
        .next(loadPmsApiKey)
        .next(loadOrganizations)
        .next(loadPmsProperties)
        .next(loadPmsReservations)
        .next(convertPmsPropertiesToSafely)
        .next(convertPmsReservationsToSafely)
        .next(loadPropertiesFromSafely)
        .next(loadReservationsFromSafely)
        .next(computePropertiesChangeList)
        .next(computeReservationsChangeList)
        .next(savePropertiesToSafely)
        .next(saveReservationsToSafely)
        .end()
        .build();
  }
}