package com.example.microservices;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

import java.util.Properties;

@Configuration
public class Config {
    Properties getKafkaProperties(){
        //Producer properties
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        //Consumer properties
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,StringSerializer.class);
        return properties;
    }

    ProducerFactory<String,String> getProducerFactory(){
        return new DefaultKafkaProducerFactory(getKafkaProperties());
    }

    ConsumerFactory<String,String> getConsumerFactory(){
        return new DefaultKafkaConsumerFactory(getKafkaProperties());
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String,String> getListenerFactory(){
        ConcurrentKafkaListenerContainerFactory<String,String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(getConsumerFactory());
        return factory;
    }


    @Bean
    public KafkaTemplate<String,String> getKafkaTemplate(){
        return new KafkaTemplate(getProducerFactory());
    }
}
