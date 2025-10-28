package com.app.fdaccount.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.app.fdaccount.entity.FdAccount;
import com.app.fdaccount.enums.AccountStatus;

/**
 * Repository interface for FdAccount entity
 */
@Repository
public interface FdAccountRepository extends JpaRepository<FdAccount, Long> {

    /**
     * Find account by account number
     */
    Optional<FdAccount> findByAccountNumber(String accountNumber);

    /**
     * Find account by IBAN
     */
    Optional<FdAccount> findByIbanNumber(String ibanNumber);

    /**
     * Check if account number exists
     */
    boolean existsByAccountNumber(String accountNumber);

    /**
     * Check if IBAN exists
     */
    boolean existsByIbanNumber(String ibanNumber);

    /**
     * Find all accounts by status
     */
    List<FdAccount> findByStatus(AccountStatus status);

    /**
     * Find all accounts by product code
     */
    @Query("SELECT DISTINCT a FROM FdAccount a " +
           "LEFT JOIN FETCH a.roles " +
           "WHERE a.productCode = :productCode")
    List<FdAccount> findByProductCode(@Param("productCode") String productCode);

    /**
     * Find all accounts by branch code
     */
    @Query("SELECT DISTINCT a FROM FdAccount a " +
           "LEFT JOIN FETCH a.roles " +
           "WHERE a.branchCode = :branchCode")
    List<FdAccount> findByBranchCode(@Param("branchCode") String branchCode);

    /**
     * Find accounts by customer ID through roles
     */
    @Query("SELECT DISTINCT a FROM FdAccount a " +
           "JOIN FETCH a.roles r " +
           "WHERE r.customerId = :customerId")
    List<FdAccount> findByCustomerId(@Param("customerId") Long customerId);

    /**
     * Find accounts maturing between dates
     */
    @Query("SELECT DISTINCT a FROM FdAccount a " +
           "LEFT JOIN FETCH a.roles " +
           "WHERE a.maturityDate BETWEEN :startDate AND :endDate AND a.status = 'ACTIVE'")
    List<FdAccount> findAccountsMaturingBetween(@Param("startDate") LocalDate startDate, 
                                                  @Param("endDate") LocalDate endDate);

    /**
     * Find accounts maturing on a specific date
     */
    List<FdAccount> findByMaturityDateAndStatus(LocalDate maturityDate, AccountStatus status);

    /**
     * Find all active accounts
     */
    @Query("SELECT a FROM FdAccount a WHERE a.status = 'ACTIVE'")
    List<FdAccount> findAllActiveAccounts();

    /**
     * Search accounts with multiple criteria
     */
    @Query("SELECT a FROM FdAccount a WHERE " +
           "(:accountNumber IS NULL OR a.accountNumber LIKE %:accountNumber%) AND " +
           "(:accountName IS NULL OR LOWER(a.accountName) LIKE LOWER(CONCAT('%', :accountName, '%'))) AND " +
           "(:productCode IS NULL OR a.productCode = :productCode) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:branchCode IS NULL OR a.branchCode = :branchCode) AND " +
           "(:effectiveDateFrom IS NULL OR a.effectiveDate >= :effectiveDateFrom) AND " +
           "(:effectiveDateTo IS NULL OR a.effectiveDate <= :effectiveDateTo) AND " +
           "(:maturityDateFrom IS NULL OR a.maturityDate >= :maturityDateFrom) AND " +
           "(:maturityDateTo IS NULL OR a.maturityDate <= :maturityDateTo)")
    Page<FdAccount> searchAccounts(
            @Param("accountNumber") String accountNumber,
            @Param("accountName") String accountName,
            @Param("productCode") String productCode,
            @Param("status") AccountStatus status,
            @Param("branchCode") String branchCode,
            @Param("effectiveDateFrom") LocalDate effectiveDateFrom,
            @Param("effectiveDateTo") LocalDate effectiveDateTo,
            @Param("maturityDateFrom") LocalDate maturityDateFrom,
            @Param("maturityDateTo") LocalDate maturityDateTo,
            Pageable pageable);

    /**
     * Find accounts by customer ID with pagination
     */
    @Query("SELECT DISTINCT a FROM FdAccount a JOIN a.roles r WHERE r.customerId = :customerId")
    Page<FdAccount> findByCustomerIdPaged(@Param("customerId") Long customerId, Pageable pageable);

    /**
     * Count accounts by status
     */
    long countByStatus(AccountStatus status);

    /**
     * Count accounts by branch
     */
    long countByBranchCode(String branchCode);
}
