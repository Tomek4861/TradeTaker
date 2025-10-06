package com.tomek4861.tradetaker.repository;


import com.tomek4861.tradetaker.entity.ApiKey;
import com.tomek4861.tradetaker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Integer> {

    Optional<ApiKey> findByUser(User user);


}
