package com.devs.feedback_servless.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.devs.feedback_servless.config.AwsClientConfig;
import com.devs.feedback_servless.service.ReportService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class WeeklyReportHandler implements RequestHandler<ScheduledEvent, String> {

    private static AnnotationConfigApplicationContext context;
    private static ReportService reportService;

    @Override
    public String handleRequest(ScheduledEvent event, Context ctx) {

        if (context == null) {
            context = new AnnotationConfigApplicationContext(AwsClientConfig.class);
            reportService = context.getBean(ReportService.class);
        }

        reportService.generateAndStoreWeeklyReport();
        return "REPORT_OK";
    }
}
