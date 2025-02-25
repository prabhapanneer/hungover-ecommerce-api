package com.hungover.admin.repository;

import com.hungover.common.domain.DomainObject;
import com.hungover.core.domain.customer.CustomerOrderStatus;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * Custom implementation for querying CustomerOrderStatus entities based on a customer's measurement size name.
 * Uses Criteria API to create a query that retrieves CustomerOrderStatus entities
 * matching the provided sizeName within the CustomerMeasurements field.
 */
@Repository
public class CustomerOrderStatusCustomImpl {

    private static final String JSON_EXTRACT = "JSON_EXTRACT";
    private static final String CUSTOMER_MEASUREMENT_SIZE_NAME = "$.name";

    @PersistenceContext
    EntityManager entityManagerObj;

    /**
     * Retrieves a list of CustomerOrderStatus entities based on the provided sizeName
     * within the CustomerMeasurements field.
     *
     * @param sizeName The size name to search for within the CustomerMeasurements field.
     * @return A list of CustomerOrderStatus entities that match the provided sizeName.
     */
    public List<CustomerOrderStatus> customerMeasurementSizeName(String sizeName) {
        CriteriaBuilder criteriaBuilderObj = entityManagerObj.getCriteriaBuilder();
        CriteriaQuery<CustomerOrderStatus> criteriaQueryObj = criteriaBuilderObj
                .createQuery(CustomerOrderStatus.class);
        Root<CustomerOrderStatus> customerOrderStatusRoot = criteriaQueryObj.from(CustomerOrderStatus.class);
        Predicate conditionPredicate = criteriaBuilderObj.equal(
                criteriaBuilderObj.function(JSON_EXTRACT, String.class,
                        customerOrderStatusRoot.get(DomainObject.CustomerMeasurements.CUSTOMER_MEASUREMENT),
                        criteriaBuilderObj.literal(CUSTOMER_MEASUREMENT_SIZE_NAME)), sizeName);
        criteriaQueryObj.select(customerOrderStatusRoot).where(conditionPredicate);
        return entityManagerObj.createQuery(criteriaQueryObj).getResultList();
    }
}
