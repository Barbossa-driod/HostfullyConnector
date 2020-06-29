package com.safely.batch.connector.steps.convertPmsReservationsToSafely;

import com.safely.api.domain.*;
import com.safely.api.domain.enumeration.*;
import com.safely.batch.connector.pms.reservation.PmsReservation;
import com.safely.batch.connector.steps.JobContext;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ConvertPmsReservationsToSafelyTasklet implements Tasklet {

  private static final Logger log = LoggerFactory
      .getLogger(ConvertPmsReservationsToSafelyTasklet.class);

  @Autowired
  public JobContext jobContext;

  private static final String CONVERTED= "converted";
  private static final String PROCESSED = "processed";
  private static final String FAILED = "failed";
  private static final String FAILED_IDS = "failed_ids";
  private static final String STEP_NAME = "convert_pms_reservations_to_safely";

  @Override
  public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext)
      throws Exception {

    HashMap<String, Object> stepStatistics = new HashMap<>();

    Organization organization = jobContext.getOrganization();

    List<PmsReservation> pmsReservations = jobContext.getPmsReservations();

    List<Reservation> pmsConvertedReservations = new ArrayList<>();

    List<String> failedReservationUids = new ArrayList<>();

    for (PmsReservation pmsReservation : pmsReservations) {
      try {
        Reservation reservation = convertToSafelyReservation(organization, pmsReservation);
        pmsConvertedReservations.add(reservation);
      } catch(Exception e) {
        log.error("Failed to convert Reservation with Uid {}", pmsReservation.getUid());
        failedReservationUids.add(pmsReservation.getUid());
      }
    }

    jobContext.setPmsSafelyReservations(pmsConvertedReservations);

    stepStatistics.put(CONVERTED, pmsConvertedReservations.size());
    stepStatistics.put(PROCESSED, pmsReservations.size());
    stepStatistics.put(FAILED, failedReservationUids.size());
    stepStatistics.put(FAILED_IDS, failedReservationUids);
    jobContext.getJobStatistics().put(STEP_NAME, stepStatistics);

    return RepeatStatus.FINISHED;
  }

  protected Reservation convertToSafelyReservation(Organization organization,
      PmsReservation pmsReservation) {

    Reservation safelyReservation = new Reservation();

    safelyReservation.setOrganizationId(organization.getEntityId());

    safelyReservation.setLegacyOrganizationId(organization.getLegacyOrganizationId());

    safelyReservation.setReferenceId(String.valueOf(pmsReservation.getUid()));

    setReservationType(pmsReservation, safelyReservation, organization);
    setBookingChannelType(pmsReservation, safelyReservation, organization);
    setReservationGuests(pmsReservation, safelyReservation);
    setGuestTypeCounts(pmsReservation, safelyReservation);

    setReservationDates(pmsReservation, safelyReservation);

    setReservationStatus(pmsReservation, safelyReservation);

    safelyReservation.setPmsObjectHashcode(String.valueOf(pmsReservation.hashCode()));

    return safelyReservation;
  }

  private void setBookingChannelType(PmsReservation pmsReservation, Reservation safelyReservation,
      Organization organization) {

    if (pmsReservation.getSource() != null) {
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
      guestAddress.setPostalCode(pmsReservation.getPostalCode().toString());
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

    ReservationStatus status = null;

    switch (pmsReservation.getStatus().toLowerCase()) {
      case "new":
      case "booked":
      case "paid_in_full":
        status = ReservationStatus.ACTIVE;
        break;
      case "cancelled_by_traveler":
      case "cancelled_by_owner":
        status = ReservationStatus.CANCELLED;
        break;
      default:
        status = ReservationStatus.INACTIVE;
        log.warn("Unsupported Reservation Status {} found.", pmsReservation.getStatus());
        break;
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
