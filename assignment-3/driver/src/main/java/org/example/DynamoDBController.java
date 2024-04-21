package org.example;

import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.HashMap;
import java.util.Map;

public class DynamoDBController {
    private final DynamoDbClient client;

    public DynamoDBController() {
        AwsSessionCredentials awsSessionCredentials = AwsSessionCredentials.create(
                "remove",
                "for",
                "secrets"
        );
        client = DynamoDbClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsSessionCredentials))
                .region(Region.US_WEST_2)
                .build();
    }

    public void insertSkierActivity(SkierActivity activity) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("skierID", AttributeValue.builder().s(activity.getSkierID()).build());
        item.put("date", AttributeValue.builder().s(activity.getDate()).build());
        item.put("resortID", AttributeValue.builder().s(activity.getResortID()).build());
        item.put("verticalTotals", AttributeValue.builder().n(Integer.toString(activity.getVerticalTotals())).build());

        Map<String, AttributeValue> liftsMap = new HashMap<>();
        activity.getLifts().forEach((liftID, count) ->
                liftsMap.put(liftID, AttributeValue.builder().n(Integer.toString(count)).build()));
        item.put("lifts", AttributeValue.builder().m(liftsMap).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName("skierActivities")
                .item(item)
                .build();

        try {
            client.putItem(request);
            System.out.println("Insert successful!");
        } catch (DynamoDbException e) {
            System.err.println("Insert failed!");
            e.printStackTrace();
        }
    }
    public void updateSkierActivity(String skierID, String date, String resortID, int liftID) {
        // 构建更新请求
        try {
            UpdateItemRequest updateItemRequest = UpdateItemRequest.builder()
                    .tableName("skierActivities")
                    .key(Map.of(
                            "skierID", AttributeValue.builder().s(skierID).build(),
                            "date", AttributeValue.builder().s(date).build()
                    ))
                    .updateExpression("SET resortID = :resortID ADD lifts.#liftID :incr, verticalTotals :vTotals")
                    .expressionAttributeValues(Map.of(
                            ":resortID", AttributeValue.builder().s(resortID).build(),
                            ":incr", AttributeValue.builder().n("1").build(),
                            ":vTotals", AttributeValue.builder().n(String.valueOf(liftID * 10)).build()
                    ))
                    .expressionAttributeNames(Map.of("#liftID", String.valueOf(liftID)))
                    .returnValues(ReturnValue.ALL_NEW)
                    .build();

            // 执行更新操作
            UpdateItemResponse response = client.updateItem(updateItemRequest);
            System.out.println("Update successful! New item: " + response.attributes());
        } catch (DynamoDbException e) {
            System.err.println("Update failed!");
            e.printStackTrace();
        }
    }
    public void upsertSkierActivity(String skierID, String date, String resortID, Map<String, Integer> lifts, int liftID) {
        GetItemRequest getItemRequest = GetItemRequest.builder()
                .tableName("skierActivities")
                .key(Map.of(
                        "skierID", AttributeValue.builder().s(skierID).build(),
                        "date", AttributeValue.builder().s(date).build()
                ))
                .build();

        try {
            GetItemResponse response = client.getItem(getItemRequest);
            if (response.hasItem()) {
                updateSkierActivity(skierID, date, resortID, liftID);
            } else {
                // 假设 SkierActivity 构造器按顺序接收：skierID, date, resortID, lifts, verticalTotals
                SkierActivity activity = new SkierActivity(skierID, date, resortID, lifts, liftID * 10);
                insertSkierActivity(activity);
            }
        } catch (DynamoDbException e) {
            System.err.println("Access failed!");
            e.printStackTrace();
        }
    }
}
