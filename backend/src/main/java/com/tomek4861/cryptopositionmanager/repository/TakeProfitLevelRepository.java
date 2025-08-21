package com.tomek4861.cryptopositionmanager.repository;


import com.tomek4861.cryptopositionmanager.entity.Position;
import com.tomek4861.cryptopositionmanager.entity.TakeProfitLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TakeProfitLevelRepository extends JpaRepository<TakeProfitLevel, Integer> {

    List<TakeProfitLevel> findByPosition(Position position);

}
