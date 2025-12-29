package com.devs.feedback.service;

import com.devs.feedback.model.Feedback;
import com.devs.feedback.repository.FeedbackRepository;
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

//    public void generateAndStoreWeeklyReport() {
//        // consultar DynamoDB para última semana — calcular média, contagens por dia e por urgência
//        // montar CSV/JSON — salvar no S3 — opcional: publicar SNS para admins com link
//        String reportKey = "reports/report-" + java.time.LocalDate.now().toString() + ".json";
//        String content = "{ \"summary\": \"exemplo\" }"; // montar com reais cálculos
//        s3.putObject(b -> b.bucket(bucketName).key(reportKey).build(),
//                software.amazon.awssdk.core.sync.RequestBody.fromString(content));
//    }

    public void generateAndStoreWeeklyReport() {
        try {
            log.info("Iniciando geração de relatório semanal");

            // Buscar todos os feedbacks
            List<Feedback> allFeedbacks = repo.findAll();
            log.info("Total de feedbacks encontrados: {}", allFeedbacks.size());

            // Filtrar feedbacks da última semana
            Instant oneWeekAgo = Instant.now().minus(7, ChronoUnit.DAYS);
            List<Feedback> weeklyFeedbacks = allFeedbacks.stream()
                    .filter(f -> f.getDataEnvio() != null && f.getDataEnvio().isAfter(oneWeekAgo))
                    .collect(Collectors.toList());

            log.info("Feedbacks da última semana: {}", weeklyFeedbacks.size());

            // Calcular estatísticas
            Map<String, Object> report = buildReport(weeklyFeedbacks);

            // Converter para JSON
            String reportJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(report);
            log.info("Relatório gerado: {}", reportJson);

            // Salvar no S3
            String reportKey = "reports/weekly-report-" + LocalDate.now() + ".json";
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(reportKey)
                    .contentType("application/json")
                    .build();

            s3.putObject(putRequest, RequestBody.fromString(reportJson));

            log.info("Relatório salvo no S3: s3://{}/{}", bucketName, reportKey);

        } catch (Exception e) {
            log.error("Erro ao gerar relatório semanal", e);
            throw new RuntimeException("Erro ao gerar relatório semanal", e);
        }
    }

    private Map<String, Object> buildReport(List<Feedback> feedbacks) {
        Map<String, Object> report = new HashMap<>();

        // Período do relatório
        report.put("periodo", Map.of(
                "inicio", Instant.now().minus(7, ChronoUnit.DAYS).toString(),
                "fim", Instant.now().toString(),
                "geradoEm", Instant.now().toString()
        ));

        // Total de feedbacks
        report.put("totalFeedbacks", feedbacks.size());

        if (feedbacks.isEmpty()) {
            report.put("media", 0.0);
            report.put("urgentes", 0);
            report.put("naoUrgentes", 0);
            report.put("contagemPorDia", Map.of());
            report.put("contagemPorNota", Map.of());
            return report;
        }

        // Calcular média das notas
        double media = feedbacks.stream()
                .mapToInt(Feedback::getNota)
                .average()
                .orElse(0.0);
        report.put("media", String.format("%.2f", media));

        // Contagem por urgência
        long urgentes = feedbacks.stream().filter(Feedback::isUrgente).count();
        long naoUrgentes = feedbacks.size() - urgentes;
        report.put("urgentes", urgentes);
        report.put("naoUrgentes", naoUrgentes);

        // Contagem por dia
        Map<String, Long> contagemPorDia = feedbacks.stream()
                .filter(f -> f.getDataEnvio() != null)
                .collect(Collectors.groupingBy(
                        f -> LocalDate.ofInstant(f.getDataEnvio(), ZoneId.systemDefault()).toString(),
                        Collectors.counting()
                ));
        report.put("contagemPorDia", contagemPorDia);

        // Contagem por nota (0-10)
        Map<Integer, Long> contagemPorNota = feedbacks.stream()
                .collect(Collectors.groupingBy(
                        Feedback::getNota,
                        Collectors.counting()
                ));
        report.put("contagemPorNota", contagemPorNota);

        // Estatísticas adicionais
        int notaMinima = feedbacks.stream()
                .mapToInt(Feedback::getNota)
                .min()
                .orElse(0);
        int notaMaxima = feedbacks.stream()
                .mapToInt(Feedback::getNota)
                .max()
                .orElse(0);

        report.put("notaMinima", notaMinima);
        report.put("notaMaxima", notaMaxima);

        // Percentual de urgentes
        double percentualUrgentes = feedbacks.isEmpty() ? 0.0 : (urgentes * 100.0 / feedbacks.size());
        report.put("percentualUrgentes", String.format("%.2f%%", percentualUrgentes));

        return report;
    }
}
