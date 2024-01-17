package com.example.ContaGest.repository;

import com.example.ContaGest.model.AccountantModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountantRepository extends JpaRepository<AccountantModel,Integer> {

    @Query("SELECT f FROM AccountantModel f WHERE f.ci=:userCI")
    Optional<AccountantModel> findByUsername(@Param("userCI") String userCI);

    @Query("SELECT f FROM AccountantModel f WHERE f.email=:email")
    Optional<AccountantModel> findByEmail(@Param("email") String email);
}
