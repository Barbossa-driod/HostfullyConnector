package com.safely.batch.connector.client.clientV2;


import com.google.common.util.concurrent.RateLimiter;
import com.safely.batch.connector.pmsV2.orders.OrderV2;
import com.safely.batch.connector.pmsV2.reservation.PmsReservationV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;


@Service
public class ReservationsOrdersServiceV2 {

    private static final Logger log = LoggerFactory.getLogger(ReservationsOrdersServiceV2.class);

    private ReservationsOrdersV2ApiClient reservationsOrdersV2ApiClient;

    private final RateLimiter rateLimiter;

    public ReservationsOrdersServiceV2(ReservationsOrdersV2ApiClient reservationsOrdersV2ApiClient,
                                       @Qualifier("PmsApiRateLimiter") RateLimiter rateLimiter) {
        this.reservationsOrdersV2ApiClient = reservationsOrdersV2ApiClient;
        this.rateLimiter = rateLimiter;
    }

    public void getOrder(String token, List<PmsReservationV2> pmsReservationV2s) throws Exception {

        List<OrderV2> orderV2s = new ArrayList<>();

        try {
            rateLimiter.acquire();

            for (PmsReservationV2 reservation : pmsReservationV2s){

                Call<List<OrderV2>> apiCall = reservationsOrdersV2ApiClient
                        .getOrder(token, reservation.getUid());

                Response<List<OrderV2>> response = apiCall.execute();

                if (!response.isSuccessful()) {
                    log.error("ListOrders call failed! Error Code: {}", response.code());
                    throw new Exception(response.message());
                }

                orderV2s = response.body();

                if (orderV2s.get(0) == null){
                    log.error("Order call failed! Order could not be null");
                    throw new Exception(response.message());
                }

                OrderV2 orderV2 = new OrderV2();
                orderV2 = orderV2s.get(0);
                reservation.setOrderV2(orderV2);
            }
        }catch (Exception ex){
            log.error("Exception while calling ListOrders!", ex);
            throw ex;
        }
    }

}
