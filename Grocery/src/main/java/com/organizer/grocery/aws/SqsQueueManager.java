package com.organizer.grocery.aws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.organizer.grocery.dto.FailedOrderRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;



import java.util.List;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class SqsQueueManager {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    @Value("{aws.sqs.order-execution-address}")
    private String orderExecutionQueueURL;
    private final SnsPublisher snsPublisher;
    private final OrderRetry orderRetry;

    @Autowired
    public SqsQueueManager(SqsClient sqsClient, ObjectMapper objectMapper, SnsPublisher snsPublisher, OrderRetry orderRetry) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.objectMapper.registerModule(new JavaTimeModule());
        this.snsPublisher = snsPublisher;
        this.orderRetry = orderRetry;
    }

    public void sendOrder(Object order) throws Exception {
        String messageBody = objectMapper.writeValueAsString(order);
        String deduplicationId = String.valueOf(System.currentTimeMillis());
        SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                .queueUrl(orderExecutionQueueURL)
                .messageBody(messageBody)
                .messageGroupId("order-group")
                .messageDeduplicationId(deduplicationId)
                .build();
        System.out.println("Sending message to SQS: " + messageBody);
        sqsClient.sendMessage(sendMsgRequest);
    }

    @Scheduled(fixedDelay = 30000)
    public void pollAndCreateOrders() throws Exception {
        System.out.println("Polling SQS for messages...");
        ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(orderExecutionQueueURL)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(10)
                .build();

        List<Message> messages = sqsClient.receiveMessage(receiveRequest).messages();

        for (Message message : messages) {
            String orderJson = message.body();
            System.out.println("Processing message from SQS: " + orderJson);

            FailedOrderRequestDto order = objectMapper.readValue(orderJson, FailedOrderRequestDto.class);
            Long orderId = order.orderId();
            Duration difference = Duration.between(order.initializationTime(), LocalDateTime.now());
            if (difference.toHours() > 12) {
                snsPublisher.publishFailure(orderId);
            } else if (orderRetry.retryFailedOrder(order.orderRequestDto(), orderId)) {
                snsPublisher.publishSuccess(orderId);
            } else {
                sendOrder(order);
            }
            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(orderExecutionQueueURL)
                    .receiptHandle(message.receiptHandle())
                    .build());
        }
    }
}
