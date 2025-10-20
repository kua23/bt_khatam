package com.app.calculator.service;

import com.app.calculator.dto.MonthlyBreakdown;
import com.app.calculator.enums.CompoundingFrequency;
import com.app.calculator.enums.TenureUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for Compound Interest calculations
 * Formula: M = P × (1 + r/n)^(nt)
 * Where n = compounding frequency per year
 */
@Service
@Slf4j
public class CompoundInterestCalculator {
    
    /**
     * Calculate compound interest
     * @param principal Principal amount
     * @param rate Annual interest rate (%)
     * @param tenure Tenure value
     * @param tenureUnit Unit of tenure
     * @param frequency Compounding frequency
     * @return Interest earned
     */
    public BigDecimal calculateInterest(BigDecimal principal, BigDecimal rate,
                                       int tenure, TenureUnit tenureUnit,
                                       CompoundingFrequency frequency) {
        log.debug("Calculating compound interest: P={}, R={}, T={} {}, Freq={}", 
                 principal, rate, tenure, tenureUnit, frequency);
        
        BigDecimal maturityAmount = calculateMaturityAmountBeforeTDS(principal, rate, 
                                                                      tenure, tenureUnit, frequency);
        BigDecimal interest = maturityAmount.subtract(principal);
        
        log.debug("Calculated compound interest: {}", interest);
        return interest;
    }
    
    /**
     * Calculate maturity amount before TDS using compound interest formula
     * Formula: M = P × (1 + r/n)^(nt)
     */
    public BigDecimal calculateMaturityAmountBeforeTDS(BigDecimal principal, BigDecimal rate,
                                                       int tenure, TenureUnit tenureUnit,
                                                       CompoundingFrequency frequency) {
        double tenureInYears = tenureUnit.toYears(tenure);
        int n = frequency.getPeriodsPerYear(); // Compounding periods per year
        
        // Convert rate from percentage to decimal: r/100
        double rateDecimal = rate.doubleValue() / 100.0;
        
        // Formula: M = P × (1 + r/n)^(nt)
        // Where:
        // P = Principal
        // r = Annual interest rate (as decimal)
        // n = Number of compounding periods per year
        // t = Time in years
        
        double ratePerPeriod = rateDecimal / n; // r/n
        double numberOfPeriods = n * tenureInYears; // nt
        
        double compoundFactor = Math.pow(1 + ratePerPeriod, numberOfPeriods);
        double maturityValue = principal.doubleValue() * compoundFactor;
        
        BigDecimal maturityAmount = BigDecimal.valueOf(maturityValue)
            .setScale(2, RoundingMode.HALF_UP);
        
        return maturityAmount;
    }
    
    /**
     * Calculate maturity amount after TDS
     */
    public BigDecimal calculateMaturityAmount(BigDecimal principal, BigDecimal rate,
                                             int tenure, TenureUnit tenureUnit,
                                             CompoundingFrequency frequency,
                                             BigDecimal tdsRate) {
        BigDecimal interest = calculateInterest(principal, rate, tenure, tenureUnit, frequency);
        BigDecimal tds = calculateTDS(interest, tdsRate);
        BigDecimal netInterest = interest.subtract(tds);
        
        return principal.add(netInterest);
    }
    
    /**
     * Calculate TDS on interest
     */
    public BigDecimal calculateTDS(BigDecimal interest, BigDecimal tdsRate) {
        if (tdsRate == null || tdsRate.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return interest
            .multiply(tdsRate)
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
    
    /**
     * Generate monthly breakdown for compound interest
     */
    public List<MonthlyBreakdown> generateMonthlyBreakdown(BigDecimal principal,
                                                           BigDecimal annualRate,
                                                           int tenureMonths,
                                                           CompoundingFrequency frequency,
                                                           LocalDate startDate) {
        List<MonthlyBreakdown> breakdown = new ArrayList<>();
        
        int n = frequency.getPeriodsPerYear();
        double rateDecimal = annualRate.doubleValue() / 100.0;
        double ratePerPeriod = rateDecimal / n;
        
        BigDecimal openingBalance = principal;
        BigDecimal cumulativeInterest = BigDecimal.ZERO;
        
        for (int month = 1; month <= tenureMonths; month++) {
            LocalDate monthEndDate = startDate.plusMonths(month);
            
            // Determine if compounding happens this month
            boolean isCompoundingMonth = switch (frequency) {
                case DAILY -> true; // Approximate daily as continuous
                case MONTHLY -> true; // Every month
                case QUARTERLY -> (month % 3 == 0);
                case SEMI_ANNUALLY -> (month % 6 == 0);
                case ANNUALLY -> (month % 12 == 0);
            };
            
            BigDecimal monthlyInterest;
            BigDecimal closingBalance;
            
            if (isCompoundingMonth || frequency == CompoundingFrequency.DAILY) {
                // Calculate compound interest for the period
                double periodsElapsed = month / (12.0 / n);
                double compoundFactor = Math.pow(1 + ratePerPeriod, periodsElapsed);
                closingBalance = principal.multiply(BigDecimal.valueOf(compoundFactor))
                    .setScale(2, RoundingMode.HALF_UP);
                
                monthlyInterest = closingBalance.subtract(openingBalance);
                cumulativeInterest = closingBalance.subtract(principal);
            } else {
                // Simple interest for non-compounding months
                monthlyInterest = openingBalance
                    .multiply(BigDecimal.valueOf(rateDecimal / 12))
                    .setScale(2, RoundingMode.HALF_UP);
                
                closingBalance = openingBalance.add(monthlyInterest);
                cumulativeInterest = cumulativeInterest.add(monthlyInterest);
            }
            
            MonthlyBreakdown entry = MonthlyBreakdown.builder()
                .month(month)
                .date(monthEndDate)
                .openingBalance(openingBalance)
                .interestEarned(monthlyInterest)
                .closingBalance(closingBalance)
                .cumulativeInterest(cumulativeInterest)
                .build();
            
            breakdown.add(entry);
            openingBalance = closingBalance;
        }
        
        return breakdown;
    }
}
