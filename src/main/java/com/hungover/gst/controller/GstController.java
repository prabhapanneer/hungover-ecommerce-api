package com.hungover.gst.controller;

import com.hungover.common.apiresponse.SingleDataResponse;
import com.hungover.core.dto.gst.GstDto;
import com.hungover.gst.service.GstApiResponseService;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class for managing GST-related operations.
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/gst")
public class GstController {

    private GstApiResponseService gstApiResponseService;
    private static final String ENDPOINT_VERSION = "v2";

    public GstController(GstApiResponseService gstApiResponseService) {
        super();
        this.gstApiResponseService = gstApiResponseService;
    }

    /**
     * Save GST.
     *
     * @param gstDto The GST data to be saved.
     * @return ResponseEntity containing the API response for saving the GST data.
     */
    @PostMapping(value = ENDPOINT_VERSION+"/")
    @ApiOperation(value = "Save GST", nickname = "Save GST", notes = "This endpoint for Save GST",
            produces = "application/json", consumes = "application/json")
    public ResponseEntity<SingleDataResponse> saveGst(@RequestBody GstDto gstDto) {
        return new ResponseEntity<>(gstApiResponseService.saveGst(gstDto), HttpStatus.OK);
    }
}
