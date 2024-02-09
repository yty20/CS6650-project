package org.CS6650;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;

public class testThroughput {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath("http://34.221.93.255:8080/upic-server");
        SkiersApi skiersApi = new SkiersApi(apiClient);
        for(int i = 0; i < 1000; i++){
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
        double totalTime = (endTime - startTime) / 1000.0;
        System.out.println("Test throughput" + 10000/totalTime + " /s");
    }
}
