package org.CS6650;

import java.util.concurrent.ConcurrentLinkedQueue;

public class PerformanceQueue {
    private final ConcurrentLinkedQueue<PerformanceData> queue;

    public PerformanceQueue(){
        this.queue = new ConcurrentLinkedQueue<>();
    }

    public void enqueue(PerformanceData data) throws InterruptedException {
        queue.offer(data);
    }

    public PerformanceData dequeue() throws InterruptedException {
        return queue.poll();
    }

    public boolean isEmpty(){
        return queue.isEmpty();
    }
}
