package com.example.ContaGest.repository;

import com.example.ContaGest.model.AccountantModel;
import com.example.ContaGest.model.UserInfoModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountantRepository extends JpaRepository<AccountantModel,Long> {

    @Query("SELECT f FROM AccountantModel f WHERE f.userCI=:userCI")
    Optional<AccountantModel> findByUsername(@Param("userCI") String userCI);
}
