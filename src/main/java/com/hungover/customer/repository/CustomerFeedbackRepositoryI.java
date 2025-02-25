package com.hungover.customer.repository;

import com.hungover.core.domain.customer.CustomerFeedback;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Repository interface for managing customer feedback data.
 */
@Repository
public interface CustomerFeedbackRepositoryI extends CrudRepository<CustomerFeedback, Integer> {

    /**
     * Retrieves a list of customer feedback entries created between the specified dates,
     * ordered by creation date in descending order.
     *
     * @param fromDate The start date for the search range.
     * @param toDate   The end date for the search range.
     * @return A list of customer feedback entries within the specified date range,
     * ordered by creation date in descending order.
     */
    List<CustomerFeedback> findByCreatedDateBetweenOrderByCreatedDateDesc(Date fromDate, Date toDate);

    /**
     * Retrieves a list of all customer feedback entries, ordered by creation date in descending order.
     *
     * @return A list of all customer feedback entries, ordered by creation date in descending order.
     */
    List<CustomerFeedback> findAllByOrderByCreatedDateDesc();
}
