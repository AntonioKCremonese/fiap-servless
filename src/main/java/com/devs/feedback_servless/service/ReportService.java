package com.devs.feedback_servless.service;

import com.devs.feedback_servless.repository.FeedbackRepository;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;

@Service
public class ReportService {

    private final FeedbackRepository repo;
    private final S3Client s3;
    private final String bucketName = System.getenv("REPORTS_BUCKET");

    public ReportService(FeedbackRepository repo, S3Client s3) {
        this.repo = repo; this.s3 = s3;
    }

    public void generateAndStoreWeeklyReport() {
        // consultar DynamoDB para última semana — calcular média, contagens por dia e por urgência
        // montar CSV/JSON — salvar no S3 — opcional: publicar SNS para admins com link
        String reportKey = "reports/report-" + java.time.LocalDate.now().toString() + ".json";
        String content = "{ \"summary\": \"exemplo\" }"; // montar com reais cálculos
        s3.putObject(b -> b.bucket(bucketName).key(reportKey).build(),
                software.amazon.awssdk.core.sync.RequestBody.fromString(content));
    }
}
