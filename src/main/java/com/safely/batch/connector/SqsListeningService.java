package com.safely.batch.connector;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safely.api.domain.enumeration.EventSeverity;
import com.safely.batch.connector.components.ComputePropertiesChangeListService;
import com.safely.batch.connector.components.ComputeReservationsChangeListService;
import com.safely.batch.connector.components.ConvertPmsPropertiesToSafelyService;
import com.safely.batch.connector.components.ConvertPmsReservationsToSafelyService;
import com.safely.batch.connector.components.LoadOrganizationService;
import com.safely.batch.connector.components.LoadPmsPropertiesService;
import com.safely.batch.connector.components.LoadPmsReservationsService;
import com.safely.batch.connector.components.LoadPropertiesFromSafelyService;
import com.safely.batch.connector.components.LoadReservationsFromSafelyService;
import com.safely.batch.connector.components.LoadSafelyAuthService;
import com.safely.batch.connector.components.SaveCompletionEventToSafelyService;
import com.safely.batch.connector.components.SavePropertiesToSafelyService;
import com.safely.batch.connector.components.SaveReservationsToSafelyService;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.listener.Acknowledgment;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.Visibility;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Service;

@Service
public class SqsListeningService {

  private static final Logger log = LoggerFactory.getLogger(SqsListeningService.class);

  @Value("${safely.api.username}")
  private String apiUsername;
  @Value("${safely.api.password}")
  private String apiPassword;
  @Value("${safely.queue.inbound.visibility}")
  private int inboundQueueVisibility;
  @Value("${safely.queue.outbound.name}")
  private String outboundQueueName;
  @Value("${safely.pms.apiKey")
  private String apiKey;

  private final LoadSafelyAuthService loadSafelyAuthService;
  private final LoadOrganizationService loadOrganizationService;
  private final LoadPmsPropertiesService loadPmsPropertiesService;
  private final LoadPmsReservationsService loadPmsReservationsService;
  private final ConvertPmsPropertiesToSafelyService convertPmsPropertiesToSafelyService;
  private final ConvertPmsReservationsToSafelyService convertPmsReservationsToSafelyService;
  private final LoadPropertiesFromSafelyService loadPropertiesFromSafelyService;
  private final LoadReservationsFromSafelyService loadReservationsFromSafelyService;
  private final ComputePropertiesChangeListService computePropertiesChangeListService;
  private final ComputeReservationsChangeListService computeReservationsChangeListService;
  private final SavePropertiesToSafelyService savePropertiesToSafelyService;
  private final SaveReservationsToSafelyService saveReservationsToSafelyService;
  private final SaveCompletionEventToSafelyService saveCompletionEventToSafelyService;
  private final ObjectMapper objectMapper;
  private final AmazonSQSAsync amazonSQSAsync;

  public SqsListeningService(LoadSafelyAuthService loadSafelyAuthService,
      LoadOrganizationService loadOrganizationService,
      LoadPmsPropertiesService loadPmsPropertiesService,
      LoadPmsReservationsService loadPmsReservationsService,
      ConvertPmsPropertiesToSafelyService convertPmsPropertiesToSafelyService,
      ConvertPmsReservationsToSafelyService convertPmsReservationsToSafelyService,
      LoadPropertiesFromSafelyService loadPropertiesFromSafelyService,
      LoadReservationsFromSafelyService loadReservationsFromSafelyService,
      ComputePropertiesChangeListService computePropertiesChangeListService,
      ComputeReservationsChangeListService computeReservationsChangeListService,
      SavePropertiesToSafelyService savePropertiesToSafelyService,
      SaveReservationsToSafelyService saveReservationsToSafelyService,
      SaveCompletionEventToSafelyService saveCompletionEventToSafelyService,
      ObjectMapper objectMapper, AmazonSQSAsync amazonSQSAsync) {
    this.loadSafelyAuthService = loadSafelyAuthService;
    this.loadOrganizationService = loadOrganizationService;
    this.loadPmsPropertiesService = loadPmsPropertiesService;
    this.loadPmsReservationsService = loadPmsReservationsService;
    this.convertPmsPropertiesToSafelyService = convertPmsPropertiesToSafelyService;
    this.convertPmsReservationsToSafelyService = convertPmsReservationsToSafelyService;
    this.loadPropertiesFromSafelyService = loadPropertiesFromSafelyService;
    this.loadReservationsFromSafelyService = loadReservationsFromSafelyService;
    this.computePropertiesChangeListService = computePropertiesChangeListService;
    this.computeReservationsChangeListService = computeReservationsChangeListService;
    this.savePropertiesToSafelyService = savePropertiesToSafelyService;
    this.saveReservationsToSafelyService = saveReservationsToSafelyService;
    this.saveCompletionEventToSafelyService = saveCompletionEventToSafelyService;
    this.objectMapper = objectMapper;
    this.amazonSQSAsync = amazonSQSAsync;
  }

  @SqsListener(value = "${safely.queue.inbound.name}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
  public void receiveMessage(String messageJson, Visibility visibility,
      Acknowledgment acknowledgment) {

    LocalDateTime startTime = LocalDateTime.now();
    JobContext jobContext = new JobContext();
    jobContext.setStartTime(startTime);

    EventSeverity eventSeverity = EventSeverity.INFO;

    try {
      // get organizationId from message
      ConnectorMessage message = objectMapper.readValue(messageJson, ConnectorMessage.class);
      String organizationId = message.getOrganizationId();

      log.info("Processing message for Organization Id: {} at UTC: {}. Message created on: {}",
          organizationId, jobContext.getStartTime(), message.getCreateDate());

      // setup for this run
      loadSafelyAuthService.execute(jobContext, apiUsername, apiPassword);
      loadOrganizationService.execute(jobContext, organizationId);

      // load data from the PMS API
      refreshVisibility(visibility, startTime, 180);
      loadPmsPropertiesService.execute(jobContext, apiKey);
      refreshVisibility(visibility, startTime, 180);
      loadPmsReservationsService.execute(jobContext, apiKey);

      // convert PMS data to Safely format
      convertPmsPropertiesToSafelyService.execute(jobContext);
      convertPmsReservationsToSafelyService.execute(jobContext);

      // load previous data
      refreshVisibility(visibility, startTime, jobContext.getPmsProperties().size());
      loadPropertiesFromSafelyService.execute(jobContext);
      refreshVisibility(visibility, startTime, jobContext.getPmsReservations().size());
      loadReservationsFromSafelyService.execute(jobContext);

      // compare previous data to new data for changes
      computePropertiesChangeListService.execute(jobContext);
      computeReservationsChangeListService.execute(jobContext);

      // save any changes
      int propertyCount =
          jobContext.getNewProperties().size() + jobContext.getUpdatedProperties().size();
      refreshVisibility(visibility, startTime,
          propertyCount); // assume one second per property to save
      savePropertiesToSafelyService.execute(jobContext);

      int reservationCount =
          jobContext.getNewReservations().size() + jobContext.getUpdatedReservations().size();
      refreshVisibility(visibility, startTime, reservationCount);
      saveReservationsToSafelyService.execute(jobContext);

      // if any step reported any failures, mark severity as warning
      if (jobContext.getJobStatistics() != null) {
        for (Map.Entry<String, Map<String, Object>> entry : jobContext.getJobStatistics()
            .entrySet()) {
          Map<String, Object> map = entry.getValue();
          if (map.containsKey("failed") && (int) map.get("failed") > 0) {
            eventSeverity = EventSeverity.WARNING;
            break;
          }
        }
      }

      // send a message to legacy sync
      sendMessageToQueue(organizationId);

    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
      eventSeverity = EventSeverity.ERROR;
    } finally {
      // exceptions in save event are handled
      jobContext.setEndTime(LocalDateTime.now());
      saveCompletionEventToSafelyService.execute(jobContext, eventSeverity);
    }

    try {
      switch (eventSeverity) {
        case INFO:
        case WARNING:
          log.info("Job completed with status {}. Removing message from queue.", eventSeverity);
          acknowledgment.acknowledge().get();
          break;
        case ERROR:
          log.info(
              "Job completed with status {}. Allowing the message to time out back into the queue.",
              eventSeverity);
          break;
        default:
          log.error("Unrecognized event severity: {}", eventSeverity);
      }
    } catch (Exception ex) {
      log.error("Error while trying to clean up a message.");
      log.error(ex.getMessage(), ex);
    }
  }

  private void sendMessageToQueue(String organizationId) {

    log.info("Sending message for organizationId: {} to queue: '{}'", organizationId,
        outboundQueueName);

    LocalDateTime createDate = LocalDateTime.now();
    try {
      ConnectorMessage message = new ConnectorMessage();
      message.setOrganizationId(organizationId);
      message.setCreateDate(createDate);

      String messageAsJsonString = objectMapper.writeValueAsString(message);

      SendMessageRequest sendMessageRequest = new SendMessageRequest()
          .withQueueUrl(outboundQueueName)
          .withMessageBody(messageAsJsonString);

      this.amazonSQSAsync.sendMessage(sendMessageRequest);
    } catch (Exception ex) {
      log.error("Error while sending a message to queue.", ex);
    }
  }

  private void refreshVisibility(Visibility visibility, LocalDateTime startTIme,
      int additionalSeconds) throws ExecutionException, InterruptedException {
    int lengthOfJobInSeconds = (int) ChronoUnit.SECONDS.between(startTIme, LocalDateTime.now());
    if (inboundQueueVisibility - lengthOfJobInSeconds <= additionalSeconds) {
      visibility.extend(additionalSeconds).get();
      inboundQueueVisibility += additionalSeconds;
    }
  }
}
