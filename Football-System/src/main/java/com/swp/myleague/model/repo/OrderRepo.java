package com.swp.myleague.model.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swp.myleague.model.entities.saleproduct.Orders;

@Repository
public interface OrderRepo extends JpaRepository<Orders, UUID> {

    List<Orders> findAllByUserUserId(UUID fromString);

    Optional<Orders> findByOrderCode(Long orderCode);
    
}
