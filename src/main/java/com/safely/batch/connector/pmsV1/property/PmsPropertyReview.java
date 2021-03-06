package com.safely.batch.connector.pmsV1.property;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PmsPropertyReview {

    @JsonProperty("average")
    private BigDecimal average;

    @JsonProperty("total")
    private Integer total;

}
