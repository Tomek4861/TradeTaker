package com.tomek4861.cryptopositionmanager.entity;


import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "closed_positions")
public class ClosedPosition {
    public enum PositionSide {
        LONG,
        SHORT
    }


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;


    @Column(name = "leverage", nullable = false)
    private BigDecimal leverage;


    @Column(name = "volume", nullable = false)
    private BigDecimal volume;

    @Column(name = "avg_entry_price", nullable = false)
    private BigDecimal avgEntryPrice;

    @Column(name = "avg_close_price", nullable = false)
    private BigDecimal avgClosePrice;


    @Column(name = "filled_at")
    private LocalDateTime filledAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;


    @Column(name = "realized_pnl")
    private BigDecimal realizedPnl;

    @Column(name = "paid_commission")
    private BigDecimal paidCommission;


}
