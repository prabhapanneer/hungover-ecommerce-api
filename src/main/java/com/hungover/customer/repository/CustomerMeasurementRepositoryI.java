package com.hungover.customer.repository;

import com.hungover.core.domain.customer.CustomerMeasurement;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for managing CustomerMeasurement entities.
 */
@Repository
public interface CustomerMeasurementRepositoryI extends CrudRepository<CustomerMeasurement, Integer>  {

    /**
     * Find customer measurements by customer ID.
     *
     * @param customerId The ID of the customer.
     * @return A list of customer measurements associated with the specified customer ID.
     */
    List<CustomerMeasurement> findByCustomerId(String customerId);

    /**
     * Find customer measurement by name and customer ID.
     *
     * @param name       The name of the customer measurement.
     * @param customerId The ID of the customer.
     * @return The customer measurement with the specified name and associated with the specified customer ID.
     */
    CustomerMeasurement findByNameAndCustomerId(String name, String customerId);

    CustomerMeasurement findByCustomerIdAndName(String customerId, String sizeName);
}


