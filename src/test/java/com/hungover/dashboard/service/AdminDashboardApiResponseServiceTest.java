package com.hungover.dashboard.service;

import com.hungover.common.apiresponse.SingleDataResponse;
import com.hungover.common.constant.ApplicationConstants;
import com.hungover.core.dto.dashboard.AdminDashboardResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AdminDashboardApiResponseServiceTest {

    @InjectMocks
    AdminDashboardApiResponseService adminDashboardApiResponseService;
    @Mock
    private AdminDashboardService adminDashboardService;
    @Mock
    private MessageSource messageSource;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Test adminDashboardSummary success")
    void testAdminDashboardSummary_Success() throws Exception {
        String fromDate = "2023-01-01";
        String toDate = "2023-12-31";
        Integer noOfDays = 30;

        AdminDashboardResponseDto adminDashboardResponseDto = new AdminDashboardResponseDto();
        // Set up your mocks and expected behavior for adminDashboardService
        when(adminDashboardService.adminDashboardSummary(fromDate, toDate, noOfDays))
                .thenReturn(adminDashboardResponseDto);

        // Set up your mocks and expected behavior for messageSource
        when(messageSource.getMessage(eq("api.admin.dashboard.filter.success"), any(), any(Locale.class)))
                .thenReturn("Success message");

        SingleDataResponse expectedSingleDataResponse = new SingleDataResponse();
        expectedSingleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS, "Success message", adminDashboardResponseDto);

        SingleDataResponse actualSingleDataResponse = adminDashboardApiResponseService.adminDashboardSummary(fromDate, toDate, noOfDays);

        // Perform assertions to verify the expected behavior
        assertNotNull(actualSingleDataResponse);
        assertEquals(expectedSingleDataResponse.getStatus().getSuccess(), actualSingleDataResponse.getStatus().getSuccess());
        assertEquals(expectedSingleDataResponse.getStatus().getMessage(), actualSingleDataResponse.getStatus().getMessage());
        assertEquals(expectedSingleDataResponse.getData(), actualSingleDataResponse.getData());

        // Verify the interactions
        verify(adminDashboardService, times(1)).adminDashboardSummary(fromDate, toDate, noOfDays);
        verify(messageSource, times(1)).getMessage(eq("api.admin.dashboard.filter.success"), any(), any(Locale.class));
    }

    @Test
    @DisplayName("Test adminDashboardSummary failure")
    void testAdminDashboardSummary_Failure() throws Exception {
        String fromDate = "2023-01-01";
        String toDate = "2023-12-31";
        Integer noOfDays = 30;

        // Set up your mocks and expected behavior for adminDashboardService
        when(adminDashboardService.adminDashboardSummary(fromDate, toDate, noOfDays))
                .thenReturn(null);

        // Set up your mocks and expected behavior for messageSource
        when(messageSource.getMessage(eq("api.admin.dashboard.filter.fail"), any(), any(Locale.class)))
                .thenReturn("Failure message");

        SingleDataResponse expectedSingleDataResponse = new SingleDataResponse();
        expectedSingleDataResponse.setResponse(ApplicationConstants.Status.FAIL, "Failure message", null);

        SingleDataResponse actualSingleDataResponse = adminDashboardApiResponseService.adminDashboardSummary(fromDate, toDate, noOfDays);

        // Perform assertions to verify the expected behavior
        assertNotNull(actualSingleDataResponse);
        assertEquals(expectedSingleDataResponse.getStatus().getSuccess(), actualSingleDataResponse.getStatus().getSuccess());
        assertEquals(expectedSingleDataResponse.getStatus().getMessage(), actualSingleDataResponse.getStatus().getMessage());
        assertEquals(expectedSingleDataResponse.getData(), actualSingleDataResponse.getData());

        // Verify the interactions
        verify(adminDashboardService, times(1)).adminDashboardSummary(fromDate, toDate, noOfDays);
        verify(messageSource, times(1)).getMessage(eq("api.admin.dashboard.filter.fail"), any(), any(Locale.class));
    }

}