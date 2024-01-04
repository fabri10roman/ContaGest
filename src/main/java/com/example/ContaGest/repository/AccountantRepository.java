package com.example.ContaGest.repository;

import com.example.ContaGest.model.AccountantModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountantRepository extends JpaRepository<AccountantModel,Long> {
}
