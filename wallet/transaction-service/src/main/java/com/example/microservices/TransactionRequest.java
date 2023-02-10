package com.example.microservices;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequest {

    @NotNull
    private String sender;
    @NotNull
    private String  receiver;
    @NotNull
    private double amount;
    private String message;

    public Transaction to(){
        return Transaction.builder()
                .amount(amount)
                .sender(sender)
                .receiver(receiver)
                .transactionMessage(message)
                .transactionId(UUID.randomUUID().toString())
                .transactionStatus(TransactionStatus.PENDING)
                .build();
    }

}
