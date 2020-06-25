package com.safely.batch.connector.steps.loadPmsProperties;

import com.safely.api.domain.Organization;
import com.safely.batch.connector.client.PropertiesService;
import com.safely.batch.connector.pms.property.PmsProperty;
import com.safely.batch.connector.steps.JobContext;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

public class LoadPmsPropertiesTasklet implements Tasklet {

  private static final Logger log = LoggerFactory.getLogger(LoadPmsPropertiesTasklet.class);

  @Autowired
  private JobContext jobContext;

  @Autowired
  private PropertiesService propertyServices;

  private static final String STEP_NAME= "LOAD_PROPERTIES";
  private static final String LOADED = "LOADED";

  @Override
  public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext)
      throws Exception {

    HashMap<String, Object> stepStatistics = new HashMap<>();

    log.info("Loading properties from PMS");

    String apiKey = jobContext.getHostfullyApiKey();
    String agencyUid = jobContext.getAgencyUid();

    List<PmsProperty> properties = propertyServices.getProperties(apiKey, agencyUid);

    log.info("Loaded {} properties from PMS.", properties.size());

    stepStatistics.put(LOADED, properties.size());
    jobContext.getJobStatistics().put(STEP_NAME, stepStatistics);

    jobContext.setPmsProperties(properties);

    return RepeatStatus.FINISHED;
  }

}
