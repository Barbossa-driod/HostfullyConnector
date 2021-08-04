package com.safely.batch.connector.components.commonV2;


import com.safely.api.domain.*;
import com.safely.api.domain.enumeration.*;
import com.safely.batch.connector.JobContext;
import com.safely.batch.connector.pmsV2.reservation.PmsReservationV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConvertPmsReservationsToSafelyServiceV2 {

    private static final Logger log = LoggerFactory.getLogger(ConvertPmsReservationsToSafelyServiceV2.class);

    private static final String CONVERTED = "converted";
    private static final String PROCESSED = "processed";
    private static final String FAILED = "failed";
    private static final String FAILED_IDS = "failed_ids";
    private static final String STEP_NAME = "convert_pms_reservations_to_safely";

    public void execute(JobContext jobContext) {
        Map<String, Object> stepStatistics = new HashMap<>();

        Organization organization = jobContext.getOrganization();

        List<PmsReservationV2> pmsReservationV2s = jobContext.getPmsReservationsV2();

        log.info("OrganizationId: {}. Convert PMS reservations to Safely structure.", jobContext.getOrganizationId());

        List<Reservation> pmsConvertedReservations = new ArrayList<>();

        List<String> failedReservationUids = new ArrayList<>();

        for (PmsReservationV2 pmsReservationV2 : pmsReservationV2s) {
            try {
                Reservation reservation = convertToSafelyReservation(organization, pmsReservationV2);
                pmsConvertedReservations.add(reservation);
            } catch (Exception e) {
                String message = String.format("OrganizationId: %s. Failed to convert Reservation with referenceId %s",
                        jobContext.getOrganizationId(), pmsReservationV2.getUid());
                log.error(message, e);
                failedReservationUids.add(pmsReservationV2.getUid());
            }
        }

        jobContext.setPmsSafelyReservations(pmsConvertedReservations);

        log.info("OrganizationId: {}. Converted reservations count: {}", jobContext.getOrganizationId(), pmsConvertedReservations.size());

        stepStatistics.put(CONVERTED, pmsConvertedReservations.size());
        stepStatistics.put(PROCESSED, pmsReservationV2s.size());
        stepStatistics.put(FAILED, failedReservationUids.size());
        stepStatistics.put(FAILED_IDS, failedReservationUids);
        jobContext.getJobStatistics().put(STEP_NAME, stepStatistics);

    }

    protected Reservation convertToSafelyReservation(Organization organization,
                                                     PmsReservationV2 pmsReservationV2) {

        Reservation safelyReservation = new Reservation();

        safelyReservation.setOrganizationId(organization.getEntityId());
        safelyReservation.setLegacyOrganizationId(organization.getLegacyOrganizationId());
        safelyReservation.setReferenceId(String.valueOf(pmsReservationV2.getUid()));

        // set category 1 & 2 codes
        safelyReservation.setCategory1(pmsReservationV2.getSource());
        safelyReservation.setCategory2(pmsReservationV2.getStatus());

        if (pmsReservationV2.getPropertyUid() != null) {
            safelyReservation.setPropertyReferenceId(pmsReservationV2.getPropertyUid());
        }

        setReservationType(pmsReservationV2, safelyReservation, organization);
        setBookingChannelType(pmsReservationV2, safelyReservation, organization);
        setReservationGuests(pmsReservationV2, safelyReservation);
        setGuestTypeCounts(pmsReservationV2, safelyReservation);
        setReservationDates(pmsReservationV2, safelyReservation);
        setReservationStatus(pmsReservationV2, safelyReservation);

        if (pmsReservationV2.getOrderV2().getTotal() != null) {

            String stringTotalPrice = pmsReservationV2.getOrderV2().getTotal();
            BigDecimal price = new BigDecimal(stringTotalPrice);

            safelyReservation.setPriceTotal(price);
            safelyReservation.setCurrency(pmsReservationV2.getPreferredCurrency());

            if (safelyReservation.getArrivalDate() != null && safelyReservation.getDepartureDate() != null) {
                long daysBetween = ChronoUnit.DAYS
                        .between(safelyReservation.getArrivalDate(), safelyReservation.getDepartureDate());
                if (daysBetween > 0) {
                    safelyReservation.setPriceNightly(safelyReservation.getPriceTotal()
                            .divide(BigDecimal.valueOf(daysBetween), 2, RoundingMode.HALF_UP));
                }
            }
        }

        safelyReservation.setPmsObjectHashcode(String.valueOf(pmsReservationV2.hashCode()));

        return safelyReservation;
    }

    private void setBookingChannelType(PmsReservationV2 pmsReservationV2, Reservation safelyReservation,
                                       Organization organization) {

        if (pmsReservationV2.getSource() != null) {
            safelyReservation.setCategory3(pmsReservationV2.getSource());

            //calculate the mapped ReservationType
            BookingChannelType bookingChannelType = BookingChannelType.OTHER;
            String typeCode = pmsReservationV2.getSource();
            Map<String, String> bookingChannelTypeMap = organization
                    .getPmsChannelTypesToSafelyBookingChannelTypesMapping();
            if (bookingChannelTypeMap != null) {
                String safelyBookingChannelType = BookingChannelType.OTHER.name();

                if (bookingChannelTypeMap.containsKey(typeCode)) {
                    safelyBookingChannelType = bookingChannelTypeMap.get(typeCode);
                } else {
                    log.warn("No booking channel type mapping found for PMS value {} for client {} ({})",
                            typeCode, organization.getName(), organization.getId());
                }

                try {
                    bookingChannelType = BookingChannelType.valueOf(safelyBookingChannelType);
                } catch (Exception ex) {
                    log.error(
                            "Failed to convert Booking Channel Type string to enum. PMS value: {}. Safely value: {} for client {} ({})",
                            typeCode, safelyBookingChannelType, organization.getName(), organization.getId());
                    log.error(ex.getMessage());
                    bookingChannelType = BookingChannelType.OTHER;
                }
            } else {
                log.warn("No booking type mappings setup for client {} ({})", organization.getName(),
                        organization.getId());
                bookingChannelType = BookingChannelType.OTHER;
            }
            safelyReservation.setBookingChannelType(bookingChannelType);
        }
    }

    private void setReservationType(PmsReservationV2 pmsReservationV2, Reservation safelyReservation,
                                    Organization organization) {
        //we are setting this setting to other because at this time Hostfully does not record this
        //lead have field "lead Type" i don't sure is it suitable here
        safelyReservation.setReservationType(ReservationType.OTHER);

    }

    private void setReservationGuests(PmsReservationV2 pmsReservationV2, Reservation safelyReservation) {

        Guest guest = new Guest();
        guest.setFirstName(pmsReservationV2.getFirstName());
        guest.setLastName(pmsReservationV2.getLastName());

        setGuestEmails(pmsReservationV2, guest);

        List<GuestAddress> guestAddresses = new ArrayList<>();
        GuestAddress guestAddress = new GuestAddress();
        guestAddress.setCity(pmsReservationV2.getCity());
        guestAddress.setStateCode(pmsReservationV2.getState());

        guestAddress.setCurrent(Boolean.TRUE);
        guestAddress.setType(AddressType.HOME);
        guestAddresses.add(guestAddress);
        guest.setGuestAddresses(guestAddresses);

        setGuestPhoneNumbers(pmsReservationV2, guest);

        List<Guest> guests = new ArrayList<>();
        guests.add(guest);
        safelyReservation.setGuests(guests);

    }

    private void setReservationStatus(PmsReservationV2 pmsReservationV2, Reservation safelyReservation) {

        ReservationStatus status = ReservationStatus.INACTIVE;

        if (pmsReservationV2.getStatus() != null) {
            // Hostfully has a list of status codes, however there can be a value added to the end of the "booked" code
            if (pmsReservationV2.getStatus().toLowerCase().startsWith("booked")) {
                status = ReservationStatus.ACTIVE;
            } else {
                // handle all the standard codes
                switch (pmsReservationV2.getStatus().toLowerCase()) {
                    case "booked":
                    case "paid_in_full":
                        status = ReservationStatus.ACTIVE;
                        break;
                    case "cancelled":
                    case "cancelled_by_traveler":
                    case "cancelled_by_owner":
                        status = ReservationStatus.CANCELLED;
                        break;
                    case "new":
                    case "on_hold":
                    case "blocked":
                    case "declined":
                    case "ignored":
                    case "pending":
                    case "closed_quote":
                    case "closed_hold":
                    case "hold_expired":
                    case "quote_sent":
                        status = ReservationStatus.INACTIVE;
                        break;
                    default:
                        status = ReservationStatus.INACTIVE;
                        log.warn("Unsupported Reservation Status {} found.", pmsReservationV2.getStatus());
                        break;
                }
            }
        } else {
            log.warn("Reservation {} has a missing status code.", pmsReservationV2.getUid());
            status = ReservationStatus.INACTIVE;
        }

        safelyReservation.setPmsStatus(status);
        safelyReservation.setStatus(status);
    }

    private void setReservationDates(PmsReservationV2 pmsReservationV2, Reservation safelyReservation) {

    	safelyReservation.setArrivalDate(pmsReservationV2.getCheckInDate());
        safelyReservation.setDepartureDate(pmsReservationV2.getCheckOutDate());

        LocalDateTime now = LocalDateTime.now();
        safelyReservation.setPmsCreateDate(now);
        safelyReservation.setBookingDate(LocalDate.from(now));
    }

    private void setGuestTypeCounts(PmsReservationV2 pmsReservationV2, Reservation safelyReservation) {

        if (pmsReservationV2.getAdultCount() != null) {
            safelyReservation.setAdults(pmsReservationV2.getAdultCount());
        }
        if (pmsReservationV2.getChildrenCount() != null) {
            safelyReservation.setChildren(pmsReservationV2.getChildrenCount());
        }
        if (pmsReservationV2.getPetCount() != null) {
            safelyReservation.setPets(pmsReservationV2.getPetCount());
        }
    }

    private void setGuestEmails(PmsReservationV2 pmsReservationV2, Guest guest) {

        List<GuestEmail> guestEmails = new ArrayList<>();

        if (pmsReservationV2.getEmail() != null && !pmsReservationV2.getEmail().isEmpty()) {
            GuestEmail guestEmail = new GuestEmail();
            guestEmail.setPrimary(Boolean.TRUE);
            guestEmail.setEmailAddress(pmsReservationV2.getEmail());
            guestEmails.add(guestEmail);
        }
        guest.setGuestEmails(guestEmails);
    }

    private void setGuestPhoneNumbers(PmsReservationV2 pmsReservationV2, Guest guest) {

        List<GuestPhone> guestPhones = new ArrayList<>();

        if (pmsReservationV2.getPhoneNumber() != null) {
            GuestPhone guestPhone = new GuestPhone();
            guestPhone.setNumber(pmsReservationV2.getPhoneNumber());
            guestPhone.setType(PhoneType.PERSONAL);
            guestPhone.setPrimary(Boolean.TRUE);
            guestPhones.add(guestPhone);
        }
        guest.setGuestPhones(guestPhones);
    }
}
