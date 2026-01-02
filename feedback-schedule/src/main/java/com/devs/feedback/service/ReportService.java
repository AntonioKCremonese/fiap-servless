package com.devs.feedback.service;

import com.devs.feedback.model.Feedback;
import com.devs.feedback.repository.FeedbackRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportService {

    private final FeedbackRepository repo;
    private final S3Client s3;
    private final String bucketName = System.getenv("REPORTS_BUCKET");
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReportService(FeedbackRepository repo, S3Client s3) {
        this.repo = repo;
        this.s3 = s3;
    }

    public void generateAndStoreWeeklyReport() {

        try {
            log.info("Iniciando geração de relatório semanal");

            List<Feedback> weeklyFeedbacks = loadWeeklyFeedbacks();
            Map<String, Object> report = buildWeeklyReport(weeklyFeedbacks);

            storeReport(report);

        } catch (Exception ex) {
            log.error("Erro ao gerar relatório semanal", ex);
            throw new RuntimeException("Erro ao gerar relatório semanal", ex);
        }
    }

    private List<Feedback> loadWeeklyFeedbacks() {
        Instant oneWeekAgo = Instant.now().minus(7, ChronoUnit.DAYS);

        List<Feedback> all = repo.findAll();

        log.info("Total de feedbacks encontrados: {}", all.size());

        List<Feedback> weekly = all.stream()
                .filter(f -> f.getDataEnvio() != null)
                .filter(f -> f.getDataEnvio().isAfter(oneWeekAgo))
                .toList();

        log.info("Feedbacks da última semana: {}", weekly.size());
        return weekly;
    }

    private Map<String, Object> buildWeeklyReport(List<Feedback> feedbacks) {
        Map<String, Object> report = new HashMap<>();

        report.put("periodo", buildPeriod());
        report.put("totalFeedbacks", feedbacks.size());

        if (feedbacks.isEmpty()) {
            report.putAll(buildEmptyStats());
            return report;
        }

        report.put("media", calculateAverage(feedbacks));
        report.putAll(calculateUrgencyStats(feedbacks));
        report.put("contagemPorDia", countByDay(feedbacks));
        report.put("contagemPorNota", countByRating(feedbacks));
        report.putAll(calculateMinMax(feedbacks));

        return report;
    }

    private Map<String, String> buildPeriod() {
        Instant now = Instant.now();
        return Map.of(
                "inicio", now.minus(7, ChronoUnit.DAYS).toString(),
                "fim", now.toString(),
                "geradoEm", now.toString()
        );
    }

    private Map<String, Object> buildEmptyStats() {
        return Map.of(
                "media", 0.0,
                "urgentes", 0,
                "naoUrgentes", 0,
                "contagemPorDia", Map.of(),
                "contagemPorNota", Map.of(),
                "notaMinima", 0,
                "notaMaxima", 0,
                "percentualUrgentes", "0.00%"
        );
    }

    private String calculateAverage(List<Feedback> feedbacks) {
        double media = feedbacks.stream()
                .mapToInt(Feedback::getNota)
                .average()
                .orElse(0.0);

        return String.format("%.2f", media);
    }

    private Map<String, Long> countByDay(List<Feedback> feedbacks) {
        return feedbacks.stream()
                .filter(f -> f.getDataEnvio() != null)
                .collect(Collectors.groupingBy(
                        f -> LocalDate.ofInstant(
                                f.getDataEnvio(),
                                ZoneId.systemDefault()
                        ).toString(),
                        Collectors.counting()
                ));
    }

    private Map<String, Object> calculateUrgencyStats(List<Feedback> feedbacks) {
        long urgentes = feedbacks.stream().filter(Feedback::isUrgente).count();
        long total = feedbacks.size();

        double percentual = (urgentes * 100.0) / total;

        return Map.of(
                "urgentes", urgentes,
                "naoUrgentes", total - urgentes,
                "percentualUrgentes", String.format("%.2f%%", percentual)
        );
    }

    private Map<Integer, Long> countByRating(List<Feedback> feedbacks) {
        return feedbacks.stream()
                .collect(Collectors.groupingBy(
                        Feedback::getNota,
                        Collectors.counting()
                ));
    }

    private Map<String, Integer> calculateMinMax(List<Feedback> feedbacks) {
        IntSummaryStatistics stats = feedbacks.stream()
                .mapToInt(Feedback::getNota)
                .summaryStatistics();

        return Map.of(
                "notaMinima", stats.getMin(),
                "notaMaxima", stats.getMax()
        );
    }

    private void storeReport(Map<String, Object> report) throws JsonProcessingException {
        String json = objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(report);

        String key = "reports/weekly-report-" + LocalDate.now() + ".json";

        s3.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType("application/json")
                        .build(),
                RequestBody.fromString(json)
        );

        log.info("Relatório salvo no S3: s3://{}/{}", bucketName, key);
    }
}
