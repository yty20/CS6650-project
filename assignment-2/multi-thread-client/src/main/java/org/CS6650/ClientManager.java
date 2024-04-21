package org.CS6650;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientManager {
    private static final int THREAD_COUNT = 200;
    private static final int TOTAL_REQUEST = 200000;
    private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
    private final LiftRideEventQueue eventQueue;

    public ClientManager() {
        LiftRideEventQueue eventQueue = new LiftRideEventQueue();
        EventGenerator eventGenerator = new EventGenerator(eventQueue, TOTAL_REQUEST);

        Thread generatorThread = new Thread(eventGenerator);
        generatorThread.start();

        this.eventQueue = eventQueue;

//        PerformanceQueue performanceQueue = new PerformanceQueue();
//        String csvFilePath = "performance_data.csv";
//        PerformanceMonitor monitor = new PerformanceMonitor(performanceQueue, csvFilePath);
//        Thread monitorThread = new Thread(monitor);
//        monitorThread.start();
//
//        this.performanceMonitor = monitor;
//        this.performanceQueue = performanceQueue;

//        scheduler.scheduleAtFixedRate(() -> {
//            int totalRequests = totalSuccessfulRequests.get() + totalFailedRequests.get();
//            System.out.println(totalRequests);
//            requestsPlot.add(totalRequests);
//        }, 0, 30, TimeUnit.SECONDS);
    }

    public void runClientManager() {
        long startTime = System.currentTimeMillis();

        List<Future<PostRequestResult>> futures = new ArrayList<>();

        for (int i = 0; i < THREAD_COUNT; i++) {
            PostRequestThread task =new PostRequestThread(eventQueue, (TOTAL_REQUEST / THREAD_COUNT) + 1);
            futures.add(executor.submit(task));
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        long endTime = System.currentTimeMillis();

        int totalSuccess = 0;
        int totalFailures = 0;
        List<PerformanceData> mergedData = new ArrayList<>();
        for (Future<PostRequestResult> future : futures) {
            try {
                PostRequestResult result = future.get();
                totalSuccess += result.successfulRequests();
                totalFailures += result.failedRequests();
                mergedData.addAll(result.performanceDataList());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        printData(totalSuccess, totalFailures, mergedData, startTime, endTime);
    }

    private void printData(int totalSuccess, int totalFailures, List<PerformanceData> mergedData, long startTime, long endTime) {
        double totalTime = (endTime - startTime) / 1000.0;
        double throughput = (totalSuccess + totalFailures) / totalTime;
        List<Long> latencies = new ArrayList<>();

        long[] throughputPerSecond = new long[(int) totalTime + 1];

        for (PerformanceData data : mergedData) {
            int second = (int) ((data.startTime() - startTime) / 1000);
            if (second < throughputPerSecond.length) {
                throughputPerSecond[second]++;
            }
            latencies.add(data.latency());
        }
        Collections.sort(latencies);

        double mean = latencies.stream().mapToDouble(Long::doubleValue).average().orElse(0.0);
        long median = latencies.size() % 2 == 0 ?
                (latencies.get(latencies.size() / 2 - 1) + latencies.get(latencies.size() / 2)) / 2
                : latencies.get(latencies.size() / 2);
        long min = latencies.get(0);
        long max = latencies.get(latencies.size() - 1);
        long p99 = latencies.get((int) (latencies.size() * 0.99) - 1);

        System.out.println("\nThroughput (requests/s):");
        System.out.println("Second | Throughput");
        for (int i = 0; i < throughputPerSecond.length; i++) {
            System.out.println(i + "s | " + throughputPerSecond[i]);
        }

        System.out.println("Total Successful Requests: " + totalSuccess);
        System.out.println("Total Failed Requests: " + totalFailures);
        System.out.println("Total Time: " + totalTime + " s");
        System.out.println("Total throughput: " + throughput + " requests/s");

        System.out.println("Latency Statistics:");
        System.out.println("Mean Latency: " + mean + " ms");
        System.out.println("Median Latency: " + median + " ms");
        System.out.println("Min Latency: " + min + " ms");
        System.out.println("Max Latency: " + max + " ms");
        System.out.println("99th Percentile Latency: " + p99 + " ms");
    }

}


