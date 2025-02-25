package com.hungover.admin.repository;

import com.hungover.core.domain.customer.CustomerOrderStatusDetails;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * A repository interface for managing customer order status details.
 *
 * @extends CrudRepository<CustomerOrderStatusDetails,Integer>
 */
@Repository
public interface CustomerOrderStatusDetailsRepositoryI extends CrudRepository<CustomerOrderStatusDetails,Integer> {

    CustomerOrderStatusDetails findByOrderId(String orderId);

    List<CustomerOrderStatusDetails> findAllByOrderByCustomerOrderStatusDetailsIdDesc();
}
