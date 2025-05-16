package com.heima.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ConsumerService {

    @KafkaListener(topics = {"test"})
    public void message(String message) {
        log.info(message);
    }
}
