package com.safely.batch.connector.pms.property;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PmsProperty {
    @JsonProperty("postalCode")
    private Integer postalCode;

    //note: I am parsing the lat and long as a BigDecimal because that is in MyVR
    @JsonProperty("latitude")
    private BigDecimal latitude;

    @JsonProperty("longitude")
    private BigDecimal longitude;

    @JsonProperty("type")
    private String type;

    @JsonProperty("photos")
    private List<PmsPropertyPhoto> photos;

    @JsonProperty("uid")
    private String uid;

    @JsonProperty("maximumGuests")
    private Integer maximumGuests;

    //this is being parsed as a integer for now but might need to be changed
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

    //TODO: make this into a series of constants???
    @JsonProperty("state")
    private String state;

    @JsonProperty("baseGuests")
    private Integer baseGuests;

    @JsonProperty("floor")
    private Integer floor;

    @JsonProperty("availabilityCalendarUrl")
    private String availabilityCalendarUrl;

    @JsonProperty("areaSize")
    private Integer areaSize;

    @JsonProperty("minimumStay")
    private Integer minimumStay;

    @JsonProperty("address1")
    private String address1;

    @JsonProperty("address2")
    private String address2;

    @JsonProperty("bathrooms")
    private Integer bathrooms;

    @JsonProperty("bedrooms")
    private Integer bedrooms;

    //note: I think it's a URL
    @JsonProperty("picture")
    private String picture;

    @JsonProperty("acceptInstantBook")
    private Boolean acceptInstantBook;

    @JsonProperty("acceptInstantBookingRequest")
    private Boolean acceptInstantBookingRequest;

    //note: I think there is a fixed list of values for this
    @JsonProperty("areaSizeUnit")
    private String areaSizeUnit;

    @JsonProperty("bookingWindow")
    private Integer bookingWindow;

    @JsonProperty("bookingWIndowAfterCheckout")
    private Integer bookingWindowAfterCheckout;

    //i think it returns this as raw HTML
    @JsonProperty("bookingNotes")
    private String bookingNotes;

    @JsonProperty("name")
    private String name;

    @JsonProperty("baseDailyRate")
    private Integer baseDailyRate;

    @JsonProperty("externalCalendarUrls")
    private List<String> externalCalendarUrls;

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
    private PmsPropertyReview reviews;

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

    @JsonProperty("rentalLicenseNumber")
    private BigInteger rentalLicenseNumber;

    @JsonProperty("rentalLicenseNumberExpirationNumber")
    private LocalDate rentalLicenseNumberExpirationNumber;

    @JsonProperty("rentalConditions")
    private List<PmsPropertyRentalConditions> rentalConditions;

    @JsonProperty("propertyURL")
    private String propertyURL;

    @JsonProperty("bookingLeadTime")
    private BigInteger bookingLeadTime;

    @JsonProperty("taxationRate")
    private BigDecimal taxationRate;

    @JsonProperty("cancelationPolicy")
    private String cancelationPolicy;

    @JsonProperty("wifiNetwork")
    private String wifiNetwork;

    @JsonProperty("wifiPassword")
    private String wifiPassword;

    //they literally do this as an integer from 1 - 23 don't ask me why
    @JsonProperty("defaultCheckoutTime")
    private BigInteger defaultCheckoutTime;

    @JsonProperty("defaultCheckinTime")
    private Integer defaultCheckinTime;

    @JsonProperty("fullPaymentTiming")
    private Integer fullPaymentTiming;

    @JsonProperty("maximumStay")
    private Integer maximumStay;

    @JsonProperty("createdDate")
    private LocalDateTime createdDate;

    @JsonProperty("extraGuestFee")
    private Integer extraGuestFee;

    @JsonProperty("airBnBID")
    private String airBnBID;

    public static class type{
        public static final String HOUSE = "HOUSE";
        public static final String APARTMENT = "APARTMENT";
        public static final String COTTAGE = "COTTAGE";
        public static final String CONDO = "CONDO";
        //I did not verify the value of this but i assume it is underscores
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

