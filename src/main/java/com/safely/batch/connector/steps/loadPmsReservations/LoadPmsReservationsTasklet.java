package com.safely.batch.connector.steps.loadPmsReservations;

import com.safely.api.domain.Organization;
import com.safely.batch.connector.client.ReservationsService;
import com.safely.batch.connector.pms.reservation.PmsReservation;
import com.safely.batch.connector.steps.JobContext;
import java.util.List;
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

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {

        Organization organization = jobContext.getOrganization();

//        String startingDate = jobContext.getReservationLoadDate();
//        String lastUpdatedDate = jobContext.getLastUpdateDateFromPms();
//
//        String serverKey = jobContext.getServerKey();
//        String serverSecret = jobContext.getServerSecret();
//
//        log.info("Loading reservations with bookingDate: {} and lastUpdatedDate: {}", startingDate, lastUpdatedDate);
//
//        List<PmsReservation> serverReservations = getReservations(startingDate, lastUpdatedDate, serverKey, serverSecret);
//
//        scanReservationTypes(serverReservations);
//
//        jobContext.setPmsReservations(serverReservations);

        return RepeatStatus.FINISHED;
    }


    private void scanReservationTypes(List<PmsReservation> reservations) {
        // TODO: Implement logic to scan reservation types and output unique reservation types
    }
}
