package com.safely.batch.connector.steps.loadPmsProperties;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadPmsPropertiesStepConfig {

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public LoadPmsPropertiesTasklet getLoadPmsPropertiesTasklet() {
        return new LoadPmsPropertiesTasklet();
    }

    @Bean
    @Qualifier("loadPmsProperties")
    public Step getLoadPmsPropertiesStep(LoadPmsPropertiesTasklet loadPmsPropertiesTasklet) {
        return stepBuilderFactory.get("LoadPmsProperties")
                .tasklet(loadPmsPropertiesTasklet)
                .build();
    }
}
