package com.safely.batch.connector.steps.loadPmsReservations;

import com.safely.batch.connector.client.PropertiesService;
import com.safely.batch.connector.client.ReservationsService;
import com.safely.batch.connector.pms.property.PmsProperty;
import com.safely.batch.connector.pms.reservation.PmsReservation;
import com.safely.batch.connector.pms.reservation.PmsReservationProperty;
import com.safely.batch.connector.steps.JobContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

public class LoadPmsReservationsTasklet implements Tasklet {

  private static final Logger log = LoggerFactory.getLogger(LoadPmsReservationsTasklet.class);

  @Autowired
  private JobContext jobContext;

  @Autowired
  private ReservationsService reservationsService;

  @Autowired
  private PropertiesService propertiesService;

  private static final String STEP_NAME = "load_reservations_from_pms";
  private static final String LOADED = "loaded";

  @Override
  public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext)
      throws Exception {
    Map<String, Object> stepStatistics = new HashMap<>();

    log.info("Loading reservations from PMS");
    String token = jobContext.getHostfullyApiKey();
    String agencyUid = jobContext.getAgencyUid();
    List<PmsReservation> serverReservations = reservationsService.getReservations(token, agencyUid);

    // get properties and convert to map
    List<PmsProperty> properties = jobContext.getPmsProperties();
    Map<String, PmsProperty> propertyMapById = new HashMap<>();
    for (PmsProperty property : properties) {
      propertyMapById.put(property.getUid(), property);
    }

    // check to ensure all properties for reservations exist, if one is not in list, load it singly
    for (PmsReservation reservation : serverReservations) {
      PmsReservationProperty reservationProperty = reservation.getProperty();
      if (reservationProperty == null) {
        continue;
      }
      if (propertyMapById.containsKey(reservationProperty.getUid())) {
        continue;
      }

      // missing property
      String uid = reservationProperty.getUid();
      try {
        PmsProperty newProperty = propertiesService.getProperty(token, uid, agencyUid);
        if (newProperty != null) {
          propertyMapById.put(uid, newProperty);
          properties.add(newProperty);
        }
      } catch (Exception ex) {
        log.error("Error while trying to load inactive property with id: {}", uid);
        log.error(ex.getMessage(), ex);
      }
    }

    log.info("Finished Loading {} from PMS ", serverReservations.size());
    scanReservationTypes(serverReservations);

    stepStatistics.put(LOADED, serverReservations.size());
    jobContext.getJobStatistics().put(STEP_NAME, stepStatistics);

    jobContext.setPmsReservations(serverReservations);
    jobContext.setPmsProperties(properties);

    return RepeatStatus.FINISHED;
  }


  private void scanReservationTypes(List<PmsReservation> reservations) {

    Map<String, String> source = new HashMap<>();
    Map<String, String> status = new HashMap<>();

    for (PmsReservation reservation : reservations) {
      if (reservation != null) {
        if (!source.containsKey(reservation.getSource())) {
          source.put(reservation.getSource(), reservation.getSource());
        }
        if (!status.containsKey(reservation.getStatus())) {
          status.put(reservation.getStatus(), reservation.getStatus());
        }
      }
    }

    log.info("Reservation source:");
    for (Entry<String, String> entry : source.entrySet()) {
      log.info("{}: {}", entry.getKey(), entry.getValue());
    }
    log.info("Reservation status:");
    for (Entry<String, String> entry : status.entrySet()) {
      log.info("{}: {}", entry.getKey(), entry.getValue());
    }
  }
}
