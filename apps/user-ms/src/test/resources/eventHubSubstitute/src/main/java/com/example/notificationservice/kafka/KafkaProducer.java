package com.example.notificationservice.kafka;

import com.example.notificationservice.model.UserNotificationToSend;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {

    private final KafkaTemplate<String, UserNotificationToSend> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, UserNotificationToSend> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topic, UserNotificationToSend message) {
            kafkaTemplate.send(topic, message.getId(), message);
    }
}
