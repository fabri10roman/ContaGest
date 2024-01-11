package com.example.ContaGest.repository;

import com.example.ContaGest.model.TokenModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<TokenModel,Integer> {


    @Query("SELECT f FROM TokenModel f WHERE " +
            "(f.client.id=:userId AND f.expired=false AND f.revoke=false) " +
            "OR (f.accountant.id=:userId AND f.expired=false AND f.revoke=false)")
    List<TokenModel> findAllValidTokensByUser(@Param("userId") Integer userId);


    @Query("SELECT f FROM TokenModel f WHERE f.token=:token")
    Optional<TokenModel> findByToken(@Param("token") String token);


}