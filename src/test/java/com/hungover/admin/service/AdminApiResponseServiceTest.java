package com.hungover.admin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hungover.common.apiresponse.ApiListResponse;
import com.hungover.common.apiresponse.SingleDataResponse;
import com.hungover.common.constant.ApplicationConstants;
import com.hungover.core.domain.customer.CustomerOrderStatus;
import com.hungover.core.dto.customer.CustomerMeasurementDto;
import com.hungover.core.dto.customer.CustomerOrderStatusDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.mockito.Mockito.*;

class AdminApiResponseServiceTest {

    @InjectMocks
    AdminApiResponseService adminApiResponseService;
    @Mock
    private AdminService adminService;
    @Mock
    private MessageSource messageSource;
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveCustomerMeasurement() throws ParseException {
        CustomerMeasurementDto customerMeasurementDto = new CustomerMeasurementDto();
        customerMeasurementDto.setName("Chest");
        customerMeasurementDto.setCustomerId(String.valueOf(1L));
        customerMeasurementDto.setCustomerName("John Doe");
        customerMeasurementDto.setCustomerEmail("john@example.com");
        customerMeasurementDto.setAdminName("Admin");

        CustomerMeasurementDto savedMeasurementDto = new CustomerMeasurementDto();
        savedMeasurementDto.setName("Chest");
        savedMeasurementDto.setCustomerId(String.valueOf(1L));
        savedMeasurementDto.setCustomerEmail("john@example.com");

        when(adminService.saveCustomerMeasurement(customerMeasurementDto, true)).thenReturn(savedMeasurementDto);
        when(messageSource.getMessage(any(), any(), any())).thenReturn("Success message");
        SingleDataResponse singleDataResponse = adminApiResponseService.saveCustomerMeasurement(customerMeasurementDto);

        Assertions.assertEquals(ApplicationConstants.Status.SUCCESS, singleDataResponse.getStatus().getSuccess());
        Assertions.assertEquals("Success message", singleDataResponse.getStatus().getMessage());
        Assertions.assertEquals(savedMeasurementDto, singleDataResponse.getData());

        // Verify interactions with mocked adminService and messageSource
        verify(adminService).saveCustomerMeasurement(customerMeasurementDto, true);
        verify(messageSource).getMessage(any(), any(), any());
    }

    @Test
    @DisplayName("Should return fail response when saving customer measurement fails")
    void saveCustomerMeasurement_Fail() throws ParseException {
        // Arrange
        CustomerMeasurementDto customerMeasurementInputDto = new CustomerMeasurementDto();
        SingleDataResponse expectedSingleDataResponse = new SingleDataResponse();
        expectedSingleDataResponse.setResponse(ApplicationConstants.Status.FAIL,
                "Failed to save measurement", customerMeasurementInputDto);

        when(adminService.saveCustomerMeasurement(any(CustomerMeasurementDto.class), eq(true)))
                .thenReturn(null);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Failed to save measurement");

        // Act
        SingleDataResponse actualSingleDataResponse = adminApiResponseService.saveCustomerMeasurement(customerMeasurementInputDto);

        // Assert
        verify(adminService).saveCustomerMeasurement(customerMeasurementInputDto, true);
        verify(messageSource).getMessage(anyString(), any(), any(Locale.class));
        Assertions.assertEquals(expectedSingleDataResponse, actualSingleDataResponse);
    }

    @Test
    @DisplayName("Should update customer measurement and return success response")
    void updateCustomerMeasurement_Success() throws ParseException {
        // Arrange
        CustomerMeasurementDto customerMeasurementInputDto = new CustomerMeasurementDto();
        CustomerMeasurementDto customerMeasurementUpdatedDto = new CustomerMeasurementDto();
        SingleDataResponse expectedSingleDataResponse = new SingleDataResponse();
        expectedSingleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                "Measurement updated successfully", customerMeasurementUpdatedDto);

        when(adminService.updateCustomerMeasurement(any(CustomerMeasurementDto.class), any(Integer.class)))
                .thenReturn(customerMeasurementUpdatedDto);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Measurement updated successfully");

        // Act
        SingleDataResponse actualSingleDataResponse = adminApiResponseService.updateCustomerMeasurement(customerMeasurementInputDto, 123);

        // Assert
        verify(adminService).updateCustomerMeasurement(customerMeasurementInputDto, 123);
        verify(messageSource).getMessage(anyString(), any(), any(Locale.class));
        Assertions.assertEquals(expectedSingleDataResponse, actualSingleDataResponse);
    }

    @Test
    @DisplayName("Should return fail response when updating customer measurement fails")
    void updateCustomerMeasurement_Fail() throws ParseException {
        // Arrange
        CustomerMeasurementDto customerMeasurementInputDto = new CustomerMeasurementDto();
        SingleDataResponse expectedSingleDataResponse = new SingleDataResponse();
        expectedSingleDataResponse.setResponse(ApplicationConstants.Status.FAIL,
                "Failed to update measurement", customerMeasurementInputDto);

        when(adminService.updateCustomerMeasurement(any(CustomerMeasurementDto.class), any(Integer.class)))
                .thenReturn(null);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Failed to update measurement");

        // Act
        SingleDataResponse actualSingleDataResponse = adminApiResponseService.updateCustomerMeasurement(customerMeasurementInputDto, 123);

        // Assert
        verify(adminService).updateCustomerMeasurement(customerMeasurementInputDto, 123);
        verify(messageSource).getMessage(anyString(), any(), any(Locale.class));
        Assertions.assertEquals(expectedSingleDataResponse.getStatus().getSuccess(), actualSingleDataResponse.getStatus().getSuccess());
    }

    @Test
    void testSendLoginIssueMailForAdmin_Success() {
        String userName = "John Doe";
        String userEmail = "john@example.com";
        String userMessage = "Login issue message";

        doNothing().when(adminService).sendLoginIssueMailForAdmin(userName, userEmail, userMessage);
        when(messageSource.getMessage(any(), any(), any())).thenReturn("Mail sent successfully");

        SingleDataResponse singleDataResponse = adminApiResponseService.sendLoginIssueMailForAdmin(userName, userEmail, userMessage);

        verify(adminService).sendLoginIssueMailForAdmin(userName, userEmail, userMessage);
        verify(messageSource).getMessage(any(), any(), any());

        Assertions.assertEquals(ApplicationConstants.Status.SUCCESS, singleDataResponse.getStatus().getSuccess());
        Assertions.assertEquals("Mail sent successfully", singleDataResponse.getStatus().getMessage());
        Assertions.assertNull(singleDataResponse.getData());
    }

    @Test
    @DisplayName("Save Customer Order Status - Success")
    void testSaveCustomerOrderStatus_Success() throws JsonProcessingException {
        String userName = "TestUser"; // Set your username
        String userEmail = "test@example.com"; // Set your user email
        String orderStatus = "New Status"; // Set your new order status
        String orderNumber = "234567";
        String orderDate = "16041998";
        String addressInformationDto="";


        List<CustomerOrderStatusDto> customerOrderStatusDtoInputList = new ArrayList<>();
        CustomerOrderStatusDto customerOrderStatusDto = new CustomerOrderStatusDto();
        customerOrderStatusDto.setCustomerOrderStatusId(1);
        customerOrderStatusDto.setOrderId("12344568763234");
        customerOrderStatusDto.setOrderStatus("StartProduction");
        customerOrderStatusDto.setIsFitSample(Boolean.TRUE);
        customerOrderStatusDtoInputList.add(customerOrderStatusDto);

        List<CustomerOrderStatus> savedCustomerOrderStatusList = new ArrayList<>();
        CustomerOrderStatus customerOrderStatus = new CustomerOrderStatus();
        customerOrderStatus.setCustomerOrderStatusId(1);
        customerOrderStatus.setOrderId("12344568763234");
        customerOrderStatus.setOrderStatus("StartProduction");
        customerOrderStatus.setIsFitSample(Boolean.TRUE);
        savedCustomerOrderStatusList.add(customerOrderStatus);

        ApiListResponse expectedApiListResponse = new ApiListResponse();
        expectedApiListResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                "Customer order status saved successfully", customerOrderStatusDtoInputList);

        when(adminService.saveCustomerOrderStatus(customerOrderStatusDtoInputList,addressInformationDto)).thenReturn(customerOrderStatusDtoInputList);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Customer order status saved successfully");

        // Act
        ApiListResponse actualApiListResponse = adminApiResponseService.saveCustomerOrderStatus(
                customerOrderStatusDtoInputList,addressInformationDto);

        // Assert
        verify(adminService).saveCustomerOrderStatus(customerOrderStatusDtoInputList,addressInformationDto);
        verify(messageSource).getMessage(anyString(), any(), any(Locale.class));
        Assertions.assertEquals(ApplicationConstants.Status.SUCCESS, actualApiListResponse.getStatus().getSuccess());
        Assertions.assertEquals(expectedApiListResponse, actualApiListResponse);
    }

    @Test
    @DisplayName("Should return fail response when saving customer order status fails")
    void testSaveCustomerOrderStatus_Fail() throws JsonProcessingException {
        String orderStatus = "New Status"; // Set your new order status
        String userName = "TestUser"; // Set your username
        String userEmail = "test@example.com"; // Set your user email
        String orderDate = "16041998";
        String orderNumber = "234567";
        String addressInformationDto="";
        // Arrange
        List<CustomerOrderStatusDto> customerOrderStatusDtoInputList = new ArrayList<>();
        CustomerOrderStatusDto customerOrderStatusDto = new CustomerOrderStatusDto();
        customerOrderStatusDto.setCustomerOrderStatusId(1);
        customerOrderStatusDto.setOrderId("12344568763234");
        customerOrderStatusDto.setOrderStatus("StartProduction");
        customerOrderStatusDto.setIsFitSample(Boolean.TRUE);
        customerOrderStatusDtoInputList.add(customerOrderStatusDto);

        List<CustomerOrderStatus> savedCustomerOrderStatusList = new ArrayList<>();
        CustomerOrderStatus customerOrderStatus = new CustomerOrderStatus();
        customerOrderStatus.setCustomerOrderStatusId(1);
        customerOrderStatus.setOrderId("12344568763234");
        customerOrderStatus.setOrderStatus("StartProduction");
        customerOrderStatus.setIsFitSample(Boolean.TRUE);
        savedCustomerOrderStatusList.add(customerOrderStatus);

        ApiListResponse expectedApiListResponse = new ApiListResponse();
        expectedApiListResponse.setResponse(ApplicationConstants.Status.FAIL,
                "Customer order status not able to save", null);

        when(adminService.saveCustomerOrderStatus(customerOrderStatusDtoInputList,addressInformationDto)).thenReturn(null);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Customer order status not able to save");

        // Act
        ApiListResponse actualApiListResponse = adminApiResponseService.saveCustomerOrderStatus(
                customerOrderStatusDtoInputList,addressInformationDto);

        // Assert
        verify(adminService).saveCustomerOrderStatus(customerOrderStatusDtoInputList,addressInformationDto);
        verify(messageSource).getMessage(anyString(), any(), any(Locale.class));
        Assertions.assertEquals(expectedApiListResponse, actualApiListResponse);
    }

    @Test
    @DisplayName("Update Customer Order Status - Success")
    void testUpdateCustomerOrderStatus_Success() throws JsonProcessingException {
        // Arrange
        Integer customerOrderStatusId = 123; // Set your customer order status ID
        String orderStatus = "New Status"; // Set your new order status
        String orderTrackingNumber = "00340434292135100186";
        String addressInformationDto="";

        CustomerOrderStatusDto expectedCustomerOrderStatusDto = new CustomerOrderStatusDto();
        expectedCustomerOrderStatusDto.setCustomerOrderStatusId(customerOrderStatusId);

        when(adminService.updateCustomerOrderStatus(customerOrderStatusId, orderStatus,orderTrackingNumber,addressInformationDto)).thenReturn(expectedCustomerOrderStatusDto);

        // Act
        SingleDataResponse actualSingleDataResponse = adminApiResponseService.updateCustomerOrderStatus(
                customerOrderStatusId,orderStatus, orderTrackingNumber,addressInformationDto);

        // Assert
        Assertions.assertEquals(ApplicationConstants.Status.SUCCESS, actualSingleDataResponse.getStatus().getSuccess());
        Assertions.assertEquals(expectedCustomerOrderStatusDto, actualSingleDataResponse.getData());
    }

    @Test
    @DisplayName("Update Customer Order Status - Fail")
    void testUpdateCustomerOrderStatus_Fail() throws JsonProcessingException {
        // Arrange
        Integer customerOrderStatusId = 123; // Set your customer order status ID
        String orderStatus = "New Status"; // Set your new order status
        String orderTrackingNumber = "00340434292135100186";
        String addressInformationDto="";

        when(adminService.updateCustomerOrderStatus(customerOrderStatusId,orderStatus, orderTrackingNumber,addressInformationDto)).thenReturn(null);

        // Act
        SingleDataResponse actualSingleDataResponse = adminApiResponseService.updateCustomerOrderStatus(
                customerOrderStatusId, orderStatus,orderTrackingNumber,addressInformationDto);

        // Assert
        Assertions.assertEquals(ApplicationConstants.Status.FAIL, actualSingleDataResponse.getStatus().getSuccess());
        Assertions.assertNull(actualSingleDataResponse.getData());
    }
}