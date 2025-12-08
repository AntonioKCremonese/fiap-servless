package com.devs.feedback_servless.functions;

import com.devs.feedback_servless.model.Feedback;
import com.devs.feedback_servless.service.FeedbackRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.util.function.Function;

@Component
public class ReceiveFeedbackFunction {

    private final FeedbackRepository repo;
    private final SnsClient snsClient;
    private final String snsTopicArn; // inject via env var

    public ReceiveFeedbackFunction(FeedbackRepository repo, SnsClient snsClient) {
        this.repo = repo;
        this.snsClient = snsClient;
        this.snsTopicArn = System.getenv("URGENCY_SNS_ARN");
    }

    @Bean
    public Function<Feedback, String> receiveFeedback() {
        return feedback -> {
            // validações básicas
            if (feedback.getNota() < 0 || feedback.getNota() > 10) {
                throw new IllegalArgumentException("Nota fora do intervalo 0-10");
            }
            feedback.setDataEnvio(java.time.Instant.now());
            feedback.setUrgente(feedback.getNota() <= 3);
            // persiste
            repo.save(feedback);
            // se urgente, publicar SNS
            if (feedback.isUrgente()) {
                String message = String.format("URGENTE: %s (nota=%d) data=%s",
                        feedback.getDescricao(), feedback.getNota(), feedback.getDataEnvio());
                PublishRequest pr = PublishRequest.builder()
                        .topicArn(snsTopicArn)
                        .subject("Alerta de feedback urgente")
                        .message(message)
                        .build();
                snsClient.publish(pr);
            }
            return "OK";
        };
    }
}
