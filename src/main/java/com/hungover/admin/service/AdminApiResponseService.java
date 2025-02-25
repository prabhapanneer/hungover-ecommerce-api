package com.hungover.admin.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.hungover.common.apiresponse.ApiListResponse;
import com.hungover.common.apiresponse.SingleDataResponse;
import com.hungover.common.constant.ApplicationConstants;
import com.hungover.core.domain.customer.CustomerOrderStatusDetails;
import com.hungover.core.dto.customer.CustomerMeasurementDto;
import com.hungover.core.dto.customer.CustomerOrderStatusDetailsDto;
import com.hungover.core.dto.customer.CustomerOrderStatusDto;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Service class for handling API responses for admin-related operations.
 */
@Service
public class AdminApiResponseService {
    private AdminService adminService;
    private MessageSource messageSource;

    public AdminApiResponseService(AdminService adminService, MessageSource messageSource) {
        super();
        this.adminService = adminService;
        this.messageSource = messageSource;
    }

    /**
     * Save customer measurement and create a single data response for the API.
     *
     * @param customerMeasurementDto The customer measurement data to be saved.
     * @return SingleDataResponse containing the API response.
     * @throws ParseException If there is an error in parsing the data.
     */
    public SingleDataResponse saveCustomerMeasurement(CustomerMeasurementDto customerMeasurementDto)
            throws ParseException {
        SingleDataResponse singleDataResponse = new SingleDataResponse();
        CustomerMeasurementDto savedCustomerMeasurementDto =
                adminService.saveCustomerMeasurement(customerMeasurementDto, true);
        if (Optional.ofNullable(savedCustomerMeasurementDto).isPresent()) {
            singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                    messageSource.getMessage("api.customer.measurement.saved.success", null,
                            Locale.ENGLISH), savedCustomerMeasurementDto);
        }
        else {
            singleDataResponse.setResponse(ApplicationConstants.Status.FAIL,
                    messageSource.getMessage("api.customer.measurement.saved.fail", null, Locale.ENGLISH),
                    customerMeasurementDto);
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
        CustomerMeasurementDto updatedCustomerMeasurementDto =
                adminService.updateCustomerMeasurement(customerMeasurementDto, customerMeasurementId);
        if (Optional.ofNullable(updatedCustomerMeasurementDto).isPresent()) {
            singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                    messageSource.getMessage("api.customer.measurement.update.success", null,
                            Locale.ENGLISH), updatedCustomerMeasurementDto);
        } else {
            singleDataResponse.setResponse(ApplicationConstants.Status.FAIL,
                    messageSource.getMessage("api.customer.measurement.update.fail", null, Locale.ENGLISH),
                    customerMeasurementDto);
        }
        return singleDataResponse;
    }

    /**
     * Send a login issue email to the admin and create a single data response for the API.
     *
     * @param userName    The name of the user reporting the login issue.
     * @param userEmail   The email of the user reporting the login issue.
     * @param userMessage The message describing the login issue.
     * @return SingleDataResponse containing the API response.
     * @throws ParseException If there is an error in parsing the data.
     */
    public SingleDataResponse sendLoginIssueMailForAdmin(String userName, String userEmail, String userMessage) {
        SingleDataResponse singleDataResponse = new SingleDataResponse();
        adminService.sendLoginIssueMailForAdmin(userName, userEmail, userMessage);
        singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                messageSource.getMessage("api.mail.sent.success", null, Locale.ENGLISH), null);
        return singleDataResponse;
    }

    /**
     * Saves a list of customer order status DTOs to the database and returns an API response.
     *
     * @param customerOrderStatusDtoList The list of customer order status DTOs to be saved.
     * @return An API response indicating the success or failure of the operation.
     */
    public ApiListResponse saveCustomerOrderStatus(List<CustomerOrderStatusDto> customerOrderStatusDtoList,
        String addressInformationDto) throws JsonProcessingException {
        ApiListResponse apiListResponse = new ApiListResponse();
        List<CustomerOrderStatusDto> savedCustomerOrderStatusDtoList = adminService.saveCustomerOrderStatus(
                customerOrderStatusDtoList,addressInformationDto);
        if (Objects.nonNull(savedCustomerOrderStatusDtoList)) {
            apiListResponse.setResponse(ApplicationConstants.Status.SUCCESS, messageSource.
                            getMessage("api.customer.order.status.saved.success", null, Locale.ENGLISH),
                    savedCustomerOrderStatusDtoList);
        } else {
            apiListResponse.setResponse(ApplicationConstants.Status.FAIL,
                    messageSource.getMessage("api.customer.order.status.saved.fail", null, Locale.ENGLISH),
                    null);
        }
        return apiListResponse;
    }

    /**
     * Updates the customer order status with the specified ID and sends a status notification email to the customer.
     *
     * @param customerOrderStatusId The ID of the customer order status to update.
     * @return A response containing the updated customer order status DTO.
     *
     */
    public SingleDataResponse updateCustomerOrderStatus(Integer customerOrderStatusId,String orderStatus,
                                                        String orderTrackingNumber, String addressInformationDto)
            throws JsonProcessingException {
        SingleDataResponse singleDataResponse = new SingleDataResponse();
        CustomerOrderStatusDto updatedCustomerOrderStatusDto = adminService.
                updateCustomerOrderStatus(customerOrderStatusId,orderStatus, orderTrackingNumber,addressInformationDto);
        if (Objects.nonNull(updatedCustomerOrderStatusDto)) {
            singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS, messageSource.
                            getMessage("api.customer.order.status.updated.success", null, Locale.ENGLISH),
                    updatedCustomerOrderStatusDto);
        } else {
            singleDataResponse.setResponse(ApplicationConstants.Status.FAIL, messageSource.
                    getMessage("api.customer.order.status.updated.fail", null, Locale.ENGLISH), null);
        }
        return singleDataResponse;
    }

    /**
     * Retrieves customer order status details and generates an API response.
     *
     * @return An API response containing the customer order status details or an error message.
     */
    public ApiListResponse getCustomerOrderStatusDetails() {
        ApiListResponse apiListResponse = new ApiListResponse();
        List<CustomerOrderStatusDetailsDto> customerOrderStatusDetailsDtoList = adminService
                .getCustomerOrderStatusDetails();
        if(Optional.ofNullable(customerOrderStatusDetailsDtoList).isPresent()){
            apiListResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                    messageSource.getMessage("api.customer.order.status.details.get.success", null,
                            Locale.ENGLISH),customerOrderStatusDetailsDtoList);
        }else {
            apiListResponse.setResponse(ApplicationConstants.Status.FAIL,
                    messageSource.getMessage("api.customer.order.status.details.get.failed", null,
                            Locale.ENGLISH),null);
        }
        return apiListResponse;
    }
}
