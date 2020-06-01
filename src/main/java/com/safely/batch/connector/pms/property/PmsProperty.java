package com.safely.batch.connector.pms.property;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PmsProperty {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("shortName")
    private String shortName;

    @JsonProperty("streetAddress")
    private String streetAddress;
    @JsonProperty("extendedAddress")
    private String extendedAddress;
    @JsonProperty("locality")
    private String locality;
    @JsonProperty("region")
    private String region;
    @JsonProperty("postal")
    private String postal;
    @JsonProperty("country")
    private String country;
    @JsonProperty("longitude")
    private String longitude;
    @JsonProperty("latitude")
    private String latitude;

    @JsonProperty("bedrooms")
    private Integer bedrooms;
    @JsonProperty("fullBathrooms")
    private Integer fullBathrooms;
    @JsonProperty("threeQuarterBathrooms")
    private Integer threeQuarterBathrooms;
    @JsonProperty("halfBathrooms")
    private Integer halfBathrooms;
    @JsonProperty("maxOccupancy")
    private Integer maxOccupancy;
    @JsonProperty("coverImage")
    private String coverImage;

    @JsonProperty("createdAt")
    private String createdAt;
    @JsonProperty("createdBy")
    private String createdBy;
    @JsonProperty("updatedAt")
    private String updatedAt;
    @JsonProperty("updatedBy")
    private String updatedBy;

    @JsonProperty("unitCode")
    private String unitCode;
    @JsonProperty("headline")
    private String headLine;
    @JsonProperty("shortDescription")
    private String shortDescription;
    @JsonProperty("longDescription")
    private String longDescription;
    @JsonProperty("houseRules")
    private String houseRules;

    @JsonProperty("nodeId")
    private Integer nodeId;
    @JsonProperty("directions")
    private String directions;
    @JsonProperty("website")
    private String website;
    @JsonProperty("petsFriendly")
    private Boolean petsFriendly;
    @JsonProperty("maxPets")
    private Integer maxPets;

    @JsonProperty("unitTypeId")
    private Integer unitTypeId;
    @JsonProperty("isActive")
    private Boolean isActive;
}

