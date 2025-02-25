package com.hungover.admin.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.hungover.admin.service.AdminApiResponseService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

class AdminControllerTest {

    @InjectMocks
    AdminController adminController;
    @Mock
    private AdminApiResponseService adminApiResponseService;
    @Mock
    private MessageSource messageSource;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveCustomerMeasurement_Success() throws ParseException {
        CustomerMeasurementDto customerMeasurementInputDto = new CustomerMeasurementDto();
        customerMeasurementInputDto.setName("Chest");
        customerMeasurementInputDto.setCustomerId(String.valueOf(1L));
        customerMeasurementInputDto.setCustomerName("John Doe");
        customerMeasurementInputDto.setCustomerEmail("john@example.com");
        customerMeasurementInputDto.setAdminName("Admin");

        SingleDataResponse singleDataMockResponse = new SingleDataResponse();
        when(adminApiResponseService.saveCustomerMeasurement(customerMeasurementInputDto)).thenReturn(singleDataMockResponse);

        ResponseEntity<SingleDataResponse> singleDataResponseResponseEntity = adminController.saveCustomerMeasurement(customerMeasurementInputDto);

        verify(adminApiResponseService).saveCustomerMeasurement(customerMeasurementInputDto);

        Assertions.assertEquals(HttpStatus.OK, singleDataResponseResponseEntity.getStatusCode());
        Assertions.assertSame(singleDataMockResponse, singleDataResponseResponseEntity.getBody());
    }

    @Test
    void testUpdateCustomerMeasurement_Success() throws ParseException {
        Integer customerMeasurementId = 1;
        CustomerMeasurementDto customerMeasurementInputDto = new CustomerMeasurementDto();
        customerMeasurementInputDto.setName("Chest");
        customerMeasurementInputDto.setCustomerId(String.valueOf(1L));
        customerMeasurementInputDto.setCustomerName("John Doe");
        customerMeasurementInputDto.setCustomerEmail("john@example.com");
        customerMeasurementInputDto.setAdminName("Admin");

        SingleDataResponse singleDataMockResponse = new SingleDataResponse();
        when(adminApiResponseService.updateCustomerMeasurement(customerMeasurementInputDto, customerMeasurementId)).thenReturn(singleDataMockResponse);

        ResponseEntity<SingleDataResponse> singleDataResponseResponseEntity = adminController.updateCustomerMeasurement(customerMeasurementInputDto,
                customerMeasurementId);

        verify(adminApiResponseService).updateCustomerMeasurement(customerMeasurementInputDto, customerMeasurementId);

        Assertions.assertEquals(HttpStatus.OK, singleDataResponseResponseEntity.getStatusCode());
        Assertions.assertSame(singleDataMockResponse, singleDataResponseResponseEntity.getBody());
    }

    @Test
    void testSendLoginIssueMailForAdmin_Success() throws ParseException {
        String userName = "John Doe";
        String userEmail = "john@example.com";
        String userMessage = "Login issue";

        SingleDataResponse singleDataMockResponse = new SingleDataResponse();
        when(adminApiResponseService.sendLoginIssueMailForAdmin(userName, userEmail, userMessage)).thenReturn(singleDataMockResponse);

        ResponseEntity<SingleDataResponse> singleDataResponseResponseEntity = adminController.sendLoginIssueMailForAdmin(userName,
                userEmail, userMessage);

        verify(adminApiResponseService).sendLoginIssueMailForAdmin(userName, userEmail, userMessage);

        Assertions.assertEquals(HttpStatus.OK, singleDataResponseResponseEntity.getStatusCode());
        Assertions.assertSame(singleDataMockResponse, singleDataResponseResponseEntity.getBody());
    }

    @Test
    @DisplayName("Save Customer Order Status - Success")
    void testSaveCustomerOrderStatus_Success() throws JsonProcessingException {
        String userName = "TestUser"; // Set your username
        String userEmail = "test@example.com"; // Set your user email
        String orderStatus = "New Status"; // Set your new order status
        String orderNumber = "234567";
        String orderDate = "16041998";
        String addressInformation ="";
        ApiListResponse apiListResponse = new ApiListResponse();
        List<CustomerOrderStatusDto> customerOrderStatusDtoList = new ArrayList<>();

        apiListResponse.setResponse(ApplicationConstants.Status.SUCCESS, "Customer order status saved successfully", customerOrderStatusDtoList);
        ResponseEntity<ApiListResponse> expectedApiListResponseResponseEntity = new ResponseEntity<>(apiListResponse, HttpStatus.OK);

        when(adminApiResponseService.saveCustomerOrderStatus(customerOrderStatusDtoList,addressInformation)).thenReturn(apiListResponse);

        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Customer order status saved successfully");

        ResponseEntity<ApiListResponse> actualApiListResponseResponseEntity = adminController.saveCustomerOrderStatus(
                customerOrderStatusDtoList,addressInformation);

        verify(adminApiResponseService).saveCustomerOrderStatus(customerOrderStatusDtoList,addressInformation);
        Assertions.assertEquals(expectedApiListResponseResponseEntity, actualApiListResponseResponseEntity);
    }

    @Test
    @DisplayName("Update Customer Order Status - Success")
    void testUpdateCustomerOrderStatus_Success() throws JsonProcessingException {
        // Arrange
        Integer customerOrderStatusId = 1; // Replace with your test data
        String orderStatus = "Shipped"; // Replace with your test data
        String userName = "JohnDoe"; // Replace with your test data
        String userEmail = "john@example.com"; // Replace with your test data
        String orderTrackingNumber = "00340434292135100186";
        String OrderDate = "16041998";
        String orderNumber = "236453476";
        String addressInformationDto ="";
        String orderName = "1";

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

        SingleDataResponse singleDataResponse = new SingleDataResponse();

        CustomerOrderStatusDto expectedCustomerOrderStatusDto = new CustomerOrderStatusDto();

        singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS, "Customer order status updated successfully", expectedCustomerOrderStatusDto);
        ResponseEntity<SingleDataResponse> expectedSingleDataResponseResponseEntity = new ResponseEntity<>(singleDataResponse, HttpStatus.OK);

        when(adminApiResponseService.updateCustomerOrderStatus(customerOrderStatusId,orderStatus,orderTrackingNumber,addressInformationDto))
                .thenReturn(singleDataResponse);
        // Act
        ResponseEntity<SingleDataResponse> actualSingleDataResponseResponseEntity = adminController.updateCustomerOrderStatus(
                customerOrderStatusId, orderStatus,orderTrackingNumber,addressInformationDto);
        // Assert
        Assertions.assertEquals(HttpStatus.OK, actualSingleDataResponseResponseEntity.getStatusCode());
    }
}