package com.hungover.customer.repository;

import com.hungover.core.domain.customer.CustomerOtp;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * A Spring Data JPA repository interface for managing Customer OTP (One-Time Password) entities.
 * This repository provides CRUD operations for interacting with the CustomerOtp table in the database.
 */
@Repository
public interface CustomerOtpRepository extends CrudRepository<CustomerOtp, Integer> {

    /**
     * Retrieves a Customer OTP entity from the database by the provided customer email.
     *
     * @param customerEmail The email address of the customer to search for.
     * @return The Customer OTP entity associated with the provided email, or null if not found.
     */
    CustomerOtp findByCustomerEmail(String customerEmail);

    /**
     * Retrieves a Customer OTP entity from the database by the provided customer email and OTP code.
     *
     * @param customerEmail The email address of the customer to search for.
     * @param code          The OTP code to search for.
     * @return The Customer OTP entity associated with the provided email and code, or null if not found.
     */
    CustomerOtp findByCustomerEmailAndCode(String customerEmail, Integer code);
}
