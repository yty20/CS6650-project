package org.example;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Driver {
    private final static String QUEUE_NAME = "my_queue";
    private static final int THREAD_COUNT = 200;
    private static final ConcurrentHashMap<Integer, Connection> connectionMap = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, Integer> skierRides = new ConcurrentHashMap<>();
    public static void main(String[] args) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("52.32.52.22");
        factory.setUsername("myuser");
        factory.setPassword("mypassword");

        ExecutorService pool = Executors.newFixedThreadPool(THREAD_COUNT);
        for(int i = 0; i < THREAD_COUNT; i++){
            final int threadId = i;
            Runnable worker = () -> {
                try {
                    Connection connection = factory.newConnection();
                    connectionMap.put(threadId, connection);
                    final Channel channel = connection.createChannel();
                    channel.queueDeclare(QUEUE_NAME, true, false, false, null);
                    channel.basicQos(1);

                    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                        processMessage(message);
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    };

                    channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {});
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
            pool.submit(worker);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            connectionMap.forEach((id, conn) -> {
                try {
                    if (conn != null && conn.isOpen()) {
                        conn.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            pool.shutdown();
        }));
    }

    private static void processMessage(String message){
        String[] parts = message.split("#");
        if(parts.length == 4){
            String skierID = parts[3];
            skierRides.merge(skierID, 1, Integer::sum);
        }
    }
}