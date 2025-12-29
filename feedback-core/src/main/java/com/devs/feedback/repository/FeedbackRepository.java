package com.devs.feedback.repository;

import com.devs.feedback.model.Feedback;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

@Repository
public class FeedbackRepository {

    private final DynamoDbClient dynamo;
    private final String tableName = System.getenv("DYNAMODB_TABLE_NAME");
    private final ObjectMapper mapper = new ObjectMapper();

    public FeedbackRepository(DynamoDbClient dynamo) { this.dynamo = dynamo; }

    public void save(Feedback f) {
        try {
            String pk = java.util.UUID.randomUUID().toString();
            f.setId(pk);
            String json = mapper.writeValueAsString(f);
            PutItemRequest req = PutItemRequest.builder()
                    .tableName(tableName)
                    .item(java.util.Map.of("id", AttributeValue.builder().s(pk).build(),
                            "payload", AttributeValue.builder().s(json).build(),
                            "nota", AttributeValue.builder().n(String.valueOf(f.getNota())).build(),
                            "dataEnvio", AttributeValue.builder().s(f.getDataEnvio().toString()).build(),
                            "urgente", AttributeValue.builder().bool(f.isUrgente()).build()))
                    .build();
            dynamo.putItem(req);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
