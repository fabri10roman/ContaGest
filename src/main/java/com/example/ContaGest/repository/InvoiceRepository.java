package com.example.ContaGest.repository;

import com.example.ContaGest.model.InvoiceModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceModel,Integer>{

    @Query("SELECT f FROM InvoiceModel f WHERE f.client.ci =:clientCI AND f.month =:month AND f.year =:year")
    List<InvoiceModel> findByClientIdAndMonthAndYear(@Param("clientCI") String clientCI, @Param("month") int month, @Param("year") int year);

    @Query("SELECT f.id FROM InvoiceModel f WHERE f.client.ci =:clientCI AND f.year =:year")
    List<Integer> findIdByClientCI (@Param("clientCI") String clientCI, @Param("year") int year);

    @Query("SELECT f.id FROM InvoiceModel f WHERE f.client.ci =:clientCI AND f.month =:month AND f.year =:year")
    List<Integer> findIdByClientCiAndMonthAndYear(@Param("clientCI") String clientCI, @Param("month") int month , @Param("year") int year);
}
