package com.tomek4861.cryptopositionmanager.entity;


import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

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

    @Column(name = "risk_percentage")
    private BigDecimal riskPercent;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "api_key_id", referencedColumnName = "id")
    private ApiKey apiKey;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Position> positionsList;


}
