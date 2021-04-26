package com.safely.batch.connector;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safely.api.domain.enumeration.EventSeverity;
import com.safely.batch.connector.components.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.listener.Acknowledgment;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.Visibility;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class SqsListeningService {

    private static final Logger log = LoggerFactory.getLogger(SqsListeningService.class);
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
    private final AmazonSQSAsync amazonSqsAsync;

    final int maxSeconds = 60 * 60 * 12; // 12 hours is max allowed by sqs

    @Value("${safely.api.username}")
    private String apiUsername;
    @Value("${safely.api.password}")
    private String apiPassword;
    @Value("${safely.queue.inbound.visibility}")
    private int inboundQueueVisibility;
    @Value("${safely.queue.outbound.name}")
    private String outboundQueueName;
    @Value("${safely.pms.apiKey}")
    private String apiKey;

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
                               ObjectMapper objectMapper, AmazonSQSAsync amazonSqsAsync) {
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
        this.amazonSqsAsync = amazonSqsAsync;
    }

    @SqsListener(value = "${safely.queue.inbound.name}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void receiveMessage(String messageJson, Visibility visibility, Acknowledgment acknowledgment) {

        LocalDateTime startTime = LocalDateTime.now();
        JobContext jobContext = new JobContext();
        String organizationId = null;
        jobContext.setStartTime(startTime);

        EventSeverity eventSeverity = EventSeverity.INFO;

        try {
            // get organizationId from message
            ConnectorMessage message = objectMapper.readValue(messageJson, ConnectorMessage.class);
            organizationId = message.getOrganizationId();
            jobContext.setOrganizationId(organizationId);

            log.info("OrganizationId: {}. Processing message at UTC: {}. Message created on: {}",
                    organizationId, jobContext.getStartTime(), message.getCreateDate());

            // setup for this run
            log.info("OrganizationId: {}. Authentication in safelyAPI and loading organization data.", organizationId);
            loadSafelyAuthService.execute(jobContext, apiUsername, apiPassword);
            loadOrganizationService.execute(jobContext, organizationId);

            // load data from the PMS API
            log.info("OrganizationId: {}. Preparing to load property data from PMS.", organizationId);
            refreshVisibility(visibility, startTime, 180);
            loadPmsPropertiesService.execute(jobContext, apiKey);
            log.info("OrganizationId: {}. Preparing to load reservation data from PMS.", organizationId);
            refreshVisibility(visibility, startTime, 180);
            loadPmsReservationsService.execute(jobContext, apiKey);

            // convert PMS data to Safely format
            log.info("OrganizationId: {}. Preparing to convert PMS properties to Safely structure", organizationId);
            convertPmsPropertiesToSafelyService.execute(jobContext);
            log.info("OrganizationId: {}. Preparing to convert PMS reservations to Safely structure", organizationId);
            convertPmsReservationsToSafelyService.execute(jobContext);

            // load previous data
            log.info("OrganizationId: {}. Preparing to load property data from Safely.", organizationId);
            refreshVisibility(visibility, startTime, jobContext.getPmsProperties().size());
            loadPropertiesFromSafelyService.execute(jobContext);
            log.info("OrganizationId: {}. Preparing to load reservation data from Safely.", organizationId);
            refreshVisibility(visibility, startTime, jobContext.getPmsReservations().size());
            loadReservationsFromSafelyService.execute(jobContext);

            // compare previous data to new data for changes
            log.info("OrganizationId: {}. Preparing to compute properties change list", organizationId);
            computePropertiesChangeListService.execute(jobContext);
            log.info("OrganizationId: {}. Preparing to compute reservations change list", organizationId);
            computeReservationsChangeListService.execute(jobContext);

            // save any changes
            log.info("OrganizationId: {}. Preparing to save properties to Safely", organizationId);
            int propertyCount =
                    jobContext.getNewProperties().size() + jobContext.getUpdatedProperties().size();
            refreshVisibility(visibility, startTime,
                    propertyCount); // assume one second per property to save
            savePropertiesToSafelyService.execute(jobContext);

            log.info("OrganizationId: {}. Preparing to save reservations to Safely", organizationId);
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
            log.error("OrganizationId: {}, Error message: {}, Error: {}", organizationId, ex.getMessage(), ex);
            eventSeverity = EventSeverity.ERROR;
        } finally {
            // exceptions in save event are handled
            log.info("OrganizationId: {}. Preparing to save completion event to Safely", organizationId);
            jobContext.setEndTime(LocalDateTime.now());
            saveCompletionEventToSafelyService.execute(jobContext, eventSeverity);
        }

        try {
            switch (eventSeverity) {
                case INFO:
                case WARNING:
                    log.info("OrganizationId: {}. Job completed with status {}. Removing message from queue.", organizationId, eventSeverity);
                    acknowledgment.acknowledge().get();
                    break;
                case ERROR:
                    log.info("OrganizationId: {}. Job completed with status {}. Allowing the message to time out back into the queue.",
                            organizationId, eventSeverity);
                    break;
                default:
                    log.error("OrganizationId: {}. Unrecognized event severity: {}", organizationId, eventSeverity);
            }
        } catch (Exception ex) {
            log.error("OrganizationId: {}. Error while trying to clean up a message. Error message: {}", organizationId, ex.getMessage());
        }
    }

    private void sendMessageToQueue(String organizationId) {

        log.info("OrganizationId: {}. Sending message to queue: '{}'", organizationId,
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

            this.amazonSqsAsync.sendMessage(sendMessageRequest);
        } catch (Exception ex) {
            log.error("OrganizationId: {}. Error while sending a message to queue. Error message: {}", organizationId, ex);
        }
    }

    private void refreshVisibility(Visibility visibility, LocalDateTime startTIme, int additionalSeconds) throws ExecutionException, InterruptedException {
        int lengthOfJobInSeconds = (int) ChronoUnit.SECONDS.between(startTIme, LocalDateTime.now());
        int secondsLeftInVisibility = inboundQueueVisibility - lengthOfJobInSeconds;
        int maxAllowedSecondsToAdd = maxSeconds - secondsLeftInVisibility;
        int finalSecondsToAdd = Math.max(Math.min(maxAllowedSecondsToAdd, additionalSeconds), 0);
        log.info("Message visibility timeout refresh. Current Length of Job: {} Seconds Left in Visibility: {}, Seconds to Add: {} Final Seconds to Add: {}", lengthOfJobInSeconds, secondsLeftInVisibility, additionalSeconds, finalSecondsToAdd);
        if (finalSecondsToAdd > secondsLeftInVisibility) {
            visibility.extend(finalSecondsToAdd).get();
            inboundQueueVisibility += finalSecondsToAdd;
        }
    }
}
