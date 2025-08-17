package com.organizer.grocery.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.organizer.grocery.dto.FailedOrderRequestDto;

import java.time.LocalDateTime;

import java.time.Duration;

public class OrderLambdaHandler implements RequestHandler<SQSEvent, Void> {
    private final SnsPublisher snsPublisher;
    private final OrderRetry orderRetry;
    private final SqsQueueManager sqsQueueManager;

    public OrderLambdaHandler(SnsPublisher snsPublisher, OrderRetry orderRetry, SqsQueueManager sqsQueueManager) {
        this.snsPublisher = snsPublisher;
        this.orderRetry = orderRetry;
        this.sqsQueueManager = sqsQueueManager;
    }

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        for (SQSEvent.SQSMessage msg : event.getRecords()) {
            String orderJson = msg.getBody();
            try {
                FailedOrderRequestDto order = new ObjectMapper().readValue(orderJson, FailedOrderRequestDto.class);
                Long orderId = order.orderId();
                Duration difference = Duration.between(order.initializationTime(), LocalDateTime.now());
                if(difference.toHours() > 12){
                    snsPublisher.publishFailure(orderId);
                }else if (orderRetry.retryFailedOrder(order.orderRequestDto(), orderId)) {
                    snsPublisher.publishSuccess(orderId);
                } else {
                    sqsQueueManager.sendOrder(order);
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to parse order JSON: " + orderJson, e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}