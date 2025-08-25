package com.tomek4861.cryptopositionmanager.repository;


import com.tomek4861.cryptopositionmanager.entity.Order;
import com.tomek4861.cryptopositionmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

}
