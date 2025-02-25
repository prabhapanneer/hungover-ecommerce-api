package com.hungover.customer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hungover.common.apiresponse.ApiListResponse;
import com.hungover.common.apiresponse.SingleDataResponse;
import com.hungover.core.dto.customer.CustomerCreationKVDto;
import com.hungover.core.dto.customer.CustomerDetailKeyValueResponseDto;
import com.hungover.core.dto.customer.CustomerMeasurementDto;
import com.hungover.core.dto.customer.CustomerMeasurementFeedbackDto;
import com.hungover.core.dto.customer.CustomerOtpDto;
import com.hungover.core.dto.customer.CustomerResetPasswordDto;
import com.hungover.core.dto.customer.CustomerWishlistDto;
import com.hungover.customer.service.CustomerApiResponseService;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.ParseException;

/**
 * Controller class for handling customer-related API endpoints.
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/customer")
public class CustomerController {

    private CustomerApiResponseService customerApiResponseService;
    private static final String ENDPOINT_VERSION = "v2";

    public CustomerController(CustomerApiResponseService customerApiResponseService) {
        this.customerApiResponseService = customerApiResponseService;
    }

    /**
     * Get customer wishlist based on customer id.
     *
     * @param customerId The customer id for which to retrieve the wishlist.
     * @return ResponseEntity containing the customer wishlist as an ApiListResponse.
     */
    @GetMapping(value = ENDPOINT_VERSION + "/{customerId}/wishlist")
    @ApiOperation(value = "Customer wishlist based on customer id",
            nickname = "Customer wishlist based on customer id",
            notes = "This endpoint for Customer wishlist based on customer id",
            produces = "application/json", consumes = "application/json")
    public ResponseEntity<ApiListResponse> getCustomerWishlistByCustomerId(
            @PathVariable(value = "customerId") String customerId) {
        return new ResponseEntity<>(customerApiResponseService.getCustomerWishlistByCustomerId(customerId),
                HttpStatus.OK);
    }

    /**
     * Get all customer wishlists grouped by variant id.
     *
     * @return ResponseEntity containing all customer wishlists as an ApiListResponse.
     */
    @GetMapping(value = ENDPOINT_VERSION + "/all/wishlist")
    @ApiOperation(value = "All Customer wishlist group by variant id",
            nickname = "All Customer wishlist group by variant id",
            notes = "This endpoint for All Customer wishlist group by variant id",
            produces = "application/json", consumes = "application/json")
    public ResponseEntity<ApiListResponse> getAllCustomerWishlist() {
        return new ResponseEntity<>(customerApiResponseService.getAllCustomerWishlist(), HttpStatus.OK);
    }

    /**
     * Get customer wishlist based on variant id.
     *
     * @param variantId The variant id for which to retrieve the wishlist.
     * @return ResponseEntity containing the customer wishlist as an ApiListResponse.
     */
    @GetMapping(value = ENDPOINT_VERSION + "/variant/{variantId}/wishlist")
    @ApiOperation(value = "Customer wishlist based on variant id",
            nickname = "Customer wishlist based on variant id",
            notes = "This endpoint for Customer wishlist based on variant id",
            produces = "application/json", consumes = "application/json")
    public ResponseEntity<ApiListResponse> getCustomerWishlistByVariantId(
            @PathVariable(value = "variantId") String variantId) {
        return new ResponseEntity<>(customerApiResponseService.getCustomerWishlistByVariantId(variantId),
                HttpStatus.OK);
    }

    /**
     * Get customer feedbacks.
     *
     * @return ResponseEntity containing the customer feedbacks as an ApiListResponse.
     */
    @GetMapping(value = ENDPOINT_VERSION + "/product/feedback")
    @ApiOperation(value = "Get customer feedback", nickname = "Get customer feedback",
            notes = "This endpoint for Get customer feedback", produces = "application/json",
            consumes = "application/json")
    public ResponseEntity<ApiListResponse> getCustomerFeedbacks() {
        return new ResponseEntity<>(customerApiResponseService.getCustomerFeedbacks(), HttpStatus.OK);
    }

    /**
     * Get customer measurement based on customer id.
     *
     * @param customerId The customer id for which to retrieve the measurement.
     * @return ResponseEntity containing the customer measurement as an ApiListResponse.
     */
    @GetMapping(value = ENDPOINT_VERSION + "/{customerId}/measurement")
    @ApiOperation(value = "Get customer measurement based on customer id",
            nickname = "Get customer measurement based on customer id",
            notes = "This endpoint for Get customer measurement based on customer id",
            produces = "application/json", consumes = "application/json")
    public ResponseEntity<ApiListResponse> getCustomerMeasurementByCustomerId(
            @PathVariable(value = "customerId") String customerId) {
        return new ResponseEntity<>(customerApiResponseService.getCustomerMeasurementByCustomerId(customerId),
                HttpStatus.OK);
    }

    @GetMapping(value = ENDPOINT_VERSION + "/{customerId}/measurement/{sizeName}")
    @ApiOperation(value = "Get customer measurement based on customer id and sizeName",
            nickname = "Get customer measurement based on customer id and sizeName",
            notes = "This endpoint for Get customer measurement based on customer id and sizeName",
            produces = "application/json", consumes = "application/json")
    public ResponseEntity<SingleDataResponse> getCustomerMeasurementByCustomerIdAndSizeName(
            @PathVariable(value = "customerId") String customerId,
            @PathVariable(value = "sizeName") String sizeName) {
        return new ResponseEntity<>(customerApiResponseService
                .getCustomerMeasurementByCustomerMeasurementId(customerId, sizeName), HttpStatus.OK);
    }

    /**
     * Delete customer measurement based on customer measurement id.
     *
     * @param customerMeasurementId The customer measurement id to be deleted.
     * @return ResponseEntity containing the status of the deletion as a SingleDataResponse.
     */
    @DeleteMapping(value = ENDPOINT_VERSION + "/measurement/{customerMeasurementId}")
    @ApiOperation(value = "Customer measurement deleted based on customer measurement id",
            nickname = "Customer measurement deleted based on customer measurement id",
            notes = "This endpoint for Customer measurement deleted based on customer measurement id",
            produces = "application/json", consumes = "application/json")
    public ResponseEntity<SingleDataResponse> deleteCustomerMeasurementById(
            @PathVariable(value = "customerMeasurementId") Integer customerMeasurementId) {
        return new ResponseEntity<>(customerApiResponseService.
                deleteCustomerMeasurementById(customerMeasurementId), HttpStatus.OK);
    }

    /**
     * Save measurement by user.
     *
     * @param customerMeasurementDto The customer measurement data to be saved.
     * @return ResponseEntity containing the saved customer measurement as a SingleDataResponse.
     * @throws ParseException If an error occurs while parsing the data.
     */
    @PostMapping(value = ENDPOINT_VERSION + "/measurement")
    @ApiOperation(value = "Save measurement by user", nickname = "Save measurement by user",
            notes = "This endpoint for Save measurement by user", produces = "application/json",
            consumes = "application/json")
    public ResponseEntity<SingleDataResponse> saveMeasurement(
            @RequestBody CustomerMeasurementDto customerMeasurementDto) throws ParseException {
        return new ResponseEntity<>(customerApiResponseService.
                saveMeasurement(customerMeasurementDto), HttpStatus.OK);
    }

    /**
     * Endpoint for updating customer measurement by customer measurement id.
     *
     * @param customerMeasurementDto The updated customer measurement data.
     * @param customerMeasurementId  The ID of the customer measurement to be updated.
     * @return ResponseEntity containing the API response for the operation.
     * @throws ParseException If there is an error in parsing the data.
     */
    @PutMapping(value = ENDPOINT_VERSION + "/customer-measurement/{customerMeasurementId}")
    @ApiOperation(value = "Update customer measurement by customer measurement id",
            nickname = "Update customer measurement by customer measurement id",
            notes = "This endpoint for Update customer measurement by customer measurement id",
            produces = "application/json", consumes = "application/json")
    public ResponseEntity<SingleDataResponse> updateCustomerMeasurement(
            @RequestBody CustomerMeasurementDto customerMeasurementDto,
            @PathVariable(value = "customerMeasurementId") Integer customerMeasurementId) throws ParseException {
        return new ResponseEntity<>(customerApiResponseService.updateCustomerMeasurement(customerMeasurementDto,
                customerMeasurementId), HttpStatus.OK);
    }

    /**
     * Save customer wishlist.
     *
     * @param customerWishlistDto The customer wishlist data to be saved.
     * @return ResponseEntity containing the saved customer wishlist as a SingleDataResponse.
     */
    @PostMapping(value = ENDPOINT_VERSION + "/wishlist")
    @ApiOperation(value = "Save customer wishlist", nickname = "Save customer wishlist",
            notes = "This endpoint for Save customer wishlist", produces = "application/json",
            consumes = "application/json")
    public ResponseEntity<SingleDataResponse> saveCustomerWishlist(
            @RequestBody CustomerWishlistDto customerWishlistDto) {
        return new ResponseEntity<>(customerApiResponseService.saveCustomerWishlist(customerWishlistDto),
                HttpStatus.OK);
    }

    /**
     * Get customer wishlist based on customer id and variant id.
     *
     * @param customerId The customer id for which to retrieve the wishlist.
     * @param variantId  The variant id for which to retrieve the wishlist.
     * @return ResponseEntity containing the customer wishlist as a SingleDataResponse.
     */
    @GetMapping(value = ENDPOINT_VERSION + "/{customerId}/variant/{variantId}")
    @ApiOperation(value = "Customer wishlist based on customer id and variant id",
            nickname = "Customer wishlist based on customer id and variant id",
            notes = "This endpoint for Customer wishlist based on customer id and variant id",
            produces = "application/json", consumes = "application/json")
    public ResponseEntity<SingleDataResponse> getCustomerWishlistByCustomerIdAndVariantId(
            @PathVariable(value = "customerId") String customerId,
            @PathVariable(value = "variantId") String variantId) {
        return new ResponseEntity<>(customerApiResponseService.
                getCustomerWishlistByCustomerIdAndVariantId(customerId, variantId), HttpStatus.OK);
    }

    /**
     * Delete customer wishlist based on customer id and variant id.
     *
     * @param customerId The customer id for which to delete the wishlist.
     * @param variantId  The variant id for which to delete the wishlist.
     * @return ResponseEntity containing the status of the deletion as a SingleDataResponse.
     */
    @DeleteMapping(value = ENDPOINT_VERSION + "/{customerId}/variant/{variantId}")
    @ApiOperation(value = "Customer wishlist based on customer id and variant id",
            nickname = "Customer wishlist based on customer id and variant id",
            notes = "This endpoint for Customer wishlist based on customer id and variant id",
            produces = "application/json", consumes = "application/json")
    public ResponseEntity<SingleDataResponse> deleteCustomerWishlistByCustomerIdAndVariantId(
            @PathVariable(value = "customerId") String customerId,
            @PathVariable(value = "variantId") String variantId) {
        return new ResponseEntity<>(customerApiResponseService.
                deleteCustomerWishlistByCustomerIdAndVariantId(customerId, variantId), HttpStatus.OK);
    }

    /**
     * Endpoint to retrieve all customers.
     *
     * @return ResponseEntity containing the API list response with customer information.
     * @throws IOException If an error occurs while processing the API request.
     */
    @GetMapping(value = ENDPOINT_VERSION + "/all")
    @ApiOperation(value = "Get all customers", nickname = "Get all customers", notes = "This endpoint for " +
            "Get all customers", produces = "application/json", consumes = "application/json")
    public ResponseEntity<ApiListResponse> getAllCustomer() throws IOException {
        return new ResponseEntity<>(customerApiResponseService.getAllCustomer(), HttpStatus.OK);
    }

    /**
     * Retrieves the count of customers registered in the last year by month from the Shopify API.
     *
     * @param lastYear    The last year for which to fetch customer count data.
     * @param currentYear The current year for which to fetch customer count data.
     * @return A ResponseEntity containing a SingleDataResponse with the customer count data.
     * @throws IOException If an I/O error occurs while making API calls.
     */
    @GetMapping(value = ENDPOINT_VERSION + "/last-year")
    @ApiOperation(value = "Get last year customers registered count", nickname = "Get last year customers " +
            "registered count", notes = "This endpoint for Get last year customers registered count",
            produces = "application/json", consumes = "application/json")
    public ResponseEntity<SingleDataResponse> getCustomerCountByMonthForYears(
            @RequestParam(value = "lastYear") int lastYear, @RequestParam(value = "currentYear") int currentYear
    ) throws IOException {
        return new ResponseEntity<>(customerApiResponseService.getCustomerCountByMonthForYears(lastYear, currentYear),
                HttpStatus.OK);
    }

    /**
     * Handles the PUT request to update the login password for a customer.
     *
     * @param resetPasswordDto The {@link CustomerResetPasswordDto} containing the new password and confirmation.
     * @param customerId       The ID of the customer for whom the password needs to be updated.
     * @return A {@link ResponseEntity} containing the {@link SingleDataResponse} with the response status
     * and the updated customer's information after the password update.
     */
    @PutMapping(value = ENDPOINT_VERSION + "/reset-password/{customerId}")
    @ApiOperation(value = "Update customer login password", nickname = "Update customer login password ",
            notes = "This endpoint for Update customer login password", produces = "application/json",
            consumes = "application/json")
    public ResponseEntity<SingleDataResponse> customerLoginPasswordReset(
            @RequestBody CustomerResetPasswordDto resetPasswordDto,
            @PathVariable(value = "customerId") Long customerId) {
        return new ResponseEntity<>(customerApiResponseService.
                customerLoginPasswordReset(resetPasswordDto, customerId), HttpStatus.OK);
    }

    /**
     * Endpoint for saving the customer's OTP code and sending it to the customer's email address.
     *
     * @param customerEmail The email address of the customer.
     * @return The ResponseEntity containing the response data for saving the OTP
     * and sending it to the customer's email.
     */
    @PostMapping(value = ENDPOINT_VERSION + "/otp")
    @ApiOperation(value = "Save customer otp code and send it to customer email",
            nickname = "Save customer otp code and send it to customer email",
            notes = "This endpoint for Save customer otp code and send it to customer email",
            produces = "application/json", consumes = "application/json")
    public ResponseEntity<SingleDataResponse> saveCustomerOtp(
            @RequestParam(value = "customerEmail") String customerEmail) {
        return new ResponseEntity<>(customerApiResponseService.saveCustomerOtp(customerEmail), HttpStatus.OK);
    }

    /**
     * Endpoint for validating the customer's OTP code.
     *
     * @param customerOtpDto The CustomerOtpDto containing the customer's email and OTP code.
     * @return The ResponseEntity containing the response data for validating the OTP code.
     */
    @PostMapping(value = ENDPOINT_VERSION + "/otp-validation")
    @ApiOperation(value = "Customer otp code validation", nickname = "Customer otp code validation",
            notes = "This endpoint for Customer otp code validation", produces = "application/json",
            consumes = "application/json")
    public ResponseEntity<SingleDataResponse> validateCustomerOtp(@RequestBody CustomerOtpDto customerOtpDto) {
        return new ResponseEntity<>(customerApiResponseService.validateCustomerOtp(customerOtpDto), HttpStatus.OK);
    }

    /**
     * Endpoint for creating a customer using the provided CustomerCreationKVDto.
     *
     * @param customerCreationKVDto The DTO (Data Transfer Object) containing customer information to be created.
     * @return A ResponseEntity containing a SingleDataResponse object with the response status, message,
     * and the created customer information.
     */
    @PostMapping(value = ENDPOINT_VERSION + "/")
    @ApiOperation(value = "Create Customer", nickname = "Create Customer", notes = "This endpoint for Create Customer",
            produces = "application/json", consumes = "application/json")
    public ResponseEntity<SingleDataResponse> createCustomer(@RequestBody CustomerCreationKVDto customerCreationKVDto) {
        return new ResponseEntity<>(customerApiResponseService.createCustomer(customerCreationKVDto), HttpStatus.OK);
    }

    /**
     * Updates customer details for a specific customer identified by their unique customer ID.
     *
     * @param customerId                        The unique identifier of the customer to update.
     * @param customerDetailKeyValueResponseDto The updated customer details to apply.
     * @return A ResponseEntity containing a SingleDataResponse with the updated customer details.
     */
    @PutMapping(value = ENDPOINT_VERSION + "/{customerId}")
    @ApiOperation(value = "Update Customer Details", nickname = "Update Customer Details",
            notes = "This endpoint for Update Customer Details", produces = "application/json",
            consumes = "application/json")
    public ResponseEntity<SingleDataResponse> updateCustomer(@PathVariable(value = "customerId") Long customerId,
                                                             @RequestBody CustomerDetailKeyValueResponseDto
                                                                     customerDetailKeyValueResponseDto) {
        return new ResponseEntity<>(customerApiResponseService.updateCustomerDetails(customerId,
                customerDetailKeyValueResponseDto), HttpStatus.OK);
    }

    /**
     * @param customerMeasurementFeedbackDto The CustomerMeasurementFeedbackDto containing
     *                                      the feedback data in JSON format.
     * @return A ResponseEntity with a SingleDataResponse, containing the response data in
     *         JSON format.
     * @throws ParseException If there is an issue with parsing the input data.
     * @throws JsonProcessingException If there is an issue with processing JSON data.
     */
    @PostMapping(value = ENDPOINT_VERSION + "/measurement/feedback")
    @ApiOperation(value = "create customer measurement feedback", nickname = "create customer measurement feedback",
            notes = "This endpoint for create customer measurement feedback",
            produces = "application/json", consumes = "application/json")
    public ResponseEntity<SingleDataResponse> saveCustomerMeasurementFeedback(
            @RequestBody CustomerMeasurementFeedbackDto customerMeasurementFeedbackDto) throws ParseException,
            JsonProcessingException {
        return new ResponseEntity<>(customerApiResponseService
                .saveCustomerMeasurementFeedback(customerMeasurementFeedbackDto), HttpStatus.OK);
    }

    /**
     * This endpoint allows you to retrieve customer measurement feedback associated with a
     * specific order by providing the order's unique identifier.
     * @param orderId The unique identifier of the order for which you want to retrieve
     *                customer measurement feedback.
     */
    @GetMapping(value = ENDPOINT_VERSION + "/order/{orderId}/measurement/feedback")
    @ApiOperation(value = "Get customer measurement feedback by order id",
            nickname = "Get customer measurement feedback by order id",
            notes = "This endpoint is for Get customer measurement feedback by order id",
            produces = "application/json", consumes = "application/json")
    public ResponseEntity<SingleDataResponse> getCustomerMeasurementFeedbackByOrderId(
            @PathVariable(value = "orderId") String orderId) {
        return new ResponseEntity<>(customerApiResponseService.getCustomerMeasurementFeedbackByOrderId(orderId),
                HttpStatus.OK);
    }

    /**
     * PUT request to update customer measurement feedback by customer measurement feedback ID.
     * This endpoint allows the update of customer measurement feedback based on the provided ID.
     *
     * @param customerMeasurementFeedbackId The ID of the customer measurement feedback to be updated.
     * @param loggedInUserId The ID of the logged-in user performing the update.
     * @return ResponseEntity with a SingleDataResponse containing the result of the operation.
     * @throws ParseException If there is an issue with parsing date values.
     * @throws JsonProcessingException If there is an issue with JSON processing.
     */
    @PutMapping(value = ENDPOINT_VERSION + "/measurement/feedback/{customerMeasurementFeedbackId}")
    @ApiOperation(value = "update customer measurement feedback by customer measurement feedback id",
            nickname = "update customer measurement feedback by customer measurement feedback id",
            notes = "This endpoint is for Get customer measurement feedback by order id",
            produces = "application/json", consumes = "application/json")
    public ResponseEntity<SingleDataResponse> updateCustomerMeasurementFeedbackByCustomerMeasurementFeedbackId(
            @PathVariable(value = "customerMeasurementFeedbackId") Integer customerMeasurementFeedbackId,
            @RequestBody CustomerMeasurementFeedbackDto customerMeasurementFeedbackDto,
            @RequestParam(value = "loggedInUserId")Integer loggedInUserId) throws ParseException,
            JsonProcessingException {
        return new ResponseEntity<>(customerApiResponseService.
                updateCustomerMeasurementFeedbackByCustomerMeasurementFeedbackId(customerMeasurementFeedbackId,
                        customerMeasurementFeedbackDto, loggedInUserId),HttpStatus.OK);
    }
}
