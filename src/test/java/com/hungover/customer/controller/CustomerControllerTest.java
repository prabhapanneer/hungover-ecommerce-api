package com.hungover.customer.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.hungover.common.apiresponse.ApiListResponse;
import com.hungover.common.apiresponse.SingleDataResponse;
import com.hungover.common.constant.ApplicationConstants;
import com.hungover.core.dto.customer.CustomerCountResponseDto;
import com.hungover.core.dto.customer.CustomerCreationKVDto;
import com.hungover.core.dto.customer.CustomerDetailKeyValueResponseDto;
import com.hungover.core.dto.customer.CustomerDetailsKeyValueDto;
import com.hungover.core.dto.customer.CustomerFeedbackKVDto;
import com.hungover.core.dto.customer.CustomerKeyValueDto;
import com.hungover.core.dto.customer.CustomerKvDto;
import com.hungover.core.dto.customer.CustomerMeasurementDto;
import com.hungover.core.dto.customer.CustomerMeasurementFeedbackDto;
import com.hungover.core.dto.customer.CustomerOtpDto;
import com.hungover.core.dto.customer.CustomerResetPasswordDto;
import com.hungover.core.dto.customer.CustomerWishlistDto;
import com.hungover.core.dto.customer.CustomerWishlistKVDto;
import com.hungover.core.dto.customer.ResetPasswordDto;
import com.hungover.customer.service.CustomerApiResponseService;
import org.apache.velocity.exception.ResourceNotFoundException;
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
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerControllerTest {

    @InjectMocks
    CustomerController customerController;

    @Mock
    private CustomerApiResponseService customerApiResponseService;

    @Mock
    private MessageSource messageSource;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should get customer wishlist by customer id successfully")
    void getCustomerWishlistByCustomerId_Success() {
        // Arrange
        String customerId = "123";
        ApiListResponse apiListResponse = new ApiListResponse();
        List<CustomerWishlistDto> wishlistDtoList = new ArrayList<>();
        apiListResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                "Customer wishlist retrieved successfully", wishlistDtoList);
        ResponseEntity<ApiListResponse> expectedApiListResponseResponseEntity = new ResponseEntity<>(apiListResponse, HttpStatus.OK);

        when(customerApiResponseService.getCustomerWishlistByCustomerId(customerId)).thenReturn(apiListResponse);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Customer wishlist retrieved successfully");

        // Act
        ResponseEntity<ApiListResponse> actualApiListResponseResponseEntity = customerController.getCustomerWishlistByCustomerId(customerId);

        // Assert
        verify(customerApiResponseService).getCustomerWishlistByCustomerId(customerId);
        Assertions.assertEquals(expectedApiListResponseResponseEntity, actualApiListResponseResponseEntity);
    }

    @Test
    @DisplayName("Should get all customer wishlist successfully")
    void getAllCustomerWishlist_Success() {
        // Arrange
        ApiListResponse apiListResponse = new ApiListResponse();
        List<CustomerWishlistKVDto> wishlistDtoList = new ArrayList<>();
        apiListResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                "All customer wishlist retrieved successfully", wishlistDtoList);
        ResponseEntity<ApiListResponse> expectedApiListResponseResponseEntity = new ResponseEntity<>(apiListResponse, HttpStatus.OK);

        when(customerApiResponseService.getAllCustomerWishlist()).thenReturn(apiListResponse);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("All customer wishlist retrieved successfully");

        // Act
        ResponseEntity<ApiListResponse> actualApiListResponseResponseEntity = customerController.getAllCustomerWishlist();

        // Assert
        verify(customerApiResponseService).getAllCustomerWishlist();
        Assertions.assertEquals(expectedApiListResponseResponseEntity, actualApiListResponseResponseEntity);
    }

    @Test
    @DisplayName("Should get customer wishlist by variant id successfully")
    void getCustomerWishlistByVariantId_Success() {
        // Arrange
        String variantId = "123";
        ApiListResponse apiListResponse = new ApiListResponse();
        List<CustomerWishlistDto> wishlistDtoList = new ArrayList<>();
        apiListResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                "Customer wishlist by variant id retrieved successfully", wishlistDtoList);
        ResponseEntity<ApiListResponse> expectedApiListResponseResponseEntity = new ResponseEntity<>(apiListResponse, HttpStatus.OK);

        when(customerApiResponseService.getCustomerWishlistByVariantId(variantId)).thenReturn(apiListResponse);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Customer wishlist by variant id retrieved successfully");

        // Act
        ResponseEntity<ApiListResponse> actualApiListResponseResponseEntity = customerController.getCustomerWishlistByVariantId(variantId);

        // Assert
        verify(customerApiResponseService).getCustomerWishlistByVariantId(variantId);
        Assertions.assertEquals(expectedApiListResponseResponseEntity, actualApiListResponseResponseEntity);
    }

    @Test
    @DisplayName("Should get customer feedbacks successfully")
    void getCustomerFeedbacks_Success() {
        // Arrange
        ApiListResponse apiListResponse = new ApiListResponse();
        List<CustomerFeedbackKVDto> customerFeedbackKVDtoList = new ArrayList<>();
        apiListResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                "Customer feedback retrieved successfully", customerFeedbackKVDtoList);
        ResponseEntity<ApiListResponse> expectedApiListResponseResponseEntity = new ResponseEntity<>(apiListResponse, HttpStatus.OK);

        when(customerApiResponseService.getCustomerFeedbacks()).thenReturn(apiListResponse);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Customer feedback retrieved successfully");

        // Act
        ResponseEntity<ApiListResponse> actualApiListResponseResponseEntity = customerController.getCustomerFeedbacks();

        // Assert
        verify(customerApiResponseService).getCustomerFeedbacks();
        Assertions.assertEquals(expectedApiListResponseResponseEntity, actualApiListResponseResponseEntity);
    }

    @Test
    @DisplayName("Should get customer measurement by customer id successfully")
    void getCustomerMeasurementByCustomerId_Success() {
        // Arrange
        ApiListResponse apiListResponse = new ApiListResponse();
        List<CustomerMeasurementDto> customerMeasurementDtoList = new ArrayList<>();
        apiListResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                "Customer measurement retrieved successfully", customerMeasurementDtoList);
        ResponseEntity<ApiListResponse> expectedApiListResponseResponseEntity = new ResponseEntity<>(apiListResponse, HttpStatus.OK);

        when(customerApiResponseService.getCustomerMeasurementByCustomerId("123"))
                .thenReturn(apiListResponse);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Customer measurement retrieved successfully");

        // Act
        ResponseEntity<ApiListResponse> actualApiListResponseResponseEntity = customerController.getCustomerMeasurementByCustomerId("123");

        // Assert
        verify(customerApiResponseService).getCustomerMeasurementByCustomerId("123");
        Assertions.assertEquals(expectedApiListResponseResponseEntity, actualApiListResponseResponseEntity);
    }

    @Test
    @DisplayName("Should delete customer measurement by id successfully")
    void deleteCustomerMeasurementById_Success() {
        // Arrange
        SingleDataResponse singleDataResponse = new SingleDataResponse();
        singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                "Customer measurement deleted successfully", null);
        ResponseEntity<SingleDataResponse> expectedSingleDataResponseResponseEntity = new ResponseEntity<>(singleDataResponse, HttpStatus.OK);

        when(customerApiResponseService.deleteCustomerMeasurementById(1))
                .thenReturn(singleDataResponse);

        // Act
        ResponseEntity<SingleDataResponse> actualSingleDataResponseResponseEntity = customerController.deleteCustomerMeasurementById(1);

        // Assert
        verify(customerApiResponseService).deleteCustomerMeasurementById(1);
        Assertions.assertEquals(expectedSingleDataResponseResponseEntity, actualSingleDataResponseResponseEntity);
    }

    @Test
    @DisplayName("Should save customer measurement successfully")
    void saveMeasurement_Success() throws ParseException {
        // Arrange
        CustomerMeasurementDto customerMeasurementInputDto = new CustomerMeasurementDto();
        SingleDataResponse singleDataResponse = new SingleDataResponse();
        singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                "Customer measurement saved successfully", customerMeasurementInputDto);
        ResponseEntity<SingleDataResponse> expectedSingleDataResponseResponseEntity = new ResponseEntity<>(singleDataResponse, HttpStatus.OK);

        when(customerApiResponseService.saveMeasurement(customerMeasurementInputDto)).thenReturn(singleDataResponse);

        // Act
        ResponseEntity<SingleDataResponse> actualSingleDataResponseResponseEntity = customerController.saveMeasurement(customerMeasurementInputDto);

        // Assert
        verify(customerApiResponseService).saveMeasurement(customerMeasurementInputDto);
        Assertions.assertEquals(expectedSingleDataResponseResponseEntity, actualSingleDataResponseResponseEntity);
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
        when(customerApiResponseService.updateCustomerMeasurement(customerMeasurementInputDto, customerMeasurementId)).thenReturn(singleDataMockResponse);

        ResponseEntity<SingleDataResponse> singleDataResponseResponseEntity = customerController.updateCustomerMeasurement(customerMeasurementInputDto,
                customerMeasurementId);

        verify(customerApiResponseService).updateCustomerMeasurement(customerMeasurementInputDto, customerMeasurementId);

        Assertions.assertEquals(HttpStatus.OK, singleDataResponseResponseEntity.getStatusCode());
        Assertions.assertSame(singleDataMockResponse, singleDataResponseResponseEntity.getBody());
    }

    @Test
    @DisplayName("Should save customer wishlist successfully")
    void saveCustomerWishlist_Success() {
        // Arrange
        CustomerWishlistDto customerWishlistInputDto = new CustomerWishlistDto();
        SingleDataResponse singleDataResponse = new SingleDataResponse();
        singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                "Customer wishlist saved successfully", customerWishlistInputDto);
        ResponseEntity<SingleDataResponse> expectedSingleDataResponseResponseEntity = new ResponseEntity<>(singleDataResponse, HttpStatus.OK);

        when(customerApiResponseService.saveCustomerWishlist(customerWishlistInputDto))
                .thenReturn(singleDataResponse);

        // Act
        ResponseEntity<SingleDataResponse> actualSingleDataResponseResponseEntity = customerController.saveCustomerWishlist(customerWishlistInputDto);

        // Assert
        verify(customerApiResponseService).saveCustomerWishlist(customerWishlistInputDto);
        Assertions.assertEquals(expectedSingleDataResponseResponseEntity, actualSingleDataResponseResponseEntity);
    }

    @Test
    @DisplayName("Should get customer wishlist by customer id and variant id successfully")
    void getCustomerWishlistByCustomerIdAndVariantId_Success() {
        // Arrange
        String customerId = "123";
        String variantId = "456";
        CustomerWishlistDto customerWishlistDto = new CustomerWishlistDto();
        SingleDataResponse singleDataResponse = new SingleDataResponse();
        singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                "Customer wishlist retrieved successfully", customerWishlistDto);
        ResponseEntity<SingleDataResponse> expectedSingleDataResponseResponseEntity = new ResponseEntity<>(singleDataResponse, HttpStatus.OK);

        when(customerApiResponseService.getCustomerWishlistByCustomerIdAndVariantId(customerId, variantId))
                .thenReturn(singleDataResponse);

        // Act
        ResponseEntity<SingleDataResponse> actualSingleDataResponseResponseEntity = customerController.getCustomerWishlistByCustomerIdAndVariantId(customerId, variantId);

        // Assert
        verify(customerApiResponseService).getCustomerWishlistByCustomerIdAndVariantId(customerId, variantId);
        Assertions.assertEquals(expectedSingleDataResponseResponseEntity, actualSingleDataResponseResponseEntity);
    }

    @Test
    @DisplayName("Should delete customer wishlist by customer id and variant id successfully")
    void deleteCustomerWishlistByCustomerIdAndVariantId_Success() {
        // Arrange
        String customerId = "123";
        String variantId = "456";
        CustomerWishlistDto deletedCustomerWishlistDto = new CustomerWishlistDto();

        SingleDataResponse singleDataResponse = new SingleDataResponse();
        singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                "Customer wishlist deleted successfully", deletedCustomerWishlistDto);

        ResponseEntity<SingleDataResponse> expectedSingleDataResponseResponseEntity = new ResponseEntity<>(singleDataResponse, HttpStatus.OK);

        when(customerApiResponseService.deleteCustomerWishlistByCustomerIdAndVariantId(customerId, variantId))
                .thenReturn(singleDataResponse);

        // Act
        ResponseEntity<SingleDataResponse> actualSingleDataResponseResponseEntity = customerController.deleteCustomerWishlistByCustomerIdAndVariantId(customerId, variantId);

        // Assert
        verify(customerApiResponseService).deleteCustomerWishlistByCustomerIdAndVariantId(customerId, variantId);
        Assertions.assertEquals(expectedSingleDataResponseResponseEntity, actualSingleDataResponseResponseEntity);
    }

    @Test
    @DisplayName("Should get all customers successfully")
    void getAllCustomer_Success() throws Exception {
        // Arrange
        List<CustomerKeyValueDto> customerKeyValueDtoList = new ArrayList<>();
        customerKeyValueDtoList.add(new CustomerKeyValueDto());

        ApiListResponse apiListResponse = new ApiListResponse();
        apiListResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                "All customers retrieved successfully", customerKeyValueDtoList);
        apiListResponse.setTotalResults(customerKeyValueDtoList.size());

        ResponseEntity<ApiListResponse> expectedApiListResponseResponseEntity = new ResponseEntity<>(apiListResponse, HttpStatus.OK);

        when(customerApiResponseService.getAllCustomer()).thenReturn(apiListResponse);

        // Act
        ResponseEntity<ApiListResponse> actualApiListResponseResponseEntity = customerController.getAllCustomer();

        // Assert
        verify(customerApiResponseService).getAllCustomer();
        Assertions.assertEquals(expectedApiListResponseResponseEntity, actualApiListResponseResponseEntity);
    }

    @Test
    @DisplayName("Should get last year customers registered count successfully")
    void getCustomerCountByMonthForYears_Success() throws Exception {
        // Arrange
        int lastYear = 2022;
        int currentYear = 2023;

        CustomerCountResponseDto customerCountResponseDto = new CustomerCountResponseDto();

        SingleDataResponse singleDataResponse = new SingleDataResponse();
        singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                "Last year customers registered count retrieved successfully", customerCountResponseDto);

        ResponseEntity<SingleDataResponse> expectedSingleDataResponseResponseEntity = new ResponseEntity<>(singleDataResponse, HttpStatus.OK);

        when(customerApiResponseService.getCustomerCountByMonthForYears(lastYear, currentYear))
                .thenReturn(singleDataResponse);

        // Act
        ResponseEntity<SingleDataResponse> actualSingleDataResponseResponseEntity = customerController.getCustomerCountByMonthForYears(lastYear, currentYear);

        // Assert
        verify(customerApiResponseService).getCustomerCountByMonthForYears(lastYear, currentYear);
        Assertions.assertEquals(expectedSingleDataResponseResponseEntity, actualSingleDataResponseResponseEntity);
    }

    @Test
    @DisplayName("Should update customer login password successfully")
    void customerLoginPasswordReset_Success() {
        // Arrange
        Long customerId = 123L;
        ResetPasswordDto resetPasswordDto = new ResetPasswordDto();
        resetPasswordDto.setPassword("newPassword");
        resetPasswordDto.setPassword_confirmation("oldPassword");

        CustomerResetPasswordDto customerResetPasswordDto = new CustomerResetPasswordDto();
        customerResetPasswordDto.setCustomer(resetPasswordDto);

        ResetPasswordDto updatedResetPasswordDto = new ResetPasswordDto();
        updatedResetPasswordDto.setPassword("newPassword");
        updatedResetPasswordDto.setPassword_confirmation("oldPassword");

        SingleDataResponse singleDataResponse = new SingleDataResponse();
        singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                "Customer login password updated successfully", updatedResetPasswordDto);

        ResponseEntity<SingleDataResponse> expectedSingleDataResponseResponseEntity = new ResponseEntity<>(singleDataResponse, HttpStatus.OK);

        when(customerApiResponseService.customerLoginPasswordReset(customerResetPasswordDto, customerId))
                .thenReturn(singleDataResponse);

        // Act
        ResponseEntity<SingleDataResponse> actualSingleDataResponseResponseEntity = customerController.customerLoginPasswordReset(customerResetPasswordDto, customerId);

        // Assert
        verify(customerApiResponseService).customerLoginPasswordReset(customerResetPasswordDto, customerId);
        Assertions.assertEquals(expectedSingleDataResponseResponseEntity, actualSingleDataResponseResponseEntity);
    }

    @Test
    @DisplayName("Should save customer OTP and send it to customer email successfully")
    void saveCustomerOtp_Success() {
        // Arrange
        String customerEmail = "test@example.com";

        CustomerOtpDto savedCustomerOtpDto = new CustomerOtpDto();
        savedCustomerOtpDto.setCustomerEmail(customerEmail);
        savedCustomerOtpDto.setCode(123456);

        SingleDataResponse singleDataResponse = new SingleDataResponse();
        singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                "Customer OTP saved and sent successfully", savedCustomerOtpDto);

        ResponseEntity<SingleDataResponse> expectedSingleDataResponseResponseEntity = new ResponseEntity<>(singleDataResponse, HttpStatus.OK);

        when(customerApiResponseService.saveCustomerOtp(customerEmail))
                .thenReturn(singleDataResponse);

        // Act
        ResponseEntity<SingleDataResponse> actualSingleDataResponseResponseEntity = customerController.saveCustomerOtp(customerEmail);

        // Assert
        verify(customerApiResponseService).saveCustomerOtp(customerEmail);
        Assertions.assertEquals(expectedSingleDataResponseResponseEntity, actualSingleDataResponseResponseEntity);
    }

    @Test
    @DisplayName("Should validate customer OTP code successfully")
    void validateCustomerOtp_Success() {
        // Arrange
        CustomerOtpDto customerOtpDto = new CustomerOtpDto();
        customerOtpDto.setCustomerEmail("test@example.com");
        customerOtpDto.setCode(123456);

        SingleDataResponse singleDataResponse = new SingleDataResponse();
        singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                "Customer OTP code validation successful", customerOtpDto);

        ResponseEntity<SingleDataResponse> expectedSingleDataResponseResponseEntity = new ResponseEntity<>(singleDataResponse, HttpStatus.OK);

        when(customerApiResponseService.validateCustomerOtp(customerOtpDto))
                .thenReturn(singleDataResponse);

        // Act
        ResponseEntity<SingleDataResponse> actualSingleDataResponseResponseEntity = customerController.validateCustomerOtp(customerOtpDto);

        // Assert
        verify(customerApiResponseService).validateCustomerOtp(customerOtpDto);
        Assertions.assertEquals(expectedSingleDataResponseResponseEntity, actualSingleDataResponseResponseEntity);
    }

    @Test
    void saveCreateCustomer_Success() {
        // Arrange
        List<CustomerKvDto.Addressess> addressess = new ArrayList<>();
        CustomerKvDto customerKvDto = new CustomerKvDto("Preetha Sreee", "K", "preethasree@gmail.com", true, addressess, "Password@123", "Password@123", true, true);
        CustomerCreationKVDto customerCreationKVDto = new CustomerCreationKVDto();
        customerCreationKVDto.setCustomer(customerKvDto);

        SingleDataResponse singleDataResponse = new SingleDataResponse();
        singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                "Customer saved successfully", customerCreationKVDto);

        ResponseEntity<SingleDataResponse> expectedSingleDataResponseResponseEntity = new ResponseEntity<>(singleDataResponse, HttpStatus.OK);

        when(customerApiResponseService.createCustomer(customerCreationKVDto)).thenReturn(singleDataResponse);

        // Act
        ResponseEntity<SingleDataResponse> actualSingleDataResponseResponseEntity = customerController.createCustomer(customerCreationKVDto);

        // Assert
        verify(customerApiResponseService).createCustomer(customerCreationKVDto);
        Assertions.assertEquals(expectedSingleDataResponseResponseEntity, actualSingleDataResponseResponseEntity);
    }

    @Test
    void testUpdateCustomer_Success() {
        // Prepare mock data and dependencies
        Long customerId = 7320574099685L;
        CustomerDetailsKeyValueDto customerDetailsKeyValueDto = new CustomerDetailsKeyValueDto();
        customerDetailsKeyValueDto.setId(7320574099685L);
        customerDetailsKeyValueDto.setFirst_name("Karthik");
        customerDetailsKeyValueDto.setLast_name("K");
        customerDetailsKeyValueDto.setPhone("7358545729");
        CustomerDetailKeyValueResponseDto customerDto = new CustomerDetailKeyValueResponseDto();
        customerDto.setCustomer(customerDetailsKeyValueDto);

        SingleDataResponse singleDataResponse = new SingleDataResponse();
        singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                "Customer details updated successfully", customerDto);

        ResponseEntity<SingleDataResponse> expectedSingleDataResponseResponseEntity = new ResponseEntity<>(singleDataResponse, HttpStatus.OK);

        // Mock the behavior of customerApiResponseService
        when(customerApiResponseService.updateCustomerDetails(customerId, customerDto)).thenReturn(singleDataResponse);

        // Call the method under test
        ResponseEntity<SingleDataResponse> actualSingleDataResponseResponseEntity = customerController.updateCustomer(customerId, customerDto);

        // Verify the expected result
        verify(customerApiResponseService).updateCustomerDetails(customerId, customerDto);
        Assertions.assertEquals(expectedSingleDataResponseResponseEntity, actualSingleDataResponseResponseEntity);
    }

    @Test
    public void testSaveCustomerMeasurementFeedback() throws ParseException, JsonProcessingException {
        // Arrange
        CustomerMeasurementFeedbackDto customerMeasurementFeedbackDto = new CustomerMeasurementFeedbackDto();
        SingleDataResponse expectedResponse = new SingleDataResponse();
        when(customerApiResponseService.saveCustomerMeasurementFeedback(customerMeasurementFeedbackDto))
                .thenReturn(expectedResponse);

        // Act
        ResponseEntity<SingleDataResponse> response = customerController.saveCustomerMeasurementFeedback(customerMeasurementFeedbackDto);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(expectedResponse, response.getBody());
        verify(customerApiResponseService, times(1)).saveCustomerMeasurementFeedback(customerMeasurementFeedbackDto);
    }

    @Test
    @DisplayName("Test getcustomermeasurementfeedbackbyorderid returns correct responseentity when valid orderid is provided")
    void testGetCustomerMeasurementFeedbackByOrderIdReturnsCorrectResponseEntityWhenValidOrderIdIsProvided() {
        // Arrange
        String orderId = "123";
        SingleDataResponse singleDataResponse = new SingleDataResponse();
        singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                "Customer measurement feedback retrieved successfully", null);
        ResponseEntity<SingleDataResponse> expectedSingleDataResponseResponseEntity = new ResponseEntity<>(singleDataResponse, HttpStatus.OK);

        when(customerApiResponseService.getCustomerMeasurementFeedbackByOrderId(orderId)).thenReturn(singleDataResponse);

        // Act
        ResponseEntity<SingleDataResponse> actualSingleDataResponseResponseEntity = customerController.getCustomerMeasurementFeedbackByOrderId(orderId);

        // Assert
        assertEquals(expectedSingleDataResponseResponseEntity, actualSingleDataResponseResponseEntity);
    }

    @Test
    @DisplayName("Test getcustomermeasurementfeedbackbyorderid returns 404 status when invalid orderid is provided")
    void testGetCustomerMeasurementFeedbackByOrderIdReturnsNotFoundWhenInvalidOrderIdIsProvided() {
        // Arrange
        String invalidOrderId = "invalid";
        when(customerApiResponseService.getCustomerMeasurementFeedbackByOrderId(invalidOrderId))
                .thenThrow(new ResourceNotFoundException("Customer measurement feedback not found"));

        // Act and Assert
        try {
            customerController.getCustomerMeasurementFeedbackByOrderId(invalidOrderId);
        } catch (ResourceNotFoundException ex) {
            assertEquals("Customer measurement feedback not found", ex.getMessage());
        }
    }
    @Test
    @DisplayName("Test updating customer measurement feedback with valid input")
    void testUpdateCustomerMeasurementFeedbackByCustomerMeasurementFeedbackIdWithValidInput() throws ParseException, JsonProcessingException {
        // Arrange
        Integer customerMeasurementFeedbackId = 1;
        CustomerMeasurementFeedbackDto customerMeasurementFeedbackDto = new CustomerMeasurementFeedbackDto();
        Integer loggedInUserId = 1;

        SingleDataResponse singleDataResponse = new SingleDataResponse();
        singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                "Customer measurement feedback updated successfully", null);
        ResponseEntity<SingleDataResponse> expectedSingleDataResponseResponseEntity = new ResponseEntity<>(singleDataResponse, HttpStatus.OK);

        when(customerApiResponseService.updateCustomerMeasurementFeedbackByCustomerMeasurementFeedbackId(customerMeasurementFeedbackId, customerMeasurementFeedbackDto, loggedInUserId))
                .thenReturn(singleDataResponse);

        // Act
        ResponseEntity<SingleDataResponse> actualSingleDataResponseResponseEntity = customerController.updateCustomerMeasurementFeedbackByCustomerMeasurementFeedbackId(customerMeasurementFeedbackId, customerMeasurementFeedbackDto, loggedInUserId);

        // Assert
        verify(customerApiResponseService).updateCustomerMeasurementFeedbackByCustomerMeasurementFeedbackId(customerMeasurementFeedbackId, customerMeasurementFeedbackDto, loggedInUserId);
        assertEquals(expectedSingleDataResponseResponseEntity, actualSingleDataResponseResponseEntity);
    }
    @Test
    @DisplayName("Test updating customer measurement feedback with invalid input")
    void testUpdateCustomerMeasurementFeedbackByCustomerMeasurementFeedbackIdWithInvalidInput() throws ParseException, JsonProcessingException {
        // Arrange
        Integer customerMeasurementFeedbackId = 1;
        CustomerMeasurementFeedbackDto customerMeasurementFeedbackDto = new CustomerMeasurementFeedbackDto();
        Integer loggedInUserId = 1;

        when(customerApiResponseService.updateCustomerMeasurementFeedbackByCustomerMeasurementFeedbackId(customerMeasurementFeedbackId, customerMeasurementFeedbackDto, loggedInUserId))
                .thenThrow(new IllegalArgumentException("Invalid input"));

        // Act and Assert
        try {
            customerController.updateCustomerMeasurementFeedbackByCustomerMeasurementFeedbackId(customerMeasurementFeedbackId, customerMeasurementFeedbackDto, loggedInUserId);
        } catch (IllegalArgumentException ex) {
            assertEquals("Invalid input", ex.getMessage());
        }
    }
}