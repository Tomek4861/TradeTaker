package com.tomek4861.tradetaker;

import com.tomek4861.tradetaker.dto.positions.preview.CalculationParameters;
import com.tomek4861.tradetaker.dto.positions.preview.CalculationResult;
import com.tomek4861.tradetaker.dto.positions.takeprofit.TakeProfitLevel;
import com.tomek4861.tradetaker.exception.CalculationException;
import com.tomek4861.tradetaker.service.PositionCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PositionCalculatorTest {

    private PositionCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PositionCalculator();
    }

    private CalculationParameters createCalculationParameters(
            String balance, String riskPercentage,
            String entry, String sl,
            List<TakeProfitLevel> tps,
            String maxLev, String qtyStep
    ) {
        return new CalculationParameters(
                new BigDecimal(balance),
                new BigDecimal(riskPercentage),
                new BigDecimal(entry),
                new BigDecimal(sl),
                tps,
                new BigDecimal(maxLev),
                new BigDecimal(qtyStep)
        );
    }

    // 1) Happy path with TPs: verify all fields + scales.
    @Test
    void calculate_happyPath_withTP_returnsAllFields() {
        // balance=1000, risk=1% -> riskAmount(final loss)=10
        // entry=100, SL=95 (5%), size≈2.000 after qtyStep=0.001
        // TPs: 110(50%), 120(50%) -> WAP=115 -> profit=(115-100)*2=30
        // lev=((1-0.003)/0.05) UP(2)=19.94 *0.9=17.946; margin=200/17.946 (scale=8)=11.14454475
        var tps = List.of(
                new TakeProfitLevel(new BigDecimal("110"), new BigDecimal("50")),
                new TakeProfitLevel(new BigDecimal("120"), new BigDecimal("50"))
        );
        var p = createCalculationParameters("1000", "1", "100", "95", tps, "20", "0.001");

        CalculationResult r = calculator.calculate(p);

        // riskAmount holds final potential loss
        assertThat(r.riskAmount()).isEqualByComparingTo("10");

        // value & size
        assertThat(r.positionValue()).isEqualByComparingTo("200");
        assertThat(r.positionSize()).isEqualByComparingTo("2.000"); // after step trim

        // leverage & margin
        assertThat(r.leverage()).isEqualByComparingTo("17.946");
        assertThat(r.requiredMargin()).isEqualByComparingTo("11.14454475");
        assertThat(r.requiredMargin().scale()).isEqualTo(8); // calculator returns scale(8)

        // profit & R:R
        assertThat(r.potentialProfit()).isEqualByComparingTo("30");
        assertThat(r.riskToRewardRatio()).isEqualByComparingTo("3.00");
        assertThat(r.riskToRewardRatio().scale()).isEqualTo(2); // calculator returns scale(2)
    }

    // 2) No TPs -> profit=0, RR=0.
    @Test
    void calculate_noTP_profitAndRRZero() {
        var p = createCalculationParameters("1000", "1", "100", "95", List.of(), "100", "0.001");
        CalculationResult r = calculator.calculate(p);

        assertThat(r.potentialProfit()).isEqualByComparingTo("0");
        assertThat(r.riskToRewardRatio()).isEqualByComparingTo("0.00");
    }

    // 3) entry == SL -> throws.
    @Test
    void calculate_slEqualsEntry_throwsCalculationException() {
        var p = createCalculationParameters("1000", "1", "100", "100", null, "100", "0.001");
        assertThatThrownBy(() -> calculator.calculate(p))
                .isInstanceOf(CalculationException.class)
                .hasMessageContaining("Stop Loss equals entry price");
    }

    // 4) SL distance ≤ maintenance margin (0.3%) -> throws.
    @Test
    void calculate_slTooCloseForLeverage_throwsCalculationException() {
        // entry=100, SL=99.7 => 0.3%
        var p = createCalculationParameters("1000", "1", "100", "99.7", null, "100", "0.001");
        assertThatThrownBy(() -> calculator.calculate(p))
                .isInstanceOf(CalculationException.class)
                .hasMessageContaining("Stop loss is too close to entry price for leverage calculation");
    }

    // 5) Leverage capped at maxLeverage.
    @Test
    void calculate_leverageCappedByMaxLeverage() {
        // entry=100, SL=99 => ≈1% -> raw lev≈89.73 -> capped to 50
        var p = createCalculationParameters("1000", "1", "100", "99", null, "50", "0.001");
        CalculationResult r = calculator.calculate(p);

        assertThat(r.leverage()).isEqualByComparingTo("50");
    }

    // 6) Large SL distance -> floored to 1x.
    @Test
    void calculate_leverageFlooredToOne() {
        // entry=100, SL=200 => ~100% -> ~0.90 -> floor to 1
        var p = createCalculationParameters("1000", "1", "100", "200", null, "125", "0.001");
        CalculationResult r = calculator.calculate(p);

        assertThat(r.leverage()).isEqualByComparingTo("1");
    }

    // 7) Size rounded down to qtyStep.
    @Test
    void calculate_qtyRoundedDownToQtyStep() {
        // craft raw size≈3.333..., step=0.2 -> expect 3.2
        var p = createCalculationParameters("1000", "1", "123", "120", null, "100", "0.2");
        CalculationResult r = calculator.calculate(p);

        assertThat(r.positionSize()).isEqualByComparingTo("3.2");

        // sanity: multiple of step
        BigDecimal step = new BigDecimal("0.2");
        BigDecimal recomputed = r.positionSize()
                .divide(step, 0, RoundingMode.DOWN)
                .multiply(step);
        assertThat(recomputed).isEqualByComparingTo(r.positionSize());
    }
}
