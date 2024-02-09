package org.CS6650;

import java.util.Random;

public class EventGenerator implements Runnable{
    private static final int SKIER_ID_BOUND = 100000;
    private static final int RESORT_ID_BOUND = 10;
    private static final int LIFT_ID_BOUND = 40;
    private static final int TIME_BOUND = 360;
    private static final int TOTAL_EVENT = 200000;
    private final LiftRideEventQueue queue;
    private final Random random;

    public EventGenerator(LiftRideEventQueue queue){
        this.queue = queue;
        this.random = new Random();
    }

    public LiftRideData generateLiftRideData(){
        int skierID = random.nextInt(SKIER_ID_BOUND) + 1;
        int resortID = random.nextInt(RESORT_ID_BOUND) + 1;
        int liftID = random.nextInt(LIFT_ID_BOUND) + 1;
        int time = random.nextInt(TIME_BOUND) + 1;
        return new LiftRideData(skierID, resortID, liftID, time);
    }

    @Override
    public void run() {
        try {
            for(int i = 0; i < TOTAL_EVENT; i++){
                queue.enqueue(generateLiftRideData());
            }
            queue.enqueue(new LiftRideData(-1, -1, -1, -1));
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        System.out.println("event finish generated");
    }
}
