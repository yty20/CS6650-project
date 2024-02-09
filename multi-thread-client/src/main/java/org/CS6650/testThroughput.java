package org.CS6650;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;

public class testThroughput {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath("http://34.221.93.255:8080/upic-server");
        SkiersApi skiersApi = new SkiersApi(apiClient);
        for(int i = 0; i < 10000; i++){
            try {
                LiftRide liftRide = new LiftRide();
                liftRide.setTime(1);
                liftRide.setLiftID(1);
                skiersApi.writeNewLiftRide(liftRide, 1, "2024", "1", 1);
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        }
        long endTime = System.currentTimeMillis();
        double averageResponseTime = (endTime - startTime) / 10000.0;
        System.out.println("Average response time: " + (endTime - startTime) / 10000.0 + " ms");
        System.out.println("Given N=32, estimate throughout is: " + 32 / averageResponseTime * 1000 + " request/second");
    }
}
