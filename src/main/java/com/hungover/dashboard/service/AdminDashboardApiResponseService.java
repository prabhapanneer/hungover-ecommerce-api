package com.hungover.dashboard.service;

import com.hungover.common.apiresponse.SingleDataResponse;
import com.hungover.common.constant.ApplicationConstants;
import com.hungover.core.dto.dashboard.AdminDashboardResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Service class responsible for handling API responses related to the Admin Dashboard.
 */
@Service
public class AdminDashboardApiResponseService {

    private final Logger adminDashboardApiResponseServiceLogger = LoggerFactory.getLogger(this.getClass());

    private AdminDashboardService adminDashboardService;
    private MessageSource messageSource;

    public AdminDashboardApiResponseService(AdminDashboardService adminDashboardService, MessageSource messageSource) {
        this.adminDashboardService = adminDashboardService;
        this.messageSource = messageSource;
    }

    /**
     * Retrieves the Admin Dashboard summary data and creates the API response.
     *
     * @param fromDate The start date for filtering data.
     * @param toDate   The end date for filtering data.
     * @param noOfDays  The number of days to consider for certain data components.
     * @return The SingleDataResponse containing the aggregated dashboard data.
     * @throws ExecutionException if there is an error while fetching the data.
     * @throws InterruptedException if there is an error while fetching the data.
     */
    public SingleDataResponse adminDashboardSummary(String fromDate, String toDate, Integer noOfDays)
            throws ExecutionException, InterruptedException {
        adminDashboardApiResponseServiceLogger.info("Entered admin dashboard filter details ApiResponse Service:::::");
        SingleDataResponse singleDataResponse = new SingleDataResponse();
        AdminDashboardResponseDto adminDashboardResponseDto = adminDashboardService.adminDashboardSummary(
                fromDate, toDate, noOfDays);
        if (Optional.ofNullable(adminDashboardResponseDto).isPresent()) {
            singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                    messageSource.getMessage("api.admin.dashboard.filter.success", null, Locale.ENGLISH),
                    adminDashboardResponseDto);
        } else {
            singleDataResponse.setResponse(ApplicationConstants.Status.FAIL,
                    messageSource.getMessage("api.admin.dashboard.filter.fail", null, Locale.ENGLISH),
                    null);
        }
        return singleDataResponse;
    }
}
