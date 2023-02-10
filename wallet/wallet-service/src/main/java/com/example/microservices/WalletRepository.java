package com.example.microservices;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet,Integer> {

    Wallet findByEmail(String email);
}
