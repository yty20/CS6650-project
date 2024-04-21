package org.example;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Driver {
    private final static String QUEUE_NAME = "my_queue";
    private static final int THREAD_COUNT = 200;
    private static final ConcurrentHashMap<Integer, Connection> connectionMap = new ConcurrentHashMap<>();
    private static DynamoDBController dynamoDBController;

    public static void main(String[] args) {
        dynamoDBController = new DynamoDBController();  // 初始化 DynamoDB Controller
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("52.32.52.22");
        factory.setUsername("myuser");
        factory.setPassword("mypassword");

        ExecutorService pool = Executors.newFixedThreadPool(THREAD_COUNT);
        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadId = i;
            Runnable worker = () -> {
                try {
                    Connection connection = factory.newConnection();
                    connectionMap.put(threadId, connection);
                    Channel channel = connection.createChannel();
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

    private static void processMessage(String message) {
        String[] parts = message.split("#");
        if (parts.length == 5) {
            String resortID = parts[0];
            String seasonID = parts[1];
            String dayID = parts[2];
            String skierID = parts[3];
            int liftID = Integer.parseInt(parts[4]);
            String date = seasonID + "-" + dayID;
            Map<String, Integer> lifts = new HashMap<>();
            lifts.put(parts[4], 1);
            dynamoDBController.upsertSkierActivity(skierID, date, resortID, lifts, liftID);
        }
    }
}
