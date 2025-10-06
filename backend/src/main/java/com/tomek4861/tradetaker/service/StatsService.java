package com.tomek4861.tradetaker.service;


import com.tomek4861.tradetaker.dto.stats.ClosedPositionDTO;
import com.tomek4861.tradetaker.dto.stats.PnlByDayDTO;
import com.tomek4861.tradetaker.entity.ClosedPosition;
import com.tomek4861.tradetaker.entity.User;
import com.tomek4861.tradetaker.repository.ClosedPositionRepository;
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
