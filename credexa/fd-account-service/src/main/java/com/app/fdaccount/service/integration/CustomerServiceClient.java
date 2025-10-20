package com.app.fdaccount.service.integration;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.app.fdaccount.dto.external.CustomerDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service to integrate with customer-service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${integration.customer-service.url}")
    private String customerServiceUrl;

    @Value("${integration.customer-service.timeout:5000}")
    private int timeout;

    /**
     * Get customer by customer ID
     * Cached to reduce external calls
     */
    @Cacheable(value = "customers", key = "#customerId")
    public CustomerDto getCustomerById(Long customerId) {
        log.debug("Fetching customer: {}", customerId);

        try {
            CustomerDto customer = webClientBuilder.build()
                    .get()
                    .uri(customerServiceUrl + "/{customerId}", customerId)
                    .retrieve()
                    .bodyToMono(CustomerDto.class)
                    .timeout(Duration.ofMillis(timeout))
                    .block();

            if (customer == null) {
                throw new RuntimeException("Customer not found: " + customerId);
            }

            if (!Boolean.TRUE.equals(customer.getIsActive())) {
                throw new RuntimeException("Customer is not active: " + customerId);
            }

            log.info("✅ Fetched customer: {} - {}", customerId, customer.getCustomerName());
            return customer;

        } catch (Exception e) {
            log.error("❌ Failed to fetch customer: {}", customerId, e);
            throw new RuntimeException("Failed to fetch customer details: " + e.getMessage(), e);
        }
    }

    /**
     * Validate customer exists and is active
     */
    public boolean validateCustomer(Long customerId) {
        try {
            CustomerDto customer = getCustomerById(customerId);
            return customer != null && Boolean.TRUE.equals(customer.getIsActive());
        } catch (Exception e) {
            log.warn("Customer validation failed for: {}", customerId);
            return false;
        }
    }

    /**
     * Check if customer has completed KYC
     */
    public boolean isKycCompleted(Long customerId) {
        try {
            CustomerDto customer = getCustomerById(customerId);
            return customer != null && "COMPLETED".equalsIgnoreCase(customer.getKycStatus());
        } catch (Exception e) {
            log.warn("Could not check KYC status for: {}", customerId);
            return false;
        }
    }

    /**
     * Get customer classification
     */
    public String getCustomerClassification(Long customerId) {
        try {
            CustomerDto customer = getCustomerById(customerId);
            return customer != null ? customer.getCustomerClassification() : null;
        } catch (Exception e) {
            log.warn("Could not get classification for customer: {}", customerId);
            return null;
        }
    }
}
