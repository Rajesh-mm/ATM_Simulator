package com.atm.service;

import com.atm.model.Transaction;
import com.atm.model.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// import java.util.ArrayList;
import java.util.List;

/**
 * The TransactionService class provides business logic for an account
 * 
 * 
 * 
 */
@Service
public class TransactionService {
    @Autowired
    TransactionRepository transactionRepository;

    /**
     * Function to save a transaction to the repository
     * @param transaction
     */
    public void save(Transaction transaction) {
        transactionRepository.save(transaction);
    }

    public Transaction getTransaction() {
        Transaction t = transactionRepository.findTopByOrderByTransactionIdDesc();
        return t;
    }

        // Method to retrieve all transactions
    public List<Transaction> getAllTransactions() {
        // List<Transaction> transactions = new ArrayList<>();
        return transactionRepository.findAll();
    }
}
