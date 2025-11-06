package com.app.customer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.app.customer.repository.CustomerRepository;

@Service
public class CustomerAuthorizationService {

    @Autowired
    private CustomerRepository customerRepository;

    public boolean isOwnProfile(Long customerId, String username) {
        return customerRepository.findById(customerId)
                .map(customer -> customer.getUsername().equals(username))
                .orElse(false);
    }

    public boolean isOwnProfile(Long customerId, Authentication authentication) {
        if (authentication == null) return false;
        return isOwnProfile(customerId, authentication.getName());
    }
}