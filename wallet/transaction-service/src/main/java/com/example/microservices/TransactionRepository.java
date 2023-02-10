package com.example.microservices;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction,Integer> {

    // @Query("update Transaction t set t.transactionStatus = ?1 where t.transactionId = ?2")
    // public void update(TransactionStatus transactionStatus, String transactionId);

    List<Transaction> findByTransactionId(String txnId);
}
