package com.safely.batch.connector.steps.convertPmsReservationsToSafely;

import com.safely.api.domain.*;
import com.safely.api.domain.enumeration.*;
import com.safely.batch.connector.pms.reservation.PmsReservation;
import com.safely.batch.connector.steps.JobContext;
import java.util.Map;
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

    Reservation safelyReservation = new Reservation();

    safelyReservation.setOrganizationId(organization.getId());
    safelyReservation.setReferenceId(String.valueOf(pmsReservation.getUid()));

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

        if (pmsReservation.getSource() != null) {
            //calculate the mapped ReservationType
            BookingChannelType bookingChannelType = BookingChannelType.OTHER;
            String typeCode = pmsReservation.getSource();
            Map<String, String> bookingChannelTypeMap = organization.getPmsChannelTypesToSafelyBookingChannelTypesMapping();
            if (bookingChannelTypeMap != null) {
                String safelyBookingChannelType = BookingChannelType.OTHER.name();

                if (bookingChannelTypeMap.containsKey(typeCode)) {
                    safelyBookingChannelType = bookingChannelTypeMap.get(typeCode);
                } else {
                    log.warn("No booking channel type mapping found for PMS value {} for client {} ({})", typeCode, organization.getName(), organization.getId());
                }

                try {
                    bookingChannelType = BookingChannelType.valueOf(safelyBookingChannelType);
                } catch (Exception ex) {
                    log.error("Failed to convert Booking Channel Type string to enum. PMS value: {}. Safely value: {} for client {} ({})", typeCode, safelyBookingChannelType, organization.getName(), organization.getId());
                    log.error(ex.getMessage());
                    bookingChannelType = BookingChannelType.OTHER;
                }
            } else {
                log.warn("No booking type mappings setup for client {} ({})", organization.getName(), organization.getId());
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
        guestAddress.setPostalCode(pmsReservation.getPostalCode().toString());
        guestAddress.setCountryCode(pmsReservation.getCountryCode());
        guestAddress.setCurrent(Boolean.TRUE);
        guestAddress.setType(AddressType.HOME);
        guestAddresses.add(guestAddress);
        guest.setGuestAddresses(guestAddresses);

        List<Guest> guests = new ArrayList<>();
        guests.add(guest);
        safelyReservation.setGuests(guests);
  }

  private void setReservationStatus(PmsReservation pmsReservation, Reservation safelyReservation) {

    ReservationStatus status = null;

    switch(pmsReservation.getStatus().toLowerCase()) {
      case "new":
      case "booked":
      case "paid_in_full":
        safelyReservation.setStatus(ReservationStatus.ACTIVE);
      case "cancelled_by_traveler":
      case "cancelled_by_owner":
        safelyReservation.setStatus(ReservationStatus.CANCELLED);
      default:
        safelyReservation.setStatus(ReservationStatus.INACTIVE);
    }
    safelyReservation.setPmsStatus(status);
    safelyReservation.setStatus(status);
  }

  private void setReservationDates(PmsReservation pmsReservation, Reservation safelyReservation) {

    if(pmsReservation.getCheckInDate() != null) {
      safelyReservation.setArrivalDate(pmsReservation.getCheckInDate().toLocalDate());
    }

    if(pmsReservation.getCheckOutDate() != null) {
      safelyReservation.setDepartureDate(pmsReservation.getCheckOutDate().toLocalDate());
    }

    if(pmsReservation.getCreated() != null){
      safelyReservation.setPmsCreateDate(pmsReservation.getCreated());
    }

    if(pmsReservation.getCreated() != null) {
      safelyReservation.setBookingDate(pmsReservation.getCreated().toLocalDate());
    }

    if(pmsReservation.getModified() != null){
      safelyReservation.setPmsUpdateDate(pmsReservation.getModified());
    }
    //}
  }

  private void setGuestTypeCounts(PmsReservation pmsReservation, Reservation safelyReservation) {

    if(pmsReservation.getAdultCount() != null){
      safelyReservation.setAdults(pmsReservation.getAdultCount());
    }
    if(pmsReservation.getChildrenCount() != null){
      safelyReservation.setChildren(pmsReservation.getChildrenCount());
    }
    if(pmsReservation.getInfantCount()  !=  null){
      safelyReservation.setInfants(pmsReservation.getInfantCount());
    }
  }

  private void setGuestEmails(PmsReservation pmsReservation, Guest guest) {

        List<GuestEmail> guestEmails = new ArrayList<>();

        boolean isPrimaryEmailSet = false;
        if (pmsReservation.getEmail() != null && !pmsReservation.getEmail().isEmpty()) {
            GuestEmail guestEmail = new GuestEmail();
            guestEmail.setPrimary(Boolean.TRUE);
            guestEmail.setEmailAddress(pmsReservation.getEmail());
            guestEmails.add(guestEmail);

            isPrimaryEmailSet = true;
        }
  }

  private void setGuestPhoneNumbers(PmsReservation pmsReservation, Guest guest) {

        List<GuestPhone> guestPhones = new ArrayList<>();

        boolean isPrimaryPhoneSet = false;

         if (pmsReservation.getPhoneNumber() != null) {
           GuestPhone guestPhone = new GuestPhone();
           guestPhone.setNumber(pmsReservation.getPhoneNumber());
           guestPhone.setType(PhoneType.PERSONAL);
           guestPhone.setPrimary(Boolean.TRUE);
           guestPhones.add(guestPhone);
           isPrimaryPhoneSet = true;
         }
        if (pmsReservation.getCellphoneNumber() != null && !pmsReservation.getCellphoneNumber().isEmpty()) {
            GuestPhone guestPhone = new GuestPhone();
            guestPhone.setNumber(pmsReservation.getCellphoneNumber());
            guestPhone.setType(PhoneType.MOBILE);
            guestPhones.add(guestPhone);
        }
  }
}
