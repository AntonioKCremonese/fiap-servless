package com.devs.feedback.repository;

import com.devs.feedback.model.Feedback;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public List<Feedback> findAll() {
        try {
            List<Feedback> feedbacks = new ArrayList<>();
            ScanRequest scanRequest = ScanRequest.builder()
                    .tableName(tableName)
                    .build();

            ScanResponse response = dynamo.scan(scanRequest);

            for (Map<String, AttributeValue> item : response.items()) {
                Feedback feedback = new Feedback();
                feedback.setId(item.get("id").s());

                if (item.containsKey("nota")) {
                    feedback.setNota(Integer.parseInt(item.get("nota").n()));
                }

                if (item.containsKey("dataEnvio")) {
                    feedback.setDataEnvio(Instant.parse(item.get("dataEnvio").s()));
                }

                if (item.containsKey("urgente")) {
                    feedback.setUrgente(item.get("urgente").bool());
                }

                if (item.containsKey("payload")) {
                    try {
                        Feedback fullFeedback = mapper.readValue(item.get("payload").s(), Feedback.class);
                        feedback.setDescricao(fullFeedback.getDescricao());
                    } catch (Exception e) {
                        // Se falhar, continua sem a descrição
                    }
                }

                feedbacks.add(feedback);
            }

            return feedbacks;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar feedbacks do DynamoDB", e);
        }
    }
}
