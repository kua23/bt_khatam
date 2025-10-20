package com.app.product.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.app.product.enums.ProductStatus;
import com.app.product.enums.ProductType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Main Product Entity
 * 
 * A product is a combination of different elements that the bank offers 
 * to its customers in the form of an account.
 */
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==================== BASIC DETAILS ====================
    
    @NotBlank(message = "Product name is required")
    @Column(nullable = false, length = 200)
    private String productName;
    
    @NotBlank(message = "Product code is required")
    @Column(nullable = false, unique = true, length = 50)
    private String productCode;
    
    @NotNull(message = "Product type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ProductType productType;
    
    @Column(length = 1000)
    private String description;
    
    @NotNull(message = "Effective date is required")
    @Column(nullable = false)
    private LocalDate effectiveDate;
    
    @Column
    private LocalDate endDate;
    
    @NotBlank(message = "Bank/Branch code is required")
    @Column(nullable = false, length = 50)
    private String bankBranchCode;
    
    @NotBlank(message = "Currency code is required")
    @Column(nullable = false, length = 3)
    private String currencyCode;  // ISO currency code (USD, EUR, INR, etc.)
    
    @NotNull(message = "Product status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ProductStatus status = ProductStatus.DRAFT;
    
    // ==================== SIMPLE BUSINESS RULES ====================
    
    // Term-related rules (for Fixed Deposits)
    @Column(precision = 10, scale = 2)
    private BigDecimal minTermMonths;  // Minimum term in months
    
    @Column(precision = 10, scale = 2)
    private BigDecimal maxTermMonths;  // Maximum term in months
    
    // Amount-related rules
    @Column(precision = 19, scale = 2)
    private BigDecimal minAmount;  // Minimum deposit amount
    
    @Column(precision = 19, scale = 2)
    private BigDecimal maxAmount;  // Maximum deposit amount
    
    @Column(precision = 19, scale = 2)
    private BigDecimal minBalanceRequired;  // Minimum balance for FD
    
    // Interest/Rate rules
    @Column(precision = 5, scale = 2)
    private BigDecimal baseInterestRate;  // Base rate for the product
    
    @Column(length = 50)
    private String interestCalculationMethod;  // Simple, Compound, Daily, Monthly
    
    @Column(length = 50)
    private String interestPayoutFrequency;  // Monthly, Quarterly, Annually, On-Maturity
    
    // Flags and boolean rules
    @Column
    @Builder.Default
    private Boolean prematureWithdrawalAllowed = false;
    
    @Column
    @Builder.Default
    private Boolean partialWithdrawalAllowed = false;
    
    @Column
    @Builder.Default
    private Boolean loanAgainstDepositAllowed = false;
    
    @Column
    @Builder.Default
    private Boolean autoRenewalAllowed = false;
    
    @Column
    @Builder.Default
    private Boolean nomineeAllowed = true;
    
    @Column
    @Builder.Default
    private Boolean jointAccountAllowed = true;
    
    // Tax-related
    @Column(precision = 5, scale = 2)
    private BigDecimal tdsRate;  // Tax Deduction at Source rate
    
    @Column
    @Builder.Default
    private Boolean tdsApplicable = true;
    
    // ==================== RELATIONSHIPS ====================
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductRole> allowedRoles = new ArrayList<>();
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductCharge> charges = new ArrayList<>();
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InterestRateMatrix> interestRateMatrix = new ArrayList<>();
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductTransactionType> transactionTypes = new ArrayList<>();
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductBalanceType> balanceTypes = new ArrayList<>();
    
    // ==================== AUDIT FIELDS ====================
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    @Column(length = 100)
    private String createdBy;
    
    @Column(length = 100)
    private String updatedBy;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Check if product is active on a given date
     */
    public boolean isActiveOn(LocalDate date) {
        if (status != ProductStatus.ACTIVE) {
            return false;
        }
        if (effectiveDate.isAfter(date)) {
            return false;
        }
        if (endDate != null && endDate.isBefore(date)) {
            return false;
        }
        return true;
    }
    
    /**
     * Check if product is currently active
     */
    public boolean isCurrentlyActive() {
        return isActiveOn(LocalDate.now());
    }
    
    /**
     * Add allowed role to product
     */
    public void addRole(ProductRole role) {
        allowedRoles.add(role);
        role.setProduct(this);
    }
    
    /**
     * Add charge to product
     */
    public void addCharge(ProductCharge charge) {
        charges.add(charge);
        charge.setProduct(this);
    }
    
    /**
     * Add interest rate slab to matrix
     */
    public void addInterestRateSlab(InterestRateMatrix slab) {
        interestRateMatrix.add(slab);
        slab.setProduct(this);
    }
}
