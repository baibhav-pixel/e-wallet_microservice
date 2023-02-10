package com.example.microservices;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class Config {

    //Kafka Configs

    //Defining more configs

    Properties getKafkaProperties(){
        Properties properties = new Properties();

        //Consumer properties
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,StringSerializer.class);
        return properties;
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

    //Configuring Java Mail Sender
    @Bean
    JavaMailSender getMailSender(){
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost("smtp.gmail.com");
        javaMailSender.setUsername("<your-email>@gmail.com");
        javaMailSender.setPassword("<your-pass>");
        javaMailSender.setPort(587);

        Properties properties = javaMailSender.getJavaMailProperties();
        properties.put("mail.smtp.starttls.enable", true); // this is needed otherwise error will come and we can't send email
        properties.put("mail.debug", true); // Not necessary, helps in getting debug messages of java mail sender

        return javaMailSender;
    }

    @Bean
    SimpleMailMessage getMailMessage(){
        return new SimpleMailMessage();
    }

}
