package com.example.ContaGest.repository;

import com.example.ContaGest.model.ClientModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<ClientModel,Integer> {

    @Query("SELECT f FROM ClientModel f WHERE f.ci=:userCI")
    Optional<ClientModel> findByUsername(@Param("userCI") String userCI);

    @Query("SELECT f FROM ClientModel f WHERE f.email=:email")
    Optional<ClientModel> findByEmail(@Param("email") String email);

    @Query("SELECT f FROM ClientModel f WHERE f.accountant.id=:accountantID AND f.isEnable=true")
    List<ClientModel> findAllByAccountantIdAndIsEnable(Integer accountantID);
}
