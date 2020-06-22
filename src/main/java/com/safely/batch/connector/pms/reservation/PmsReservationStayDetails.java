package com.safely.batch.connector.pms.reservation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PmsReservationStayDetails {
  @JsonProperty("extraNotes")
  private String extraNotes;

  @JsonProperty("departureDate")
  private String departureDate;

  @JsonProperty("arrivalDate")
  private String arrivalDate;
}
