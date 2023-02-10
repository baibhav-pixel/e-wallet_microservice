package com.example.microservices;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class WalletService {

    private static Logger logger = LoggerFactory.getLogger(WalletService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WalletRepository walletRepository;
    private final KafkaTemplate<String,String> kafkaTemplate;

    public WalletService(WalletRepository walletRepository, KafkaTemplate<String,String> kafkaTemplate){
        this.walletRepository = walletRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    //@KafkaListener(topicPattern = "user",groupId = "MANDATORY FIELD") --> it will listen all the topics that has "user" containing
    //consumer
    @KafkaListener(topics = {"USER_CREATE_TOPIC"}, groupId = "wallet123")
    public void createWallet(String message) throws JsonProcessingException {
        JSONObject walletCreateRequest = objectMapper.readValue(message, JSONObject.class);
        Wallet wallet = to(walletCreateRequest);
        walletRepository.save(wallet);

        //Create a event
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("walletId", wallet.getId());
        jsonObject.put("balance", wallet.getBalance());
        jsonObject.put("email", wallet.getEmail());
        jsonObject.put("message","Hi "+ walletCreateRequest.get("name")+ " we have added Rs "+wallet.getBalance()+" to your wallet.");

        kafkaTemplate.send("WALLET_CREATE_TOPIC", objectMapper.writeValueAsString(jsonObject));
    }

    @KafkaListener(topics = {"TRANSACTION_CREATE_TOPIC"}, groupId = "wallet123")
    public void updateWallet(String msg) throws Exception {

        //Below template will be used to send msg after wallet is updated
        JSONObject senderWalletUpdateResponse = new JSONObject();

        //This will take all the necessary info from the message sent via Kafka through Transaction controller
        JSONObject walletUpdateRequest = objectMapper.readValue(msg, JSONObject.class);
        String sender = (String) walletUpdateRequest.getOrDefault("sender",null);
        String receiver = (String) walletUpdateRequest.getOrDefault("receiver",null);
        Double amount = (Double) walletUpdateRequest.getOrDefault("amount",null);
        String transactionId = (String) walletUpdateRequest.getOrDefault("transactionId",null);

        //Checks on receiver, sender, amount
        if(receiver == null || sender == null || sender.equals(receiver) || amount == null || amount <= 0.0){
            logger.warn("Either sender or receiver or amount is not correct");
            senderWalletUpdateResponse.put("transactionStatus", "FAILED");
            senderWalletUpdateResponse.put("transactionId",transactionId);
            senderWalletUpdateResponse.put("amount", amount);
            kafkaTemplate.send("WALLET_UPDATE_TOPIC", objectMapper.writeValueAsString(senderWalletUpdateResponse));
            return;
        }

        Wallet senderWallet = walletRepository.findByEmail(sender);
        Wallet receiverWallet = walletRepository.findByEmail(receiver);

        if(senderWallet == null || receiverWallet == null || senderWallet.getBalance() < amount){
            logger.warn("Either receiver or sender doesn't have a wallet or " +
                    "there is no sufficient balance in sender's account");
            senderWalletUpdateResponse.put("transactionStatus", "FAILED");
            senderWalletUpdateResponse.put("transactionId",transactionId);
            senderWalletUpdateResponse.put("amount", amount);
            kafkaTemplate.send("WALLET_UPDATE_TOPIC", objectMapper.writeValueAsString(senderWalletUpdateResponse));
            return;
        }

        //If all checks good, update sender and receiver balance
        senderWallet.setBalance(senderWallet.getBalance() - amount);
        receiverWallet.setBalance(receiverWallet.getBalance() + amount);

        walletRepository.saveAll(Arrays.asList(senderWallet, receiverWallet));

        senderWalletUpdateResponse.put("transactionStatus", "SUCCESS");
        senderWalletUpdateResponse.put("transactionId",transactionId);
        senderWalletUpdateResponse.put("amount", amount);

        kafkaTemplate.send("WALLET_UPDATE_TOPIC", objectMapper.writeValueAsString(senderWalletUpdateResponse));

        // Handling receiver, will not pass txnId so that we don't update receiver
        JSONObject receiverWalletUpdateResponse = new JSONObject();
        receiverWalletUpdateResponse.put("transactionId",transactionId);
        receiverWalletUpdateResponse.put("transactionStatus", "SUCCESS");
        receiverWalletUpdateResponse.put("email", receiver);
        receiverWalletUpdateResponse.put("amount", amount);
        receiverWalletUpdateResponse.put("message", "Hi your wallet has been credited by amount of Rs"+amount);

        kafkaTemplate.send("WALLET_UPDATE_TOPIC", objectMapper.writeValueAsString(receiverWalletUpdateResponse));
    }

    public Wallet to(JSONObject walletRequest){
        return Wallet.builder()
                .nationalId((String) walletRequest.getOrDefault("nationalId",null))
                .balance((Double) walletRequest.getOrDefault("amount",null))
                .country((String) walletRequest.getOrDefault("country", null))
                .email((String) walletRequest.getOrDefault("email", null))
                .build();
    }
}
