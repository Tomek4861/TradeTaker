package com.tomek4861.cryptopositionmanager.entity;


import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "positions")
public class Position {
    public enum PositionSide {
        LONG,
        SHORT
    }

    public enum PositionState {
        OPEN,
        CLOSED
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private PositionState state;


    @Column(name = "leverage", nullable = false)
    private BigDecimal leverage;


    @Column(name = "current_volume", nullable = false)
    private BigDecimal currentVolume;


    @Column(name = "move_sl", nullable = false)
    private boolean isMoveStopLoss;

    @Column(name = "filled_at")
    private LocalDateTime filledAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;


    @Column(name = "current_stop_loss_price")
    private BigDecimal currentStopLossPrice;


    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "position",
            orphanRemoval = true,
            cascade = CascadeType.ALL
    )
    private List<TakeProfitLevel> takeProfits;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "realized_pnl")
    private BigDecimal realizedPnl;

    @Column(name = "paid_commission")
    private BigDecimal paidCommission;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "opening_order_id", nullable = false)
    private Order openingOrder;


}
