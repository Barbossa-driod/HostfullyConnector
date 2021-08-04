package com.safely.batch.connector.pmsV2.property;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PmsPropertyV2 {

    @JsonProperty("postalCode")
    private String postalCode;

    @JsonProperty("latitude")
    private BigDecimal latitude;

    @JsonProperty("longitude")
    private BigDecimal longitude;

    @JsonProperty("type")
    private String type;

    //Get it from another endpoint, don't include in JSON dto
    private List<PmsPropertyPhotoV2> photos;

    @JsonProperty("uid")
    private String uid;

    @JsonProperty("maximumGuests")
    private Integer maximumGuests;

    @JsonProperty("cleaningFeeAmount")
    private Integer cleaningFeeAmount;

    @JsonProperty("countryCode")
    private String countryCode;

    @JsonProperty("securityDepositAmount")
    private BigInteger securityDepositAmount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("currencySymbol")
    private String currencySymbol;

    @JsonProperty("state")
    private String state;

    @JsonProperty("baseGuests")
    private Integer baseGuests;

    @JsonProperty("floor")
    private Integer floor;

    @JsonProperty("areaSize")
    private Integer areaSize;

    @JsonProperty("minimumStay")
    private Integer minimumStay;

    @JsonProperty("address1")
    private String address1;

    @JsonProperty("address2")
    private String address2;

    @JsonProperty("bathrooms")
    private String bathrooms;

    @JsonProperty("bedrooms")
    private Integer bedrooms;

    @JsonProperty("picture")
    private String picture;

    @JsonProperty("acceptInstantBook")
    private Boolean acceptInstantBook;

    @JsonProperty("acceptInstantBookingRequest")
    private Boolean acceptInstantBookingRequest;

    @JsonProperty("areaSizeUnit")
    private String areaSizeUnit;

    @JsonProperty("bookingWindow")
    private Integer bookingWindow;

    @JsonProperty("bookingWIndowAfterCheckout")
    private Integer bookingWindowAfterCheckout;

    @JsonProperty("name")
    private String name;

    @JsonProperty("baseDailyRate")
    private Integer baseDailyRate;

    @JsonProperty("webLink")
    private String webLink;

    @JsonProperty("externalID")
    private String externalID;

    @JsonProperty("city")
    private String city;

    @JsonProperty("weekEndRatePercentAdjustment")
    private BigDecimal weekEndRatePercentAdjustment;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("reviews")
    private PmsPropertyReviewV2 reviews;

    @JsonProperty("turnOverDays")
    private Integer turnOverDays;

    @JsonProperty("percentUponReservation")
    private Integer percentUponReservation;

    @JsonProperty("minimumWeekendStay")
    private Integer minimumWeekendStay;

    @JsonProperty("shortDescription")
    private String shortDescription;

    @JsonProperty("description")
    private String description;

    @JsonProperty("propertyURL")
    private String propertyURL;

    @JsonProperty("bookingLeadTime")
    private BigInteger bookingLeadTime;

    @JsonProperty("taxationRate")
    private BigDecimal taxationRate;

    @JsonProperty("wifiNetwork")
    private String wifiNetwork;

    @JsonProperty("wifiPassword")
    private String wifiPassword;

    @JsonProperty("defaultCheckoutTime")
    private BigInteger defaultCheckoutTime;

    @JsonProperty("defaultCheckinTime")
    private Integer defaultCheckinTime;

    @JsonProperty("fullPaymentTiming")
    private Integer fullPaymentTiming;

    @JsonProperty("maximumStay")
    private Integer maximumStay;

    @JsonProperty("createdDate")
    private String createdDate;

    @JsonProperty("extraGuestFee")
    private Integer extraGuestFee;

    @JsonProperty("airBnBID")
    private String airBnBID;

    public static class typeV2 {
        public static final String HOUSE = "HOUSE";
        public static final String APARTMENT = "APARTMENT";
        public static final String COTTAGE = "COTTAGE";
        public static final String CONDO = "CONDO";
        public static final String BED_AND_BREAKFAST = "BED_AND_BREAKFAST";
        public static final String VILLA = "VILLA";
        public static final String CHALET = "CHALET";
        public static final String CABIN = "CABIN";
        public static final String STUDIO = "STUDIO";
        public static final String ROOM = "ROOM";
        public static final String HOSTEL = "HOSTEL";
        public static final String HOTEL = "HOTEL";
        public static final String LODGE = "LODGE";
        public static final String TOWNHOUSE = "TOWNHOUSE";
        public static final String DUPLEX = "DUPLEX";
        public static final String TRIPLEX = "TRIPLEX";
        public static final String FOURPLEX = "FOURPLEX";
        public static final String BUNGALOW = "BUNGALOW";
        public static final String FARMHOUSE = "FARMHOUSE";
        public static final String BOAT = "BOAT";
        public static final String HOUSEBOAT = "HOUSEBOAT";
        public static final String RESORT = "RESORT";
        public static final String PENTHOUSE = "PENTHOUSE";
        public static final String CHATEAU = "CHATEAU";
        public static final String GUEST_HOUSE = "GUEST_HOUSE";
        public static final String HOMESTAY = "HOMESTAY";
        public static final String CAMPGROUND = "CAMPGROUND";
        public static final String MOTEL_ROOM = "MOTEL_ROOM";
        public static final String COUNTRY_HOUSE = "COUNTRY_HOUSE";
        public static final String OTHER = "OTHER";
    }
}
