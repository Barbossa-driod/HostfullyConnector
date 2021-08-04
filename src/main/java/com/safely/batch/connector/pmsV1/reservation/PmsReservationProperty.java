package com.safely.batch.connector.pmsV1.reservation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.safely.batch.connector.pmsV1.property.PmsPropertyPhoto;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PmsReservationProperty {
    @JsonProperty("postalCode")
    private String postalCode;

    @JsonProperty("latitude")
    private BigDecimal latitude;

    @JsonProperty("longitude")
    private BigInteger longitude;

    @JsonProperty("type")
    private String type;

    @JsonProperty("photos")
    private List<PmsPropertyPhoto> photos;

    @JsonProperty("uid")
    private String uid;

    @JsonProperty("maximumGuests")
    private Integer maximumGuests;

    //this is being parsed as a double for now but might need to be changed
    @JsonProperty("cleaningFeeAmount")
    private Double cleaningFeeAmount;

    @JsonProperty("countryCode")
    private String countryCode;

    @JsonProperty("securityDepositAmount")
    private Double securityDepositAmount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("state")
    private String state;

    @JsonProperty("baseGuests")
    private Integer baseGuests;

    @JsonProperty("floor")
    private Integer floor;

    @JsonProperty("url")
    private String url;

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

    //note: I think there is a fixed list of values for this
    @JsonProperty("areaSizeUnit")
    private String areaSizeUnit;

    @JsonProperty("name")
    private String name;

    @JsonProperty("baseDailyRate")
    private Integer baseDailyRate;

    @JsonProperty("webLink")
    private String webLink;

    @JsonProperty("city")
    private String city;

    @JsonProperty("externalID")
    private String externalID;

    @JsonProperty("availabilityCalendarUrl")
    private String availabilityCalendarUrl;

    public static class type {
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
