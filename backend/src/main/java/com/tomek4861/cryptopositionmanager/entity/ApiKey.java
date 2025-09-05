package com.tomek4861.cryptopositionmanager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Entity
@Table(name = "api_keys")
@NoArgsConstructor
@Getter
@Setter
public class ApiKey {

    public ApiKey(String key, String secret) {
        this.key = key;
        this.secret = secret;
        this.createdAt = LocalDateTime.now();
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "key", nullable = false)
    private String key;

    @Column(name = "api_secret", nullable = false)
    private String secret;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "apiKey")
    private User user;


}
