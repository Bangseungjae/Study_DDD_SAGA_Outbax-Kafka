package com.food.ordering.system.order.service.messaging.publisher.kafka;

import com.food.ordering.system.kafka.config.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.config.producer.service.KafkaProducer;
import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfigData;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.payment.OrderCreatedPaymentRequestMessagePublisher;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CreateOrderKafkaMessagePublisher implements OrderCreatedPaymentRequestMessagePublisher {

    private final OrderMessagingDataMapper orderMessagingDataMapper;
    private final OrderServiceConfigData orderServiceConfigData;
    private final KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer;
    private final KafkaMessageHelper kafkaMessageHelper;

    public CreateOrderKafkaMessagePublisher(
            OrderMessagingDataMapper orderMessagingDataMapper,
            OrderServiceConfigData orderServiceConfigData,
            KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer,
            KafkaMessageHelper kafkaMessageHelper) {
        this.orderMessagingDataMapper = orderMessagingDataMapper;
        this.orderServiceConfigData = orderServiceConfigData;
        this.kafkaProducer = kafkaProducer;
        this.kafkaMessageHelper = kafkaMessageHelper;
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void publish(@NotNull OrderCreatedEvent domainEvent) {
        String orderId = domainEvent.getOrder().getId().getValue().toString();
        logger.info("Received OrderCreatedEvent for order id: {}", orderId);
        try {

            PaymentRequestAvroModel paymentRequestAvroModel =
                    orderMessagingDataMapper.orderCreatedEventToPaymentRequestAvroModel(domainEvent);

            kafkaProducer.send(
                    orderServiceConfigData.getPaymentRequestTopicName(),
                    orderId,
                    paymentRequestAvroModel,
                    kafkaMessageHelper.getKafkaCallback(
                            orderServiceConfigData.getPaymentResponseTopicName(),
                            paymentRequestAvroModel,
                            orderId,
                            "PaymentRequestAvroModel"
                    )
            );
            logger.info("PaymentRequestAvroModel sent to Kafka for order id: {}", paymentRequestAvroModel.orderId);
        } catch (Exception e) {
            logger.error("Error while sending PaymentRequestModel message" +
                    " to kafka with order id: {} error: {}", orderId, e.getMessage());
        }
    }


}
