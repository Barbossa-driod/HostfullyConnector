package com.safely.batch.connector.steps.convertPmsReservationsToSafely;

import com.safely.api.domain.*;
import com.safely.api.domain.enumeration.*;
import com.safely.batch.connector.pms.reservation.PmsReservation;
import com.safely.batch.connector.steps.JobContext;
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

  @Override
  public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext)
      throws Exception {

    Organization organization = jobContext.getOrganization();
    if (organization.getConnectorOperationMode() != ConnectorOperationMode.RESERVATIONS
        && organization.getConnectorOperationMode() != ConnectorOperationMode.ALL) {
      log.info("Skipping convert reservations from PMS to Safely. Connector Operation Mode: {}",
          organization.getConnectorOperationMode().name());
      return RepeatStatus.FINISHED;
    }

    List<PmsReservation> pmsReservations = jobContext.getPmsReservations();

    List<Reservation> pmsConvertedReservations = new ArrayList<>();
    for (PmsReservation pmsReservation : pmsReservations) {
      Reservation reservation = convertToSafelyReservation(organization, pmsReservation);
      pmsConvertedReservations.add(reservation);
    }

    jobContext.setPmsSafelyReservations(pmsConvertedReservations);

    return RepeatStatus.FINISHED;
  }

  protected Reservation convertToSafelyReservation(Organization organization,
      PmsReservation pmsReservation) {

    // TODO: Implement custom logic to convert PMS Reservation to SafelyReservation

    Reservation safelyReservation = new Reservation();

    safelyReservation.setOrganizationId(organization.getId());
    safelyReservation.setReferenceId(String.valueOf(pmsReservation.getId()));

    setReservationType(pmsReservation, safelyReservation, organization);
    setBookingChannelType(pmsReservation, safelyReservation, organization);
    setGuestTypeCounts(pmsReservation, safelyReservation);

    setReservationDates(pmsReservation, safelyReservation);

    setReservationStatus(pmsReservation, safelyReservation);

    safelyReservation.setPmsObjectHashcode(String.valueOf(pmsReservation.hashCode()));

    return safelyReservation;
  }

  private void setBookingChannelType(PmsReservation pmsReservation, Reservation safelyReservation,
      Organization organization) {

    // TODO: Implement custom logic to determine BookingChannelType in the SafelyReservation from info in PMS Property

//        if (pmsReservation.getTypeInline() != null) {
//            //calculate the mapped ReservationType
//            BookingChannelType bookingChannelType = BookingChannelType.OTHER;
//            String typeCode = pmsReservation.getTypeInline().getCode();
//            Map<String, String> bookingChannelTypeMap = organization.getPmsChannelTypesToSafelyBookingChannelTypesMapping();
//            if (bookingChannelTypeMap != null) {
//                String safelyBookingChannelType = BookingChannelType.OTHER.name();
//
//                if (bookingChannelTypeMap.containsKey(typeCode)) {
//                    safelyBookingChannelType = bookingChannelTypeMap.get(typeCode);
//                } else {
//                    log.warn("No booking channel type mapping found for PMS value {} for client {} ({})", typeCode, organization.getName(), organization.getId());
//                }
//
//                try {
//                    bookingChannelType = BookingChannelType.valueOf(safelyBookingChannelType);
//                } catch (Exception ex) {
//                    log.error("Failed to convert Booking Channel Type string to enum. PMS value: {}. Safely value: {} for client {} ({})", typeCode, safelyBookingChannelType, organization.getName(), organization.getId());
//                    log.error(ex.getMessage());
//                    bookingChannelType = BookingChannelType.OTHER;
//                }
//            } else {
//                log.warn("No booking type mappings setup for client {} ({})", organization.getName(), organization.getId());
//                bookingChannelType = BookingChannelType.OTHER;
//            }
//            safelyReservation.setBookingChannelType(bookingChannelType);
//        }
  }

  private void setReservationType(PmsReservation pmsReservation, Reservation safelyReservation,
      Organization organization) {

    // TODO: Implement custom logic to determine ReservationType in the Safely Reservation from the info in PMS Property

//        if (pmsReservation.getTypeInline() != null) {
//            safelyReservation.setCategory1(pmsReservation.getTypeInline().getCode());
//
//            //calculate the mapped ReservationType
//            ReservationType reservationType = ReservationType.OTHER;
//            String typeCode = pmsReservation.getTypeInline().getCode();
//            Map<String, String> reservationTypeMap = organization.getPmsReservationTypesToSafelyReservationTypesMapping();
//            if (reservationTypeMap != null) {
//                String safelyReservationType = ReservationType.OTHER.name();
//
//                if (reservationTypeMap.containsKey(typeCode)) {
//                    safelyReservationType = reservationTypeMap.get(typeCode);
//                } else {
//                    log.warn("No reservation type mapping found for PMS value {} for client {} ({})", typeCode, organization.getName(), organization.getId());
//                }
//
//                try {
//                    reservationType = ReservationType.valueOf(safelyReservationType);
//                } catch (Exception ex) {
//                    log.error("Failed to convert Reservation Type string to enum. PMS value: {}. Safely value: {} for client {} ({})", typeCode, safelyReservationType, organization.getName(), organization.getId());
//                    log.error(ex.getMessage());
//                    reservationType = ReservationType.OTHER;
//                }
//            } else {
//                log.warn("No reservation type mappings setup for client {} ({})", organization.getName(), organization.getId());
//                reservationType = ReservationType.OTHER;
//            }
//            safelyReservation.setReservationType(reservationType);
//        }
  }

  private void setReservationGuests(Object pmsGuest, Reservation safelyReservation) {

    // TODO: Implement custom logic to migrate Guest data from PMS Reservation to SafelyReservation

    //Create the guest data
//        Guest guest = new Guest();
//        guest.setReferenceId(String.valueOf(pmsGuest.getId()));
//        guest.setFirstName(pmsGuest.getFirstName());
//        guest.setLastName(pmsGuest.getLastName());
//
//        setGuestPhoneNumbers(pmsGuest, guest);
//        setGuestEmails(pmsGuest, guest);
//
//        List<GuestAddress> guestAddresses = new ArrayList<>();
//        GuestAddress guestAddress = new GuestAddress();
//        guestAddress.setStreetLine1(pmsGuest.getStreetAddress());
//        guestAddress.setStreetLine2(pmsGuest.getExtendedAddress());
//        guestAddress.setCity(pmsGuest.getLocality());
//        guestAddress.setStateCode(pmsGuest.getRegion());
//        guestAddress.setPostalCode(pmsGuest.getPostalCode());
//        guestAddress.setCountryCode(pmsGuest.getCountry());
//        guestAddress.setCurrent(Boolean.TRUE);
//        guestAddress.setType(AddressType.HOME);
//        guestAddresses.add(guestAddress);
//        guest.setGuestAddresses(guestAddresses);
//
//        List<Guest> guests = new ArrayList<>();
//        guests.add(guest);
//        safelyReservation.setGuests(guests);
  }

  private void setReservationStatus(PmsReservation pmsReservation, Reservation safelyReservation) {

    // TODO: Implement custom logic to handle PMS Reservation status codes.

    ReservationStatus status = null;
    switch (pmsReservation.getStatus().toLowerCase()) {
      case "hold":
        // a HOLD is a calendar blocking reservation status that has not been confirmed/ or met the guarantee policy
        status = ReservationStatus.ACTIVE;
        break;
      case "confirmed":
      case "checked in":
        status = ReservationStatus.ACTIVE;
        break;
      case "checked out":
        status = ReservationStatus.COMPLETE;
        break;
      case "cancelled":
        status = ReservationStatus.CANCELLED;
        break;
      default:
        log.warn("Unrecognized PMS reservation status code: {}", pmsReservation.getStatus());
        status = ReservationStatus.ACTIVE;
        break;
    }
    safelyReservation.setPmsStatus(status);
    safelyReservation.setStatus(status);
  }

  private void setReservationDates(PmsReservation pmsReservation, Reservation safelyReservation) {

    // TODO: Implement custom logic to extract reservation dates if needed

    // TODO: Revert when we move to >= JAVA 11
    //if (pmsReservation.getArrivalDate() != null && !pmsReservation.getArrivalDate().isBlank()) {
    if (pmsReservation.getArrivalDate() != null && !pmsReservation.getArrivalDate().isEmpty()) {
      LocalDate arrivalDate = LocalDate
          .parse(pmsReservation.getArrivalDate(), DateTimeFormatter.ISO_LOCAL_DATE);
      safelyReservation.setArrivalDate(arrivalDate);
    }
    //if (pmsReservation.getDepartureDate() != null && !pmsReservation.getDepartureDate().isBlank()) {
    if (pmsReservation.getDepartureDate() != null && !pmsReservation.getDepartureDate().isEmpty()) {
      LocalDate departureDate = LocalDate
          .parse(pmsReservation.getDepartureDate(), DateTimeFormatter.ISO_LOCAL_DATE);
      safelyReservation.setDepartureDate(departureDate);
    }
    //if (pmsReservation.getCreatedAt() != null && !pmsReservation.getCreatedAt().isBlank()) {
    if (pmsReservation.getCreatedAt() != null && !pmsReservation.getCreatedAt().isEmpty()) {
      LocalDateTime createdAt = LocalDateTime
          .parse(pmsReservation.getCreatedAt(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
      safelyReservation.setPmsCreateDate(createdAt);

      LocalDate bookingDate = LocalDate
          .parse(pmsReservation.getCreatedAt(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
      safelyReservation.setBookingDate(bookingDate);
    }
    //if (pmsReservation.getUpdatedAt() != null && !pmsReservation.getUpdatedAt().isBlank()) {
    if (pmsReservation.getUpdatedAt() != null && !pmsReservation.getUpdatedAt().isEmpty()) {
      LocalDateTime updatedAt = LocalDateTime
          .parse(pmsReservation.getUpdatedAt(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
      safelyReservation.setPmsUpdateDate(updatedAt);
    }
  }

  private void setGuestTypeCounts(PmsReservation pmsReservation, Reservation safelyReservation) {

    // TODO: Custom logic for guest counts if needed, otherwise remove
  }

  private void setGuestEmails(Object pmsGuest, Guest guest) {

    // TODO: Implement custom logic to handle guest email addresses if needed, otherwise remove

//        List<GuestEmail> guestEmails = new ArrayList<>();
//
//        // get guest email addresses, with order primary, secondary
//        boolean isPrimaryEmailSet = false;
//        // TODO: Revert when >= JAVA 11
//        //if (pmsGuest.getPrimaryEmail() != null && !pmsGuest.getPrimaryEmail().isBlank()) {
//        if (pmsGuest.getPrimaryEmail() != null && !pmsGuest.getPrimaryEmail().isEmpty()) {
//            GuestEmail guestEmail = new GuestEmail();
//            guestEmail.setPrimary(Boolean.TRUE);
//            guestEmail.setEmailAddress(pmsGuest.getPrimaryEmail());
//            guestEmails.add(guestEmail);
//
//            isPrimaryEmailSet = true;
//        }
//        // TODO: Revert when >= JAVA 11
//        //if (pmsGuest.getSecondaryEmail() != null && !pmsGuest.getSecondaryEmail().isBlank()) {
//        if (pmsGuest.getSecondaryEmail() != null && !pmsGuest.getSecondaryEmail().isEmpty()) {
//            GuestEmail guestEmail = new GuestEmail();
//            if (!isPrimaryEmailSet) {
//                guestEmail.setPrimary(Boolean.TRUE);
//                isPrimaryEmailSet = true;
//            } else {
//                guestEmail.setPrimary(Boolean.FALSE);
//            }
//            guestEmail.setEmailAddress(pmsGuest.getSecondaryEmail());
//            guestEmails.add(guestEmail);
//        }
//        guest.setGuestEmails(guestEmails);
  }

  private void setGuestPhoneNumbers(Object pmsGuest, Guest guest) {

    // TODO: Implement custom code to handle guest phone numbers if needed, othewise remove

//        // get guest phone numbers, mobile, home, other
//        List<GuestPhone> guestPhones = new ArrayList<>();
//
//        boolean isPrimaryPhoneSet = false;
//        // TODO: Revert when >= JAVA 11
//        // if (pmsGuest.getCellPhone() != null && !pmsGuest.getCellPhone().isBlank()) {
//        if (pmsGuest.getCellPhone() != null && !pmsGuest.getCellPhone().isEmpty()) {
//            GuestPhone guestPhone = new GuestPhone();
//            guestPhone.setNumber(pmsGuest.getCellPhone());
//            guestPhone.setType(PhoneType.MOBILE);
//            guestPhone.setPrimary(Boolean.TRUE);
//            guestPhones.add(guestPhone);
//
//            isPrimaryPhoneSet = true;
//        }
//        // TODO: Revert when >= JAVA 11
//        //if (pmsGuest.getHomePhone() != null && !pmsGuest.getHomePhone().isBlank()) {
//        if (pmsGuest.getHomePhone() != null && !pmsGuest.getHomePhone().isEmpty()) {
//            GuestPhone guestPhone = new GuestPhone();
//            guestPhone.setNumber(pmsGuest.getHomePhone());
//            guestPhone.setType(PhoneType.HOME);
//            if (!isPrimaryPhoneSet) {
//                guestPhone.setPrimary(Boolean.TRUE);
//                isPrimaryPhoneSet = true;
//            } else {
//                guestPhone.setPrimary(Boolean.FALSE);
//            }
//            guestPhones.add(guestPhone);
//        }
//        // TODO: Revert when >= JAVA 11
//        //if (pmsGuest.getOtherPhone() != null && !pmsGuest.getOtherPhone().isBlank()) {
//        if (pmsGuest.getOtherPhone() != null && !pmsGuest.getOtherPhone().isEmpty()) {
//            GuestPhone guestPhone = new GuestPhone();
//            guestPhone.setNumber(pmsGuest.getOtherPhone());
//            guestPhone.setType(PhoneType.PERSONAL);
//            if (!isPrimaryPhoneSet) {
//                guestPhone.setPrimary(Boolean.TRUE);
//                isPrimaryPhoneSet = true;
//            } else {
//                guestPhone.setPrimary(Boolean.FALSE);
//            }
//            guestPhones.add(guestPhone);
//        }
//        guest.setGuestPhones(guestPhones);
  }
}
