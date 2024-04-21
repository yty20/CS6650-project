package org.CS6650;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LiftRideEventQueue {
    private final BlockingQueue<LiftRideData> queue;
    private static final int CAPACITY = 3200;

    public LiftRideEventQueue(){
        this.queue = new LinkedBlockingQueue<>(CAPACITY);
    }

    public void enqueue(LiftRideData event) throws InterruptedException {
        queue.put(event);
    }

    public LiftRideData dequeue() throws InterruptedException {
        return queue.take();
    }

    public int getSize() {
        return queue.size();
    }
}
