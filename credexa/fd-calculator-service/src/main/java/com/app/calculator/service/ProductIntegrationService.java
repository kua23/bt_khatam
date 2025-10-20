package com.app.calculator.service;

import com.app.calculator.dto.external.InterestRateDto;
import com.app.calculator.dto.external.ProductDto;
import com.app.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

/**
 * Service for integrating with product-pricing-service
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProductIntegrationService {
    
    private final WebClient.Builder webClientBuilder;
    
    @Value("${services.product-pricing.url}")
    private String productPricingUrl;
    
    /**
     * Get product details by ID (cached)
     */
    @Cacheable(value = "products", key = "#productId")
    public ProductDto getProduct(Long productId) {
        log.info("Fetching product details for ID: {}", productId);
        
        try {
            WebClient webClient = webClientBuilder.baseUrl(productPricingUrl).build();
            
            ApiResponse<ProductDto> response = webClient.get()
                .uri("/{id}", productId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<ProductDto>>() {})
                .block();
            
            if (response != null && response.isSuccess() && response.getData() != null) {
                log.debug("Successfully fetched product: {}", response.getData().getProductCode());
                return response.getData();
            }
            
            throw new RuntimeException("Product not found with ID: " + productId);
        } catch (Exception e) {
            log.error("Failed to fetch product {}: {}", productId, e.getMessage());
            throw new RuntimeException("Unable to fetch product details: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get applicable interest rate (cached)
     */
    @Cacheable(value = "interestRates", key = "#productId + '-' + #amount + '-' + #termMonths + '-' + #classification")
    public InterestRateDto getApplicableRate(Long productId, BigDecimal amount, 
                                            Integer termMonths, String classification) {
        log.info("Fetching applicable rate for product: {}, amount: {}, term: {} months, classification: {}", 
                productId, amount, termMonths, classification);
        
        try {
            WebClient webClient = webClientBuilder.baseUrl(productPricingUrl).build();
            
            String uri = String.format("/%d/interest-rates/applicable?amount=%s&termMonths=%d", 
                                     productId, amount.toString(), termMonths);
            
            if (classification != null && !classification.isBlank()) {
                uri += "&classification=" + classification;
            }
            
            ApiResponse<InterestRateDto> response = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<InterestRateDto>>() {})
                .block();
            
            if (response != null && response.isSuccess() && response.getData() != null) {
                log.debug("Found applicable rate: {}%", response.getData().getTotalRate());
                return response.getData();
            }
            
            log.warn("No applicable rate found for product {}", productId);
            return null;
        } catch (Exception e) {
            log.error("Failed to fetch interest rate: {}", e.getMessage());
            return null; // Return null to fallback to product base rate
        }
    }
}
