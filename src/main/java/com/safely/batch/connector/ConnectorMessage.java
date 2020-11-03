package com.safely.batch.connector;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConnectorMessage {
    @JsonProperty("organizationId")
    private String organizationId;
    @JsonProperty("createDate")
    private LocalDateTime createDate;
}
