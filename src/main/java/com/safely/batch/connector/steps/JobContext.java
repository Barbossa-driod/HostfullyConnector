package com.safely.batch.connector.steps;


import com.safely.api.domain.Organization;
import com.safely.api.domain.Property;
import com.safely.api.domain.Reservation;
import com.safely.batch.connector.common.domain.safely.auth.JWTToken;
import com.safely.batch.connector.pms.property.PmsProperty;
import com.safely.batch.connector.pms.reservation.PmsReservation;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Data
public class JobContext {

    private final static String BASE_URL = "BASE_URL";

    private final static String AGENCY_UID ="AGENCY_UID";

    // authentication toke for Safely API
    private JWTToken safelyToken;

    // the organization this job is being performed for
    private Organization organization;

    // properties loaded from PMS
    private List<PmsProperty> pmsProperties;
    // reservations loaded from PMS
    private List<PmsReservation> pmsReservations;

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

    private String hostfullyApiKey;

    public String getAgencyUid() {
        return getOrganization().getOrganizationSourceCredentials().getCustomCredentialsData().get(AGENCY_UID);
    }
}
