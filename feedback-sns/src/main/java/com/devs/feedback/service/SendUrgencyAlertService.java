package com.devs.feedback.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Service
public class SendUrgencyAlertService {

    private final SesClient sesClient;
    private final String adminEmail;

    public SendUrgencyAlertService(SesClient sesClient) {
        this.sesClient = sesClient;
        this.adminEmail = System.getenv("SES_SOURCE_EMAIL");
    }

    public void send(String message) {
        Destination dest = Destination.builder()
                .toAddresses(adminEmail)
                .build();

        Content subject = Content.builder()
                .data("Alerta: feedback urgente")
                .build();

        Content body = Content.builder()
                .data(message)
                .build();

        Body b = Body.builder().text(body).build();

        Message msg = Message.builder()
                .subject(subject)
                .body(b)
                .build();

        SendEmailRequest req = SendEmailRequest.builder()
                .destination(dest)
                .message(msg)
                .source(adminEmail)
                .build();

        sesClient.sendEmail(req);
    }
}
