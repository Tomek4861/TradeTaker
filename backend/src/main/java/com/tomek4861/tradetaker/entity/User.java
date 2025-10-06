package com.tomek4861.tradetaker.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "users")
public class User implements UserDetails {

    public enum AuthProviderEnum {
        GOOGLE,
        LOCAL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;


    @Column(name = "password", nullable = false)
    private String password;


    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider")
    private AuthProviderEnum authProviderEnum;

    @Column(name = "risk_percentage")
    private BigDecimal riskPercent;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "api_key_id", referencedColumnName = "id")
    private ApiKey apiKey;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClosedPosition> positionsList;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }


}
