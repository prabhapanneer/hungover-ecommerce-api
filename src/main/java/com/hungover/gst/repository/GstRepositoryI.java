package com.hungover.gst.repository;

import com.hungover.core.domain.gst.Gst;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing GST data.
 */
@Repository
public interface GstRepositoryI extends CrudRepository<Gst, Integer> {

}
