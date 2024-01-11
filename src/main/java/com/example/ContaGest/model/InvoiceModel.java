package com.example.ContaGest.model;


import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Invoices")
public class InvoiceModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private int month;
    private byte[] img;

    @ManyToOne
    @JoinColumn(name = "client_fk",nullable = false)
    private ClientModel client;

}
