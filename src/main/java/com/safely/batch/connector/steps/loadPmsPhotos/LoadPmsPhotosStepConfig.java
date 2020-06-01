package com.safely.batch.connector.steps.loadPmsPhotos;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadPmsPhotosStepConfig {

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public LoadPmsPhotosTasklet getLoadPmsPhotosTasklet() {
        return new LoadPmsPhotosTasklet();
    }

    @Bean
    @Qualifier("loadPmsPhotos")
    public Step getLoadPmsPhotosStep(LoadPmsPhotosTasklet loadPmsPhotosTasklet) {
        return stepBuilderFactory.get("LoadPmsPhotos")
                .tasklet(loadPmsPhotosTasklet)
                .build();
    }
}
