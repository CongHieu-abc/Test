package com.swp.myleague.model.repo;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.swp.myleague.model.entities.saleproduct.Product;

@Repository
public interface ProductRepo extends JpaRepository<Product, UUID> {
    
    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.productAmount = p.productAmount - :amount WHERE p.productId = :productId AND p.productAmount >= :amount")
    int decreaseStock(@Param("productId") UUID productId, @Param("amount") int amount);

}
