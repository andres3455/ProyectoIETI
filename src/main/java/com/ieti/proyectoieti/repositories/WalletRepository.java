package com.ieti.proyectoieti.repositories;

import com.ieti.proyectoieti.models.SharedWallet;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletRepository extends MongoRepository<SharedWallet, String> {
  List<SharedWallet> findByParticipantsContaining(String userId);

  Optional<SharedWallet> findByName(String name);

  List<SharedWallet> findByBalanceGreaterThan(double amount);
}
