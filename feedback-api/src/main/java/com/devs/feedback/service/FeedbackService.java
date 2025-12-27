package com.devs.feedback.service;

import com.devs.feedback.model.Feedback;
import com.devs.feedback.repository.FeedbackRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.time.Instant;

@Service
@Slf4j
public class FeedbackService {

    private final FeedbackRepository repo;
    private final SnsClient snsClient;
    private final String snsTopicArn = System.getenv("URGENCY_SNS_ARN");

    public FeedbackService(FeedbackRepository repo, SnsClient snsClient) {
        this.repo = repo;
        this.snsClient = snsClient;
    }

    public void process(Feedback feedback) {
        if (feedback.getNota() < 0 || feedback.getNota() > 10) {
            throw new IllegalArgumentException("Nota fora do intervalo 0-10");
        }

        feedback.setDataEnvio(Instant.now());
        feedback.setUrgente(feedback.getNota() <= 3);

        repo.save(feedback);

        if (feedback.isUrgente()) {
            log.info("Enviando alerta SNS para feedback urgente: {}", feedback);
            snsClient.publish(PublishRequest.builder()
                    .topicArn(snsTopicArn)
                    .subject("Alerta de feedback urgente")
                    .message(buildMessage(feedback))
                    .build());
        }
    }

    private String buildMessage(Feedback f) {
        return String.format(
                "URGENTE: %s (nota=%d) data=%s",
                f.getDescricao(),
                f.getNota(),
                f.getDataEnvio()
        );
    }
}
