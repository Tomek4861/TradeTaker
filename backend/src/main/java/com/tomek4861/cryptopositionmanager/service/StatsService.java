package com.tomek4861.cryptopositionmanager.service;


import com.tomek4861.cryptopositionmanager.dto.stats.ClosedPositionDTO;
import com.tomek4861.cryptopositionmanager.dto.stats.PnlByDayDTO;
import com.tomek4861.cryptopositionmanager.entity.ClosedPosition;
import com.tomek4861.cryptopositionmanager.entity.User;
import com.tomek4861.cryptopositionmanager.repository.ClosedPositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final ClosedPositionRepository closedPositionRepository;

    public List<ClosedPositionDTO> getClosedPositionsForMonthAndYear(User user, int year, int month) {
        List<ClosedPosition> allPositionsPerMonth = closedPositionRepository.findAllByUserAndMonth(user, year, month);

        return allPositionsPerMonth.stream().map(
                position -> new ClosedPositionDTO(
                        position.getId(),
                        position.getSide().toString(),
                        position.getVolume(),
                        position.getAvgEntryPrice(),
                        position.getAvgClosePrice(),
                        position.getFilledAt(),
                        position.getClosedAt(),
                        position.getRealizedPnl(),
                        position.getPaidCommission()
                )
        ).toList();


    }

    public List<PnlByDayDTO> getPnlByDayForMonthAndYear(User user, int year, int month) {
        return closedPositionRepository.getPnlPerDayByUserAndMonthAndYear(user, year, month);
    }

}
