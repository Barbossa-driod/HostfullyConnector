package com.safely.batch.connector.components.externalV2;

import com.safely.batch.connector.JobContext;
import com.safely.batch.connector.client.clientV2.PropertiesServiceV2;
import com.safely.batch.connector.pmsV2.property.PmsPropertyV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LoadPmsPropertiesServiceV2 {

    private static final Logger log = LoggerFactory.getLogger(LoadPmsPropertiesServiceV2.class);

    private static final String STEP_NAME = "load_properties_from_pms";
    private static final String LOADED = "loaded";

    private final PropertiesServiceV2 propertiesServiceV2;

    public LoadPmsPropertiesServiceV2(PropertiesServiceV2 propertiesServiceV2) {
        this.propertiesServiceV2 = propertiesServiceV2;
    }

    public void execute(JobContext jobContext, String apiKey) throws Exception {

        //check API version if it is false it means, that API V1
        if (jobContext.getApiVersion()){
            Map<String, Object> stepStatistics = new HashMap<>();

            log.info("OrganizationId: {}. Loading properties from PMS", jobContext.getOrganizationId());
            String agencyUid = jobContext.getAgencyUid();

            List<PmsPropertyV2> properties = propertiesServiceV2.getProperties(apiKey, agencyUid);

            log.info("OrganizationId: {}. Loaded {} properties from PMS.", jobContext.getOrganizationId(), properties.size());

            stepStatistics.put(LOADED, properties.size());
            jobContext.getJobStatistics().put(STEP_NAME, stepStatistics);

            jobContext.setPmsPropertiesV2(properties);
        }
    }
}
