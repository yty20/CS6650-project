package org.CS6650;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;

import java.util.concurrent.Callable;

public class PostRequestThread implements Callable<PostRequestResult> {
    private static final int SUCCESS_CODE = 201;
    private final LiftRideEventQueue eventQueue;
    private final SkiersApi skiersApi;
    private static final int REQUESTS_PER_THREAD = 1000;
    private int successfulRequests = 0;
    private int failedRequests = 0;
    private static final String BASE_PATH = "http://34.221.93.255:8080/upic-server";

    public PostRequestThread(LiftRideEventQueue eventQueue) {
        this.eventQueue = eventQueue;
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(BASE_PATH);
        this.skiersApi = new SkiersApi(apiClient);
    }

    @Override
    public PostRequestResult call() throws InterruptedException {
        int requestCount = 0;
        while (requestCount < REQUESTS_PER_THREAD) {
            try {
                LiftRideData event = eventQueue.dequeue();
                if(isPoisonPill(event)){
                    eventQueue.enqueue(event);
                    break;
                }

                boolean success = false;
                for (int i = 0; i < 5; i++) {
                    success = sendPostRequest(event);
                    if (success) {
                        successfulRequests++;
                        break;
                    }
                }

                if (!success) {
                    failedRequests++;
                }

                requestCount++;
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                throw new InterruptedException("Thread was interrupted.");
            }
        }
        System.out.println("thread finished");
        return new PostRequestResult(successfulRequests, failedRequests);
    }

    private boolean isPoisonPill(LiftRideData event){
        return event.skierID() == -1;
    }

    private boolean sendPostRequest(LiftRideData event) {
        try {
            LiftRide liftRide = new LiftRide();
            liftRide.setLiftID(event.liftID());
            liftRide.setTime(event.time());
            ApiResponse<Void> apiResponse = skiersApi.writeNewLiftRideWithHttpInfo(liftRide, event.resortID(), "2024", "1", event.skierID());
            return apiResponse.getStatusCode() == SUCCESS_CODE;
        } catch (ApiException e) {
            return false;
        }
    }
}

