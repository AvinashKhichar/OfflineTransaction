package com.offlinetransaction.models;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findTop20ByOrderByIdDesc();
    boolean existsByReceiverVpa(String packetHash);//will prevent duplicate transactions in future
}
