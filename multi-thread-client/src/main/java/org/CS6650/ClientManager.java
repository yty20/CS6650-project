package org.CS6650;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ClientManager {
    private static final int THREAD_COUNT = 24;
    private static final int NEEDED_THREAD = 200;
    private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
    private final LiftRideEventQueue eventQueue;

    public ClientManager(LiftRideEventQueue eventQueue) {
        this.eventQueue = eventQueue;
    }

    public void runClientManager() {
        long startTime = System.currentTimeMillis();
        List<Future<PostRequestResult>> futures = new ArrayList<>();

        // 提交所有线程到线程池执行
        for (int i = 0; i < NEEDED_THREAD; i++) {
            PostRequestThread task = new PostRequestThread(eventQueue);
            futures.add(executor.submit(task));
        }

        // 等待所有线程执行完成并处理结果
        int totalSuccess = 0;
        int totalFailures = 0;
        for (Future<PostRequestResult> future : futures) {
            try {
                PostRequestResult result = future.get(); // 阻塞直到线程完成
                totalSuccess += result.successfulRequests();
                totalFailures += result.failedRequests();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 关闭 ExecutorService
        executor.shutdown();

        long endTime = System.currentTimeMillis();
        double totalTime = (endTime - startTime) / 1000.0;
        double throughput = (totalSuccess + totalFailures) / totalTime;

        System.out.println("Total Successful Requests: " + totalSuccess);
        System.out.println("Total Failed Requests: " + totalFailures);
        System.out.println("Total Time: " + totalTime + " s");
        System.out.println("Total throughput:" + throughput + " /s");
    }
}


