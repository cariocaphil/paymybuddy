package com.paymybuddy.repository;

import com.paymybuddy.models.Transaction;
import com.paymybuddy.models.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
  List<Transaction> findAllBySender(User sender);
  List<Transaction> findAllByReceiver(User receiver);
}