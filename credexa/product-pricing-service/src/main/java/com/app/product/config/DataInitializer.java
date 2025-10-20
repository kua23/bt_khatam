package com.app.product.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.app.product.dto.CreateProductRequest;
import com.app.product.dto.InterestRateMatrixRequest;
import com.app.product.dto.ProductChargeRequest;
import com.app.product.dto.ProductRoleRequest;
import com.app.product.enums.ChargeFrequency;
import com.app.product.enums.ProductStatus;
import com.app.product.enums.ProductType;
import com.app.product.enums.RoleType;
import com.app.product.service.ProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Data Initializer - Loads default FD products on application startup
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ProductService productService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking if default FD products exist...");
        
        // Check if products already exist
        if (productService.getAllProducts(0, 1, "id", "ASC").getTotalElements() > 0) {
            log.info("Products already exist in database. Skipping initialization.");
            return;
        }
        
        log.info("Loading default FD products...");
        
        try {
            createStandardFixedDeposit();
            createSeniorCitizenFD();
            createTaxSaverFD();
            createCumulativeFD();
            createNonCumulativeFD();
            createFlexiFD();
            
            log.info("Successfully loaded {} default FD products", 6);
        } catch (Exception e) {
            log.error("Error loading default FD products: {}", e.getMessage(), e);
        }
    }

    private void createStandardFixedDeposit() {
        log.info("Creating Standard Fixed Deposit...");
        
        CreateProductRequest request = CreateProductRequest.builder()
                .productName("Standard Fixed Deposit")
                .productCode("FD-STD-001")
                .productType(ProductType.FIXED_DEPOSIT)
                .description("Standard FD with flexible tenure from 6 to 60 months. Suitable for all customers seeking safe returns.")
                .effectiveDate(LocalDate.of(2025, 1, 1))
                .bankBranchCode("HEAD")
                .currencyCode("INR")
                .status(ProductStatus.ACTIVE)
                
                .minTermMonths(BigDecimal.valueOf(6))
                .maxTermMonths(BigDecimal.valueOf(60))
                .minAmount(BigDecimal.valueOf(10000))
                .maxAmount(BigDecimal.valueOf(10000000))
                .minBalanceRequired(BigDecimal.valueOf(10000))
                .baseInterestRate(BigDecimal.valueOf(6.5))
                .interestCalculationMethod("COMPOUND")
                .interestPayoutFrequency("ON_MATURITY")
                
                .prematureWithdrawalAllowed(true)
                .partialWithdrawalAllowed(false)
                .loanAgainstDepositAllowed(true)
                .autoRenewalAllowed(true)
                .nomineeAllowed(true)
                .jointAccountAllowed(true)
                
                .tdsRate(BigDecimal.valueOf(10.0))
                .tdsApplicable(true)
                
                .allowedRoles(Arrays.asList(
                    ProductRoleRequest.builder()
                        .roleType(RoleType.OWNER)
                        .mandatory(true)
                        .minCount(1)
                        .maxCount(1)
                        .description("Primary account holder")
                        .build(),
                    ProductRoleRequest.builder()
                        .roleType(RoleType.NOMINEE)
                        .mandatory(false)
                        .minCount(0)
                        .maxCount(2)
                        .description("Beneficiary in case of death")
                        .build()
                ))
                
                .charges(Arrays.asList(
                    ProductChargeRequest.builder()
                        .chargeName("Premature Withdrawal Penalty")
                        .chargeType("PENALTY")
                        .description("1% penalty for early withdrawal")
                        .percentageRate(BigDecimal.valueOf(1.0))
                        .frequency(ChargeFrequency.ONE_TIME)
                        .waivable(true)
                        .build()
                ))
                
                .interestRateMatrix(Arrays.asList(
                    InterestRateMatrixRequest.builder()
                        .minAmount(BigDecimal.valueOf(10000))
                        .maxAmount(BigDecimal.valueOf(100000))
                        .minTermMonths(BigDecimal.valueOf(6))
                        .maxTermMonths(BigDecimal.valueOf(12))
                        .interestRate(BigDecimal.valueOf(6.5))
                        .effectiveDate(LocalDate.of(2025, 1, 1))
                        .build(),
                    InterestRateMatrixRequest.builder()
                        .minAmount(BigDecimal.valueOf(100000))
                        .maxAmount(BigDecimal.valueOf(1000000))
                        .minTermMonths(BigDecimal.valueOf(6))
                        .maxTermMonths(BigDecimal.valueOf(12))
                        .interestRate(BigDecimal.valueOf(7.0))
                        .additionalRate(BigDecimal.valueOf(0.25))
                        .effectiveDate(LocalDate.of(2025, 1, 1))
                        .remarks("Premium rate for high-value deposits")
                        .build(),
                    InterestRateMatrixRequest.builder()
                        .minAmount(BigDecimal.valueOf(10000))
                        .maxAmount(BigDecimal.valueOf(10000000))
                        .minTermMonths(BigDecimal.valueOf(12))
                        .maxTermMonths(BigDecimal.valueOf(60))
                        .interestRate(BigDecimal.valueOf(7.5))
                        .effectiveDate(LocalDate.of(2025, 1, 1))
                        .build()
                ))
                .build();
        
        productService.createProduct(request);
        log.info("Created: Standard Fixed Deposit");
    }

    private void createSeniorCitizenFD() {
        log.info("Creating Senior Citizen Fixed Deposit...");
        
        CreateProductRequest request = CreateProductRequest.builder()
                .productName("Senior Citizen Fixed Deposit")
                .productCode("FD-SR-001")
                .productType(ProductType.SENIOR_CITIZEN_FD)
                .description("Special FD for senior citizens (60+ years) with higher interest rates and quarterly payouts")
                .effectiveDate(LocalDate.of(2025, 1, 1))
                .bankBranchCode("HEAD")
                .currencyCode("INR")
                .status(ProductStatus.ACTIVE)
                
                .minTermMonths(BigDecimal.valueOf(12))
                .maxTermMonths(BigDecimal.valueOf(120))
                .minAmount(BigDecimal.valueOf(25000))
                .maxAmount(BigDecimal.valueOf(10000000))
                .minBalanceRequired(BigDecimal.valueOf(25000))
                .baseInterestRate(BigDecimal.valueOf(7.5))
                .interestCalculationMethod("COMPOUND")
                .interestPayoutFrequency("QUARTERLY")
                
                .prematureWithdrawalAllowed(true)
                .partialWithdrawalAllowed(false)
                .loanAgainstDepositAllowed(true)
                .autoRenewalAllowed(true)
                .nomineeAllowed(true)
                .jointAccountAllowed(true)
                
                .tdsApplicable(false)
                
                .allowedRoles(Arrays.asList(
                    ProductRoleRequest.builder()
                        .roleType(RoleType.OWNER)
                        .mandatory(true)
                        .minCount(1)
                        .maxCount(1)
                        .description("Senior citizen account holder (60+ years)")
                        .build(),
                    ProductRoleRequest.builder()
                        .roleType(RoleType.NOMINEE)
                        .mandatory(true)
                        .minCount(1)
                        .maxCount(2)
                        .description("Mandatory nominee for senior citizen accounts")
                        .build()
                ))
                
                .interestRateMatrix(Arrays.asList(
                    InterestRateMatrixRequest.builder()
                        .minAmount(BigDecimal.valueOf(25000))
                        .maxAmount(BigDecimal.valueOf(10000000))
                        .minTermMonths(BigDecimal.valueOf(12))
                        .maxTermMonths(BigDecimal.valueOf(36))
                        .customerClassification("SENIOR_CITIZEN")
                        .interestRate(BigDecimal.valueOf(8.0))
                        .additionalRate(BigDecimal.valueOf(0.5))
                        .effectiveDate(LocalDate.of(2025, 1, 1))
                        .remarks("Special rate for senior citizens")
                        .build(),
                    InterestRateMatrixRequest.builder()
                        .minAmount(BigDecimal.valueOf(25000))
                        .maxAmount(BigDecimal.valueOf(10000000))
                        .minTermMonths(BigDecimal.valueOf(36))
                        .maxTermMonths(BigDecimal.valueOf(120))
                        .customerClassification("SENIOR_CITIZEN")
                        .interestRate(BigDecimal.valueOf(8.5))
                        .additionalRate(BigDecimal.valueOf(0.5))
                        .effectiveDate(LocalDate.of(2025, 1, 1))
                        .remarks("Higher rate for longer tenure")
                        .build()
                ))
                .build();
        
        productService.createProduct(request);
        log.info("Created: Senior Citizen Fixed Deposit");
    }

    private void createTaxSaverFD() {
        log.info("Creating Tax Saver Fixed Deposit...");
        
        CreateProductRequest request = CreateProductRequest.builder()
                .productName("Tax Saver Fixed Deposit")
                .productCode("FD-TAX-001")
                .productType(ProductType.TAX_SAVER_FD)
                .description("5-year lock-in FD with tax benefits under Section 80C (up to ₹1.5 Lakh)")
                .effectiveDate(LocalDate.of(2025, 1, 1))
                .bankBranchCode("HEAD")
                .currencyCode("INR")
                .status(ProductStatus.ACTIVE)
                
                .minTermMonths(BigDecimal.valueOf(60))
                .maxTermMonths(BigDecimal.valueOf(60))
                .minAmount(BigDecimal.valueOf(10000))
                .maxAmount(BigDecimal.valueOf(1500000))
                .minBalanceRequired(BigDecimal.valueOf(10000))
                .baseInterestRate(BigDecimal.valueOf(7.0))
                .interestCalculationMethod("COMPOUND")
                .interestPayoutFrequency("ON_MATURITY")
                
                .prematureWithdrawalAllowed(false)
                .partialWithdrawalAllowed(false)
                .loanAgainstDepositAllowed(false)
                .autoRenewalAllowed(false)
                .nomineeAllowed(true)
                .jointAccountAllowed(false)
                
                .tdsRate(BigDecimal.valueOf(10.0))
                .tdsApplicable(true)
                
                .allowedRoles(Arrays.asList(
                    ProductRoleRequest.builder()
                        .roleType(RoleType.OWNER)
                        .mandatory(true)
                        .minCount(1)
                        .maxCount(1)
                        .description("Individual account holder only (no joint accounts)")
                        .build()
                ))
                
                .interestRateMatrix(Arrays.asList(
                    InterestRateMatrixRequest.builder()
                        .minAmount(BigDecimal.valueOf(10000))
                        .maxAmount(BigDecimal.valueOf(1500000))
                        .minTermMonths(BigDecimal.valueOf(60))
                        .maxTermMonths(BigDecimal.valueOf(60))
                        .interestRate(BigDecimal.valueOf(7.0))
                        .effectiveDate(LocalDate.of(2025, 1, 1))
                        .remarks("Fixed 5-year tenure for Section 80C tax benefits")
                        .build()
                ))
                .build();
        
        productService.createProduct(request);
        log.info("Created: Tax Saver Fixed Deposit");
    }

    private void createCumulativeFD() {
        log.info("Creating Cumulative Fixed Deposit...");
        
        CreateProductRequest request = CreateProductRequest.builder()
                .productName("Cumulative Fixed Deposit")
                .productCode("FD-CUM-001")
                .productType(ProductType.CUMULATIVE_FD)
                .description("Interest compounded quarterly and paid at maturity for maximum returns")
                .effectiveDate(LocalDate.of(2025, 1, 1))
                .bankBranchCode("HEAD")
                .currencyCode("INR")
                .status(ProductStatus.ACTIVE)
                
                .minTermMonths(BigDecimal.valueOf(12))
                .maxTermMonths(BigDecimal.valueOf(120))
                .minAmount(BigDecimal.valueOf(50000))
                .maxAmount(BigDecimal.valueOf(50000000))
                .minBalanceRequired(BigDecimal.valueOf(50000))
                .baseInterestRate(BigDecimal.valueOf(6.75))
                .interestCalculationMethod("COMPOUND")
                .interestPayoutFrequency("ON_MATURITY")
                
                .prematureWithdrawalAllowed(true)
                .partialWithdrawalAllowed(false)
                .loanAgainstDepositAllowed(true)
                .autoRenewalAllowed(true)
                .nomineeAllowed(true)
                .jointAccountAllowed(true)
                
                .tdsRate(BigDecimal.valueOf(10.0))
                .tdsApplicable(true)
                
                .allowedRoles(Arrays.asList(
                    ProductRoleRequest.builder()
                        .roleType(RoleType.OWNER)
                        .mandatory(true)
                        .minCount(1)
                        .maxCount(2)
                        .description("Single or joint account")
                        .build(),
                    ProductRoleRequest.builder()
                        .roleType(RoleType.CO_OWNER)
                        .mandatory(false)
                        .minCount(0)
                        .maxCount(1)
                        .description("Joint account holder")
                        .build()
                ))
                
                .interestRateMatrix(Arrays.asList(
                    InterestRateMatrixRequest.builder()
                        .minAmount(BigDecimal.valueOf(50000))
                        .maxAmount(BigDecimal.valueOf(50000000))
                        .minTermMonths(BigDecimal.valueOf(12))
                        .maxTermMonths(BigDecimal.valueOf(36))
                        .interestRate(BigDecimal.valueOf(7.0))
                        .effectiveDate(LocalDate.of(2025, 1, 1))
                        .build(),
                    InterestRateMatrixRequest.builder()
                        .minAmount(BigDecimal.valueOf(50000))
                        .maxAmount(BigDecimal.valueOf(50000000))
                        .minTermMonths(BigDecimal.valueOf(36))
                        .maxTermMonths(BigDecimal.valueOf(120))
                        .interestRate(BigDecimal.valueOf(7.75))
                        .effectiveDate(LocalDate.of(2025, 1, 1))
                        .remarks("Higher rate for long-term deposits")
                        .build()
                ))
                .build();
        
        productService.createProduct(request);
        log.info("Created: Cumulative Fixed Deposit");
    }

    private void createNonCumulativeFD() {
        log.info("Creating Non-Cumulative Fixed Deposit...");
        
        CreateProductRequest request = CreateProductRequest.builder()
                .productName("Non-Cumulative Fixed Deposit")
                .productCode("FD-NCUM-001")
                .productType(ProductType.NON_CUMULATIVE_FD)
                .description("Interest paid monthly for regular income. Ideal for retirees and pension seekers.")
                .effectiveDate(LocalDate.of(2025, 1, 1))
                .bankBranchCode("HEAD")
                .currencyCode("INR")
                .status(ProductStatus.ACTIVE)
                
                .minTermMonths(BigDecimal.valueOf(12))
                .maxTermMonths(BigDecimal.valueOf(60))
                .minAmount(BigDecimal.valueOf(100000))
                .maxAmount(BigDecimal.valueOf(10000000))
                .minBalanceRequired(BigDecimal.valueOf(100000))
                .baseInterestRate(BigDecimal.valueOf(6.5))
                .interestCalculationMethod("SIMPLE")
                .interestPayoutFrequency("MONTHLY")
                
                .prematureWithdrawalAllowed(true)
                .partialWithdrawalAllowed(false)
                .loanAgainstDepositAllowed(true)
                .autoRenewalAllowed(false)
                .nomineeAllowed(true)
                .jointAccountAllowed(true)
                
                .tdsRate(BigDecimal.valueOf(10.0))
                .tdsApplicable(true)
                
                .allowedRoles(Arrays.asList(
                    ProductRoleRequest.builder()
                        .roleType(RoleType.OWNER)
                        .mandatory(true)
                        .minCount(1)
                        .maxCount(1)
                        .description("Primary account holder")
                        .build()
                ))
                
                .charges(Arrays.asList(
                    ProductChargeRequest.builder()
                        .chargeName("Monthly Payout Processing Fee")
                        .chargeType("FEE")
                        .description("Processing fee for monthly interest payout")
                        .fixedAmount(BigDecimal.valueOf(50))
                        .frequency(ChargeFrequency.MONTHLY)
                        .waivable(false)
                        .build()
                ))
                
                .interestRateMatrix(Arrays.asList(
                    InterestRateMatrixRequest.builder()
                        .minAmount(BigDecimal.valueOf(100000))
                        .maxAmount(BigDecimal.valueOf(10000000))
                        .minTermMonths(BigDecimal.valueOf(12))
                        .maxTermMonths(BigDecimal.valueOf(60))
                        .interestRate(BigDecimal.valueOf(6.5))
                        .effectiveDate(LocalDate.of(2025, 1, 1))
                        .build()
                ))
                .build();
        
        productService.createProduct(request);
        log.info("Created: Non-Cumulative Fixed Deposit");
    }

    private void createFlexiFD() {
        log.info("Creating Flexi Fixed Deposit...");
        
        CreateProductRequest request = CreateProductRequest.builder()
                .productName("Flexi Fixed Deposit")
                .productCode("FD-FLEXI-001")
                .productType(ProductType.FLEXI_FD)
                .description("Auto-sweep FD with liquidity and FD benefits. Best of both worlds - savings and fixed deposit.")
                .effectiveDate(LocalDate.of(2025, 1, 1))
                .bankBranchCode("HEAD")
                .currencyCode("INR")
                .status(ProductStatus.ACTIVE)
                
                .minTermMonths(BigDecimal.valueOf(6))
                .maxTermMonths(BigDecimal.valueOf(36))
                .minAmount(BigDecimal.valueOf(25000))
                .maxAmount(BigDecimal.valueOf(5000000))
                .minBalanceRequired(BigDecimal.valueOf(25000))
                .baseInterestRate(BigDecimal.valueOf(6.0))
                .interestCalculationMethod("COMPOUND")
                .interestPayoutFrequency("ON_MATURITY")
                
                .prematureWithdrawalAllowed(true)
                .partialWithdrawalAllowed(true)
                .loanAgainstDepositAllowed(true)
                .autoRenewalAllowed(true)
                .nomineeAllowed(true)
                .jointAccountAllowed(true)
                
                .tdsRate(BigDecimal.valueOf(10.0))
                .tdsApplicable(true)
                
                .allowedRoles(Arrays.asList(
                    ProductRoleRequest.builder()
                        .roleType(RoleType.OWNER)
                        .mandatory(true)
                        .minCount(1)
                        .maxCount(1)
                        .description("Primary account holder")
                        .build()
                ))
                
                .charges(Arrays.asList(
                    ProductChargeRequest.builder()
                        .chargeName("Auto-Sweep Service Fee")
                        .chargeType("FEE")
                        .description("Annual fee for auto-sweep facility")
                        .fixedAmount(BigDecimal.valueOf(500))
                        .frequency(ChargeFrequency.ANNUALLY)
                        .waivable(true)
                        .build()
                ))
                
                .interestRateMatrix(Arrays.asList(
                    InterestRateMatrixRequest.builder()
                        .minAmount(BigDecimal.valueOf(25000))
                        .maxAmount(BigDecimal.valueOf(5000000))
                        .minTermMonths(BigDecimal.valueOf(6))
                        .maxTermMonths(BigDecimal.valueOf(36))
                        .interestRate(BigDecimal.valueOf(6.25))
                        .effectiveDate(LocalDate.of(2025, 1, 1))
                        .build()
                ))
                .build();
        
        productService.createProduct(request);
        log.info("Created: Flexi Fixed Deposit");
    }
}
