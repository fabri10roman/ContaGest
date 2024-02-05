package com.example.ContaGest.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.NaturalId;
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
@Table(name = "Accountants")
public class AccountantModel implements UserDetails{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, unique = true)
    @NaturalId()
    private String ci;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String lastname;
    @NaturalId(mutable = true)
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;
    @Column(nullable = false)
    private String phoneNumber;
    private boolean isEnable = false;
    private boolean isConfirmed = false;

    @OneToMany(mappedBy = "accountant", cascade = CascadeType.ALL)
    @Fetch(FetchMode.JOIN)
    private List<ClientModel> clients;

    @JsonManagedReference
    public List<ClientModel> getClients() {
        return clients;
    }

    @OneToMany(mappedBy = "accountant", cascade = CascadeType.ALL)
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

    @Override
    public String getUsername() {
        return ci;
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
    public boolean isEnabled() {return isEnable; }
}
