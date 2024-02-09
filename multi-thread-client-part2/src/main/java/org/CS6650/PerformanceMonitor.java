package org.CS6650;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class PerformanceMonitor implements Runnable {
    private final PerformanceQueue queue;
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);
    private final String csvFile;
    private long startTime = Long.MAX_VALUE;
    private long endTime = 0;

    public PerformanceMonitor(PerformanceQueue queue, String csvFile) {
        this.queue = queue;
        this.csvFile = csvFile;
    }

    @Override
    public void run() {
        List<Long> latencies = new ArrayList<>();
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write("StartTime,RequestType,Latency,ResponseCode\n");

            while (!isShutdown.get() || !queue.isEmpty()) {
                PerformanceData data = queue.dequeue();
                if (data != null) {
                    latencies.add(data.latency());
                    String record = String.format("%d,%s,%d,%d\n", data.startTime(), data.requestType(), data.latency(), data.responseCode());
                    writer.write(record);
                    startTime = Math.min(startTime, data.startTime());
                    endTime = Math.max(endTime, data.startTime() + data.latency());
                } else {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        calculateAndPrintStatistics(latencies);
        System.out.println("PerformanceMonitor: Data processing completed.");
    }

    private void calculateAndPrintStatistics(List<Long> latencies) {
        if (latencies.isEmpty()) {
            System.out.println("No data to calculate statistics.");
            return;
        }
        Collections.sort(latencies);
        long totalLatency = latencies.stream().mapToLong(Long::longValue).sum();
        long minLatency = latencies.get(0);
        long maxLatency = latencies.get(latencies.size() - 1);
        long meanLatency = totalLatency / latencies.size();
        long medianLatency = latencies.get(latencies.size() / 2);
        long p99Latency = latencies.get((int) (latencies.size() * 0.99) - 1);
        double throughput = latencies.size() / ((endTime - startTime) / 1000.0);

        System.out.println("Mean Response Time: " + meanLatency + " ms");
        System.out.println("Median Response Time: " + medianLatency + " ms");
        System.out.println("Throughput: " + throughput + " requests/second");
        System.out.println("P99 Response Time: " + p99Latency + " ms");
        System.out.println("Min Response Time: " + minLatency + " ms");
        System.out.println("Max Response Time: " + maxLatency + " ms");
    }
    public void shutdown() {
        isShutdown.set(true);
    }
}
