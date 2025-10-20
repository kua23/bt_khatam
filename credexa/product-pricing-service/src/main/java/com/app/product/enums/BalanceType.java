package com.app.product.enums;

/**
 * Balance types for accounts
 */
public enum BalanceType {
    PRINCIPAL("Principal Amount"),
    INTEREST_ACCRUED("Accrued Interest"),
    AVAILABLE_BALANCE("Available Balance"),
    CURRENT_BALANCE("Current Balance"),
    MINIMUM_BALANCE("Minimum Required Balance"),
    HOLD_AMOUNT("Amount on Hold"),
    OVERDRAFT_LIMIT("Overdraft Limit"),
    OUTSTANDING_PRINCIPAL("Outstanding Loan Principal"),
    OUTSTANDING_INTEREST("Outstanding Loan Interest");
    
    private final String description;
    
    BalanceType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
