package com.devs.feedback_servless.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.devs.feedback_servless.service.ReportService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class WeeklyReportHandler implements RequestHandler<ScheduledEvent, String> {

    private static final AnnotationConfigApplicationContext context;
    private static final ReportService service;

    static {
        context = new AnnotationConfigApplicationContext("com.devs.feedback_servless");
        service = context.getBean(ReportService.class);
    }

    @Override
    public String handleRequest(ScheduledEvent event, Context ctx) {
        service.generateAndStoreWeeklyReport();
        return "REPORT_OK";
    }
}
