package com.safely.batch.connector.components;

import com.safely.batch.connector.common.domain.safely.auth.JWTToken;
import com.safely.batch.connector.common.services.safely.SafelyAuthenticationService;
import com.safely.batch.connector.JobContext;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoadSafelyAuthService {

  private static final Logger log = LoggerFactory.getLogger(LoadSafelyAuthService.class);
  private final SafelyAuthenticationService safelyAuthenticationService;

  public LoadSafelyAuthService(SafelyAuthenticationService safelyAuthenticationService) {
    this.safelyAuthenticationService = safelyAuthenticationService;
  }

  public void execute(JobContext jobContext, String username, String password) throws Exception {

    Optional<JWTToken> maybeToken = safelyAuthenticationService.authenticate(username, password);

    if (maybeToken.isPresent()) {
      log.info("Authentication token for Safely API found.");
      jobContext.setSafelyToken(maybeToken.get());
    } else {
      log.error("No authentication token retrieved from Safely API.");
      throw new Exception("Authentication unsuccessful.");
    }
  }
}
