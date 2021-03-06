package com.safely.batch.connector;


import com.safely.api.domain.Organization;
import com.safely.api.domain.OrganizationConfiguration;
import com.safely.api.domain.Property;
import com.safely.api.domain.Reservation;
import com.safely.batch.connector.common.domain.safely.auth.JWTToken;
import com.safely.batch.connector.pmsV1.property.PmsProperty;
import com.safely.batch.connector.pmsV1.reservation.PmsReservation;
import com.safely.batch.connector.pmsV2.property.PmsPropertyV2;
import com.safely.batch.connector.pmsV2.reservation.PmsReservationV2;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.aws.messaging.listener.Visibility;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Data
public class JobContext {
    private static final Logger log = LoggerFactory.getLogger(JobContext.class);

    final int maxSeconds = 60 * 60 * 12; // 12 hours is max allowed by sqs

    private final static String BASE_URL = "BASE_URL";

    private final static String AGENCY_UID = "AGENCY_UID";

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int inboundQueueVisibility;
    private Visibility visibility;

    // authentication toke for Safely API
    private JWTToken safelyToken;

    // the organization this job is being performed for
    private Organization organization;
    private String organizationId;

    // properties loaded from PMS
    private List<PmsProperty> pmsProperties;
    // reservations loaded from PMS
    private List<PmsReservation> pmsReservations;

    // properties loaded from PMS V2
    private List<PmsPropertyV2> pmsPropertiesV2;
    // reservations loaded from PMS V2
    private List<PmsReservationV2> pmsReservationsV2;

    // if false it's API v1 else API v2
    private boolean apiVersion;

    // PMS properties that have been converted to the safely domain model
    private List<Property> pmsSafelyProperties;
    // PMS reservations that have been converted to the safely domain model
    private List<Reservation> pmsSafelyReservations;

    // Current properties for the organization in safely
    private List<Property> currentSafelyProperties;
    // Current reservations for the organization in safely
    private List<Reservation> currentSafelyReservations;

    // Reservations that need to be created/updated/removed from safely
    private List<Reservation> newReservations;
    private List<Reservation> updatedReservations;
    private List<Reservation> removedReservations;

    // Properties that need to be created/updated/removed from safely
    private List<Property> newProperties;
    private List<Property> updatedProperties;
    private List<Property> removedProperties;

    private Map<String, Map<String, Object>> jobStatistics
            = new HashMap<String, Map<String, Object>>();

    public String getAgencyUid() {
        return getOrganization().getOrganizationSourceCredentials().getCustomCredentialsData().get(AGENCY_UID);
    }

    public boolean getApiVersion() throws Exception {
        Map<String, String> customCredentialsData = getCustomCredentialsData();
        if (customCredentialsData == null || (customCredentialsData != null && customCredentialsData.get("API_VERSION").equals("1"))){
            apiVersion = false;
        } else if(customCredentialsData != null && customCredentialsData.get("API_VERSION").equals("2")){
            apiVersion = true;
        }
        return apiVersion;
    }

    public Map<String, String> getCustomCredentialsData() throws Exception {
        Map<String, String> customCredentialsData = getCurrentConfiguration().getPmsCredentials().getCustomCredentialsData();
        return customCredentialsData;
    }

    public OrganizationConfiguration getCurrentConfiguration() throws Exception {
        OrganizationConfiguration configuration = null;
        LocalDate now = LocalDate.now();
        List<OrganizationConfiguration> configurations;

        log.info("Started searching configuration for organization: {}", organization.getEntityId());
        configurations = organization.getConfigurations();
        if (!CollectionUtils.isEmpty(configurations)) {
            configuration = organization.getConfigurations().stream()
                    .findFirst()
                    .filter(conf -> (conf.getEffectiveStartDate().isBefore(now) || conf.getEffectiveStartDate().isEqual(now)) &&
                            (conf.getEffectiveEndDate() == null || conf.getEffectiveEndDate().isAfter(now))).orElse(null);
        }
        if (configuration == null) {
            String msg = String.format("Couldn't find the configuration in the organization object with id: %s", organization.getEntityId());
            log.error(msg);
            throw new Exception(msg);
        }
        log.info("Configuration for the organization: {} was successfully found", organization.getEntityId());

        return configuration;
    }

    public void refreshVisibility(int additionalSeconds) throws Exception {
        log.info("OrganizationId: {}. Preparing to refresh message visibility.", organizationId);
        LocalDateTime now = LocalDateTime.now();
        int lengthOfJobInSeconds = (int) ChronoUnit.SECONDS.between(this.getStartTime(), now);
        int secondsLeftInVisibility = inboundQueueVisibility - lengthOfJobInSeconds;

        if (secondsLeftInVisibility <= 0) {
            String msg = String.format("OrganizationId: %s. Job has taken longer than message visibility. StartTime: '%s' Now: '%s' Length of Job: %s Seconds In Visibility: %s", organizationId, this.getStartTime(), now, lengthOfJobInSeconds, inboundQueueVisibility);
            log.error(msg);
            throw new Exception(msg);
        }

        int maxAllowedSecondsToAdd = maxSeconds - secondsLeftInVisibility;
        int finalSecondsToAdd = Math.max(Math.min(maxAllowedSecondsToAdd, additionalSeconds), 0);
        log.info("OrganizationId: {}. Message visibility timeout refresh. Current Length of Job: {} Seconds Left in Visibility: {}, Seconds to Add: {} Final Seconds to Add: {}", organizationId, lengthOfJobInSeconds, secondsLeftInVisibility, additionalSeconds, finalSecondsToAdd);
        if (finalSecondsToAdd > secondsLeftInVisibility) {
            visibility.extend(finalSecondsToAdd).get();
            inboundQueueVisibility += finalSecondsToAdd;
        }
    }
}
