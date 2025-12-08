package com.devs.feedback_servless.functions;

import com.devs.feedback_servless.service.ReportService;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class GenerateWeeklyReportFunction {

    private final ReportService reportService;

    public GenerateWeeklyReportFunction(ReportService reportService) {
        this.reportService = reportService;
    }

    @Bean
    public Supplier<String> generateWeeklyReport() {
        return () -> {
            reportService.generateAndStoreWeeklyReport();
            return "REPORT_OK";
        };
    }
}
