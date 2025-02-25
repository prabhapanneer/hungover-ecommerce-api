package com.hungover.customer.repository;

import com.hungover.core.domain.customer.CustomerMeasurementFeedBack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for accessing and managing customer measurement feedback entities.
 * This repository provides methods for CRUD operations on customer measurement feedback data.
 */
@Repository
public interface CustomerMeasurementFeedbackRepositoryI extends JpaRepository<CustomerMeasurementFeedBack,Integer> {
    /**
     * Retrieve customer measurement feedback by the provided order ID.
     *
     * @param orderId The order ID used to fetch the corresponding feedback.
     * @return The customer measurement feedback associated with the given order ID.
     */
    CustomerMeasurementFeedBack getCustomerMeasurementFeedbackByOrderId(String orderId);
}
