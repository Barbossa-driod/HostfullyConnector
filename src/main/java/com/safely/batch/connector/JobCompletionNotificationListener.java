package com.safely.batch.connector;

import com.safely.api.domain.Event;
import com.safely.api.domain.enumeration.EventSeverity;
import com.safely.api.domain.enumeration.EventStatus;
import com.safely.api.domain.enumeration.EventType;
import com.safely.batch.connector.common.services.safely.SafelyConnectorEventsService;
import com.safely.batch.connector.steps.JobContext;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobCompletionNotificationListener extends JobExecutionListenerSupport {

  private static final Logger log = LoggerFactory
      .getLogger(JobCompletionNotificationListener.class);

  @Autowired
  private SafelyConnectorEventsService eventsService;

  @Autowired
  public JobContext jobContext;

  @Override
  public void afterJob(JobExecution jobExecution) {
    log.info("JobCompletionNotificationListener.afterJob");

    Event event = new Event();
    event.setEventType(EventType.CONNECTOR);

    // TODO: Uncomment and set correct EventSubType
    //event.setEventSubType(EventSubType.<PMS NAME>);

    event.setEventStatus(EventStatus.COMPLETE);
    event.setStartTime(asLocalDateTime(jobExecution.getStartTime()));
    event.setEndTime(asLocalDateTime(jobExecution.getEndTime()));

    event.setCreatedReservations(
        jobContext.getNewReservations() != null ? jobContext.getNewReservations().size() : 0);
    event.setUpdateReservations(
        jobContext.getUpdatedReservations() != null ? jobContext.getUpdatedReservations().size()
            : 0);
    event.setCancelledReservations(0);

    if (jobContext.getOrganization() != null) {
      event.setOrganizationId(jobContext.getOrganization().getId());
      event.setOrganizationName(jobContext.getOrganization().getName());
    }

    if (BatchStatus.COMPLETED.equals(jobExecution.getStatus())) {
      event.setSeverity(EventSeverity.INFO);
      event.setDescription("Job completed successfully.");
    } else {
      event.setSeverity(EventSeverity.ERROR);

      List<Throwable> failureExceptions = jobExecution.getFailureExceptions();
      if (failureExceptions != null && !failureExceptions.isEmpty()) {
        Throwable failureException = failureExceptions.get(0);
        event.setDescription(failureException.getMessage());
      } else if (jobExecution.getExitStatus() != null) {
        event.setDescription(jobExecution.getExitStatus().toString());
      }
    }

    try {
      eventsService.create(jobContext.getSafelyToken().getIdToken(), event);
    } catch (Exception ex) {
      log.error("Error while writing Event to API.", ex);
    }

    log.info("Job completed: [{}]", event);
  }

  private static LocalDateTime asLocalDateTime(Date date) {
    return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
  }

}

