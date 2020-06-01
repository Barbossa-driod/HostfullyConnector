package com.safely.batch.connector.steps;


import com.safely.api.domain.Organization;
import com.safely.api.domain.Property;
import com.safely.api.domain.Reservation;
import com.safely.batch.connector.common.domain.safely.auth.JWTToken;
import com.safely.batch.connector.pms.property.PmsProperty;
import com.safely.batch.connector.pms.reservation.PmsReservation;
import com.safely.batch.connector.pms.photo.PmsPhoto;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Data
public class JobContext {

    private final static String BASE_URL = "BASE_URL";
    private final static String SERVER_KEY = "SERVER_KEY";
    private final static String SERVER_SECRET = "SERVER_SECRET";

    // authentication toke for Safely API
    private JWTToken safelyToken;

    // the organization this job is being performed for
    private Organization organization;

    // properties loaded from PMS
    private List<PmsProperty> pmsProperties;
    // photos for properties from PMS
    private Map<Integer, List<PmsPhoto>> pmsPropertyPhotos;
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

    public String getBaseUrl() {
        return getOrganization().getOrganizationSourceCredentials().getCustomCredentialsData().get(BASE_URL);
    }

    public String getServerKey() {
        return getOrganization().getOrganizationSourceCredentials().getCustomCredentialsData().get(SERVER_KEY);
    }

    public String getServerSecret() {
        return getOrganization().getOrganizationSourceCredentials().getCustomCredentialsData().get(SERVER_SECRET);
    }

    public String getLastUpdateDateFromPms() {
        return getOrganization().getLastUpdateDateFromPms() != null ? getOrganization().getLastUpdateDateFromPms().format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
    }

    public String getReservationLoadDate() throws Exception {
        String startDate = getOrganization().getStartDate() != null ? getOrganization().getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
        String legacyBookingStartDate = getOrganization().getLegacyBookingStartDate() != null ? getOrganization().getLegacyBookingStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
        String legacyArrivalStartDate = getOrganization().getLegacyArrivalStartDate() != null ? getOrganization().getLegacyArrivalStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : null;

        List<String> dates = new ArrayList<>();
        if (startDate != null)
            dates.add(startDate);
        if (legacyBookingStartDate != null)
            dates.add(legacyBookingStartDate);
        if (legacyArrivalStartDate != null)
            dates.add(legacyArrivalStartDate);

        if (dates.size() == 0)
            throw new Exception("No dates found to start loading reservations.");

        Collections.sort(dates);

        return dates.get(0);
    }
}
