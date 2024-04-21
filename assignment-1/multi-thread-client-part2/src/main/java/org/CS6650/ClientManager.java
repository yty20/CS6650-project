package org.CS6650;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientManager {
    private static final int THREAD_COUNT = 32;
    private static final int NEEDED_THREAD = 200;
    private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
    private final LiftRideEventQueue eventQueue;
    private final PerformanceMonitor performanceMonitor;
    private final PerformanceQueue performanceQueue;

    private final AtomicInteger totalSuccessfulRequests = new AtomicInteger(0);
    private final AtomicInteger totalFailedRequests = new AtomicInteger(0);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final List<Integer> requestsPlot = new ArrayList<>();

    public ClientManager() {
        LiftRideEventQueue eventQueue = new LiftRideEventQueue();
        EventGenerator eventGenerator = new EventGenerator(eventQueue);

        Thread generatorThread = new Thread(eventGenerator);
        generatorThread.start();

        this.eventQueue = eventQueue;

        PerformanceQueue performanceQueue = new PerformanceQueue();
        String csvFilePath = "performance_data.csv";
        PerformanceMonitor monitor = new PerformanceMonitor(performanceQueue, csvFilePath);
        Thread monitorThread = new Thread(monitor);
        monitorThread.start();

        this.performanceMonitor = monitor;
        this.performanceQueue = performanceQueue;

        scheduler.scheduleAtFixedRate(() -> {
            int totalRequests = totalSuccessfulRequests.get() + totalFailedRequests.get();
            System.out.println(totalRequests);
            requestsPlot.add(totalRequests);
        }, 0, 30, TimeUnit.SECONDS);
    }

    public void runClientManager() {
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < NEEDED_THREAD; i++) {
            executor.execute(new PostRequestThread(eventQueue, performanceQueue, totalSuccessfulRequests, totalFailedRequests));
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
        scheduler.shutdown();

        long endTime = System.currentTimeMillis();
        double totalTime = (endTime - startTime) / 1000.0;
        double throughput = (totalSuccessfulRequests.get() + totalFailedRequests.get()) / totalTime;

        System.out.println("Total Successful Requests: " + totalSuccessfulRequests.get());
        System.out.println("Total Failed Requests: " + totalFailedRequests.get());
        System.out.println("Total Time: " + totalTime + " s");
        System.out.println("Total throughput:" + throughput + " /s");
        System.out.println(requestsPlot);

        performanceMonitor.shutdown();
    }
}


