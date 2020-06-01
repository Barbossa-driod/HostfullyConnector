package com.safely.batch.connector.steps.loadPmsProperties;

import com.safely.api.domain.Organization;
import com.safely.batch.connector.client.PropertiesService;
import com.safely.batch.connector.steps.JobContext;
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

  @Override
  public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext)
      throws Exception {

    Organization organization = jobContext.getOrganization();

    log.info("Loading properties from PMS");

//        String serverKey = jobContext.getServerKey();
//        String serverSecret = jobContext.getServerSecret();
//
//        //TODO: find out if we only need to load channel key properties. All server key properties that do not
//        // show up in the channel key list have isActive == false.
//        List<PmsProperty> properties = getProperties(serverKey, serverSecret);

//        log.info("Loaded {} properties from PMS.", properties.size());
//
//        jobContext.setPmsProperties(properties);

    return RepeatStatus.FINISHED;
  }

//  private List<PmsProperty> getProperties(String key, String secret) throws Exception {
//    int page = 0;
//    List<PmsProperty> properties = new ArrayList<>();
//    PropertiesRoot propertiesRoot;
//    do {
//      // increment counter before call, must start at page = 1
//      page++;
//      propertiesRoot = propertyClient.getProperties(page, key, secret);
//
//      properties.addAll(propertiesRoot.get_embedded().getUnits());
//
//      // keep loading properties until we are on the last page of results
//    } while (propertiesRoot.getPage_count() > page);
//
//    return properties;
//  }
}
