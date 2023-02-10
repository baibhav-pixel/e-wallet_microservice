package com.example.microservices;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final SimpleMailMessage simpleMailMessage;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EmailService(JavaMailSender javaMailSender, SimpleMailMessage simpleMailMessage){
        this.javaMailSender = javaMailSender;
        this.simpleMailMessage = simpleMailMessage;
    }

    @KafkaListener(topics = {"WALLET_CREATE_TOPIC","WALLET_UPDATE_TOPIC", "TRANSACTION_COMPLETE_TOPIC"}, groupId = "notify123")
    public void sendMessage(String message) throws Exception {
        JSONObject sendEmailRequest = objectMapper.readValue(message, JSONObject.class);
        String txtMsg = (String) sendEmailRequest.getOrDefault("message","");
        String email = (String) sendEmailRequest.getOrDefault("email",null);
        boolean notify = (Boolean) sendEmailRequest.getOrDefault("notify", true);

        if(email==null || notify==false){
            throw new Exception();
        }

        simpleMailMessage.setText(txtMsg);
        simpleMailMessage.setTo(email); // it takes an arrow
        simpleMailMessage.setFrom("<your-email>@gmail.com");
        simpleMailMessage.setSubject("Coder's Pay Welcome Onboard");

        javaMailSender.send(simpleMailMessage);
    }
}
