package com.hungover.admin.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hungover.admin.service.AdminApiResponseService;
import com.hungover.common.apiresponse.ApiListResponse;
import com.hungover.common.apiresponse.SingleDataResponse;
import com.hungover.core.dto.customer.CustomerMeasurementDto;
import com.hungover.core.dto.customer.CustomerOrderStatusDto;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.text.ParseException;
import java.util.List;

/**
 * Controller class for handling admin-related operations.
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/admin")
public class AdminController {
    private final AdminApiResponseService adminApiResponseService;
    private static final String ENDPOINT_VERSION = "v2";

    public AdminController(AdminApiResponseService adminApiResponseService) {
        super();
        this.adminApiResponseService = adminApiResponseService;
    }

    /**
     * Endpoint for saving customer measurement.
     *
     * @param customerMeasurementDto The customer measurement data to be saved.
     * @return ResponseEntity containing the API response for the operation.
     * @throws ParseException If there is an error in parsing the data.
     */
    @PostMapping(value = ENDPOINT_VERSION+"/customer-measurement")
    @ApiOperation(value = "Save customer measurement", nickname = "Save customer measurement",
            notes = "This endpoint for Save customer measurement", produces = "application/json",
            consumes = "application/json")
    public ResponseEntity<SingleDataResponse> saveCustomerMeasurement(
            @RequestBody CustomerMeasurementDto customerMeasurementDto) throws ParseException {
        return new ResponseEntity<>(adminApiResponseService.saveCustomerMeasurement(customerMeasurementDto),
                HttpStatus.OK);
    }

    /**
     * Endpoint for updating customer measurement by customer measurement id.
     *
     * @param customerMeasurementDto The updated customer measurement data.
     * @param customerMeasurementId  The ID of the customer measurement to be updated.
     * @return ResponseEntity containing the API response for the operation.
     * @throws ParseException If there is an error in parsing the data.
     */
    @PutMapping(value = ENDPOINT_VERSION+"/customer-measurement/{customerMeasurementId}")
    @ApiOperation(value = "Update customer measurement by customer measurement id",
            nickname = "Update customer measurement by customer measurement id",
            notes = "This endpoint for Update customer measurement by customer measurement id",
            produces = "application/json", consumes = "application/json")
    public ResponseEntity<SingleDataResponse> updateCustomerMeasurement(
            @RequestBody CustomerMeasurementDto customerMeasurementDto,
            @PathVariable(value = "customerMeasurementId") Integer customerMeasurementId)
            throws ParseException {
        return new ResponseEntity<>(adminApiResponseService.updateCustomerMeasurement(customerMeasurementDto,
                customerMeasurementId), HttpStatus.OK);
    }

    /**
     * Endpoint for sending a login issue email to the admin.
     *
     * @param userName    The name of the user reporting the login issue.
     * @param userEmail   The email of the user reporting the login issue.
     * @param userMessage The message describing the login issue.
     * @return ResponseEntity containing the API response for the operation.
     * @throws ParseException If there is an error in parsing the data.
     */
    @PutMapping(value = ENDPOINT_VERSION+"/user/login-issue")
    @ApiOperation(value = "User login issue mail for admin",
            nickname = "User login issue mail for admin",
            notes = "This endpoint for User login issue mail for admin",
            produces = "application/json", consumes = "application/json")
    public ResponseEntity<SingleDataResponse> sendLoginIssueMailForAdmin(
            @RequestParam(value = "userName") String userName,
            @RequestParam(value = "userEmail") String userEmail,
            @RequestParam(value = "userMessage") String userMessage) throws ParseException {
        return new ResponseEntity<>(adminApiResponseService.sendLoginIssueMailForAdmin(userName,
                userEmail, userMessage), HttpStatus.OK);
    }

    /**
     * Endpoint to save customer order status.
     *
     * @param customerOrderStatusDtoList The list of customer order status DTOs to be saved.
     * @return A ResponseEntity containing the API response with a HTTP status of 200 (OK).
     */
    @PostMapping(value = ENDPOINT_VERSION+"/customer-order-status")
    @ApiOperation(value = "Customer order status save", nickname = "Customer order status save",
            notes = "This endpoint for Customer order status save", produces = "application/json",
            consumes = "application/json")
    public ResponseEntity<ApiListResponse> saveCustomerOrderStatus(
            @RequestBody List<CustomerOrderStatusDto> customerOrderStatusDtoList,
            @RequestParam(value = "addressInformation") String addressInformation) throws JsonProcessingException {
        return new ResponseEntity<>(adminApiResponseService.saveCustomerOrderStatus(customerOrderStatusDtoList,
                addressInformation), HttpStatus.OK);
    }

    /**
     * Updates the customer order status with the specified ID and sends a status notification email to the customer.
     *
     * @param customerOrderStatusId The ID of the customer order status to update.
     * @return A response entity containing the updated customer order status DTO.
     */
    @PutMapping(value = ENDPOINT_VERSION+"/customer-order-status/{customerOrderStatusId}")
    @ApiOperation(value = "Customer order status update", nickname = "Customer order status update",
            notes = "This endpoint for Customer order status update", produces = "application/json",
            consumes = "application/json")
    public ResponseEntity<SingleDataResponse> updateCustomerOrderStatus(
            @PathVariable(value = "customerOrderStatusId") Integer customerOrderStatusId,
            @RequestParam(value = "orderStatus") String orderStatus,
            @RequestParam(value = "orderTrackingNumber", required = false) String orderTrackingNumber,
            @RequestParam(value = "addressInformation") String addressInformation) throws JsonProcessingException {
        return new ResponseEntity<>(adminApiResponseService.updateCustomerOrderStatus(customerOrderStatusId,orderStatus,
                orderTrackingNumber,addressInformation), HttpStatus.OK);
    }

    /**
     * Retrieves customer order status details by customer order status details id.
     *
     * @return A response entity containing the customer order status details.
     */
    @GetMapping(value = ENDPOINT_VERSION + "/customer-order-status-details")
    @ApiOperation(value = "Get customer order status details by customer order status details id",
            nickname = "Get customer order status details by customer order status details id",
            notes = "This endpoint is for Get customer order status details by customer order status details id",
            produces = "application/json", consumes = "application/json")
    public ResponseEntity<ApiListResponse> getCustomerOrderStatusDetails(){
         return new ResponseEntity<>(adminApiResponseService
                 .getCustomerOrderStatusDetails(), HttpStatus.OK);
    }

}
