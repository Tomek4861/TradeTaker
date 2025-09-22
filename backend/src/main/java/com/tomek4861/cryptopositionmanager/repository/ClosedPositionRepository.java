package com.tomek4861.cryptopositionmanager.repository;

import com.tomek4861.cryptopositionmanager.entity.ClosedPosition;
import com.tomek4861.cryptopositionmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClosedPositionRepository extends JpaRepository<ClosedPosition, Integer> {

    List<ClosedPosition> findByUser(User user);


}
