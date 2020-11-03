package com.safely.batch.connector.components;

import com.safely.api.domain.Organization;
import com.safely.batch.connector.JobContext;
import com.safely.batch.connector.common.domain.safely.auth.JWTToken;
import com.safely.batch.connector.common.services.safely.SafelyConnectorOrganizationsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoadOrganizationService {

    private static final Logger log = LoggerFactory.getLogger(LoadOrganizationService.class);
    private final SafelyConnectorOrganizationsService connectorOrganizationsService;

    public LoadOrganizationService(SafelyConnectorOrganizationsService connectorOrganizationsService) {

        this.connectorOrganizationsService = connectorOrganizationsService;
    }

    public void execute(JobContext jobContext, String organizationId) throws Exception {

        JWTToken token = jobContext.getSafelyToken();
        Optional<Organization> maybeOrganization = connectorOrganizationsService
                .getById(token.getIdToken(), organizationId);

        if (maybeOrganization.isPresent()) {
            log.info("Organization found: {}", maybeOrganization.get());
            jobContext.setOrganization(maybeOrganization.get());
        } else {
            throw new Exception("Could not load Organization with id: " + organizationId);
        }
    }
}
