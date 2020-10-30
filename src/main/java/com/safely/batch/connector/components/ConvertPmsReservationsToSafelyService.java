package com.safely.batch.connector.components;

import com.safely.api.domain.Guest;
import com.safely.api.domain.GuestAddress;
import com.safely.api.domain.GuestEmail;
import com.safely.api.domain.GuestPhone;
import com.safely.api.domain.Organization;
import com.safely.api.domain.Reservation;
import com.safely.api.domain.enumeration.AddressType;
import com.safely.api.domain.enumeration.BookingChannelType;
import com.safely.api.domain.enumeration.PhoneType;
import com.safely.api.domain.enumeration.ReservationStatus;
import com.safely.api.domain.enumeration.ReservationType;
import com.safely.batch.connector.pms.reservation.PmsReservation;
import com.safely.batch.connector.JobContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ConvertPmsReservationsToSafelyService {

  private static final Logger log = LoggerFactory.getLogger(ConvertPmsReservationsToSafelyService.class);

  private static final String CONVERTED = "converted";
  private static final String PROCESSED = "processed";
  private static final String FAILED = "failed";
  private static final String FAILED_IDS = "failed_ids";
  private static final String STEP_NAME = "convert_pms_reservations_to_safely";

  public void execute(JobContext jobContext) {
    Map<String, Object> stepStatistics = new HashMap<>();

    Organization organization = jobContext.getOrganization();

    List<PmsReservation> pmsReservations = jobContext.getPmsReservations();

    List<Reservation> pmsConvertedReservations = new ArrayList<>();

    List<String> failedReservationUids = new ArrayList<>();

    for (PmsReservation pmsReservation : pmsReservations) {
      try {
        Reservation reservation = convertToSafelyReservation(organization, pmsReservation);
        pmsConvertedReservations.add(reservation);
      } catch (Exception e) {
        String message = String
            .format("Failed to convert reservation with Uid %s", pmsReservation.getUid());
        log.error(message, e);
        failedReservationUids.add(pmsReservation.getUid());
      }
    }

    jobContext.setPmsSafelyReservations(pmsConvertedReservations);

    stepStatistics.put(CONVERTED, pmsConvertedReservations.size());
    stepStatistics.put(PROCESSED, pmsReservations.size());
    stepStatistics.put(FAILED, failedReservationUids.size());
    stepStatistics.put(FAILED_IDS, failedReservationUids);
    jobContext.getJobStatistics().put(STEP_NAME, stepStatistics);

  }

  protected Reservation convertToSafelyReservation(Organization organization,
      PmsReservation pmsReservation) {

    Reservation safelyReservation = new Reservation();

    safelyReservation.setOrganizationId(organization.getEntityId());
    safelyReservation.setLegacyOrganizationId(organization.getLegacyOrganizationId());
    safelyReservation.setReferenceId(String.valueOf(pmsReservation.getUid()));

    // set category 1 & 2 codes
    safelyReservation.setCategory1(pmsReservation.getSource());
    safelyReservation.setCategory2(pmsReservation.getStatus());

    if (pmsReservation.getProperty() != null) {
      safelyReservation.setPropertyReferenceId(pmsReservation.getProperty().getUid());
      safelyReservation.setPropertyName(pmsReservation.getProperty().getName());
    }

    setReservationType(pmsReservation, safelyReservation, organization);
    setBookingChannelType(pmsReservation, safelyReservation, organization);
    setReservationGuests(pmsReservation, safelyReservation);
    setGuestTypeCounts(pmsReservation, safelyReservation);
    setReservationDates(pmsReservation, safelyReservation);
    setReservationStatus(pmsReservation, safelyReservation);

    if (pmsReservation.getQuoteAmount() != null) {
      safelyReservation.setPriceTotal(pmsReservation.getQuoteAmount());
      safelyReservation.setCurrency(pmsReservation.getPreferredCurrency());

      if (safelyReservation.getArrivalDate() != null
          && safelyReservation.getDepartureDate() != null) {
        long daysBetween = ChronoUnit.DAYS
            .between(safelyReservation.getArrivalDate(), safelyReservation.getDepartureDate());
        if (daysBetween > 0) {
          safelyReservation.setPriceNightly(safelyReservation.getPriceTotal()
              .divide(BigDecimal.valueOf(daysBetween), 2, RoundingMode.HALF_UP));
        }
      }
    }

    safelyReservation.setPmsObjectHashcode(String.valueOf(pmsReservation.hashCode()));

    return safelyReservation;
  }

  private void setBookingChannelType(PmsReservation pmsReservation, Reservation safelyReservation,
      Organization organization) {

    if (pmsReservation.getSource() != null) {
      safelyReservation.setCategory3(pmsReservation.getSource());

      //calculate the mapped ReservationType
      BookingChannelType bookingChannelType = BookingChannelType.OTHER;
      String typeCode = pmsReservation.getSource();
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

  private void setReservationType(PmsReservation pmsReservation, Reservation safelyReservation,
      Organization organization) {
    //we are setting this setting to other because at this time Hostfully does not record this
    safelyReservation.setReservationType(ReservationType.OTHER);

  }

  private void setReservationGuests(PmsReservation pmsReservation, Reservation safelyReservation) {
    Guest guest = new Guest();
    guest.setFirstName(pmsReservation.getFirstName());
    guest.setLastName(pmsReservation.getLastName());

    setGuestEmails(pmsReservation, guest);

    List<GuestAddress> guestAddresses = new ArrayList<>();
    GuestAddress guestAddress = new GuestAddress();
    guestAddress.setStreetLine1(pmsReservation.getAddress1());
    guestAddress.setStreetLine2(pmsReservation.getAddress2());
    guestAddress.setCity(pmsReservation.getCity());
    guestAddress.setStateCode(pmsReservation.getState());

    if (pmsReservation.getPostalCode() != null) {
      guestAddress.setPostalCode(pmsReservation.getPostalCode());
    }

    guestAddress.setCountryCode(pmsReservation.getCountryCode());
    guestAddress.setCurrent(Boolean.TRUE);
    guestAddress.setType(AddressType.HOME);
    guestAddresses.add(guestAddress);
    guest.setGuestAddresses(guestAddresses);

    setGuestPhoneNumbers(pmsReservation, guest);

    List<Guest> guests = new ArrayList<>();
    guests.add(guest);
    safelyReservation.setGuests(guests);

  }

  private void setReservationStatus(PmsReservation pmsReservation, Reservation safelyReservation) {

    ReservationStatus status = ReservationStatus.INACTIVE;

    if (pmsReservation.getStatus() != null) {
      // Hostfully has a list of status codes, however there can be a value added to the end of the "booked" code
      if (pmsReservation.getStatus().toLowerCase().startsWith("booked")) {
        status = ReservationStatus.ACTIVE;
      } else {
        // handle all the standard codes
        switch (pmsReservation.getStatus().toLowerCase()) {
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
            log.warn("Unsupported Reservation Status {} found.", pmsReservation.getStatus());
            break;
        }
      }
    } else {
      log.warn("Reservation {} has a missing status code.", pmsReservation.getUid());
      status = ReservationStatus.INACTIVE;
    }

    safelyReservation.setPmsStatus(status);
    safelyReservation.setStatus(status);
  }

  private void setReservationDates(PmsReservation pmsReservation, Reservation safelyReservation) {

    if (pmsReservation.getCheckInDate() != null) {
      safelyReservation.setArrivalDate(LocalDate.parse(pmsReservation.getCheckInDate(),
          DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }

    if (pmsReservation.getCheckOutDate() != null) {
      safelyReservation.setDepartureDate(LocalDate.parse(pmsReservation.getCheckOutDate(),
          DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }

    if (pmsReservation.getCreated() != null) {
      safelyReservation.setPmsCreateDate(LocalDateTime.parse(pmsReservation.getCreated(),
          DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")));
    }

    if (pmsReservation.getCreated() != null) {
      safelyReservation.setBookingDate(LocalDate.parse(pmsReservation.getCreated(),
          DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")));
    }

    if (pmsReservation.getModified() != null) {
      safelyReservation.setPmsUpdateDate(LocalDateTime
          .parse(pmsReservation.getModified(),
              DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")));
    }
  }

  private void setGuestTypeCounts(PmsReservation pmsReservation, Reservation safelyReservation) {

    if (pmsReservation.getAdultCount() != null) {
      safelyReservation.setAdults(pmsReservation.getAdultCount());
    }
    if (pmsReservation.getChildrenCount() != null) {
      safelyReservation.setChildren(pmsReservation.getChildrenCount());
    }
    if (pmsReservation.getInfantCount() != null) {
      safelyReservation.setInfants(pmsReservation.getInfantCount());
    }
  }

  private void setGuestEmails(PmsReservation pmsReservation, Guest guest) {

    List<GuestEmail> guestEmails = new ArrayList<>();

    if (pmsReservation.getEmail() != null && !pmsReservation.getEmail().isEmpty()) {
      GuestEmail guestEmail = new GuestEmail();
      guestEmail.setPrimary(Boolean.TRUE);
      guestEmail.setEmailAddress(pmsReservation.getEmail());
      guestEmails.add(guestEmail);
    }
    guest.setGuestEmails(guestEmails);
  }

  private void setGuestPhoneNumbers(PmsReservation pmsReservation, Guest guest) {

    List<GuestPhone> guestPhones = new ArrayList<>();

    if (pmsReservation.getPhoneNumber() != null) {
      GuestPhone guestPhone = new GuestPhone();
      guestPhone.setNumber(pmsReservation.getPhoneNumber());
      guestPhone.setType(PhoneType.PERSONAL);
      guestPhone.setPrimary(Boolean.TRUE);
      guestPhones.add(guestPhone);
    }
    if (pmsReservation.getCellphoneNumber() != null && !pmsReservation.getCellphoneNumber()
        .isEmpty()) {
      GuestPhone guestPhone = new GuestPhone();
      guestPhone.setNumber(pmsReservation.getCellphoneNumber());
      guestPhone.setType(PhoneType.MOBILE);
      guestPhones.add(guestPhone);
    }
    guest.setGuestPhones(guestPhones);
  }
}
