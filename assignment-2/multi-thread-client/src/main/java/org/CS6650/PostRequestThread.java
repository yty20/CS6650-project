package org.CS6650;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class PostRequestThread implements Callable<PostRequestResult> {
    private static final int SUCCESS_CODE = 201;
    private final LiftRideEventQueue eventQueue;
    private final SkiersApi skiersApi;

    private final int requestNum;
    private static final String BASE_PATH = "http://upic-server-lb-1247285247.us-west-2.elb.amazonaws.com:8080/upic-server";
//    private static final String BASE_PATH = "http://localhost:8080/upic-server";

    public PostRequestThread(LiftRideEventQueue eventQueue, int requestNum) {
        this.eventQueue = eventQueue;
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(BASE_PATH);
        this.skiersApi = new SkiersApi(apiClient);
        this.requestNum = requestNum;
    }

    @Override
    public PostRequestResult call() throws Exception {
        int requestCount = 0;
        int successfulRequests = 0;
        int failedRequests = 0;
        List<PerformanceData> performanceDataList = new ArrayList<>();
        while(requestCount < requestNum){
            if(requestCount%50==0){
                System.out.println(Thread.currentThread().getName() + " " + requestCount);
            }
            long startTime = System.currentTimeMillis();
            try {
                LiftRideData event = eventQueue.dequeue();
                if(isPoisonPill(event)){
                    eventQueue.enqueue(event);
                    break;
                }

                boolean success = false;
                ApiResponse<Void> apiResponse = null;
                for(int i = 0; i < 5; i++){
                    apiResponse = sendPostRequest(event);
                    if (apiResponse != null && apiResponse.getStatusCode() == SUCCESS_CODE) {
                        successfulRequests++;
                        success = true;
                        break;
                    }
                }

                if (!success) {
                    failedRequests++;
                    System.out.println(Thread.currentThread().getName() + " failed request at: " + requestCount);
                }

                long endTime = System.currentTimeMillis();
                long latency = endTime - startTime;
                performanceDataList.add(new PerformanceData(startTime, "POST", latency, (apiResponse != null ? apiResponse.getStatusCode() : 0)));
                requestCount++;
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("thread finished");
        return new PostRequestResult(successfulRequests, failedRequests, performanceDataList);
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

