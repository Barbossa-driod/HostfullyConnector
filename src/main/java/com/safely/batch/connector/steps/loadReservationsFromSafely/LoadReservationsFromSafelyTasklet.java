package com.safely.batch.connector.steps.loadReservationsFromSafely;

import com.safely.api.domain.Organization;
import com.safely.api.domain.Reservation;
import com.safely.batch.connector.common.domain.safely.auth.JWTToken;
import com.safely.batch.connector.common.services.safely.SafelyConnectorReservationsService;
import com.safely.batch.connector.steps.JobContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

public class LoadReservationsFromSafelyTasklet implements Tasklet {

  private static final Logger log = LoggerFactory
      .getLogger(LoadReservationsFromSafelyTasklet.class);

  @Autowired
  public JobContext jobContext;

  @Autowired
  private SafelyConnectorReservationsService reservationsService;

  private static final String STEP_NAME = "load_reservations_from_safely";
  private static final String LOADED = "loaded";

  @Override
  public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext)
      throws Exception {

    Map<String, Object> stepStatistics = new HashMap<>();

    Organization organization = jobContext.getOrganization();
    JWTToken token = jobContext.getSafelyToken();

    List<Reservation> currentSafelyReservations = reservationsService
        .getAll(token.getIdToken(), organization.getEntityId());

    log.info("Loaded {} Safely reservations for organization: {} - ({}, {})",
        currentSafelyReservations.size(), organization.getName(), organization.getEntityId(),
        organization.getLegacyOrganizationId());
    jobContext.setCurrentSafelyReservations(currentSafelyReservations);

    stepStatistics.put(LOADED, currentSafelyReservations.size());
    jobContext.getJobStatistics().put(STEP_NAME, stepStatistics);

    return RepeatStatus.FINISHED;
  }
}
