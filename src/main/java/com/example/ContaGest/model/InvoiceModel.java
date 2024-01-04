package com.example.ContaGest.model;


import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Invoices")
public class InvoiceModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int month;
    private byte[] img;

    @ManyToOne
    @JoinColumn(name = "client_fk",nullable = false)
    private ClientModel client;

}
