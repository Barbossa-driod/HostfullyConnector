package com.safely.batch.connector.components;

import com.safely.batch.connector.client.PropertiesService;
import com.safely.batch.connector.pms.property.PmsProperty;
import com.safely.batch.connector.JobContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoadPmsPropertiesService {

  private static final Logger log = LoggerFactory.getLogger(LoadPmsPropertiesService.class);

  private static final String STEP_NAME= "load_properties_from_pms";
  private static final String LOADED = "loaded";

  private final PropertiesService propertiesService;

  public LoadPmsPropertiesService(PropertiesService propertiesService) {
    this.propertiesService = propertiesService;
  }

  public void execute(JobContext jobContext, String apiKey) throws Exception {
    Map<String, Object> stepStatistics = new HashMap<>();

    log.info("Loading properties from PMS");
    String agencyUid = jobContext.getAgencyUid();

    List<PmsProperty> properties = propertiesService.getProperties(apiKey, agencyUid);

    log.info("Loaded {} properties from PMS.", properties.size());

    stepStatistics.put(LOADED, properties.size());
    jobContext.getJobStatistics().put(STEP_NAME, stepStatistics);

    jobContext.setPmsProperties(properties);
  }
}
