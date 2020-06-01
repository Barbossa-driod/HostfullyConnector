package com.safely.batch.connector.steps.loadPmsReservations;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadPmsReservationsStepConfig {

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public LoadPmsReservationsTasklet getLoadPmsReservationsTasklet() {
        return new LoadPmsReservationsTasklet();
    }

    @Bean
    @Qualifier("loadPmsReservations")
    public Step getLoadPmsReservationsStep(LoadPmsReservationsTasklet loadPmsReservationsTasklet) {
        return stepBuilderFactory.get("LoadPmsReservations")
                .tasklet(loadPmsReservationsTasklet)
                .build();
    }
}
