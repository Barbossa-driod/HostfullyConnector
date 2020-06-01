package com.safely.batch.connector.steps.saveReservationsToSafely;

import com.safely.api.domain.Organization;
import com.safely.api.domain.Reservation;
import com.safely.batch.connector.common.domain.safely.auth.JWTToken;
import com.safely.batch.connector.common.services.safely.SafelyConnectorReservationsService;
import com.safely.batch.connector.steps.JobContext;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

public class SaveReservationsToSafelyTasklet implements Tasklet {

  private static final Logger log = LoggerFactory.getLogger(SaveReservationsToSafelyTasklet.class);

  @Autowired
  public JobContext jobContext;

  @Autowired
  private SafelyConnectorReservationsService reservationsService;

  public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext)
      throws Exception {

    Organization organization = jobContext.getOrganization();
    JWTToken token = jobContext.getSafelyToken();

    List<Reservation> newReservations = jobContext.getNewReservations();
    log.info("Writing {} new reservations for organization: {} - ({})", newReservations.size(),
        organization.getName(), organization.getEntityId());
    for (Reservation reservation : newReservations) {
      reservationsService.create(token.getIdToken(), reservation);
    }

    List<Reservation> updatedReservations = jobContext.getUpdatedReservations();
    log.info("Writing {} updated reservations for organization: {} - ({})",
        updatedReservations.size(), organization.getName(), organization.getEntityId());
    for (Reservation reservation : updatedReservations) {
      reservationsService.save(token.getIdToken(), reservation);
    }

    return RepeatStatus.FINISHED;
  }
}
