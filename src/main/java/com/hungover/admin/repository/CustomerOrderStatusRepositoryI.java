package com.hungover.admin.repository;

import com.hungover.core.domain.customer.CustomerOrderStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing customer order status data.
 */
@Repository
public interface CustomerOrderStatusRepositoryI extends CrudRepository<CustomerOrderStatus, Integer> {

    /**
     * Retrieves a list of customer order statuses based on the provided order ID.
     *
     * @param orderId The order ID to search for.
     * @return A list of customer order statuses associated with the given order ID.
     */
    List<CustomerOrderStatus> findByOrderId(String orderId);

    /**
     * Finds and returns a list of customer orders with a specific order status
     * that have a non-null order tracking number.
     *
     * @param dispatch The order status to search for (e.g., "Dispatch").
     * @return A list of customer orders meeting the specified criteria.
     */
    List<CustomerOrderStatus> findByOrderStatusAndOrderTrackingNumberIsNotNull(String dispatch);
}
