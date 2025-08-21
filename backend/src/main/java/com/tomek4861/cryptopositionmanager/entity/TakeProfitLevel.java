package com.tomek4861.cryptopositionmanager.entity;


import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class TakeProfitLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "volume_to_close", nullable = false)
    private BigDecimal volumeToClose;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

}
