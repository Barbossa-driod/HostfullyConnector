package com.safely.batch.connector;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import com.safely.batch.connector.client.PropertiesV1ApiClient;
import com.safely.batch.connector.client.ReservationsV1ApiClient;
import com.safely.batch.connector.common.client.safely.AuthenticationV1ApiClient;
import com.safely.batch.connector.common.client.safely.ConnectorEventsV1ApiClient;
import com.safely.batch.connector.common.client.safely.ConnectorOrganizationsV1ApiClient;
import com.safely.batch.connector.common.client.safely.ConnectorPropertiesV1ApiClient;
import com.safely.batch.connector.common.client.safely.ConnectorReservationsV1ApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Configuration
public class RetrofitConfiguration {

  private static final Logger log = LoggerFactory.getLogger(RetrofitConfiguration.class);

  @Autowired
  public SafelyPropertiesConfig safelyPropertiesConfig;
  
  @Bean
  @Qualifier("PmsApiBuilder")
  public Retrofit getPmsApiRetrofit(ObjectMapper objectMapper) {

    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(safelyPropertiesConfig.getHostfullyBaseUrl())
        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
        .build();

    return retrofit;
  }

  @Bean
  public PropertiesV1ApiClient getPropertiesV1ApiClient(
      @Qualifier("PmsApiBuilder") Retrofit retrofit) {
    return retrofit.create(PropertiesV1ApiClient.class);
  }

  @Bean
  public ReservationsV1ApiClient getReservationsV1ApiClient(
      @Qualifier("PmsApiBuilder") Retrofit retrofit) {
    return retrofit.create(ReservationsV1ApiClient.class);
  }

  @Bean
  @Qualifier("PmsApiRateLimiter")
  public RateLimiter getPmsApiRateLimiter() {
    int ratePerMinute = safelyPropertiesConfig.getPmsRateLimitPerMinute() == null ? 17
        : safelyPropertiesConfig.getPmsRateLimitPerMinute();
    double permitsPerSecond = ratePerMinute / 60.0;
    log.info("Creating Pms API RateLimiter with permits per second of: {}", permitsPerSecond);
    return RateLimiter.create(permitsPerSecond);
  }

  @Bean
  @Qualifier("SafelyApiBuilder")
  public Retrofit getSafelyApiRetrofit(ObjectMapper objectMapper) {

    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(safelyPropertiesConfig.getBaseUrl())
        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
        .build();

    return retrofit;
  }

  @Bean
  public AuthenticationV1ApiClient getSafelyAuthenticationV1ApiClient(
      @Qualifier("SafelyApiBuilder") Retrofit retrofit) {
    return retrofit.create(AuthenticationV1ApiClient.class);
  }

  @Bean
  public ConnectorOrganizationsV1ApiClient getSafelyConnectorOrganizationsV1ApiClient(
      @Qualifier("SafelyApiBuilder") Retrofit retrofit) {
    return retrofit.create(ConnectorOrganizationsV1ApiClient.class);
  }

  @Bean
  public ConnectorPropertiesV1ApiClient getSafelyConnectorPropertiesV1ApiClient(
      @Qualifier("SafelyApiBuilder") Retrofit retrofit) {
    return retrofit.create(ConnectorPropertiesV1ApiClient.class);
  }

  @Bean
  public ConnectorReservationsV1ApiClient getSafelyConnectorReservationsV1ApiClient(
      @Qualifier("SafelyApiBuilder") Retrofit retrofit) {
    return retrofit.create(ConnectorReservationsV1ApiClient.class);
  }

  @Bean
  public ConnectorEventsV1ApiClient getSafelyConnectorEventsV1ApiClient(
      @Qualifier("SafelyApiBuilder") Retrofit retrofit) {
    return retrofit.create(ConnectorEventsV1ApiClient.class);
  }

  @Bean
  @Qualifier("SafelyApiRateLimiter")
  public RateLimiter getSafelyApiRateLimiter() {
    int ratePerMinute = safelyPropertiesConfig.getSafelyRateLimitPerMinute() == null ? 100
        : safelyPropertiesConfig.getSafelyRateLimitPerMinute();
    double permitsPerSecond = ratePerMinute / 60.0;
    log.info("Creating Safely API RateLimiter with permits per second of: {}", permitsPerSecond);
    return RateLimiter.create(permitsPerSecond);
  }
}
