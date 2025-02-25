package com.hungover.gst.service;

import com.hungover.common.apiresponse.SingleDataResponse;
import com.hungover.common.constant.ApplicationConstants;
import com.hungover.core.dto.gst.GstDto;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

/**
 * Service class for managing GST API responses.
 */
@Service
public class GstApiResponseService {
    private GstService gstService;
    private MessageSource messageSource;

    public GstApiResponseService(GstService gstService, MessageSource messageSource) {
        super();
        this.gstService = gstService;
        this.messageSource = messageSource;
    }

    /**
     * Save GST data.
     *
     * @param gstDto The GST data to be saved.
     * @return SingleDataResponse containing the API response for saving the GST data.
     */
    public SingleDataResponse saveGst(GstDto gstDto) {
        SingleDataResponse singleDataResponse = new SingleDataResponse();
        GstDto savedGstDto = gstService.saveGst(gstDto);
        if (Optional.ofNullable(savedGstDto).isPresent()) {
            singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                    messageSource.getMessage("api.gst.success", null, Locale.ENGLISH), savedGstDto);
        } else {
            singleDataResponse.setResponse(ApplicationConstants.Status.FAIL,
                    messageSource.getMessage("api.gst.fail", null, Locale.ENGLISH), gstDto);
        }
        return singleDataResponse;
    }
}
