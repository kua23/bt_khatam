package com.app.calculator.service;

import com.app.calculator.dto.*;
import com.app.calculator.dto.external.InterestRateDto;
import com.app.calculator.dto.external.ProductDto;
import com.app.calculator.enums.CalculationType;
import com.app.calculator.enums.CompoundingFrequency;
import com.app.calculator.enums.TenureUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Main FD Calculator Service
 * Orchestrates calculation logic for both standalone and product-based calculations
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FdCalculatorService {
    
    private final SimpleInterestCalculator simpleInterestCalculator;
    private final CompoundInterestCalculator compoundInterestCalculator;
    private final ProductIntegrationService productIntegrationService;
    private final CustomerIntegrationService customerIntegrationService;
    
    /**
     * Calculate FD with standalone inputs (no product)
     */
    public CalculationResponse calculateStandalone(StandaloneCalculationRequest request) {
        log.info("Processing standalone calculation for principal: {}", request.getPrincipalAmount());
        
        // Apply customer classification bonuses
        BigDecimal finalRate = request.getInterestRate();
        BigDecimal additionalRate = BigDecimal.ZERO;
        
        if (request.getCustomerClassifications() != null && !request.getCustomerClassifications().isEmpty()) {
            additionalRate = calculateAdditionalRate(request.getCustomerClassifications(), request.getInterestRate());
            finalRate = finalRate.add(additionalRate);
        }
        
        // Calculate interest based on type
        BigDecimal interest;
        BigDecimal maturityBeforeTDS;
        List<MonthlyBreakdown> breakdown = null;
        
        int tenureInMonths = request.getTenureUnit().toMonths(request.getTenure());
        LocalDate startDate = LocalDate.now();
        LocalDate maturityDate = calculateMaturityDate(startDate, request.getTenure(), request.getTenureUnit());
        
        if (request.getCalculationType() == CalculationType.SIMPLE) {
            interest = simpleInterestCalculator.calculateInterest(
                request.getPrincipalAmount(),
                finalRate,
                request.getTenure(),
                request.getTenureUnit()
            );
            
            if (tenureInMonths > 0 && tenureInMonths <= 120) {
                breakdown = simpleInterestCalculator.generateMonthlyBreakdown(
                    request.getPrincipalAmount(),
                    finalRate,
                    tenureInMonths,
                    startDate
                );
            }
        } else {
            CompoundingFrequency frequency = request.getCompoundingFrequency() != null 
                ? request.getCompoundingFrequency() 
                : CompoundingFrequency.QUARTERLY;
            
            interest = compoundInterestCalculator.calculateInterest(
                request.getPrincipalAmount(),
                finalRate,
                request.getTenure(),
                request.getTenureUnit(),
                frequency
            );
            
            if (tenureInMonths > 0 && tenureInMonths <= 120) {
                breakdown = compoundInterestCalculator.generateMonthlyBreakdown(
                    request.getPrincipalAmount(),
                    finalRate,
                    tenureInMonths,
                    frequency,
                    startDate
                );
            }
        }
        
        // Calculate TDS
        BigDecimal tdsRate = request.getTdsRate() != null ? request.getTdsRate() : BigDecimal.ZERO;
        BigDecimal tdsAmount = interest.multiply(tdsRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal netInterest = interest.subtract(tdsAmount);
        BigDecimal maturityAmount = request.getPrincipalAmount().add(netInterest);
        
        return CalculationResponse.builder()
            .principalAmount(request.getPrincipalAmount())
            .interestRate(finalRate)
            .baseInterestRate(request.getInterestRate())
            .additionalInterestRate(additionalRate)
            .tenure(request.getTenure())
            .tenureUnit(request.getTenureUnit())
            .tenureInYears(request.getTenureUnit().toYears(request.getTenure()))
            .calculationType(request.getCalculationType())
            .compoundingFrequency(request.getCompoundingFrequency())
            .interestEarned(interest)
            .tdsAmount(tdsAmount)
            .tdsRate(tdsRate)
            .maturityAmount(maturityAmount)
            .netInterest(netInterest)
            .startDate(startDate)
            .maturityDate(maturityDate)
            .customerClassifications(request.getCustomerClassifications())
            .monthlyBreakdown(breakdown)
            .build();
    }
    
    /**
     * Calculate FD using product defaults
     */
    public CalculationResponse calculateWithProduct(ProductBasedCalculationRequest request) {
        log.info("Processing product-based calculation for product ID: {}", request.getProductId());
        
        // Fetch product details
        ProductDto product = productIntegrationService.getProduct(request.getProductId());
        
        // Validate principal amount against product limits
        validatePrincipalAmount(request.getPrincipalAmount(), product);
        
        // Validate tenure against product limits
        int tenureInMonths = request.getTenureUnit().toMonths(request.getTenure());
        validateTenure(tenureInMonths, product);
        
        // Determine interest rate
        BigDecimal baseRate = product.getBaseInterestRate();
        List<String> classifications = resolveCustomerClassifications(request);
        
        // Try to get rate from product-pricing-service
        InterestRateDto applicableRate = productIntegrationService.getApplicableRate(
            request.getProductId(),
            request.getPrincipalAmount(),
            tenureInMonths,
            classifications.isEmpty() ? null : classifications.get(0)
        );
        
        if (applicableRate != null) {
            baseRate = applicableRate.getTotalRate() != null 
                ? applicableRate.getTotalRate() 
                : applicableRate.getInterestRate();
        }
        
        // Apply custom rate if provided (with capping)
        BigDecimal finalRate = baseRate;
        BigDecimal additionalRate = BigDecimal.ZERO;
        
        if (request.getCustomInterestRate() != null) {
            BigDecimal maxAllowedRate = baseRate.add(BigDecimal.valueOf(2.0)); // Max 2% additional
            if (request.getCustomInterestRate().compareTo(maxAllowedRate) <= 0) {
                additionalRate = request.getCustomInterestRate().subtract(baseRate);
                finalRate = request.getCustomInterestRate();
            } else {
                log.warn("Custom rate {} exceeds maximum allowed rate {}. Using capped rate.", 
                        request.getCustomInterestRate(), maxAllowedRate);
                finalRate = maxAllowedRate;
                additionalRate = BigDecimal.valueOf(2.0);
            }
        } else if (!classifications.isEmpty()) {
            additionalRate = calculateAdditionalRate(classifications, baseRate);
            finalRate = finalRate.add(additionalRate);
        }
        
        // Determine calculation type
        CalculationType calcType = request.getCalculationType() != null
            ? request.getCalculationType()
            : determineCalculationType(product.getInterestCalculationMethod());
        
        // Determine compounding frequency
        CompoundingFrequency frequency = request.getCompoundingFrequency() != null
            ? request.getCompoundingFrequency()
            : determineCompoundingFrequency(product.getInterestPayoutFrequency());
        
        // Calculate interest
        BigDecimal interest;
        List<MonthlyBreakdown> breakdown = null;
        LocalDate startDate = LocalDate.now();
        LocalDate maturityDate = calculateMaturityDate(startDate, request.getTenure(), request.getTenureUnit());
        
        if (calcType == CalculationType.SIMPLE) {
            interest = simpleInterestCalculator.calculateInterest(
                request.getPrincipalAmount(),
                finalRate,
                request.getTenure(),
                request.getTenureUnit()
            );
            
            if (tenureInMonths > 0 && tenureInMonths <= 120) {
                breakdown = simpleInterestCalculator.generateMonthlyBreakdown(
                    request.getPrincipalAmount(),
                    finalRate,
                    tenureInMonths,
                    startDate
                );
            }
        } else {
            interest = compoundInterestCalculator.calculateInterest(
                request.getPrincipalAmount(),
                finalRate,
                request.getTenure(),
                request.getTenureUnit(),
                frequency
            );
            
            if (tenureInMonths > 0 && tenureInMonths <= 120) {
                breakdown = compoundInterestCalculator.generateMonthlyBreakdown(
                    request.getPrincipalAmount(),
                    finalRate,
                    tenureInMonths,
                    frequency,
                    startDate
                );
            }
        }
        
        // Calculate TDS
        Boolean applyTds = request.getApplyTds() != null ? request.getApplyTds() : product.getTdsApplicable();
        BigDecimal tdsRate = (applyTds != null && applyTds && product.getTdsRate() != null) 
            ? product.getTdsRate() 
            : BigDecimal.ZERO;
        BigDecimal tdsAmount = interest.multiply(tdsRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal netInterest = interest.subtract(tdsAmount);
        BigDecimal maturityAmount = request.getPrincipalAmount().add(netInterest);
        
        return CalculationResponse.builder()
            .principalAmount(request.getPrincipalAmount())
            .interestRate(finalRate)
            .baseInterestRate(baseRate)
            .additionalInterestRate(additionalRate)
            .tenure(request.getTenure())
            .tenureUnit(request.getTenureUnit())
            .tenureInYears(request.getTenureUnit().toYears(request.getTenure()))
            .calculationType(calcType)
            .compoundingFrequency(frequency)
            .interestEarned(interest)
            .tdsAmount(tdsAmount)
            .tdsRate(tdsRate)
            .maturityAmount(maturityAmount)
            .netInterest(netInterest)
            .startDate(startDate)
            .maturityDate(maturityDate)
            .productId(product.getId())
            .productName(product.getProductName())
            .productCode(product.getProductCode())
            .customerClassifications(classifications)
            .monthlyBreakdown(breakdown)
            .build();
    }
    
    /**
     * Compare multiple FD scenarios
     */
    public ComparisonResponse compareScenarios(ComparisonRequest request) {
        log.info("Comparing {} FD scenarios", request.getScenarios().size());
        
        List<CalculationResponse> results = new ArrayList<>();
        CalculationResponse bestScenario = null;
        int bestIndex = 0;
        
        for (int i = 0; i < request.getScenarios().size(); i++) {
            StandaloneCalculationRequest scenario = request.getScenarios().get(i);
            
            // Override principal if common principal is provided
            if (request.getCommonPrincipal() != null) {
                scenario.setPrincipalAmount(request.getCommonPrincipal());
            }
            
            CalculationResponse result = calculateStandalone(scenario);
            results.add(result);
            
            // Track best scenario
            if (bestScenario == null || 
                result.getMaturityAmount().compareTo(bestScenario.getMaturityAmount()) > 0) {
                bestScenario = result;
                bestIndex = i;
            }
        }
        
        return ComparisonResponse.builder()
            .scenarios(results)
            .bestScenario(bestScenario)
            .bestScenarioIndex(bestIndex)
            .build();
    }
    
    // Helper methods
    
    private List<String> resolveCustomerClassifications(ProductBasedCalculationRequest request) {
        List<String> classifications = new ArrayList<>();
        
        if (request.getCustomerId() != null) {
            try {
                String classification = customerIntegrationService.getCustomerClassification(request.getCustomerId());
                if (classification != null) {
                    classifications.add(classification);
                }
            } catch (Exception e) {
                log.warn("Failed to fetch customer classification: {}", e.getMessage());
            }
        }
        
        if (request.getCustomerClassifications() != null) {
            classifications.addAll(request.getCustomerClassifications());
        }
        
        // Remove duplicates and limit to 2
        return classifications.stream().distinct().limit(2).toList();
    }
    
    private BigDecimal calculateAdditionalRate(List<String> classifications, BigDecimal baseRate) {
        // Simple logic: 0.25% per classification, max 2% total
        BigDecimal additionalRate = BigDecimal.valueOf(classifications.size() * 0.25);
        BigDecimal maxAdditional = BigDecimal.valueOf(2.0);
        
        return additionalRate.min(maxAdditional);
    }
    
    private void validatePrincipalAmount(BigDecimal amount, ProductDto product) {
        if (product.getMinAmount() != null && amount.compareTo(product.getMinAmount()) < 0) {
            throw new IllegalArgumentException(
                String.format("Principal amount ₹%s is below minimum ₹%s for product %s",
                    amount, product.getMinAmount(), product.getProductCode()));
        }
        
        if (product.getMaxAmount() != null && amount.compareTo(product.getMaxAmount()) > 0) {
            throw new IllegalArgumentException(
                String.format("Principal amount ₹%s exceeds maximum ₹%s for product %s",
                    amount, product.getMaxAmount(), product.getProductCode()));
        }
    }
    
    private void validateTenure(int tenureMonths, ProductDto product) {
        if (product.getMinTermMonths() != null && tenureMonths < product.getMinTermMonths()) {
            throw new IllegalArgumentException(
                String.format("Tenure %d months is below minimum %d months for product %s",
                    tenureMonths, product.getMinTermMonths(), product.getProductCode()));
        }
        
        if (product.getMaxTermMonths() != null && tenureMonths > product.getMaxTermMonths()) {
            throw new IllegalArgumentException(
                String.format("Tenure %d months exceeds maximum %d months for product %s",
                    tenureMonths, product.getMaxTermMonths(), product.getProductCode()));
        }
    }
    
    private CalculationType determineCalculationType(String method) {
        if (method == null) {
            return CalculationType.COMPOUND;
        }
        
        return method.toUpperCase().contains("SIMPLE") 
            ? CalculationType.SIMPLE 
            : CalculationType.COMPOUND;
    }
    
    private CompoundingFrequency determineCompoundingFrequency(String frequency) {
        if (frequency == null) {
            return CompoundingFrequency.QUARTERLY;
        }
        
        return switch (frequency.toUpperCase()) {
            case "DAILY" -> CompoundingFrequency.DAILY;
            case "MONTHLY" -> CompoundingFrequency.MONTHLY;
            case "QUARTERLY" -> CompoundingFrequency.QUARTERLY;
            case "SEMI_ANNUALLY", "HALF_YEARLY" -> CompoundingFrequency.SEMI_ANNUALLY;
            case "ANNUALLY", "YEARLY" -> CompoundingFrequency.ANNUALLY;
            default -> CompoundingFrequency.QUARTERLY;
        };
    }
    
    private LocalDate calculateMaturityDate(LocalDate startDate, int tenure, TenureUnit unit) {
        return switch (unit) {
            case DAYS -> startDate.plusDays(tenure);
            case MONTHS -> startDate.plusMonths(tenure);
            case YEARS -> startDate.plusYears(tenure);
        };
    }
}
