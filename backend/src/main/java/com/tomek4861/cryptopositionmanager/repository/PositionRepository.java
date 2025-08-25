package com.tomek4861.cryptopositionmanager.repository;

import com.tomek4861.cryptopositionmanager.entity.Position;
import com.tomek4861.cryptopositionmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PositionRepository extends JpaRepository<Position, Integer> {

    List<Position> findByUser(User user);


}
