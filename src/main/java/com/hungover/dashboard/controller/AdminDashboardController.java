package com.hungover.dashboard.controller;

import com.hungover.common.apiresponse.SingleDataResponse;
import com.hungover.dashboard.service.AdminDashboardApiResponseService;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

/**
 * API controller for handling Admin Dashboard related requests.
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/admindashboard")
public class AdminDashboardController {

    private AdminDashboardApiResponseService adminDashboardApiResponseService;
    private static final String ENDPOINT_VERSION = "v2";

    public AdminDashboardController(AdminDashboardApiResponseService adminDashboardApiResponseService) {
        this.adminDashboardApiResponseService = adminDashboardApiResponseService;
    }

    /**
     * Retrieves the Admin Dashboard summary based on filter input details and returns the API response.
     *
     * @param fromDate The start date for filtering data.
     * @param toDate   The end date for filtering data.
     * @param noOfDays  The number of days to consider for certain data components.
     * @return The ResponseEntity containing the SingleDataResponse with the dashboard summary data.
     * @throws ExecutionException if there is an error while fetching the data.
     * @throws InterruptedException if there is an error while fetching the data.
     */
    @GetMapping(value = ENDPOINT_VERSION+"/filter")
    @ApiOperation(value = "Get admin dashboard summary based on filter input details",
            nickname = "Get admin dashboard summary based on filter input details",
            notes = "This endpoint for Get admin dashboard summary based on filter input details",
            produces = "application/json", consumes = "application/json")
    public ResponseEntity<SingleDataResponse> adminDashboardSummary(
            @RequestParam(name = "fromDate") String fromDate,
            @RequestParam(name = "toDate") String toDate,
            @RequestParam(value = "noOfDays") Integer noOfDays) throws ExecutionException, InterruptedException {
        return new ResponseEntity<>(adminDashboardApiResponseService.adminDashboardSummary(fromDate, toDate, noOfDays),
                HttpStatus.OK);
    }
}
