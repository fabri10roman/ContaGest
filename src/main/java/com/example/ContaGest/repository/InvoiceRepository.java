package com.example.ContaGest.repository;

import com.example.ContaGest.model.InvoiceModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceModel,Long>{

    @Query("SELECT f FROM InvoiceModel f WHERE f.client.userCI =:clientCI AND f.month =:month")
    List<InvoiceModel> findByClientIdAndMonth(@Param("clientCI") String clientCI,@Param("month") int month);

    @Query("SELECT f.id FROM InvoiceModel f WHERE f.client.userCI =:clientCI")
    List<Long> findIdByClientCI (@Param("clientCI") String clientCI);

    @Query("SELECT f.id FROM InvoiceModel f WHERE f.client.userCI =:clientCI AND f.month =:month")
    List<Long> findIdByClientCiAndMonth (@Param("clientCI") String clientCI,@Param("month") int month);
}
