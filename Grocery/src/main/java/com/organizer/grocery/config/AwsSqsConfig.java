package com.organizer.grocery.config;

import com.amazonaws.services.sns.AmazonSNS;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;

@Configuration
public class AwsSqsConfig {

    @Bean
    public SqsClient sqsClient() {
        return SqsClient.builder()
                .region(Region.of("eu-central-1"))
                .build();
    }

    @Bean
    public AmazonSNS amazonSNS() {
        return AmazonSNSClientBuilder
                .standard()
                .withRegion("eu-central-1")
                .build();
    }
}