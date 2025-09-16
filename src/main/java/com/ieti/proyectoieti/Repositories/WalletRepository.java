package com.ieti.proyectoieti.Repositories;

import com.ieti.proyectoieti.Models.SharedWallet;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends MongoRepository<SharedWallet, String> {
    List<SharedWallet> findByParticipantsContaining(String userId);
    Optional<SharedWallet> findByName(String name);
    List<SharedWallet> findByBalanceGreaterThan(double amount);
}