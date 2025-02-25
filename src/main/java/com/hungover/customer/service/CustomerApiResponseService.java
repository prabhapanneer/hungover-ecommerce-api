package com.hungover.customer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.hungover.common.apiresponse.ApiListResponse;
import com.hungover.common.apiresponse.SingleDataResponse;
import com.hungover.common.constant.ApplicationConstants;
import com.hungover.common.exception.RecordNotFoundException;
import com.hungover.core.dto.customer.CustomerCountResponseDto;
import com.hungover.core.dto.customer.CustomerCreationKVDto;
import com.hungover.core.dto.customer.CustomerDetailKeyValueResponseDto;
import com.hungover.core.dto.customer.CustomerFeedbackKVDto;
import com.hungover.core.dto.customer.CustomerKeyValueDto;
import com.hungover.core.dto.customer.CustomerMeasurementDto;
import com.hungover.core.dto.customer.CustomerMeasurementFeedbackDto;
import com.hungover.core.dto.customer.CustomerOtpDto;
import com.hungover.core.dto.customer.CustomerResetPasswordDto;
import com.hungover.core.dto.customer.CustomerWishlistDto;
import com.hungover.core.dto.customer.CustomerWishlistKVDto;
import com.hungover.core.dto.customer.ResetPasswordDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Service class for handling customer API response operations.
 */
@Service
public class CustomerApiResponseService {

    private CustomerService customerService;
    private MessageSource messageSource;

    @Value("${shopifyAccessToken}")
    String shopifyAccessToken;

    @Value("${shopifyCustomerEndPoint}")
    String shopifyCustomerEndPoint;
    @Value("${shopifyCustomerCreationEndPoint}")
    String shopifyCustomerCreationEndPoint;
    @Value("${shopifyUpdateCustomerDetailsEndPoint}")
    String shopifyUpdateCustomerDetailsEndPoint;

    public CustomerApiResponseService(CustomerService customerService, MessageSource messageSource) {
        super();
        this.customerService = customerService;
        this.messageSource = messageSource;
    }

    /**
     * Get customer wishlist based on customer id.
     *
     * @param customerId The customer id for which to retrieve the wishlist.
     * @return ApiListResponse containing the customer wishlist.
     */
    public ApiListResponse getCustomerWishlistByCustomerId(String customerId) {
        ApiListResponse apiListResponse = new ApiListResponse();
        List<CustomerWishlistDto> customerWishlistDtoList =
                customerService.getCustomerWishlistByCustomerId(customerId);
        apiListResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                messageSource.getMessage("api.customer.wishlist.success", null, Locale.ENGLISH),
                customerWishlistDtoList);
        return apiListResponse;
    }

    /**
     * Get all customer wishlists grouped by variant id.
     *
     * @return ApiListResponse containing all customer wishlists.
     */
    public ApiListResponse getAllCustomerWishlist() {
        ApiListResponse apiListResponse = new ApiListResponse();
        List<CustomerWishlistKVDto> customerWishlistDtoList = customerService.getAllCustomerWishlist();
        if (!customerWishlistDtoList.isEmpty()) {
            apiListResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                    messageSource.getMessage("api.customer.wishlist.success", null, Locale.ENGLISH),
                    customerWishlistDtoList);
        } else {
            apiListResponse.setResponse(ApplicationConstants.Status.FAIL,
                    messageSource.getMessage("api.customer.wishlist.fail", null, Locale.ENGLISH),
                    null);
        }
        return apiListResponse;
    }

    /**
     * Get customer wishlist based on variant id.
     *
     * @param variantId The variant id for which to retrieve the wishlist.
     * @return ApiListResponse containing the customer wishlist.
     */
    public ApiListResponse getCustomerWishlistByVariantId(String variantId) {
        ApiListResponse apiListResponse = new ApiListResponse();
        List<CustomerWishlistDto> customerWishlistDtoList =
                customerService.getCustomerWishlistByVariantId(variantId);
        apiListResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                messageSource.getMessage("api.customer.wishlist.success", null, Locale.ENGLISH),
                customerWishlistDtoList);
        return apiListResponse;
    }

    /**
     * Get customer feedbacks.
     *
     * @return ApiListResponse containing the customer feedbacks.
     */
    public ApiListResponse getCustomerFeedbacks() {
        ApiListResponse apiListResponse = new ApiListResponse();
        List<CustomerFeedbackKVDto> customerFeedbackKVDtoList = customerService.getCustomerFeedback();
        if (!(customerFeedbackKVDtoList.isEmpty())) {
            apiListResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                    messageSource.getMessage("api.customer.feedback.success", null, Locale.ENGLISH),
                    customerFeedbackKVDtoList);
            apiListResponse.setTotalResults(customerFeedbackKVDtoList.size());
        } else {
            apiListResponse.setResponse(ApplicationConstants.Status.FAIL,
                    messageSource.getMessage("api.customer.feedback.fail", null, Locale.ENGLISH),
                    null);
        }
        return apiListResponse;
    }

    /**
     * Get customer measurement based on customer id.
     *
     * @param customerId The customer id for which to retrieve the measurement.
     * @return ApiListResponse containing the customer measurement.
     */
    public ApiListResponse getCustomerMeasurementByCustomerId(String customerId) {
        ApiListResponse apiListResponse = new ApiListResponse();
        List<CustomerMeasurementDto> customerMeasurementDtoList =
                customerService.getCustomerMeasurementByCustomerId(customerId);
        apiListResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                messageSource.getMessage("api.customer.measurement.success",
                        null, Locale.ENGLISH), customerMeasurementDtoList);
        return apiListResponse;
    }

    /**
     * Get customer measurement based on customer measurement id.
     *
     * @param customerId The customer id for which to retrieve the measurement.
     * @return ApiListResponse containing the customer measurement.
     */
    public SingleDataResponse getCustomerMeasurementByCustomerMeasurementId(String customerId,String sizeName) {
        SingleDataResponse singleDataResponse = new SingleDataResponse();
        CustomerMeasurementDto customerMeasurementDtoObj =
                customerService.getCustomerMeasurementByCustomerMeasurementId(customerId,sizeName);
        singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                messageSource.getMessage("api.customer.measurement.success",
                        null, Locale.ENGLISH), customerMeasurementDtoObj);
        return singleDataResponse;
    }

    /**
     * Delete customer measurement based on customer measurement id.
     *
     * @param customerMeasurementId The customer measurement id to be deleted.
     * @return SingleDataResponse containing the status of the deletion.
     */
    public SingleDataResponse deleteCustomerMeasurementById(Integer customerMeasurementId) {
        SingleDataResponse singleDataResponse = new SingleDataResponse();
        CustomerMeasurementDto customerMeasurementDto =
                customerService.deleteCustomerMeasurementById(customerMeasurementId);
        singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                messageSource.getMessage("api.customer.measurement.delete.success",
                        null, Locale.ENGLISH), customerMeasurementDto);
        return singleDataResponse;
    }

    /**
     * Save measurement by user.
     *
     * @param customerMeasurementDto The customer measurement data to be saved.
     * @return SingleDataResponse containing the saved customer measurement.
     * @throws ParseException If an error occurs while parsing the data.
     */
    public SingleDataResponse saveMeasurement(CustomerMeasurementDto customerMeasurementDto) throws ParseException {
        SingleDataResponse singleDataResponse = new SingleDataResponse();
        CustomerMeasurementDto savedCustomerMeasurementDto =
                customerService.saveMeasurement(customerMeasurementDto);
        if (Optional.ofNullable(savedCustomerMeasurementDto).isPresent()) {
            singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                    messageSource.getMessage("api.customer.measurement.saved.success",
                            null, Locale.ENGLISH), savedCustomerMeasurementDto);
        } else {
            singleDataResponse.setResponse(ApplicationConstants.Status.FAIL,
                    messageSource.getMessage("api.customer.measurement.saved.fail",
                            null, Locale.ENGLISH), customerMeasurementDto);
        }
        return singleDataResponse;
    }

    /**
     * Update customer measurement by customer measurement id and create a single data response for the API.
     *
     * @param customerMeasurementDto The updated customer measurement data.
     * @param customerMeasurementId  The ID of the customer measurement to be updated.
     * @return SingleDataResponse containing the API response.
     * @throws ParseException If there is an error in parsing the data.
     */
    public SingleDataResponse updateCustomerMeasurement(CustomerMeasurementDto customerMeasurementDto,
                                                        Integer customerMeasurementId) throws ParseException {
        SingleDataResponse singleDataResponse = new SingleDataResponse();
        CustomerMeasurementDto updatedCustomerMeasurementDto = customerService.
                updateCustomerMeasurement(customerMeasurementDto, customerMeasurementId);
        if (Optional.ofNullable(updatedCustomerMeasurementDto).isPresent()) {
            singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS, messageSource.
                            getMessage("api.customer.measurement.update.success", null, Locale.ENGLISH),
                    updatedCustomerMeasurementDto);
        } else {
            singleDataResponse.setResponse(ApplicationConstants.Status.FAIL,
                    messageSource.getMessage("api.customer.measurement.update.fail", null, Locale.ENGLISH),
                    customerMeasurementDto);
        }
        return singleDataResponse;
    }

    /**
     * Save customer wishlist.
     *
     * @param customerWishlistDto The customer wishlist data to be saved.
     * @return SingleDataResponse containing the saved customer wishlist.
     */
    public SingleDataResponse saveCustomerWishlist(CustomerWishlistDto customerWishlistDto) {
        SingleDataResponse singleDataResponse = new SingleDataResponse();
        CustomerWishlistDto savedCustomerWishlistDto =
                customerService.saveCustomerWishlist(customerWishlistDto);
        if (Optional.ofNullable(savedCustomerWishlistDto).isPresent()) {
            singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                    messageSource.getMessage("api.save.customer.wishlist.success",
                            null, Locale.ENGLISH), savedCustomerWishlistDto);
        } else {
            singleDataResponse.setResponse(ApplicationConstants.Status.FAIL,
                    messageSource.getMessage("api.save.customer.wishlist.fail",
                            null, Locale.ENGLISH), customerWishlistDto);
        }
        return singleDataResponse;
    }

    /**
     * Get customer wishlist based on customer id and variant id.
     *
     * @param customerId The customer id for which to retrieve the wishlist.
     * @param variantId  The variant id for which to retrieve the wishlist.
     * @return SingleDataResponse containing the customer wishlist.
     */
    public SingleDataResponse getCustomerWishlistByCustomerIdAndVariantId(String customerId, String variantId) {
        SingleDataResponse singleDataResponse = new SingleDataResponse();
        CustomerWishlistDto existingCustomerWishlistDto =
                customerService.getCustomerWishlistByCustomerIdAndVariantId(customerId, variantId);
        singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                messageSource.getMessage("api.get.customer.wishlist.customerId.and.variantId.success",
                        null, Locale.ENGLISH), existingCustomerWishlistDto);
        return singleDataResponse;
    }

    /**
     * Delete customer wishlist based on customer id and variant id.
     *
     * @param customerId The customer id for which to delete the wishlist.
     * @param variantId  The variant id for which to delete the wishlist.
     * @return SingleDataResponse containing the status of the deletion.
     */
    public SingleDataResponse deleteCustomerWishlistByCustomerIdAndVariantId(String customerId, String variantId) {
        SingleDataResponse singleDataResponse = new SingleDataResponse();
        CustomerWishlistDto existingCustomerWishlistDto =
                customerService.deleteCustomerWishlistByCustomerIdAndVariantId(customerId, variantId);
        singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                messageSource.getMessage(
                        "api.delete.customer.wishlist.customerId.and.variantId.success", null,
                        Locale.ENGLISH), existingCustomerWishlistDto);
        return singleDataResponse;
    }

    /**
     * Retrieve all customers and create the API list response.
     *
     * @return ApiListResponse containing the customer information.
     * @throws IOException If an error occurs while processing the API request.
     */
    public ApiListResponse getAllCustomer() throws IOException {
        ApiListResponse apiListResponse = new ApiListResponse();
        List<CustomerKeyValueDto> customerKeyValueDtoList = customerService.
                getAllCustomerListFromShopify(shopifyCustomerEndPoint, shopifyAccessToken);
        if (!(customerKeyValueDtoList.isEmpty())) {
            apiListResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                    messageSource.getMessage("api.get.all.customer.success", null, Locale.ENGLISH),
                    customerKeyValueDtoList);
            apiListResponse.setTotalResults(customerKeyValueDtoList.size());
        } else {
            apiListResponse.setResponse(ApplicationConstants.Status.FAIL, messageSource
                    .getMessage("api.get.all.customer.fail", null, Locale.ENGLISH), null);
        }
        return apiListResponse;
    }

    /**
     * Retrieves customer count data by month for the specified years from the Shopify API.
     *
     * @param lastYear    The last year for which to fetch customer count data.
     * @param currentYear The current year for which to fetch customer count data.
     * @return A SingleDataResponse containing the customer count data for the specified years.
     * @throws IOException    If an I/O error occurs while making API calls.
     */
    public SingleDataResponse getCustomerCountByMonthForYears(int lastYear, int currentYear) throws IOException {
        SingleDataResponse singleDataResponseObj = new SingleDataResponse();
        CustomerCountResponseDto customerCountResponseDtoObj = customerService.
                getCustomerCountByMonthForYears(shopifyCustomerEndPoint, lastYear, currentYear, shopifyAccessToken);
        if (Optional.ofNullable(customerCountResponseDtoObj).isPresent()) {
            singleDataResponseObj.setResponse(ApplicationConstants.Status.SUCCESS,
                    messageSource.getMessage("api.get.last.year.customer.count.success", null,
                            Locale.ENGLISH), customerCountResponseDtoObj);
        } else {
            singleDataResponseObj.setResponse(ApplicationConstants.Status.FAIL,
                    messageSource.getMessage("api.get.last.year.customer.count.fail", null, Locale.ENGLISH),
                    null);
        }
        return singleDataResponseObj;
    }

    /**
     * Handles the password reset request from a customer on Shopify.
     *
     * @param resetPasswordDto The {@link CustomerResetPasswordDto} containing the new password and confirmation.
     * @param customerId       The ID of the customer for whom the password needs to be reset.
     * @return The {@link SingleDataResponse} containing the response status and the
     * updated customer's information after the password reset.
     */
    public SingleDataResponse customerLoginPasswordReset(CustomerResetPasswordDto resetPasswordDto, Long customerId) {
        SingleDataResponse singleDataResponse = new SingleDataResponse();
        ResetPasswordDto updatedResetPasswordDtoObj = customerService.customerLoginPasswordReset(resetPasswordDto,
                customerId, shopifyAccessToken);
        if (Optional.ofNullable(updatedResetPasswordDtoObj).isPresent()) {
            singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                    messageSource.getMessage("api.reset.password.success", null, Locale.ENGLISH),
                    updatedResetPasswordDtoObj);
        } else {
            singleDataResponse.setResponse(ApplicationConstants.Status.FAIL,
                    messageSource.getMessage("api.reset.password.fail", null, Locale.ENGLISH),
                    resetPasswordDto);
        }
        return singleDataResponse;
    }

    /**
     * Generates and saves an OTP (One-Time Password) for a customer based on their email.
     *
     * @param customerEmail The email of the customer for whom the OTP is generated and saved.
     * @return The {@link SingleDataResponse} object containing the response data for saving the customer OTP.
     */
    public SingleDataResponse saveCustomerOtp(String customerEmail) {
        SingleDataResponse singleDataResponse = new SingleDataResponse();
        CustomerOtpDto savedCustomerOtpDtoObj = customerService.saveCustomerOtp(customerEmail);
        if (Optional.ofNullable(savedCustomerOtpDtoObj).isPresent()) {
            singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                    messageSource.getMessage("api.customer.otp.success", null, Locale.ENGLISH),
                    savedCustomerOtpDtoObj);
        } else {
            singleDataResponse.setResponse(ApplicationConstants.Status.FAIL,
                    messageSource.getMessage("api.customer.otp.fail", null, Locale.ENGLISH),
                    customerEmail);
        }
        return singleDataResponse;
    }

    /**
     * Validates the customer's OTP (One-Time Password) based on the provided CustomerOtpDto.
     *
     * @param customerOtpDto The CustomerOtpDto containing the customer's email and OTP code to validate.
     * @return The {@link SingleDataResponse} object containing the response data for the validation result.
     * @throws RecordNotFoundException if the OTP code is not found or is invalid.
     */
    public SingleDataResponse validateCustomerOtp(CustomerOtpDto customerOtpDto) {
        SingleDataResponse singleDataResponse = new SingleDataResponse();
        CustomerOtpDto existingCustomerOtpDtoObj = customerService.validateCustomerOtp(customerOtpDto);
        singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                messageSource.getMessage("api.customer.otp.code.validation.success", null, Locale.ENGLISH),
                existingCustomerOtpDtoObj);
        return singleDataResponse;
    }

    /**
     * Creates a customer using the provided CustomerCreationKVDto and encapsulates the response in a
     * SingleDataResponse object.
     *
     * @param customerCreationKVDto The DTO (Data Transfer Object) containing customer information to be created.
     * @return A SingleDataResponse object containing the response status, message,
     * and the created customer information.
     */
    public SingleDataResponse createCustomer(CustomerCreationKVDto customerCreationKVDto) {
        SingleDataResponse singleDataResponse = new SingleDataResponse();
        CustomerCreationKVDto savedCustomerCreationKVDto = customerService.createCustomer(
                customerCreationKVDto, shopifyCustomerCreationEndPoint, shopifyAccessToken);
        singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                messageSource.getMessage("api.customer.saved.success", null, Locale.ENGLISH),
                savedCustomerCreationKVDto);
        return singleDataResponse;
    }

    /**
     * Updates customer details for a specific customer identified by their unique customer ID.
     *
     * @param customerId                     The unique identifier of the customer to update.
     * @param customerDetailKeyValueResponseDto The updated customer details to apply.
     * @return A SingleDataResponse containing the updated customer details or an error message.
     */
    public SingleDataResponse updateCustomerDetails(Long customerId,
        CustomerDetailKeyValueResponseDto customerDetailKeyValueResponseDto) {
        SingleDataResponse singleDataResponse = new SingleDataResponse();
        Gson gson = new Gson();
        String customerUpdationJsonPayload = gson.toJson(customerDetailKeyValueResponseDto);
        CustomerDetailKeyValueResponseDto updatedCustomerDetailKeyValueResponseDto =
                customerService.updateCustomerDetails(customerUpdationJsonPayload,
                        String.format(shopifyUpdateCustomerDetailsEndPoint, customerId), shopifyAccessToken, gson);
        singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                messageSource.getMessage("api.customer.updation.success", null, Locale.ENGLISH),
                updatedCustomerDetailKeyValueResponseDto);
        return singleDataResponse;
    }

    /**
     * Save customer measurement feedback and return a SingleDataResponse.
     *
     * @param customerMeasurementFeedbackDto The DTO containing feedback data.
     * @return SingleDataResponse with the operation result and feedback data.
     * @throws ParseException If there is an issue with date parsing.
     * @throws JsonProcessingException If there is an issue with JSON processing.
     */
    public SingleDataResponse saveCustomerMeasurementFeedback(
            CustomerMeasurementFeedbackDto customerMeasurementFeedbackDto) throws ParseException,
            JsonProcessingException {
        SingleDataResponse singleDataResponse = new SingleDataResponse();
        CustomerMeasurementFeedbackDto savedCustomerMeasurementFeedbackDtoObj = customerService
                .saveCustomerMeasurementFeedback(customerMeasurementFeedbackDto);
        if (Optional.ofNullable(savedCustomerMeasurementFeedbackDtoObj).isPresent()) {
            singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                    messageSource.getMessage("api.customer.measurement.feedback.saved.success",
                            null, Locale.ENGLISH), savedCustomerMeasurementFeedbackDtoObj);
        } else {
            singleDataResponse.setResponse(ApplicationConstants.Status.FAIL,
                    messageSource.getMessage("api.customer.measurement.feedback.saved.fail",
                            null, Locale.ENGLISH), customerMeasurementFeedbackDto);
        }
        return singleDataResponse;
    }

    /**
     * Retrieve customer measurement feedback by order ID and return a SingleDataResponse.
     *
     * @param orderId The order ID used to fetch feedback data.
     * @return SingleDataResponse with the operation result and feedback data.
     */
    public SingleDataResponse getCustomerMeasurementFeedbackByOrderId(String orderId) {
        SingleDataResponse singleDataResponse = new SingleDataResponse();
        CustomerMeasurementFeedbackDto customerMeasurementFeedbackDto = customerService.
                getCustomerMeasurementFeedbackByOrderId(orderId);
        if(Optional.ofNullable(customerMeasurementFeedbackDto).isPresent()){
            singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                    messageSource.getMessage("api.customer.measurement.feedback.get.success", null,
                            Locale.ENGLISH),customerMeasurementFeedbackDto);
        }else {
            singleDataResponse.setResponse(ApplicationConstants.Status.FAIL,
                    messageSource.getMessage("api.customer.measurement.feedback.get.fail", null,
                            Locale.ENGLISH),null);
        }
        return singleDataResponse;
    }

    /**
     * Update customer measurement feedback by ID and return a SingleDataResponse.
     *
     * @param customerMeasurementFeedbackId The ID of the feedback to update.
     * @param loggedInUserId The ID of the logged-in user performing the update.
     * @return SingleDataResponse with the operation result and updated feedback data.
     * @throws ParseException If there is an issue with date parsing.
     * @throws JsonProcessingException If there is an issue with JSON processing.
     */
    public SingleDataResponse updateCustomerMeasurementFeedbackByCustomerMeasurementFeedbackId(
            Integer customerMeasurementFeedbackId, CustomerMeasurementFeedbackDto customerMeasurementFeedbackDto,
            Integer loggedInUserId) throws ParseException, JsonProcessingException {
        SingleDataResponse singleDataResponse = new SingleDataResponse();

        CustomerMeasurementFeedbackDto uodatedCustomerMeasurementFeedbackDto = customerService.
                updateCustomerMeasurementFeedbackByCustomerMeasurementId(customerMeasurementFeedbackId,
                        customerMeasurementFeedbackDto, loggedInUserId);
        if(Optional.ofNullable(customerMeasurementFeedbackDto).isPresent()){
            singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                    messageSource.getMessage("api.customer.measurement.feedback.update.success", null,
                            Locale.ENGLISH),uodatedCustomerMeasurementFeedbackDto);
        }else {
            singleDataResponse.setResponse(ApplicationConstants.Status.FAIL,
                    messageSource.getMessage("api.customer.measurement.feedback.update.fail", null,
                            Locale.ENGLISH), null);
        }
       return singleDataResponse;
    }
}
