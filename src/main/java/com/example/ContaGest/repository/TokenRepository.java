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
            "(f.client.id=:userId AND f.isExpired=false AND f.isRevoke=false) " +
            "OR (f.accountant.id=:userId AND f.isExpired=false AND f.isRevoke=false)")
    List<TokenModel> findAllValidTokensByUser(@Param("userId") Integer userId);


    @Query("SELECT f FROM TokenModel f WHERE f.token=:token")
    Optional<TokenModel> findByToken(@Param("token") String token);


    @Query("SELECT f.token FROM TokenModel f WHERE f.client_id=:id AND f.isRegistration=true AND f.isRevoke=false AND f.isRevoke=false")
    List<String> findTokenRegisterClientByClientId(@Param("id") Integer id);

    @Query("SELECT f.token FROM TokenModel f WHERE f.accountant_id=:id AND f.isRegistration=true AND f.isRevoke=false AND f.isRevoke=false")
    List<String> findTokenRegisterAccountantByAccountantId(@Param("id") Integer id);

    @Query("SELECT f FROM TokenModel f WHERE f.isForgotPassword=true AND f.isRevoke=false AND f.isRevoke=false AND f.client_id=:id")
    Optional<TokenModel> findTokenForgotPasswordClientByClientID(@Param("id") Integer id);

    @Query("SELECT f FROM TokenModel f WHERE f.isForgotPassword=true AND f.isRevoke=false AND f.isRevoke=false AND f.accountant_id=:id")
    Optional<TokenModel> findTokenForgotPasswordAccountantByAccountantID(@Param("id") Integer id);

}