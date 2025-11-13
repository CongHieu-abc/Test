package com.swp.myleague.model.entities.saleproduct;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
public class Product {
    
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID productId;

    private String productName;
    private String productDescription;

    @Enumerated(EnumType.STRING)
    private ProductSize productSize;

    private Double productPrice;
    private Integer productAmount;
    private String productImgPath;

    @Enumerated(EnumType.STRING)
    private CategoryProduct categoryProduct;

}
