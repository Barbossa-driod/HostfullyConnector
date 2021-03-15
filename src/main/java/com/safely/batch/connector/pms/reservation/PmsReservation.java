package com.safely.batch.connector.pms.reservation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PmsReservation {
    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("notes")
    private String notes;

    @JsonProperty("adultCount")
    private Integer adultCount;

    @JsonProperty("city")
    private String city;

    @JsonProperty("postalCode")
    private String postalCode;

    @JsonProperty("source")
    private String source;

    @JsonProperty("checkInDate")
    private LocalDate checkInDate;

    @JsonProperty("uid")
    private String uid;

    @JsonProperty("checkOutDate")
    private LocalDate checkOutDate;

    @JsonProperty("countryCode")
    private String countryCode;

    @JsonProperty("quoteAmount")
    private BigDecimal quoteAmount;

    @JsonProperty("preferredCurrency")
    private String preferredCurrency;

    @JsonProperty("property")
    private PmsReservationProperty property;

    @JsonProperty("stayDetails")
    private PmsReservationStayDetails stayDetails;

    @JsonProperty("modified")
    private String modified;

    @JsonProperty("state")
    private String state;

    @JsonProperty("email")
    private String email;

    @JsonProperty("childrenCount")
    private Integer childrenCount;

    @JsonProperty("petCount")
    private Integer petCount;

    @JsonProperty("reasonForTrip")
    private String reasonForTrip;

    @JsonProperty("agency")
    private PmsReservationAgency agency;

    @JsonProperty("address1")
    private String address1;

    @JsonProperty("address2")
    private String address2;

    @JsonProperty("created")
    private String created;

    @JsonProperty("flightNumber")
    private String flightNumber;

    @JsonProperty("status")
    private String status;

    @JsonProperty("infantCount")
    private Integer infantCount;

    @JsonProperty("phoneNumber")
    private String phoneNumber;

    @JsonProperty("cellphoneNumber")
    private String cellphoneNumber;

    public static class PMSReservationStatus {
        public static final String NEW = "NEW";
        public static final String ON_HOLD = "ON_HOLD";
        public static final String BOOKED = "BOOKED";
        public static final String BLOCKED = "BLOCKED";
        public static final String DECLINED = "DECLINED";
        public static final String IGNORED = "IGNORED";
        public static final String PAID_IN_FULL = "PAID_IN_FULL";
        public static final String PENDING = "PENDING";
        public static final String CANCELLED_BY_TRAVELER = "CANCELLED_BY_TRAVELER";
        public static final String CANCELLED_BY_OWNER = "CANCELLED_BY_OWNER";
        public static final String HOLD_EXPIRED = "HOLD_EXPIRED";
        public static final String CANCELLED = "CANCELLED";
    }

    public static class PMSReservationState {
        public static final String ALABAMA = "AL";
        public static final String ALASKA = "AK";
        public static final String ARIZONA = "AZ";
        public static final String ARKANSAS = "AR";
        public static final String CALIFORNIA = "CA";
        public static final String COLORADO = "CO";
        public static final String CONNECTICUT = "CT";
        public static final String DELAWARE = "DE";
        public static final String FLORIDA = "FL";
        public static final String GEORGIA = "GA";
        public static final String HAWAII = "HI";
        public static final String IDAHO = "ID";
        public static final String ILLINOIS = "IL";
        public static final String INDIANA = "IN";
        public static final String IOWA = "IA";
        public static final String KANSAS = "KS";
        public static final String KENTUCKY = "KY";
        public static final String LOUISANA = "LA";
        public static final String MAINE = "ME";
        public static final String MARYLAND = "MD";
        public static final String MASSACHUSETTS = "MA";
        public static final String MICHIGAN = "MI";
        public static final String MINNESOTA = "MN";
        public static final String MISSISSIPPI = "MS";
        public static final String MISSOURI = "MO";
        public static final String MONTANA = "MT";
        public static final String NEBRASKA = "NE";
        public static final String NEVADA = "NV";
        public static final String NEW_HAMPSHIRE = "NH";
        public static final String NEW_JERSEY = "NJ";
        public static final String NEW_MEXICO = "NM";
        public static final String NEW_YORK = "NY";
        public static final String NORTH_CAROLINA = "NC";
        public static final String NORTH_DAKOTA = "ND";
        public static final String OHIO = "OH";
        public static final String OKLAHOMA = "OK";
        public static final String OREGON = "OR";
        public static final String PENNSYLVANIA = "PA";
        public static final String RHODE_ISLAND = "RI";
        public static final String SOUTH_CAROLINA = "SC";
        public static final String SOUTH_DAKOTA = "SD";
        public static final String TENNESSEE = "TN";
        public static final String TEXAS = "TX";
        public static final String UTAH = "UT";
        public static final String VERMONT = "VT";
        public static final String VIRGINIA = "VA";
        public static final String WASHINGTON = "WA";
        public static final String WEST_VIRGINIA = "WV";
        public static final String WISCONSIN = "WI";
        public static final String WYOMING = "WY";
    }
}



