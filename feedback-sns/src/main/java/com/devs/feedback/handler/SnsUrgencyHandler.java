package com.devs.feedback.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.devs.feedback.service.SendUrgencyAlertService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SnsUrgencyHandler implements RequestHandler<SNSEvent, Void> {

    private static final AnnotationConfigApplicationContext context;
    private static final SendUrgencyAlertService service;

    static {
        context = new AnnotationConfigApplicationContext("com.devs.feedback");
        service = context.getBean(SendUrgencyAlertService.class);
    }

    @Override
    public Void handleRequest(SNSEvent event, Context ctx) {

        event.getRecords().forEach(record -> {
            String message = record.getSNS().getMessage();
            service.send(message);
        });

        return null;
    }
}
