package com.tomek4861.cryptopositionmanager.entity;


import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "orders")

public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;


    @Column(name = "ticker", nullable = false)
    private String ticker;

    @Column(name = "cancel_price")
    private BigDecimal cancelPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false)
    private Position.PositionSide side;

    @Column(name = "trigger_price")
    private BigDecimal triggerPrice;

    @Column(name = "entry_price", nullable = false)
    private BigDecimal entryPrice;

    @Column(name = "initial_volume", nullable = false)
    private BigDecimal initialVolume;

    @Column(name = "initial_stop_loss_price")
    private BigDecimal initialStopLossPrice;

    @Column(name = "move_stop_loss")
    private boolean moveStopLoss;


}
