package com.app.customer.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.customer.dto.CreateCustomerRequest;
import com.app.customer.dto.Customer360Response;
import com.app.customer.dto.CustomerClassificationResponse;
import com.app.customer.dto.CustomerResponse;
import com.app.customer.dto.UpdateCustomerRequest;
import com.app.customer.service.CustomerAuthorizationService;
import com.app.customer.service.CustomerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for customer operations
 */
@RestController
@RequestMapping("/customers")  // Base path for customer endpoints
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer Management", description = "APIs for managing customer information")
@SecurityRequirement(name = "Bearer Authentication")
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerAuthorizationService authorizationService;

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER_MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Get all customers", description = "List all customers. Only Customer Managers and Admins can access this endpoint.")
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        log.info("Received request to list all customers");
        List<CustomerResponse> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER_MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Create new customer", description = "Create a new customer profile. Only Customer Managers and Admins can create customers.")
    public ResponseEntity<CustomerResponse> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request,
            Authentication authentication) {
        
        String authenticatedUsername = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));
        
        log.info("User '{}' (Admin: {}) creating customer profile", 
                authenticatedUsername, isAdmin);
        
        CustomerResponse response = customerService.createCustomer(request, authenticatedUsername, isAdmin);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER_MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Get customer by ID", description = "Retrieve customer details by customer ID. Only Customer Managers and Admins can access this endpoint.")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
        log.info("Received request to get customer by ID: {}", id);
        CustomerResponse response = customerService.getCustomerById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('CUSTOMER_MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Get customer by user ID", description = "Retrieve customer details by user ID from login-service. Only Customer Managers and Admins can access this endpoint.")
    public ResponseEntity<CustomerResponse> getCustomerByUserId(@PathVariable Long userId) {
        log.info("Received request to get customer by user ID: {}", userId);
        CustomerResponse response = customerService.getCustomerByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER_MANAGER', 'ADMIN') or @authorizationService.isOwnProfile(#id, authentication)")
    @Operation(summary = "Update customer", description = "Update customer information. Regular users can only update non-critical fields of their own profile. Customer Managers and Admins can update any customer's profile.")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCustomerRequest request,
            Authentication authentication) {
        
        String authenticatedUsername = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));
        boolean isManager = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_CUSTOMER_MANAGER"));
        
        log.info("User '{}' (Admin: {}, Manager: {}) updating customer ID: {}", 
                authenticatedUsername, isAdmin, isManager, id);
        
        CustomerResponse response = customerService.updateCustomer(id, request, authenticatedUsername, isAdmin || isManager);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/classification")
    @PreAuthorize("hasAnyRole('CUSTOMER_MANAGER', 'PRODUCT_MANAGER', 'ADMIN')")
    @Operation(summary = "Get customer classification", description = "Get customer classification for FD rate determination. Available to managers and admins.")
    public ResponseEntity<CustomerClassificationResponse> getCustomerClassification(@PathVariable Long id) {
        log.info("Received request to get classification for customer ID: {}", id);
        CustomerClassificationResponse response = customerService.getCustomerClassification(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/360-view")
    @PreAuthorize("hasRole('CUSTOMER_MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Get 360-degree customer view", description = "Get comprehensive customer overview including FD accounts. Only Customer Managers and Admins can access this endpoint.")
    public ResponseEntity<Customer360Response> getCustomer360View(@PathVariable Long id) {
        log.info("Received request to get 360-degree view for customer ID: {}", id);
        Customer360Response response = customerService.getCustomer360View(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the customer service is running", security = {})
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Customer Service is UP");
    }
}
