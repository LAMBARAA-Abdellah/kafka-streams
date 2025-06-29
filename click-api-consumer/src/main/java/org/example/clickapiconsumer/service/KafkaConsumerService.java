package org.example.clickapiconsumer.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class KafkaConsumerService {

    private final AtomicLong count = new AtomicLong(0);

    @KafkaListener(topics = "click-counts", groupId = "click-api-group")
    public void listen(ConsumerRecord<String, Long> record) {
        if (record.value() != null) {
            System.out.println("Message reçu depuis Kafka: " + record.value());
            count.set(record.value());
        } else {
            System.out.println("Valeur invalide reçue : " + record.value());
        }
    }

    public long getCount() {
        return count.get();
    }
}