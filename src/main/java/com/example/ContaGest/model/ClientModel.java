package com.example.ContaGest.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Clients")
public class ClientModel implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, unique = true)
    private String userCI;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String lastname;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;
    @Column(nullable = false)
    private Integer number;
    private Boolean isEnable;
    private Integer accountant_id;


    @ManyToOne
    @JoinColumn(name = "accountant_id", referencedColumnName = "id", insertable = false, updatable = false)
    private AccountantModel accountant;

    @OneToMany(mappedBy = "client")
    private List<InvoiceModel> invoices;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    @Fetch(FetchMode.JOIN)
    private List<TokenModel> tokens;

    @JsonManagedReference
    public List<TokenModel> getToken() {
        return tokens;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @JsonBackReference
    public AccountantModel getAccountant() {
        return accountant;
    }

    @Override
    public String getUsername() {
        return userCI;
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
