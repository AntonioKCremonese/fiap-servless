package com.devs.feedback_servless.functions;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import java.util.function.Consumer;

@Component
public class SendUrgencyAlertFunction {

    private final SesClient sesClient;
    private final String adminEmail;

    public SendUrgencyAlertFunction(SesClient sesClient){
        this.sesClient = sesClient;
        this.adminEmail = System.getenv("ADMIN_EMAIL");
    }

    @Bean
    public Consumer<String> sendUrgencyAlert() {
        return message -> {
            // envia email simples com SES (conta deve estar verificada)
            Destination dest = Destination.builder().toAddresses(adminEmail).build();
            Content subject = Content.builder().data("Alerta: feedback urgente").build();
            Content body = Content.builder().data(message).build();
            Body b = Body.builder().text(body).build();
            Message msg = Message.builder().subject(subject).body(b).build();
            SendEmailRequest req = SendEmailRequest.builder()
                    .destination(dest)
                    .message(msg)
                    .source(adminEmail) // must be verified
                    .build();
            sesClient.sendEmail(req);
        };
    }

}
