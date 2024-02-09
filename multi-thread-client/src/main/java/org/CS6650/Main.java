package org.CS6650;

public class Main {
    public static void main(String[] args) {
        LiftRideEventQueue eventQueue = new LiftRideEventQueue();
        EventGenerator eventGenerator = new EventGenerator(eventQueue);

        Thread generatorThread = new Thread(eventGenerator);
        generatorThread.start();

        ClientManager clientManager = new ClientManager(eventQueue);
        clientManager.runClientManager();
    }
}