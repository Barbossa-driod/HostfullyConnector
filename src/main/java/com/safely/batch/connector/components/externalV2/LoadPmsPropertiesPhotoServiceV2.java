package com.safely.batch.connector.components.externalV2;

import com.safely.batch.connector.JobContext;
import com.safely.batch.connector.client.clientV2.PropertiesPhotoServiceV2;
import com.safely.batch.connector.pmsV2.property.PmsPropertyV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;

import java.util.List;
import java.util.Map;

@Service
public class LoadPmsPropertiesPhotoServiceV2 {

    private static final Logger log = LoggerFactory.getLogger(LoadPmsPropertiesPhotoServiceV2.class);

    private static final String STEP_NAME = "load_propertiesPhoto_from_pms";

    private final PropertiesPhotoServiceV2 propertiesPhotoServiceV2;

    public LoadPmsPropertiesPhotoServiceV2(PropertiesPhotoServiceV2 propertiesPhotoServiceV2) {
        this.propertiesPhotoServiceV2 = propertiesPhotoServiceV2;
    }

    public void execute(JobContext jobContext, String apiKey) throws Exception {

        //check API version if it is false it means, that API V1
        if (jobContext.getApiVersion()){
            Map<String, Object> stepStatistics = new HashMap<>();

            log.info("OrganizationId: {}. Loading properties photos from PMS", jobContext.getOrganizationId());

            List<PmsPropertyV2> pmsProperties = jobContext.getPmsPropertiesV2();

            propertiesPhotoServiceV2.getPropertiesPhotos(apiKey,pmsProperties);

            log.info("OrganizationId: {}. Loaded properties photos from PMS.", jobContext.getOrganizationId());

            jobContext.getJobStatistics().put(STEP_NAME, stepStatistics);

            jobContext.setPmsPropertiesV2(pmsProperties);
        }

    }
}
