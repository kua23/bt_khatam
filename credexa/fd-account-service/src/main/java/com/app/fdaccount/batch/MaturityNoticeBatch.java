package com.app.fdaccount.batch;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.app.fdaccount.entity.AccountRole;
import com.app.fdaccount.entity.FdAccount;
import com.app.fdaccount.enums.AccountStatus;
import com.app.fdaccount.repository.FdAccountRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Batch job for sending maturity notices
 * Runs at 2:00 AM daily (after maturity processing)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MaturityNoticeBatch {

    private final FdAccountRepository accountRepository;

    @Value("${batch.maturity-notice.days-before:10}")
    private int daysBeforeMaturity;

    @Value("${alert.sms.enabled:true}")
    private boolean smsEnabled;

    @Value("${alert.email.enabled:true}")
    private boolean emailEnabled;

    /**
     * Send maturity notices for accounts maturing in N days
     * Scheduled to run at 2:00 AM daily
     */
    @Scheduled(cron = "${batch.maturity-notice.cron:0 0 2 * * ?}")
    public void sendMaturityNotices() {
        log.info("🕐 Starting maturity notice batch...");

        LocalDate today = LocalDate.now();
        LocalDate noticeDate = today.plusDays(daysBeforeMaturity);
        long startTime = System.currentTimeMillis();

        // Get accounts maturing on the notice date
        List<FdAccount> upcomingMaturityAccounts = accountRepository.findByMaturityDateAndStatus(
                noticeDate, AccountStatus.ACTIVE);

        log.info("Found {} accounts maturing on {} ({} days from today)",
                upcomingMaturityAccounts.size(), noticeDate, daysBeforeMaturity);

        int successCount = 0;
        int errorCount = 0;

        for (FdAccount account : upcomingMaturityAccounts) {
            try {
                sendMaturityNotice(account);
                successCount++;
            } catch (Exception e) {
                log.error("❌ Error sending maturity notice for account: {}",
                        account.getAccountNumber(), e);
                errorCount++;
            }
        }

        long duration = System.currentTimeMillis() - startTime;

        log.info("✅ Maturity notice batch completed in {}ms - Success: {}, Errors: {}",
                duration, successCount, errorCount);
    }

    /**
     * Send maturity notice for a single account
     */
    private void sendMaturityNotice(FdAccount account) {
        log.info("Sending maturity notice for account: {}", account.getAccountNumber());

        // Get primary owner
        AccountRole primaryOwner = account.getRoles().stream()
                .filter(role -> Boolean.TRUE.equals(role.getIsPrimary()) &&
                        Boolean.TRUE.equals(role.getIsActive()))
                .findFirst()
                .orElse(account.getRoles().stream()
                        .filter(AccountRole::getIsActive)
                        .findFirst()
                        .orElse(null));

        if (primaryOwner == null) {
            log.warn("No active owner found for account: {}", account.getAccountNumber());
            return;
        }

        // Format notice content
        String noticeContent = buildNoticeContent(account);

        // Send SMS if enabled
        if (smsEnabled) {
            sendSMS(primaryOwner.getCustomerId(), primaryOwner.getCustomerName(), noticeContent);
        }

        // Send Email if enabled
        if (emailEnabled) {
            sendEmail(primaryOwner.getCustomerId(), primaryOwner.getCustomerName(), noticeContent);
        }

        log.info("✅ Sent maturity notice to customer {} for account: {}",
                primaryOwner.getCustomerId(), account.getAccountNumber());
    }

    /**
     * Build notice content
     */
    private String buildNoticeContent(FdAccount account) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

        StringBuilder content = new StringBuilder();
        content.append("Dear Customer,\n\n");
        content.append("This is to inform you that your Fixed Deposit account will mature soon.\n\n");
        content.append("Account Details:\n");
        content.append(String.format("Account Number: %s\n", account.getAccountNumber()));
        content.append(String.format("Account Name: %s\n", account.getAccountName()));
        content.append(String.format("Principal Amount: %.2f\n", account.getPrincipalAmount()));
        content.append(String.format("Interest Rate: %.2f%%\n",
                account.getCustomInterestRate() != null ?
                        account.getCustomInterestRate() : account.getInterestRate()));
        content.append(String.format("Maturity Date: %s\n",
                account.getMaturityDate().format(formatter)));
        content.append(String.format("Maturity Amount: %.2f\n", account.getMaturityAmount()));
        content.append(String.format("Maturity Instruction: %s\n",
                account.getMaturityInstruction() != null ?
                        account.getMaturityInstruction().toString() : "HOLD"));

        content.append("\nPlease contact us if you wish to modify the maturity instructions.\n\n");
        content.append("Thank you for banking with us.\n");
        content.append("Regards,\n");
        content.append("Fixed Deposit Department");

        return content.toString();
    }

    /**
     * Send SMS notification (mock implementation)
     */
    private void sendSMS(Long customerId, String customerName, String message) {
        // In production, this would integrate with SMS gateway
        log.info("📱 SMS sent to customer {} ({}): {}",
                customerId, customerName, message.substring(0, Math.min(50, message.length())) + "...");

        // Mock SMS sending
        // smsGateway.send(customerPhone, message);
    }

    /**
     * Send Email notification (mock implementation)
     */
    private void sendEmail(Long customerId, String customerName, String message) {
        // In production, this would integrate with email service
        log.info("📧 Email sent to customer {} ({}): {}",
                customerId, customerName, message.substring(0, Math.min(50, message.length())) + "...");

        // Mock email sending
        // emailService.send(customerEmail, "FD Maturity Notice", message);
    }
}
