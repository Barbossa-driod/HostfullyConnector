package com.safely.batch.connector;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safely.api.domain.enumeration.EventSeverity;
import com.safely.batch.connector.components.commonV1.ComputePropertiesChangeListService;
import com.safely.batch.connector.components.commonV1.ComputeReservationsChangeListService;
import com.safely.batch.connector.components.commonV1.ConvertPmsPropertiesToSafelyService;
import com.safely.batch.connector.components.commonV1.ConvertPmsReservationsToSafelyService;
import com.safely.batch.connector.components.externalV2.LoadPmsPropertiesPhotoServiceV2;
import com.safely.batch.connector.components.externalV2.LoadPmsPropertiesServiceV2;
import com.safely.batch.connector.components.externalV2.LoadPmsReservationsServiceV2;
import com.safely.batch.connector.components.externalV2.LoadReservationsOrdersServiceV2;
import com.safely.batch.connector.components.internal.*;
import com.safely.batch.connector.components.externalV1.LoadPmsPropertiesService;
import com.safely.batch.connector.components.externalV1.LoadPmsReservationsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.listener.Acknowledgment;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.Visibility;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

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
    private final LoadPmsPropertiesServiceV2 loadPmsPropertiesServiceV2;
    private final LoadPmsPropertiesPhotoServiceV2 loadPmsPropertiesPhotoServiceV2;
    private final LoadPmsReservationsServiceV2 loadPmsReservationsServiceV2;
    private final LoadReservationsOrdersServiceV2 loadReservationsOrdersServiceV2;
    private final ComputePropertiesChangeListService computePropertiesChangeListService;
    private final ComputeReservationsChangeListService computeReservationsChangeListService;
    private final SavePropertiesToSafelyService savePropertiesToSafelyService;
    private final SaveReservationsToSafelyService saveReservationsToSafelyService;
    private final SaveCompletionEventToSafelyService saveCompletionEventToSafelyService;
    private final ObjectMapper objectMapper;
    private final AmazonSQSAsync amazonSqsAsync;
    private final ScheduledTasks scheduledTasks;

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
                               LoadPmsPropertiesServiceV2 loadPmsPropertiesServiceV2, LoadPmsPropertiesPhotoServiceV2 loadPmsPropertiesPhotoServiceV2,
                               LoadPmsReservationsServiceV2 loadPmsReservationsServiceV2, LoadReservationsOrdersServiceV2 loadReservationsOrdersServiceV2,
                               ComputePropertiesChangeListService computePropertiesChangeListService,
                               ComputeReservationsChangeListService computeReservationsChangeListService,
                               SavePropertiesToSafelyService savePropertiesToSafelyService,
                               SaveReservationsToSafelyService saveReservationsToSafelyService,
                               SaveCompletionEventToSafelyService saveCompletionEventToSafelyService,
                               ObjectMapper objectMapper, AmazonSQSAsync amazonSqsAsync, ScheduledTasks scheduledTasks) {
        this.loadSafelyAuthService = loadSafelyAuthService;
        this.loadOrganizationService = loadOrganizationService;
        this.loadPmsPropertiesService = loadPmsPropertiesService;
        this.loadPmsReservationsService = loadPmsReservationsService;
        this.convertPmsPropertiesToSafelyService = convertPmsPropertiesToSafelyService;
        this.convertPmsReservationsToSafelyService = convertPmsReservationsToSafelyService;
        this.loadPropertiesFromSafelyService = loadPropertiesFromSafelyService;
        this.loadReservationsFromSafelyService = loadReservationsFromSafelyService;
        this.loadPmsPropertiesServiceV2 = loadPmsPropertiesServiceV2;
        this.loadPmsPropertiesPhotoServiceV2 = loadPmsPropertiesPhotoServiceV2;
        this.loadPmsReservationsServiceV2 = loadPmsReservationsServiceV2;
        this.loadReservationsOrdersServiceV2 = loadReservationsOrdersServiceV2;
        this.computePropertiesChangeListService = computePropertiesChangeListService;
        this.computeReservationsChangeListService = computeReservationsChangeListService;
        this.savePropertiesToSafelyService = savePropertiesToSafelyService;
        this.saveReservationsToSafelyService = saveReservationsToSafelyService;
        this.saveCompletionEventToSafelyService = saveCompletionEventToSafelyService;
        this.objectMapper = objectMapper;
        this.amazonSqsAsync = amazonSqsAsync;
        this.scheduledTasks = scheduledTasks;
    }

    @SqsListener(value = "${safely.queue.inbound.name}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void receiveMessage(String messageJson, Visibility visibility, Acknowledgment acknowledgment) {

        LocalDateTime startTime = LocalDateTime.now();
        JobContext jobContext = new JobContext();
        String organizationId = null;
        jobContext.setStartTime(startTime);
        jobContext.setInboundQueueVisibility(inboundQueueVisibility);
        jobContext.setVisibility(visibility);

        EventSeverity eventSeverity = EventSeverity.INFO;

        try {
            // get organizationId from message
            ConnectorMessage message = objectMapper.readValue(messageJson, ConnectorMessage.class);
            organizationId = message.getOrganizationId();
            jobContext.setOrganizationId(organizationId);

            scheduledTasks.initDataToIncreaseMessageVisibility(organizationId, jobContext);

            log.info("OrganizationId: {}. Processing message at UTC: {}. Message created on: {}",
                    organizationId, jobContext.getStartTime(), message.getCreateDate());

            // setup for this run
            log.info("OrganizationId: {}. Authentication in safelyAPI and loading organization data.", organizationId);
            loadSafelyAuthService.execute(jobContext, apiUsername, apiPassword);
            loadOrganizationService.execute(jobContext, organizationId);

            // load data from the PMS API V1
            log.info("OrganizationId: {}. Preparing to load property data from PMS V1.", organizationId);
            loadPmsPropertiesService.execute(jobContext, apiKey);
            log.info("OrganizationId: {}. Preparing to load reservation data from PMS V1.", organizationId);
            loadPmsReservationsService.execute(jobContext, apiKey);

            // load data from the PMS API V2
            log.info("OrganizationId: {}. Preparing to load property data from PMS V2.", organizationId);
            loadPmsPropertiesServiceV2.execute(jobContext, apiKey);
            loadPmsPropertiesPhotoServiceV2.execute(jobContext, apiKey);
            log.info("OrganizationId: {}. Preparing to load reservation data from PMS V2.", organizationId);
            loadPmsReservationsServiceV2.execute(jobContext, apiKey);
            loadReservationsOrdersServiceV2.execute(jobContext, apiKey);

            // convert PMS data to Safely format
            log.info("OrganizationId: {}. Preparing to convert PMS properties to Safely structure", organizationId);
            convertPmsPropertiesToSafelyService.execute(jobContext);
            log.info("OrganizationId: {}. Preparing to convert PMS reservations to Safely structure", organizationId);
            convertPmsReservationsToSafelyService.execute(jobContext);

            // load previous data
            log.info("OrganizationId: {}. Preparing to load property data from Safely.", organizationId);
            loadPropertiesFromSafelyService.execute(jobContext);
            log.info("OrganizationId: {}. Preparing to load reservation data from Safely.", organizationId);
            loadReservationsFromSafelyService.execute(jobContext);

            // compare previous data to new data for changes
            log.info("OrganizationId: {}. Preparing to compute properties change list", organizationId);
            computePropertiesChangeListService.execute(jobContext);
            log.info("OrganizationId: {}. Preparing to compute reservations change list", organizationId);
            computeReservationsChangeListService.execute(jobContext);

            // save any changes
            log.info("OrganizationId: {}. Preparing to save properties to Safely", organizationId);
            savePropertiesToSafelyService.execute(jobContext);

            log.info("OrganizationId: {}. Preparing to save reservations to Safely", organizationId);
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
            scheduledTasks.setIsIncreaseVisibilityEnable(Boolean.FALSE);
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
}
