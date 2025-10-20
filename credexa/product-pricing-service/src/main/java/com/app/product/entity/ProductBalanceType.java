package com.app.product.entity;

import com.app.product.enums.BalanceType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Product Balance Type Entity
 * Defines balance types tracked for a product
 */
@Entity
@Table(name = "product_balance_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductBalanceType {

    @Id
    @jakarta.persistence.GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private BalanceType balanceType;

    @Column
    @Builder.Default
    private Boolean tracked = true;

    @Column(length = 500)
    private String description;
}
