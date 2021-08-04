package com.safely.batch.connector.pmsV2.orders;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderV2 {

    @JsonProperty("uid")
    private String	uid;

    @JsonProperty("leadUid")
    private String	leadUid;

    @JsonProperty("total")
    private String	total;

    @JsonProperty("subTotal")
    private String	subTotal;

    @JsonProperty("baseAmount")
    private String	baseAmount;

    @JsonProperty("channelCommission")
    private Integer	channelCommission;

    @JsonProperty("cleaningFeeAmount")
    private String  cleaningFeeAmount;

    @JsonProperty("cleaningFeeTaxAmount")
    private String  cleaningFeeTaxAmount;

    @JsonProperty("securityDepositAmount")
    private String	securityDepositAmount;

    @JsonProperty("taxAmount")
    private String	taxAmount;

    @JsonProperty("extraGuestFeeAmount")
    private String	extraGuestFeeAmount;

    @JsonProperty("balance")
    private String	balance;

    @JsonProperty("currency")
    private String	currency;

    @JsonProperty("orderFees")
    private List<String> orderFees;
}
