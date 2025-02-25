package com.hungover.customer.repository;

import com.hungover.core.domain.customer.CustomerWishlist;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing customer wishlist data.
 */
@Repository
public interface CustomerWishlistRepositoryI extends CrudRepository<CustomerWishlist, Integer> {

    /**
     * Find customer wishlists by customer ID.
     *
     * @param customerId The ID of the customer.
     * @return List of CustomerWishlist containing customer wishlist data for the specified customer ID.
     */
    List<CustomerWishlist> findByCustomerId(String customerId);

    /**
     * Find customer wishlists by variant ID.
     *
     * @param variantId The ID of the variant.
     * @return List of CustomerWishlist containing customer wishlist data for the specified variant ID.
     */
    List<CustomerWishlist> findByVariantId(String variantId);

    /**
     * Find customer wishlist by customer ID and variant ID.
     *
     * @param customerId The ID of the customer.
     * @param variantId  The ID of the variant.
     * @return CustomerWishlist containing the customer wishlist data for the specified customer ID and variant ID.
     */
    CustomerWishlist findByCustomerIdAndVariantId(String customerId, String variantId);

    /**
     * Delete customer wishlist by customer ID and variant ID.
     *
     * @param customerId The ID of the customer.
     * @param variantId  The ID of the variant.
     */
    void deleteByCustomerIdAndVariantId(String customerId, String variantId);
}
