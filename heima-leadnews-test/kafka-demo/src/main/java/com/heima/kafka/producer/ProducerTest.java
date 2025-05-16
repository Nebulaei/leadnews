package com.heima.kafka.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class ProducerTest {

    public static void main(String[] args) {

        // kafka的配置信息
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        // 消息key的序列化器
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");
        // 消息value的序列化器
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");
        // 生产者对象
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        ProducerRecord producerRecord = new ProducerRecord<>("topic-1", "hello, kafka");
        producer.send(producerRecord);

        producer.close();
    }
}
