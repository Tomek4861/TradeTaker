package com.tomek4861.tradetaker.repository;

import com.tomek4861.tradetaker.dto.stats.PnlByDayDTO;
import com.tomek4861.tradetaker.entity.ClosedPosition;
import com.tomek4861.tradetaker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClosedPositionRepository extends JpaRepository<ClosedPosition, Integer> {

    List<ClosedPosition> findByUser(User user);


    @Query("SELECT cp FROM ClosedPosition cp WHERE cp.user = :user AND EXTRACT(YEAR FROM cp.closedAt) = :year AND EXTRACT(MONTH FROM cp.closedAt) = :month")
    List<ClosedPosition> findAllByUserAndMonth(
            @Param("user") User user,
            @Param("year") int year,
            @Param("month") int month
    );

    @Query("SELECT new com.tomek4861.tradetaker.dto.stats.PnlByDayDTO( " +
            "CAST(cp.closedAt AS date)," +
            "SUM(cp.realizedPnl)" +
            " )" +
            "FROM ClosedPosition cp " +
            "WHERE cp.user = :user AND EXTRACT(YEAR FROM cp.closedAt) = :year AND EXTRACT(MONTH FROM cp.closedAt) = :month " +
            "GROUP BY CAST(cp.closedAt AS date) " +
            "ORDER BY CAST(cp.closedAt AS date) ASC"
    )
    List<PnlByDayDTO> getPnlPerDayByUserAndMonthAndYear(
            @Param("user") User user,
            @Param("year") int year,
            @Param("month") int month
    );


}
