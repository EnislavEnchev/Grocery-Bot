package com.organizer.grocery.aws;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import org.springframework.beans.factory.annotation.Value;

@Service
public class SnsPublisher {

    private final SnsClient snsClient = SnsClient.create();
    @Value("{aws.sns.orders-final-status-topic-arn}")
    private String topicArn;

    public void publishSuccess(Long id) {
        String message = String.format("Order with ID %d has been successfully executed.", id);
        PublishRequest request = PublishRequest.builder()
                .topicArn(topicArn)
                .message(message)
                .build();
        snsClient.publish(request);
    }

    public void publishFailure(Long id) {
        String message = String.format("Order with ID %d is older than 12 hours and will not be processed.", id);
        PublishRequest request = PublishRequest.builder()
                .topicArn(topicArn)
                .message(message)
                .build();
        snsClient.publish(request);
    }
}