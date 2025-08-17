package com.organizer.grocery.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;
import software.amazon.awssdk.services.sns.model.SubscribeResponse;

@Service
public class SnsEmailService {
    private final SnsClient snsClient = SnsClient.create();
    @Value("${aws.sns.orders-final-status-topic-arn}")
    private String topicFinalStatusArn;

    public void subscribeEmail(String email) {
        SubscribeRequest request = SubscribeRequest.builder()
                .topicArn(topicFinalStatusArn)
                .protocol("email")
                .endpoint(email)
                .build();
        SubscribeResponse response = snsClient.subscribe(request);
    }

}