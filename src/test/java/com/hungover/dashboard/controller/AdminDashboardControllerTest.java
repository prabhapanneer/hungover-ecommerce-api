package com.hungover.dashboard.controller;

import com.hungover.common.apiresponse.SingleDataResponse;
import com.hungover.common.constant.ApplicationConstants;
import com.hungover.dashboard.service.AdminDashboardApiResponseService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.mockito.Mockito.*;

class AdminDashboardControllerTest {

    @InjectMocks
    AdminDashboardController adminDashboardController;
    @Mock
    private AdminDashboardApiResponseService adminDashboardApiResponseService;
    @Mock
    private MessageSource messageSource;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Test adminDashboardSummary endpoint")
    void testAdminDashboardSummaryEndpoint() throws Exception {
        String fromDate = "2023-01-01";
        String toDate = "2023-12-31";
        Integer noOfDays = 30;

        SingleDataResponse singleDataResponse = new SingleDataResponse();
        singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                "Admin dashboard data fetched successfully", null);
        ResponseEntity<SingleDataResponse> expectedSingleDataResponseResponseEntity = new ResponseEntity<>(singleDataResponse, HttpStatus.OK);

        when(adminDashboardApiResponseService.adminDashboardSummary(fromDate, toDate, noOfDays)).thenReturn(singleDataResponse);

        // Act
        ResponseEntity<SingleDataResponse> actualSingleDataResponseResponseEntity = adminDashboardController.adminDashboardSummary(fromDate, toDate, noOfDays);

        // Assert
        verify(adminDashboardApiResponseService).adminDashboardSummary(fromDate, toDate, noOfDays);
        Assertions.assertEquals(expectedSingleDataResponseResponseEntity, actualSingleDataResponseResponseEntity);
    }

}