package com.petd.tiktok_system_be.setup;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.*;

public class KafkaResendUniqueKey {
    public static void main(String[] args) throws IOException {
        // Producer config
        Properties producerProps = new Properties();
        producerProps.put("bootstrap.servers", "14.225.192.60:9094");
        producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps);

        String path= "D:\\petd_coding\\tiktok_shop_system\\data-1757299869835.json";
        readFile(path, producer);

        producer.close();
    }

    public static void readFile(String path,  KafkaProducer<String, String> producer ) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(path)).get("data");
        for (JsonNode item : root) {
            String shopId = item.get("shopId").asText();
            // Convert cả item thành JSON string
            String mess = mapper.writeValueAsString(item);

            ProducerRecord<String, String> record =
                    new ProducerRecord<>("order-details-sync", shopId, mess);
            producer.send(record);
            System.out.println(mess);
        }
    }


    public static List<ConsumerRecord<String, String>> fetchUniqueMessages(KafkaConsumer<String, String> consumer) {
        Set<String> seenKeys = new HashSet<>();
        List<ConsumerRecord<String, String>> uniqueMessages = new ArrayList<>();

        while (true) {
            var records = consumer.poll(Duration.ofSeconds(1));
            if (records.isEmpty()) break; // dừng khi không còn message nào

            for (ConsumerRecord<String, String> record : records) {
                if (seenKeys.add(record.key())) { // chỉ giữ message đầu tiên của key
                    uniqueMessages.add(record);
                }
            }
        }

        return uniqueMessages;
    }

    public static void resendMessages(KafkaProducer<String, String> producer,
                                      List<ConsumerRecord<String, String>> messages,
                                      String outputTopic) {
        for (ConsumerRecord<String, String> record : messages) {
            ProducerRecord<String, String> newRecord =
                    new ProducerRecord<>(outputTopic, record.key(), record.value());
            producer.send(newRecord);
            System.out.printf("Re-sent key=%s, value=%s%n", record.key(), record.value());
        }
        producer.flush();
    }
}