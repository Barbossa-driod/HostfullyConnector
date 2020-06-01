package com.safely.batch.connector.pms.reservation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PmsReservation {
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("altConf")
    private String altConf;
    @JsonProperty("unitId")
    private Integer unitId;
    @JsonProperty("arrivalDate")
    private String arrivalDate;
    @JsonProperty("departureDate")
    private String departureDate;
    @JsonProperty("earlyArrival")
    private Boolean earlyArrival;
    @JsonProperty("lateDeparture")
    private Boolean lateDeparture;
    @JsonProperty("arrivalTime")
    private String arrivalTime;
    @JsonProperty("departureTime")
    private String departureTime;
    @JsonProperty("nights")
    private Integer nights;
    @JsonProperty("status")
    private String status;
    @JsonProperty("cancelledAt")
    private String cancelledAt;

    @JsonProperty("updatedAt")
    private String updatedAt;
    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("contactId")
    private Integer contactId;
    @JsonProperty("folioId")
    private Integer folioId;
    @JsonProperty("guaranteePolicyId")
    private Integer guaranteePolicyId;
    @JsonProperty("cancellationPolicyId")
    private Integer cancellationPolicyId;
    @JsonProperty("userId")
    private Integer userId;
    @JsonProperty("typeId")
    private Integer typeId;
    @JsonProperty("rateTypeId")
    private Integer rateTypeId;
    @JsonProperty("cancelledById")
    private Integer cancelledById;
    @JsonProperty("cancelledBy")
    private String cancelledBy;
    @JsonProperty("holdExpiration")
    private String holdExpiration;
    @JsonProperty("isTaxable")
    private Boolean isTaxable;
    @JsonProperty("inviteUuid")
    private String inviteUuid;
    @JsonProperty("uuid")
    private String uuid;
    @JsonProperty("source")
    private String source;
    @JsonProperty("agreementStatus")
    private String agreementStatus;
    @JsonProperty("automatePayment")
    private Boolean automatePayment;
    @JsonProperty("updatedBy")
    private String updatedBy;
    @JsonProperty("createdBy")
    private String createdBy;
}

