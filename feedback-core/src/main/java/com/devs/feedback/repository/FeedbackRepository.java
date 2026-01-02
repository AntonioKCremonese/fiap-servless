package com.devs.feedback.repository;

import com.devs.feedback.model.Feedback;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Optional;

@Repository
@Slf4j
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

                feedback.setId(getString(item, "id"));
                feedback.setNota(getInt(item, "nota"));
                feedback.setDataEnvio(getInstant(item, "dataEnvio"));
                feedback.setUrgente(getBoolean(item, "urgente"));

                getPayload(item).ifPresent(feedback::setDescricao);

                feedbacks.add(feedback);
            }

            return feedbacks;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar feedbacks do DynamoDB", e);
        }
    }

    private String getString(Map<String, AttributeValue> item, String key) {
        return item.containsKey(key) ? item.get(key).s() : null;
    }

    private Integer getInt(Map<String, AttributeValue> item, String key) {
        return item.containsKey(key) ? Integer.parseInt(item.get(key).n()) : null;
    }

    private Boolean getBoolean(Map<String, AttributeValue> item, String key) {
        return item.containsKey(key) ? item.get(key).bool() : null;
    }

    private Instant getInstant(Map<String, AttributeValue> item, String key) {
        return item.containsKey(key)
                ? Instant.parse(item.get(key).s())
                : null;
    }

    private Optional<String> getPayload(Map<String, AttributeValue> item) {
        if (!item.containsKey("payload")) {
            return Optional.empty();
        }

        try {
            Feedback full = mapper.readValue(item.get("payload").s(), Feedback.class);
            return Optional.ofNullable(full.getDescricao());
        } catch (Exception e) {
            log.warn("Erro ao desserializar payload do feedback", e);
            return Optional.empty();
        }
    }

}
