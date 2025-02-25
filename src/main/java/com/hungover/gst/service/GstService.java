package com.hungover.gst.service;

import com.hungover.core.domain.gst.Gst;
import com.hungover.core.dto.gst.GstDto;
import com.hungover.gst.repository.GstRepositoryI;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

/**
 * Service class for managing GST data.
 */
@Service
public class GstService {

    private GstRepositoryI gstRepository;
    private ModelMapper modelMapper;

    public GstService(GstRepositoryI gstRepository, ModelMapper modelMapper) {
        super();
        this.gstRepository = gstRepository;
        this.modelMapper = modelMapper;
    }

    /**
     * Save GST data.
     *
     * @param gstDto The GST data to be saved.
     * @return GstDto containing the saved GST data.
     */
    public GstDto saveGst(GstDto gstDto) {
        GstDto savedGstDto;
        Gst gst = modelMapper.map(gstDto, Gst.class);
        Gst savedGst = gstRepository.save(gst);
        savedGstDto = modelMapper.map(savedGst, GstDto.class);
        return savedGstDto;
    }
}
