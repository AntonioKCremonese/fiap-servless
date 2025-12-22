package com.devs.feedback_servless.config;

import com.devs.feedback_servless.repository.FeedbackRepository;
import com.devs.feedback_servless.service.ReportService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
@ComponentScan(basePackages = "com.devs.feedback_servless")
public class AwsClientConfig {

    @Bean
    public DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
                .region(Region.of(System.getenv("AWS_REGION")))
                .build();
    }

    @Bean
    public SnsClient snsClient() {
        return SnsClient.builder()
                .region(Region.of(System.getenv("AWS_REGION")))
                .build();
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(System.getenv("AWS_REGION")))
                .build();
    }

    @Bean
    public SesClient sesClient() {
        return SesClient.builder()
                .region(Region.of(System.getenv("AWS_REGION")))
                .build();
    }

    @Bean
    public ReportService reportService(
            FeedbackRepository repository,
            S3Client s3Client
    ) {
        return new ReportService(repository, s3Client);
    }
}

