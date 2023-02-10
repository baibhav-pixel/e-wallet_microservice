package com.example.microservices;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    public TransactionService(TransactionRepository transactionRepository, KafkaTemplate<String,String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.transactionRepository = transactionRepository;
    }

    public String doTransaction(TransactionRequest transactionRequest) throws JsonProcessingException {
        Transaction transaction = transactionRequest.to();
        transactionRepository.save(transaction);

        // Notify wallet-service to update wallet of sender and receiver

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sender",transaction.getSender());
        jsonObject.put("receiver",transaction.getReceiver());
        jsonObject.put("amount",transaction.getAmount());
        jsonObject.put("transactionId",transaction.getTransactionId());

        //transactionId is needed so that transaction status is updated to SUCCESS/FAILED,
        // this is done by TransactionService

        kafkaTemplate.send("TRANSACTION_CREATE_TOPIC", objectMapper.writeValueAsString(jsonObject));
        return transaction.getTransactionId();
    }

    @KafkaListener(topics = "WALLET_UPDATE_TOPIC", groupId = "transaction123")
    @Transactional
    public void updateTransaction(String msg) throws Exception{

        JSONObject transactionUpdateRequest = objectMapper.readValue(msg, JSONObject.class);

        String txnId = (String) transactionUpdateRequest.getOrDefault("transactionId", null);
        String txnStatus = (String) transactionUpdateRequest.getOrDefault("transactionStatus","PENDING");
        TransactionStatus transactionStatus = TransactionStatus.valueOf(txnStatus);
        Double amount = (Double) transactionUpdateRequest.getOrDefault("amount",0.0);

        Transaction transactionFromDB = transactionRepository.findByTransactionId(txnId)
                .stream()
                .findFirst()
                .orElse(null);

        transactionFromDB.setTransactionStatus(transactionStatus);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("email", transactionFromDB.getSender());

        //If transaction failed notify user on failed one
        if(transactionStatus == TransactionStatus.FAILED)
        {
            logger.warn("Transaction with id " +txnId+ " was unsuccessful");

            // Notify sender for unsuccessful transaction
            jsonObject.put("notify", true);
        }

        else
        {
            if(transactionFromDB.isSenderNotified())
            {
                jsonObject.put("notify", false);
            }

        }
        transactionFromDB.setSenderNotified(true);
        transactionFromDB = transactionRepository.save(transactionFromDB);

        jsonObject.put("message", "Hi, your transaction with id " +txnId+ " for a amount of Rs " +amount+ " is "+txnStatus);
        kafkaTemplate.send("TRANSACTION_COMPLETE_TOPIC", objectMapper.writeValueAsString(jsonObject));

    }
}
