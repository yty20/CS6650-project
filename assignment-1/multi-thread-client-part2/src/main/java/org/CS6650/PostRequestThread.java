package org.CS6650;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;

import java.util.concurrent.atomic.AtomicInteger;

public class PostRequestThread implements Runnable {
    private static final int SUCCESS_CODE = 201;
    private final LiftRideEventQueue eventQueue;
    private final PerformanceQueue performanceQueue;
    private final SkiersApi skiersApi;
    private static final int REQUESTS_PER_THREAD = 1000;
    private final AtomicInteger totalSuccessfulRequests;
    private final AtomicInteger totalFailedRequests;
    private static final String BASE_PATH = "http://35.164.116.43:8080/upic-server";

    public PostRequestThread(LiftRideEventQueue eventQueue, PerformanceQueue performanceQueue, AtomicInteger totalSuccessfulRequests, AtomicInteger totalFailedRequests) {
        this.eventQueue = eventQueue;
        this.performanceQueue = performanceQueue;
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(BASE_PATH);
        this.skiersApi = new SkiersApi(apiClient);
        this.totalSuccessfulRequests = totalSuccessfulRequests;
        this.totalFailedRequests = totalFailedRequests;
    }

    @Override
    public void run() {
        int requestCount = 0;
        while (requestCount < REQUESTS_PER_THREAD) {
            long startTime = System.currentTimeMillis();
            try {
                LiftRideData event = eventQueue.dequeue();
                if(isPoisonPill(event)){
                    eventQueue.enqueue(event);
                    break;
                }

                boolean success = false;
                ApiResponse<Void> apiResponse = null;
                for (int i = 0; i < 5; i++) {
                    apiResponse = sendPostRequest(event);
                    if (apiResponse != null && apiResponse.getStatusCode() == SUCCESS_CODE) {
                        totalSuccessfulRequests.incrementAndGet();
                        success = true;
                        break;
                    }
                }

                if (!success) {
                    totalFailedRequests.incrementAndGet();
                }
                long endTime = System.currentTimeMillis();
                long latency = endTime - startTime;
                performanceQueue.enqueue(new PerformanceData(startTime, "POST", latency, (apiResponse != null ? apiResponse.getStatusCode() : 0)));
                requestCount++;
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("thread finished");
    }

    private boolean isPoisonPill(LiftRideData event){
        return event.skierID() == -1;
    }

    private ApiResponse<Void> sendPostRequest(LiftRideData event) {
        try {
            LiftRide liftRide = new LiftRide();
            liftRide.setLiftID(event.liftID());
            liftRide.setTime(event.time());
            return skiersApi.writeNewLiftRideWithHttpInfo(liftRide, event.resortID(), "2024", "1", event.skierID());
        } catch (ApiException e) {
            return null;
        }
    }
}

