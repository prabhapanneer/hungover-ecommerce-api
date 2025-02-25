package com.hungover.customer.service;


import com.google.gson.Gson;
import com.hungover.common.apiresponse.ApiListResponse;
import com.hungover.common.apiresponse.SingleDataResponse;
import com.hungover.common.constant.ApplicationConstants;
import com.hungover.core.dto.customer.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

import static org.mockito.Mockito.*;


class CustomerApiResponseServiceTest {

    @InjectMocks
    CustomerApiResponseService customerApiResponseService;
    @Mock
    private CustomerService customerService;
    @Mock
    private MessageSource messageSource;

    @Value("${shopifyAccessToken}")
    String shopifyAccessToken;

    @Value("${shopifyCustomerEndPoint}")
    String shopifyCustomerEndPoint;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        setPrivateField(customerApiResponseService, "shopifyAccessToken", "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");
        setPrivateField(customerApiResponseService, "shopifyCustomerEndPoint", "https://e4d27c.myshopify.com//admin/api/2022-07/customers.json");
        setPrivateField(customerApiResponseService, "shopifyUpdateCustomerDetailsEndPoint", "https://e4d27c.myshopify.com/admin/api/2023-07/customers/%s.json");
    }

    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            System.out.println(fieldName);
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Should return customer wishlist by customer ID")
    void getCustomerWishlistByCustomerId_Success() {
        // Arrange
        String customerId = "123";
        List<CustomerWishlistDto> wishlistDtoList = new ArrayList<>();
        wishlistDtoList.add(new CustomerWishlistDto());

        when(customerService.getCustomerWishlistByCustomerId(customerId)).thenReturn(wishlistDtoList);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Customer wishlist retrieved successfully");

        // Act
        ApiListResponse actualApiListResponse = customerApiResponseService.getCustomerWishlistByCustomerId(customerId);

        // Assert
        verify(customerService).getCustomerWishlistByCustomerId(customerId);
        Assertions.assertEquals(ApplicationConstants.Status.SUCCESS, actualApiListResponse.getStatus().getSuccess());
        Assertions.assertEquals("Customer wishlist retrieved successfully", actualApiListResponse.getStatus().getMessage());
        Assertions.assertEquals(wishlistDtoList, actualApiListResponse.getData());
    }

    @Test
    @DisplayName("Should return all customer wishlist")
    void getAllCustomerWishlist_Success() {
        // Arrange
        List<CustomerWishlistKVDto> customerWishlistDtoList = new ArrayList<>();
        customerWishlistDtoList.add(new CustomerWishlistKVDto("Crew Neck Tee", "https://cdn.shopify.com/s/files/1/0659/7548/4645/products/charcoal_shot_1_ss_crew_tan.jpg?v=1663916699", "43457554022629", "Charcoal", "Short Sleeve", "No Pocket", 1));
        customerWishlistDtoList.add(new CustomerWishlistKVDto("V-Neck Tee", "https://cdn.shopify.com/s/files/1/0659/7548/4645/products/white_shot_1_ss_v_denim.jpg?v=1670414143", "43425807663333", "White", "Short Sleeve", "No Pocket", 1));

        when(customerService.getAllCustomerWishlist()).thenReturn(customerWishlistDtoList);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("All customer wishlist retrieved successfully");

        // Act
        ApiListResponse actualApiListResponse = customerApiResponseService.getAllCustomerWishlist();

        // Assert
        verify(customerService).getAllCustomerWishlist();
        Assertions.assertEquals(ApplicationConstants.Status.SUCCESS, actualApiListResponse.getStatus().getSuccess());
        Assertions.assertEquals("All customer wishlist retrieved successfully", actualApiListResponse.getStatus().getMessage());
        Assertions.assertEquals(customerWishlistDtoList, actualApiListResponse.getData());
    }

    @Test
    @DisplayName("Should return fail status when customer wishlist is empty")
    void getAllCustomerWishlist_Empty() {
        // Arrange
        List<CustomerWishlistKVDto> emptyWishlist = new ArrayList<>();

        when(customerService.getAllCustomerWishlist()).thenReturn(emptyWishlist);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Customer wishlist is empty");

        // Act
        ApiListResponse actualApiListResponse = customerApiResponseService.getAllCustomerWishlist();

        // Assert
        verify(customerService).getAllCustomerWishlist();
        Assertions.assertEquals(ApplicationConstants.Status.FAIL, actualApiListResponse.getStatus().getSuccess());
        Assertions.assertEquals("Customer wishlist is empty", actualApiListResponse.getStatus().getMessage());
        Assertions.assertNull(actualApiListResponse.getData());
    }

    @Test
    @DisplayName("Should return customer wishlist by variant ID")
    void getCustomerWishlistByVariantId_Success() {
        // Arrange
        String variantId = "variant123";
        List<CustomerWishlistDto> wishlistDtoList = new ArrayList<>();
        wishlistDtoList.add(new CustomerWishlistDto());

        when(customerService.getCustomerWishlistByVariantId(variantId)).thenReturn(wishlistDtoList);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Customer wishlist by variant ID retrieved successfully");

        // Act
        ApiListResponse actualApiListResponse = customerApiResponseService.getCustomerWishlistByVariantId(variantId);

        // Assert
        verify(customerService).getCustomerWishlistByVariantId(variantId);
        Assertions.assertEquals(ApplicationConstants.Status.SUCCESS, actualApiListResponse.getStatus().getSuccess());
        Assertions.assertEquals("Customer wishlist by variant ID retrieved successfully", actualApiListResponse.getStatus().getMessage());
        Assertions.assertEquals(wishlistDtoList, actualApiListResponse.getData());
    }

    @Test
    @DisplayName("Should return customer feedbacks")
    void getCustomerFeedbacks_Success() {
        // Arrange
        List<CustomerFeedbackKVDto> feedbackDtoList = new ArrayList<>();
        feedbackDtoList.add(new CustomerFeedbackKVDto());

        when(customerService.getCustomerFeedback()).thenReturn(feedbackDtoList);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Customer feedbacks retrieved successfully");

        // Act
        ApiListResponse actualApiListResponse = customerApiResponseService.getCustomerFeedbacks();

        // Assert
        verify(customerService).getCustomerFeedback();
        Assertions.assertEquals(ApplicationConstants.Status.SUCCESS, actualApiListResponse.getStatus().getSuccess());
        Assertions.assertEquals("Customer feedbacks retrieved successfully", actualApiListResponse.getStatus().getMessage());
        Assertions.assertEquals(feedbackDtoList, actualApiListResponse.getData());
        Assertions.assertEquals(feedbackDtoList.size(), actualApiListResponse.getTotalResults());
    }

    @Test
    @DisplayName("Should return failure response when customer feedbacks list is empty")
    void getCustomerFeedbacks_EmptyList() {
        // Arrange
        List<CustomerFeedbackKVDto> emptyList = new ArrayList<>();

        when(customerService.getCustomerFeedback()).thenReturn(emptyList);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("No customer feedbacks found");

        // Act
        ApiListResponse actualApiListResponse = customerApiResponseService.getCustomerFeedbacks();

        // Assert
        verify(customerService).getCustomerFeedback();
        Assertions.assertEquals(ApplicationConstants.Status.FAIL, actualApiListResponse.getStatus().getSuccess());
        Assertions.assertEquals("No customer feedbacks found", actualApiListResponse.getStatus().getMessage());
        Assertions.assertNull(actualApiListResponse.getData());
    }

    @Test
    @DisplayName("Should return customer measurements by customer ID")
    void getCustomerMeasurementByCustomerId_Success() {
        // Arrange
        List<CustomerMeasurementDto> measurementDtoList = new ArrayList<>();
        measurementDtoList.add(new CustomerMeasurementDto());

        when(customerService.getCustomerMeasurementByCustomerId(anyString())).thenReturn(measurementDtoList);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Customer measurements retrieved successfully");

        // Act
        ApiListResponse actualApiListResponse = customerApiResponseService.getCustomerMeasurementByCustomerId("123");

        // Assert
        verify(customerService).getCustomerMeasurementByCustomerId("123");
        Assertions.assertEquals(ApplicationConstants.Status.SUCCESS, actualApiListResponse.getStatus().getSuccess());
        Assertions.assertEquals("Customer measurements retrieved successfully", actualApiListResponse.getStatus().getMessage());
        Assertions.assertEquals(measurementDtoList, actualApiListResponse.getData());
    }

    @Test
    @DisplayName("Should delete customer measurement by ID")
    void deleteCustomerMeasurementById_Success() {
        // Arrange
        Integer measurementId = 123;
        CustomerMeasurementDto deletedMeasurementDto = new CustomerMeasurementDto();

        when(customerService.deleteCustomerMeasurementById(measurementId)).thenReturn(deletedMeasurementDto);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Customer measurement deleted successfully");

        // Act
        SingleDataResponse actualSingleDataResponse = customerApiResponseService.deleteCustomerMeasurementById(measurementId);

        // Assert
        verify(customerService).deleteCustomerMeasurementById(measurementId);
        Assertions.assertEquals(ApplicationConstants.Status.SUCCESS, actualSingleDataResponse.getStatus().getSuccess());
        Assertions.assertEquals("Customer measurement deleted successfully", actualSingleDataResponse.getStatus().getMessage());
        Assertions.assertEquals(deletedMeasurementDto, actualSingleDataResponse.getData());
    }

    @Test
    @DisplayName("Should save customer measurement")
    void saveMeasurement_Success() throws ParseException {
        // Arrange
        CustomerMeasurementDto measurementDtoToSave = new CustomerMeasurementDto();
        CustomerMeasurementDto savedMeasurementDto = new CustomerMeasurementDto();

        when(customerService.saveMeasurement(measurementDtoToSave)).thenReturn(savedMeasurementDto);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Customer measurement saved successfully");

        // Act
        SingleDataResponse actualSingleDataResponse = customerApiResponseService.saveMeasurement(measurementDtoToSave);

        // Assert
        verify(customerService).saveMeasurement(measurementDtoToSave);
        Assertions.assertEquals(ApplicationConstants.Status.SUCCESS, actualSingleDataResponse.getStatus().getSuccess());
        Assertions.assertEquals("Customer measurement saved successfully", actualSingleDataResponse.getStatus().getMessage());
        Assertions.assertEquals(savedMeasurementDto, actualSingleDataResponse.getData());
    }

    @Test
    @DisplayName("Should handle failure when saving customer measurement")
    void saveMeasurement_Failure() throws ParseException {
        // Arrange
        CustomerMeasurementDto customerMeasurementDto = new CustomerMeasurementDto();

        when(customerService.saveMeasurement(customerMeasurementDto))
                .thenReturn(null);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Measurement saved failed");

        // Act
        SingleDataResponse actualSingleDataResponse =customerApiResponseService.saveMeasurement(customerMeasurementDto);

        // Assert
        verify(customerService).saveMeasurement(customerMeasurementDto);
        Assertions.assertEquals(ApplicationConstants.Status.FAIL, actualSingleDataResponse.getStatus().getSuccess());
        Assertions.assertEquals("Measurement saved failed", actualSingleDataResponse.getStatus().getMessage());
        Assertions.assertEquals(customerMeasurementDto, actualSingleDataResponse.getData());
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

        when(customerService.updateCustomerMeasurement(any(CustomerMeasurementDto.class), any(Integer.class)))
                .thenReturn(customerMeasurementUpdatedDto);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Measurement updated successfully");

        // Act
        SingleDataResponse actualSingleDataResponse = customerApiResponseService.updateCustomerMeasurement(customerMeasurementInputDto, 123);

        // Assert
        verify(customerService).updateCustomerMeasurement(customerMeasurementInputDto, 123);
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

        when(customerService.updateCustomerMeasurement(any(CustomerMeasurementDto.class), any(Integer.class)))
                .thenReturn(null);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Failed to update measurement");

        // Act
        SingleDataResponse actualSingleDataResponse = customerApiResponseService.updateCustomerMeasurement(customerMeasurementInputDto, 123);

        // Assert
        verify(customerService).updateCustomerMeasurement(customerMeasurementInputDto, 123);
        verify(messageSource).getMessage(anyString(), any(), any(Locale.class));
        Assertions.assertEquals(expectedSingleDataResponse.getStatus().getSuccess(), actualSingleDataResponse.getStatus().getSuccess());
    }

    @Test
    @DisplayName("Should save customer wishlist")
    void saveCustomerWishlist_Success() {
        // Arrange
        CustomerWishlistDto wishlistDtoToSave = new CustomerWishlistDto();
        CustomerWishlistDto savedWishlistDto = new CustomerWishlistDto();

        when(customerService.saveCustomerWishlist(wishlistDtoToSave)).thenReturn(savedWishlistDto);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Customer wishlist saved successfully");

        // Act
        SingleDataResponse actualSingleDataResponse = customerApiResponseService.saveCustomerWishlist(wishlistDtoToSave);

        // Assert
        verify(customerService).saveCustomerWishlist(wishlistDtoToSave);
        Assertions.assertEquals(ApplicationConstants.Status.SUCCESS, actualSingleDataResponse.getStatus().getSuccess());
        Assertions.assertEquals("Customer wishlist saved successfully", actualSingleDataResponse.getStatus().getMessage());
        Assertions.assertEquals(savedWishlistDto, actualSingleDataResponse.getData());
    }

    @Test
    @DisplayName("Should handle failure when saving customer wishlist")
    void saveCustomerWishlist_Failure() {
        // Arrange
        CustomerWishlistDto customerWishlistDto = new CustomerWishlistDto();

        when(customerService.saveCustomerWishlist(customerWishlistDto))
                .thenReturn(null);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Wishlist saved failed");

        // Act
        SingleDataResponse actualSingleDataResponse =customerApiResponseService.saveCustomerWishlist(customerWishlistDto);

        // Assert
        verify(customerService).saveCustomerWishlist(customerWishlistDto);
        Assertions.assertEquals(ApplicationConstants.Status.FAIL, actualSingleDataResponse.getStatus().getSuccess());
        Assertions.assertEquals("Wishlist saved failed", actualSingleDataResponse.getStatus().getMessage());
        Assertions.assertEquals(customerWishlistDto, actualSingleDataResponse.getData());
    }

    @Test
    @DisplayName("Should get customer wishlist by customerId and variantId")
    void getCustomerWishlistByCustomerIdAndVariantId_Success() {
        // Arrange
        String customerId = "123";
        String variantId = "456";
        CustomerWishlistDto existingWishlistDto = new CustomerWishlistDto();

        when(customerService.getCustomerWishlistByCustomerIdAndVariantId(customerId, variantId))
                .thenReturn(existingWishlistDto);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Customer wishlist retrieved successfully");

        // Act
        SingleDataResponse actualSingleDataResponse = customerApiResponseService.getCustomerWishlistByCustomerIdAndVariantId(customerId, variantId);

        // Assert
        verify(customerService).getCustomerWishlistByCustomerIdAndVariantId(customerId, variantId);
        Assertions.assertEquals(ApplicationConstants.Status.SUCCESS, actualSingleDataResponse.getStatus().getSuccess());
        Assertions.assertEquals("Customer wishlist retrieved successfully", actualSingleDataResponse.getStatus().getMessage());
        Assertions.assertEquals(existingWishlistDto, actualSingleDataResponse.getData());
    }

    @Test
    @DisplayName("Should delete customer wishlist by customerId and variantId")
    void deleteCustomerWishlistByCustomerIdAndVariantId_Success() {
        // Arrange
        String customerId = "123";
        String variantId = "456";
        CustomerWishlistDto existingWishlistDto = new CustomerWishlistDto();

        when(customerService.deleteCustomerWishlistByCustomerIdAndVariantId(customerId, variantId))
                .thenReturn(existingWishlistDto);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Customer wishlist deleted successfully");

        // Act
        SingleDataResponse actualSingleDataResponse = customerApiResponseService.deleteCustomerWishlistByCustomerIdAndVariantId(customerId, variantId);

        // Assert
        verify(customerService).deleteCustomerWishlistByCustomerIdAndVariantId(customerId, variantId);
        Assertions.assertEquals(ApplicationConstants.Status.SUCCESS, actualSingleDataResponse.getStatus().getSuccess());
        Assertions.assertEquals("Customer wishlist deleted successfully", actualSingleDataResponse.getStatus().getMessage());
        Assertions.assertEquals(existingWishlistDto, actualSingleDataResponse.getData());
    }

    @Test
    @DisplayName("Should get all customers")
    void getAllCustomer_Success() throws Exception {
        // Arrange
        List<CustomerKeyValueDto> customerKeyValueDtoList = new ArrayList<>();
        customerKeyValueDtoList.add(new CustomerKeyValueDto("John Doe", true, "1234567890", "3", "1500.00", 12345654243562425l, "johndoe@gmail.com"));

        when(customerService.getAllCustomerListFromShopify(any(), any())).thenReturn(customerKeyValueDtoList);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Customers retrieved successfully");

        // Act
        ApiListResponse actualApiListResponse = customerApiResponseService.getAllCustomer();

        // Assert
        Assertions.assertEquals(ApplicationConstants.Status.SUCCESS, actualApiListResponse.getStatus().getSuccess());
        Assertions.assertEquals("Customers retrieved successfully", actualApiListResponse.getStatus().getMessage());
        Assertions.assertEquals(customerKeyValueDtoList, actualApiListResponse.getData());
        Assertions.assertEquals(customerKeyValueDtoList.size(), actualApiListResponse.getTotalResults());
    }

    @Test
    @DisplayName("Should handle failure when getting all customers")
    void getAllCustomer_Failure() throws Exception {
        // Arrange
        List<CustomerKeyValueDto> customerKeyValueDtoList = new ArrayList<>();
        ApiListResponse expectedResponse = new ApiListResponse();
        expectedResponse.setResponse(ApplicationConstants.Status.FAIL,
                "Failed to fetch customers", null);

        when(customerService.getAllCustomerListFromShopify(any(), any())).thenReturn(customerKeyValueDtoList);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Failed to fetch customers");

        // Act
        ApiListResponse actualApiListResponse = customerApiResponseService.getAllCustomer();

        // Assert
        Assertions.assertEquals(expectedResponse, actualApiListResponse);
    }


    @Test
    @DisplayName("Should get customer count by month for years")
    void getCustomerCountByMonthForYears_Success() throws IOException {
        // Arrange
        int lastYear = 2022;
        int currentYear = 2023;
        CustomerCountResponseDto customerCountResponseDto = new CustomerCountResponseDto();
        Map<String, Map<String, Double>> customerCountByYearMap = new HashMap<>();
        Map<String, Double> year2021CustomerCountDataMap = new HashMap<>();
        year2021CustomerCountDataMap.put("January", 10.0);
        year2021CustomerCountDataMap.put("February", 15.0);
        // ... populate other months
        customerCountByYearMap.put("2021", year2021CustomerCountDataMap);

        // Populate data for the year 2022
        Map<String, Double> year2022CustomerCountDataMap = new HashMap<>();
        year2022CustomerCountDataMap.put("January", 5.0);
        year2022CustomerCountDataMap.put("February", 8.0);
        // ... populate other months
        customerCountByYearMap.put("2022", year2022CustomerCountDataMap);

        customerCountResponseDto.setCustomerCountByYear(customerCountByYearMap);
        customerCountResponseDto.setPercentChange(new BigDecimal("10.50"));

        when(customerService.getCustomerCountByMonthForYears(any(), anyInt(), anyInt(), any())).thenReturn(customerCountResponseDto);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Customer count retrieved successfully");

        // Act
        SingleDataResponse actualSingleDataResponse =
                customerApiResponseService.getCustomerCountByMonthForYears(lastYear, currentYear);

        // Assert
        Assertions.assertEquals(ApplicationConstants.Status.SUCCESS, actualSingleDataResponse.getStatus().getSuccess());
        Assertions.assertEquals("Customer count retrieved successfully", actualSingleDataResponse.getStatus().getMessage());
        Assertions.assertEquals(customerCountResponseDto, actualSingleDataResponse.getData());
    }

    @Test
    @DisplayName("Should handle failure when getting customer count by month for years")
    void getCustomerCountByMonthForYears_Failure() throws IOException {
        // Arrange
        int lastYear = 2022;
        int currentYear = 2023;
        SingleDataResponse expectedResponse = new SingleDataResponse();
        expectedResponse.setResponse(ApplicationConstants.Status.FAIL,
                "Failed to fetch customer count", null);

        when(customerService.getCustomerCountByMonthForYears(shopifyCustomerEndPoint, lastYear, currentYear, shopifyAccessToken))
                .thenReturn(null);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Failed to fetch customer count");

        // Act
        SingleDataResponse actualSingleDataResponse = customerApiResponseService.getCustomerCountByMonthForYears(lastYear, currentYear);

        // Assert
        Assertions.assertEquals(expectedResponse, actualSingleDataResponse);
    }

    @Test
    @DisplayName("Should reset customer login password")
    void customerLoginPasswordReset_Success() {
        // Arrange
        CustomerResetPasswordDto resetPasswordDto = new CustomerResetPasswordDto();
        Long customerId = 123L;
        ResetPasswordDto updatedResetPasswordDto = new ResetPasswordDto();

        when(customerService.customerLoginPasswordReset(any(), any(), any()))
                .thenReturn(updatedResetPasswordDto);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Password reset successful");

        // Act
        SingleDataResponse actualSingleDataResponse =
                customerApiResponseService.customerLoginPasswordReset(resetPasswordDto, customerId);

        // Assert
        Assertions.assertEquals(ApplicationConstants.Status.SUCCESS, actualSingleDataResponse.getStatus().getSuccess());
        Assertions.assertEquals("Password reset successful", actualSingleDataResponse.getStatus().getMessage());
        Assertions.assertEquals(updatedResetPasswordDto, actualSingleDataResponse.getData());
    }


    @Test
    @DisplayName("Should handle failure when resetting customer login password")
    void customerLoginPasswordReset_Failure() {
        // Arrange
        Long customerId = 123L;
        CustomerResetPasswordDto resetPasswordDto = new CustomerResetPasswordDto();
        SingleDataResponse expectedResponse = new SingleDataResponse();
        expectedResponse.setResponse(ApplicationConstants.Status.FAIL,
                "Password reset failed", resetPasswordDto);

        when(customerService.customerLoginPasswordReset(resetPasswordDto, customerId, shopifyAccessToken))
                .thenReturn(null);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Password reset failed");

        // Act
        SingleDataResponse actualSingleDataResponse = customerApiResponseService.customerLoginPasswordReset(resetPasswordDto, customerId);

        // Assert
        Assertions.assertEquals(expectedResponse, actualSingleDataResponse);
    }

    @Test
    @DisplayName("Should save customer OTP successfully")
    void saveCustomerOtp_Success() {
        // Arrange
        String customerEmail = "test@example.com";
        CustomerOtpDto savedCustomerOtpDto = new CustomerOtpDto();
        savedCustomerOtpDto.setCode(123456);

        when(customerService.saveCustomerOtp(customerEmail))
                .thenReturn(savedCustomerOtpDto);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("OTP saved successfully");

        // Act
        SingleDataResponse actualSingleDataResponse =customerApiResponseService.saveCustomerOtp(customerEmail);

        // Assert
        verify(customerService).saveCustomerOtp(customerEmail);
        Assertions.assertEquals(ApplicationConstants.Status.SUCCESS, actualSingleDataResponse.getStatus().getSuccess());
        Assertions.assertEquals("OTP saved successfully", actualSingleDataResponse.getStatus().getMessage());
        Assertions.assertEquals(savedCustomerOtpDto, actualSingleDataResponse.getData());
    }

    @Test
    @DisplayName("Should handle failure when saving customer OTP")
    void saveCustomerOtp_Failure() {
        // Arrange
        String customerEmail = "test@example.com";
        SingleDataResponse expectedSingleDataResponse = new SingleDataResponse();
        expectedSingleDataResponse.setResponse(ApplicationConstants.Status.FAIL,
                "Failed to save customer OTP", customerEmail);

        when(customerService.saveCustomerOtp(customerEmail))
                .thenReturn(null);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Failed to save customer OTP");

        // Act
        SingleDataResponse actualSingleDataResponse = customerApiResponseService.saveCustomerOtp(customerEmail);

        // Assert
        verify(customerService).saveCustomerOtp(customerEmail);
        Assertions.assertEquals(expectedSingleDataResponse, actualSingleDataResponse);
    }

    @Test
    @DisplayName("Should validate customer OTP successfully")
    void validateCustomerOtp_Success() {
        // Arrange
        CustomerOtpDto customerOtpDto = new CustomerOtpDto();
        customerOtpDto.setCode(123456);
        CustomerOtpDto validatedCustomerOtpDto = new CustomerOtpDto();
        validatedCustomerOtpDto.setCode(123456);

        when(customerService.validateCustomerOtp(customerOtpDto))
                .thenReturn(validatedCustomerOtpDto);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("OTP validation success");

        // Act
        SingleDataResponse actualSingleDataResponse =customerApiResponseService.validateCustomerOtp(customerOtpDto);

        // Assert
        verify(customerService).validateCustomerOtp(customerOtpDto);
        Assertions.assertEquals(ApplicationConstants.Status.SUCCESS, actualSingleDataResponse.getStatus().getSuccess());
        Assertions.assertEquals("OTP validation success", actualSingleDataResponse.getStatus().getMessage());
        Assertions.assertEquals(validatedCustomerOtpDto, actualSingleDataResponse.getData());
    }

    @Test
    void saveCreateCustomer_Success() {
        String shopifyCustomerCreationEndPoint = "https://e4d27c.myshopify.com/admin/api/2023-07/customers.json";
        String shopifyAccessToken = "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";
        List<CustomerKvDto.Addressess> addressess = new ArrayList<>();
        CustomerKvDto customerKvDto = new CustomerKvDto("Preetha Sreee", "K", "preethasree@gmail.com", true, addressess, "Password@123", "Password@123", true, true);
        CustomerCreationKVDto customerCreationKVDto = new CustomerCreationKVDto();
        customerCreationKVDto.setCustomer(customerKvDto);

        when(customerService.createCustomer(customerCreationKVDto, shopifyCustomerCreationEndPoint, shopifyAccessToken))
                .thenReturn(customerCreationKVDto);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Customer saved successfully");

        // Act
        SingleDataResponse actualSingleDataResponse =customerApiResponseService.createCustomer(customerCreationKVDto);

        // Assert
        Assertions.assertEquals(ApplicationConstants.Status.SUCCESS, actualSingleDataResponse.getStatus().getSuccess());
        Assertions.assertEquals("Customer saved successfully", actualSingleDataResponse.getStatus().getMessage());
    }

    @Test
    void testUpdateCustomerDetails_Success() {
        // Prepare mock data and dependencies
        Long customerId = 7320574099685L;
        CustomerDetailsKeyValueDto customerDetailsKeyValueDto = new CustomerDetailsKeyValueDto();
        customerDetailsKeyValueDto.setId(7320574099685L);
        customerDetailsKeyValueDto.setFirst_name("Karthik");
        customerDetailsKeyValueDto.setLast_name("K");
        customerDetailsKeyValueDto.setPhone("7358545729");
        CustomerDetailKeyValueResponseDto customerDto = new CustomerDetailKeyValueResponseDto();
        customerDto.setCustomer(customerDetailsKeyValueDto);
        String shopifyAccessToken = "yourAccessToken";
        ResponseEntity<String> mockResponseEntity = new ResponseEntity<>("{ \"customerId\": \"123\" }", HttpStatus.OK);

        // Mock the behavior of the customerService
        when(customerService.updateCustomerDetails(anyString(), anyString(), eq(shopifyAccessToken), any(Gson.class)))
                .thenReturn(customerDto);

        // Call the method under test
        SingleDataResponse result = customerApiResponseService.updateCustomerDetails(customerId, customerDto);

        // Verify the expected result
        Assertions.assertNotNull(result);
        Assertions.assertEquals(ApplicationConstants.Status.SUCCESS, result.getStatus().getSuccess());
    }
}