package com.safely.batch.connector.pms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Links {
    @JsonProperty("self")
    private LinkContainer self;
    @JsonProperty("first")
    private LinkContainer first;
    @JsonProperty("last")
    private LinkContainer last;
    @JsonProperty("next")
    private LinkContainer next;
}

