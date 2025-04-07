package com.example.notificationservice.controller;

import com.example.notificationservice.kafka.KafkaProducer;
import com.example.notificationservice.model.UserNotificationToSend;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sc-users")
public class NotificationController {

    private final KafkaProducer kafkaProducer;

    public NotificationController(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    @PostMapping(path = "/messages")
    public ResponseEntity<Void> sendNotification(@RequestBody UserNotificationToSend notification) {
        System.out.println("DENTRO A ENDPOIND, invio: " + notification.toString());
        kafkaProducer.sendMessage("sc-users", notification);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
