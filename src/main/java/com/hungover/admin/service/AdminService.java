package com.hungover.admin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hungover.admin.repository.CustomerOrderStatusCustomImpl;
import com.hungover.admin.repository.CustomerOrderStatusDetailsRepositoryI;
import com.hungover.admin.repository.CustomerOrderStatusRepositoryI;
import com.hungover.common.constant.ApplicationConstants;
import com.hungover.common.exception.RecordNotFoundException;
import com.hungover.common.exception.UniqueRecordException;
import com.hungover.common.util.AppUtil;
import com.hungover.core.domain.customer.CustomerMeasurement;
import com.hungover.core.domain.customer.CustomerOrderStatus;
import com.hungover.core.domain.customer.CustomerOrderStatusDetails;
import com.hungover.core.domain.notification.Notification;
import com.hungover.core.dto.customer.AddressInformationDto;
import com.hungover.core.dto.customer.CustomerMeasurementDto;
import com.hungover.core.dto.customer.CustomerOrderStatusDetailsDto;
import com.hungover.core.dto.customer.CustomerOrderStatusDto;
import com.hungover.core.dto.customer.OrderCartDto;
import com.hungover.customer.repository.CustomerMeasurementRepositoryI;
import com.hungover.email.notification.service.AdminEmailNotificationService;
import com.hungover.email.notification.service.EmailNotificationService;
import com.hungover.util.AdminMapperUtil;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for handling admin-related operations.
 */
@Service
public class AdminService {
    private final CustomerMeasurementRepositoryI customerMeasurementRepository;
    private final EmailNotificationService emailNotificationService;
    private final MessageSource messageSource;
    private final VelocityEngine velocityEngine;
    private final AdminMapperUtil adminMapperUtil;
    private final AdminEmailNotificationService adminEmailNotificationService;
    private final CustomerOrderStatusRepositoryI customerOrderStatusRepository;
    private final CustomerOrderStatusDetailsRepositoryI customerOrderStatusDetailsRepositoryI;
    private final ModelMapper modelMapper;
    private static final String DATE_FORMAT_DD_MM_YYYY = "dd/MM/yyyy";
    @Value("${from.email}")
    private String fromEmail;
    @Value("${server.url}")
    private String serverUrl;
    private final CustomerOrderStatusCustomImpl customerOrderStatusCustomImpl;

    public AdminService(CustomerMeasurementRepositoryI customerMeasurementRepository,
                        EmailNotificationService emailNotificationService, MessageSource messageSource,
                        AdminMapperUtil adminMapperUtil, AdminEmailNotificationService adminEmailNotificationService,
                        CustomerOrderStatusRepositoryI customerOrderStatusRepository, ModelMapper modelMapper,
                        CustomerOrderStatusDetailsRepositoryI customerOrderStatusDetailsRepositoryI,
                        CustomerOrderStatusCustomImpl customerOrderStatusCustomImpl) {
        super();
        this.customerMeasurementRepository = customerMeasurementRepository;
        this.emailNotificationService = emailNotificationService;
        this.messageSource = messageSource;
        this.adminMapperUtil = adminMapperUtil;
        this.adminEmailNotificationService = adminEmailNotificationService;
        this.customerOrderStatusRepository = customerOrderStatusRepository;
        this.customerOrderStatusDetailsRepositoryI = customerOrderStatusDetailsRepositoryI;
        this.modelMapper = modelMapper;
        this.customerOrderStatusCustomImpl = customerOrderStatusCustomImpl;
        velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        velocityEngine.init();
    }

    /**
     * Save customer measurement and return the saved data as a DTO.
     *
     * @param customerMeasurementDto The customer measurement data to be saved.
     * @return CustomerMeasurementDto containing the saved customer measurement data.
     * @throws ParseException If there is an error in parsing the data.
     */
    public CustomerMeasurementDto saveCustomerMeasurement(CustomerMeasurementDto customerMeasurementDto,
                                                          Boolean isNotUpdate) throws ParseException {
        CustomerMeasurementDto savedCustomerMeasurementDto;
        CustomerMeasurement customerMeasurementObj = adminMapperUtil.
                convertFromCustomerMeasurementDto(customerMeasurementDto);
        customerMeasurementObj.setCreatedDate(AppUtil.getTodayDate());
        customerMeasurementObj.setCreatedBy(customerMeasurementDto.getAdminName());
        CustomerMeasurement existingCustomerMeasurementObj = customerMeasurementRepository.
                findByNameAndCustomerId(customerMeasurementDto.getName(), customerMeasurementDto.getCustomerId());
        if (Optional.ofNullable(existingCustomerMeasurementObj).isPresent()) {
            throw new UniqueRecordException(messageSource.getMessage("api.customer.measurement.already.exist",
                    null, Locale.ENGLISH));
        } else {
            CustomerMeasurement savedCustomerMeasurementObj =
                    customerMeasurementRepository.save(customerMeasurementObj);
            savedCustomerMeasurementDto = adminMapperUtil.convertToCustomerMeasurementDto(savedCustomerMeasurementObj);
            if (Boolean.TRUE.equals(isNotUpdate)) {
                adminEmailNotificationService.sendUpdatedCustomerMeasurementToCustomer(
                        customerMeasurementDto.getCustomerName(), savedCustomerMeasurementObj.getCustomerEmail());
                adminEmailNotificationService.sendUpdatedCustomerMeasurementToAdmin(
                        customerMeasurementDto.getCustomerName(), customerMeasurementDto.getAdminName(),
                        savedCustomerMeasurementObj.getCreatedDate());
            }
        }
        return savedCustomerMeasurementDto;
    }

    /**
     * Update customer measurement by customer measurement ID and return the updated data as a DTO.
     *
     * @param customerMeasurementDto The updated customer measurement data.
     * @param customerMeasurementId  The ID of the customer measurement to be updated.
     * @return CustomerMeasurementDto containing the updated customer measurement data.
     * @throws ParseException If there is an error in parsing the data.
     */
    public CustomerMeasurementDto updateCustomerMeasurement(CustomerMeasurementDto customerMeasurementDto,
                                                            Integer customerMeasurementId) throws ParseException {
        CustomerMeasurementDto updatedCustomerMeasurementDto;
        Optional<CustomerMeasurement> existingCustomerMeasurementObj =
                customerMeasurementRepository.findById(customerMeasurementId);
        if (existingCustomerMeasurementObj.isPresent()) {
            CustomerMeasurement customerMeasurementObj = adminMapperUtil.convertFromCustomerMeasurementDto(
                    customerMeasurementDto);
            customerMeasurementObj.setIsFeedbackFormSubmit(existingCustomerMeasurementObj.get()
                    .getIsFeedbackFormSubmit());
            customerMeasurementObj.setInitial(existingCustomerMeasurementObj.get().getInitial());
            customerMeasurementObj.setIsAdminUpdated(existingCustomerMeasurementObj.get().getIsAdminUpdated());
            customerMeasurementObj.setIsNewSize(existingCustomerMeasurementObj.get().getIsNewSize());
            customerMeasurementObj.setUpdatedDate(AppUtil.getTodayDate());
            customerMeasurementObj.setUpdatedBy(customerMeasurementDto.getAdminName());

            CustomerMeasurement updatedCustomerMeasurementObj = customerMeasurementRepository.
                    save(customerMeasurementObj);
            updatedCustomerMeasurementDto = adminMapperUtil.convertToCustomerMeasurementDto(
                    updatedCustomerMeasurementObj);
            List<CustomerOrderStatus> customerOrderStatusList = customerOrderStatusCustomImpl
                    .customerMeasurementSizeName(updatedCustomerMeasurementObj.getName());
            customerOrderStatusList.forEach(customerOrderStatus -> {
                updatedCustomerMeasurementObj.setFit(customerOrderStatus.getCustomerMeasurement().getFit());
                customerOrderStatus.setCustomerMeasurement(updatedCustomerMeasurementObj);
            });
            customerOrderStatusRepository.saveAll(customerOrderStatusList);
        } else {
            throw new RecordNotFoundException(messageSource.getMessage("api.customer.measurement.id.not.found",
                    new String[]{customerMeasurementId.toString()}, Locale.ENGLISH));
        }
        return updatedCustomerMeasurementDto;
    }

    /**
     * Send a login issue email to the admin.
     *
     * @param userName    The name of the user reporting the login issue.
     * @param userEmail   The email of the user reporting the login issue.
     * @param userMessage The message describing the login issue.
     */
    public void sendLoginIssueMailForAdmin(String userName, String userEmail, String userMessage) {
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put(ApplicationConstants.Email.USERNAME, userName);
        velocityContext.put(ApplicationConstants.Email.USEREMAIL, userEmail);
        velocityContext.put(ApplicationConstants.Email.USERMESSAGE, userMessage);
        Template templateObj = velocityEngine.getTemplate("emailtemplate/loginissue.vm");
        StringWriter stringWriter = new StringWriter();
        templateObj.merge(velocityContext, stringWriter);
        Notification emailNotification = new Notification(fromEmail, fromEmail,
                ApplicationConstants.EmailSubject.LOGIN_ISSUE, stringWriter.toString());
        emailNotificationService.sendLoginIssueMailForAdmin(emailNotification);
    }

    /**
     * Saves a list of customer order status DTOs to the database.
     *
     * @param customerOrderStatusDtoList The list of customer order status DTOs to be saved.
     * @return A list of saved customer order status DTOs.
     */
    public List<CustomerOrderStatusDto> saveCustomerOrderStatus(List<CustomerOrderStatusDto> customerOrderStatusDtoList,
                                                                String addressInformationDto)
            throws JsonProcessingException {

        AddressInformationDto addressInformation = new ObjectMapper().readValue(
                addressInformationDto, AddressInformationDto.class);

        List<CustomerOrderStatusDto> savedCustomerOrderStatusDtoList = new ArrayList<>();
        List<CustomerOrderStatus> saveCustomerOrderStatusList = new ArrayList<>();
        customerOrderStatusDtoList.forEach(customerOrderStatusDtoObj -> {
            CustomerOrderStatus customerOrderStatus =
                    modelMapper.map(customerOrderStatusDtoObj, CustomerOrderStatus.class);
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode addressInformationJsonNode = objectMapper.readTree(addressInformationDto);
                ((com.fasterxml.jackson.databind.node.ObjectNode) addressInformationJsonNode)
                        .put("orderStatus", "Delivered");
                String updatedAddressInformationJsonString = objectMapper
                        .writeValueAsString(addressInformationJsonNode);
                customerOrderStatus.setAddressInformation(updatedAddressInformationJsonString);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            saveCustomerOrderStatusList.add(customerOrderStatus);
        });
        List<CustomerOrderStatus> savedCustomerOrderStatusList =
                (List<CustomerOrderStatus>) customerOrderStatusRepository.saveAll(saveCustomerOrderStatusList);
        savedCustomerOrderStatusList.forEach(customerOrderStatusObj -> {
            CustomerOrderStatusDto customerOrderStatusDto =
                    modelMapper.map(customerOrderStatusObj, CustomerOrderStatusDto.class);
            savedCustomerOrderStatusDtoList.add(customerOrderStatusDto);
        });

        CustomerOrderStatusDetails customerOrderStatusDetailsObj = new CustomerOrderStatusDetails();
        customerOrderStatusDetailsObj.setOrderId(savedCustomerOrderStatusList.get(0).getOrderId());
        String orderStatusString = null;
        if (savedCustomerOrderStatusList.get(0).getIsFitSample().equals(Boolean.TRUE)) {
            if (addressInformation.getOrderStatus().equals(ApplicationConstants.OrderStatus.START_PRODUCTION)) {
                orderStatusString = ApplicationConstants.CustomerOrderStatusDetails.STRING_FIT_SAMPLE +
                        ApplicationConstants.CustomerOrderStatus.START_PRODUCTION;
            }
            customerOrderStatusDetailsObj.setOrderStatus(orderStatusString);
            saveCustomerOrderStatusDetails(customerOrderStatusDetailsObj);
        } else {
            CustomerOrderStatusDetails existingCustomerOrderStatusDetails = customerOrderStatusDetailsRepositoryI
                    .findByOrderId(savedCustomerOrderStatusList.get(0).getOrderId());
            if (Optional.ofNullable(existingCustomerOrderStatusDetails).isPresent()) {
                orderStatusString = ApplicationConstants.CustomerOrderStatusDetails.STRING_ORIGINAL_ORDER +
                        ApplicationConstants.CustomerOrderStatus.START_PRODUCTION;
                updateCustomerOrderStatusDetails(savedCustomerOrderStatusList.get(0).getOrderId(), orderStatusString);
            } else {
                orderStatusString = ApplicationConstants.CustomerOrderStatusDetails.STRING_ORIGINAL_ORDER +
                        ApplicationConstants.CustomerOrderStatus.START_PRODUCTION;
                customerOrderStatusDetailsObj.setOrderStatus(orderStatusString);
                saveCustomerOrderStatusDetails(customerOrderStatusDetailsObj);
            }
        }
        Boolean isFitSample = customerOrderStatusDtoList.get(0).getIsFitSample();
        sendOrderStatusForCustomer(addressInformation, null, null,
                isFitSample, customerOrderStatusDtoList.get(0).getOrderName(),
                customerOrderStatusDtoList.get(0).getCustomerMeasurement().getName(), null);

        return savedCustomerOrderStatusDtoList;
    }

    /**
     * Saves the customer order status details to the database.
     *
     * @param customerOrderStatusDetailsObj The customer order status details object to be saved.
     */
    public void saveCustomerOrderStatusDetails(CustomerOrderStatusDetails customerOrderStatusDetailsObj) {
        customerOrderStatusDetailsRepositoryI.save(customerOrderStatusDetailsObj);
    }

    /**
     * Updates the customer order status with the specified ID.
     *
     * @param customerOrderStatusId The ID of the customer order status to update.
     * @return The updated customer order status DTO.
     * @throws RecordNotFoundException if the customer order status with the given ID is not found.
     */
    public CustomerOrderStatusDto updateCustomerOrderStatus(Integer customerOrderStatusId, String orderStatus,
                                                            String orderTrackingNumber, String addressInformationDto)
            throws JsonProcessingException {
        AddressInformationDto addressInformation = new ObjectMapper().readValue(
                addressInformationDto, AddressInformationDto.class);
        CustomerOrderStatusDto updatedCustomerOrderStatusDto;
        Optional<CustomerOrderStatus> existingCustomerOrderStatusOptional =
                customerOrderStatusRepository.findById(customerOrderStatusId);
        if (existingCustomerOrderStatusOptional.isPresent()) {
            existingCustomerOrderStatusOptional.get().setOrderStatus(orderStatus);
            if (Optional.ofNullable(orderTrackingNumber).isPresent()) {
                existingCustomerOrderStatusOptional.get().setOrderTrackingNumber(orderTrackingNumber);
            }
            CustomerOrderStatus updatedExistingCustomerOrderStatus = customerOrderStatusRepository.
                    save(existingCustomerOrderStatusOptional.get());
            updatedCustomerOrderStatusDto = modelMapper.map(updatedExistingCustomerOrderStatus,
                    CustomerOrderStatusDto.class);
            Boolean isFitSample = existingCustomerOrderStatusOptional.get().getIsFitSample();
            sendOrderStatusForCustomer(addressInformation, orderTrackingNumber,
                    existingCustomerOrderStatusOptional.get().getCustomerMeasurement().getCustomerId(),
                    isFitSample, addressInformation.getOrderNumber(),
                    existingCustomerOrderStatusOptional.get().getCustomerMeasurement().getName(),
                    existingCustomerOrderStatusOptional.get().getOrderId());
            String orderStatusString = null;
            if (updatedExistingCustomerOrderStatus.getIsFitSample().equals(Boolean.TRUE)) {
                if (addressInformation.getOrderStatus().equals(ApplicationConstants.OrderStatus.FINISH_PRODUCTION)) {
                    orderStatusString = ApplicationConstants.CustomerOrderStatusDetails.STRING_FIT_SAMPLE +
                            ApplicationConstants.CustomerOrderStatus.FINISH_PRODUCTION;
                } else if (addressInformation.getOrderStatus()
                        .equals(ApplicationConstants.OrderStatus.MARK_AS_PACKED)) {
                    orderStatusString = ApplicationConstants.CustomerOrderStatusDetails.STRING_FIT_SAMPLE +
                            ApplicationConstants.CustomerOrderStatus.MARK_AS_PACKED;
                } else if (addressInformation.getOrderStatus().equals(ApplicationConstants.OrderStatus.DISPATCHED)) {
                    orderStatusString = ApplicationConstants.CustomerOrderStatusDetails.STRING_FIT_SAMPLE +
                            ApplicationConstants.CustomerOrderStatus.DISPATCHED;
                } else if (addressInformation.getOrderStatus().equals(ApplicationConstants.OrderStatus.DELIVERED)) {
                    orderStatusString = ApplicationConstants.CustomerOrderStatusDetails.STRING_FIT_SAMPLE +
                            ApplicationConstants.CustomerOrderStatus.DELIVERED;
                } else if (addressInformation.getOrderStatus()
                        .equals(ApplicationConstants.OrderStatus.EDIT_MEASUREMENTS)) {
                    orderStatusString = ApplicationConstants.CustomerOrderStatusDetails.STRING_FIT_SAMPLE +
                            ApplicationConstants.CustomerOrderStatus.MEASUREMENT_UPDATED;
                }
            } else {
                if (addressInformation.getOrderStatus().equals(ApplicationConstants.OrderStatus.FINISH_PRODUCTION)) {
                    orderStatusString = ApplicationConstants.CustomerOrderStatusDetails.STRING_ORIGINAL_ORDER +
                            ApplicationConstants.CustomerOrderStatus.FINISH_PRODUCTION;
                } else if (addressInformation.getOrderStatus()
                        .equals(ApplicationConstants.OrderStatus.MARK_AS_PACKED)) {
                    orderStatusString = ApplicationConstants.CustomerOrderStatusDetails.STRING_ORIGINAL_ORDER +
                            ApplicationConstants.CustomerOrderStatus.MARK_AS_PACKED;
                } else if (addressInformation.getOrderStatus().equals(ApplicationConstants.OrderStatus.DISPATCHED)) {
                    orderStatusString = ApplicationConstants.CustomerOrderStatusDetails.STRING_ORIGINAL_ORDER +
                            ApplicationConstants.CustomerOrderStatus.DISPATCHED;
                } else if (addressInformation.getOrderStatus().equals(ApplicationConstants.OrderStatus.DELIVERED)) {
                    orderStatusString = ApplicationConstants.CustomerOrderStatusDetails.STRING_ORIGINAL_ORDER +
                            ApplicationConstants.CustomerOrderStatus.ORDER_COMPLETED;
                }
            }
            updateCustomerOrderStatusDetails(updatedCustomerOrderStatusDto.getOrderId(), orderStatusString);
        } else {
            throw new RecordNotFoundException(messageSource.getMessage("api.customer.order.status.id.not.found",
                    new String[]{customerOrderStatusId.toString()}, Locale.ENGLISH));
        }
        return updatedCustomerOrderStatusDto;
    }

    /**
     * Updates the customer order status details for the specified order ID.
     *
     * @param orderId     The unique identifier for the customer order.
     * @param orderStatus The updated order status.
     */
    public void updateCustomerOrderStatusDetails(String orderId, String orderStatus) {
        CustomerOrderStatusDetails existingCustomerOrderStatusDetails =
                customerOrderStatusDetailsRepositoryI.findByOrderId(orderId);
        if (Optional.ofNullable(existingCustomerOrderStatusDetails).isPresent()) {
            existingCustomerOrderStatusDetails.setOrderStatus(orderStatus);
            customerOrderStatusDetailsRepositoryI.save(existingCustomerOrderStatusDetails);
        }
    }

    /**
     * Sends an order status notification email to the customer.
     */
    public void sendOrderStatusForCustomer(AddressInformationDto addressInformationDto, String orderTrackingNumber,
                                           String customerId, Boolean isFitSample, String orderName, String sizeName,
                                           String orderId) {
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put(ApplicationConstants.Email.CUSTOMER_NAME, addressInformationDto.getUserName());
        velocityContext.put(ApplicationConstants.Email.ORDER_NUMBER, addressInformationDto.getOrderNumber());
        velocityContext.put(ApplicationConstants.Email.ORDER_DATE, addressInformationDto.getOrderDate());

        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT_DD_MM_YYYY);
        LocalDate parsedOrderedDate = LocalDate.parse(addressInformationDto.getOrderDate(), inputFormatter);
        LocalDate sampleEstimatedDate = parsedOrderedDate.plusDays(7);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT_DD_MM_YYYY);
        String estimatedDate = sampleEstimatedDate.format(outputFormatter);
        velocityContext.put(ApplicationConstants.Email.ESTIMATED_DELIVERY_DATE, estimatedDate);

        velocityContext.put(ApplicationConstants.Email.SHIPPING_CUSTOMER_NAME,
                addressInformationDto.getShippingInformationDto().getCustomerName());
        velocityContext.put(ApplicationConstants.Email.SHIPPING_PLOT_NUMBER,
                addressInformationDto.getShippingInformationDto().getPlotNumber());
        velocityContext.put(ApplicationConstants.Email.SHIPPING_STATE,
                addressInformationDto.getShippingInformationDto().getState());
        velocityContext.put(ApplicationConstants.Email.SHIPPING_PINCODE,
                addressInformationDto.getShippingInformationDto().getPinCode());
        velocityContext.put(ApplicationConstants.Email.SHIPPING_COUNTRY,
                addressInformationDto.getShippingInformationDto().getCountry());
        velocityContext.put(ApplicationConstants.Email.SHIPPING_PHONE_NUMBER,
                addressInformationDto.getShippingInformationDto().getPhoneNumber());

        List<OrderCartDto> orderCartDtoList = addressInformationDto.getOrderCartDtoList();
        List<String> orderDetailsStringList = new ArrayList<>();
        for (OrderCartDto orderCartDto : orderCartDtoList) {
            String orderDetails = getOrderDetails(orderCartDto);
            orderDetailsStringList.add(orderDetails);
        }
        velocityContext.put(ApplicationConstants.Email.ORDER_DETAILS,
                String.join(" ", orderDetailsStringList));
        velocityContext.put(ApplicationConstants.Email.ORDER_TRACKING_NUMBER, orderTrackingNumber);
        Map<String, String> templateMap = null;

        Map<String, String> getEmailSubjectStringMap = null;
        if (isFitSample.equals(Boolean.TRUE)) {
            templateMap = getTemplateMapForFitSample();
            getEmailSubjectStringMap = getEmailSubject();
        } else {
            templateMap = getTemplateMapForItems();
            getEmailSubjectStringMap = getEmailSubjects();
        }
        String orderStatus = addressInformationDto.getOrderStatus();
        String templatePath = templateMap.get(orderStatus);

        String strGetEmailSubjectBasedOnOrderStatus = getEmailSubjectStringMap.get(orderStatus);
        String strEmailSubject = "Order " + orderName + " - " + strGetEmailSubjectBasedOnOrderStatus;

        if (templatePath != null) {
            Template templateObj = velocityEngine.getTemplate(templatePath);

            if (ApplicationConstants.Email.DELIVERED.equals(orderStatus)) {
                String feedBackFormViewLinkString = serverUrl + "?customerID=" + customerId + "&sizeName=" + sizeName
                        + "&orderId=" + orderId;
                velocityContext.put(ApplicationConstants.Email.FEEDBACK_FORM_VIEW_LINK, feedBackFormViewLinkString);
            }

            StringWriter stringWriter = new StringWriter();
            if (Optional.ofNullable(templateObj).isPresent()) {
                templateObj.merge(velocityContext, stringWriter);
                Notification emailNotification = new Notification(fromEmail, addressInformationDto.getUserEmail(),
                        strEmailSubject, stringWriter.toString());
                emailNotificationService.sendLoginIssueMailForAdmin(emailNotification);
            }
        }
    }

    /**
     * Creates a map of email template names associated with their corresponding template file paths.
     *
     * @return A map of email template names and file paths.
     */
    private static Map<String, String> getTemplateMapForFitSample() {
        Map<String, String> templateMap = new HashMap<>();
        templateMap.put(ApplicationConstants.Email.START_PRODUCTION, "emailtemplate/startproduction.vm");
        templateMap.put(ApplicationConstants.Email.FINISH_PRODUCTION, "emailtemplate/productioncompleted.vm");
        templateMap.put(ApplicationConstants.Email.DISPATCH, "emailtemplate/dispatch.vm");
        templateMap.put(ApplicationConstants.Email.DELIVERED, "emailtemplate/delivered.vm");
        templateMap.put(ApplicationConstants.Email.MEASUREMENT_EDITED, "emailtemplate/measurementedited.vm");
        return templateMap;
    }

    /**
     * Creates a map of email template names associated with their corresponding
     * template file paths for order-related emails.
     *
     * @return A map of order-related email template names and file paths.
     */
    private static Map<String, String> getTemplateMapForItems() {
        Map<String, String> templateMap = new HashMap<>();
        templateMap.put(ApplicationConstants.Email.ORDER_PLACED, "emailtemplate/orderplaced.vm");
        templateMap.put(ApplicationConstants.Email.ORDER_DISPATCHED, "emailtemplate/orderdispatched.vm");
        templateMap.put(ApplicationConstants.Email.ORDER_DELIVERED, "emailtemplate/orderdelivered.vm");
        return templateMap;
    }

    /**
     * Generates a formatted order details string from the provided order cart data.
     *
     * @param orderCartDto The order cart data containing order details.
     * @return The formatted order details string.
     */
    private String getOrderDetails(OrderCartDto orderCartDto) {
        String orderDetails = ApplicationConstants.Email.ORDER_DETAILS_TAG;
        orderDetails = orderDetails.replace(ApplicationConstants.OrderDetailsTemplateTag.IMAGE_TAG,
                orderCartDto.getImage());
        orderDetails = orderDetails.replace(ApplicationConstants.OrderDetailsTemplateTag.TEE_TYPE_TAG,
                orderCartDto.getTeeType());
        orderDetails = orderDetails.replace(ApplicationConstants.OrderDetailsTemplateTag.POCKET_TYPE_TAG,
                orderCartDto.getPocketType());
        orderDetails = orderDetails.replace(ApplicationConstants.OrderDetailsTemplateTag.SLEEVE_TYPE_TAG,
                orderCartDto.getSleeveType());
        orderDetails = orderDetails.replace(ApplicationConstants.OrderDetailsTemplateTag.COLOR_TAG,
                orderCartDto.getColor());
        orderDetails = orderDetails.replace(ApplicationConstants.OrderDetailsTemplateTag.QUANTITY_COUNT_TAG,
                orderCartDto.getQuantityCount().toString());
        return orderDetails;
    }

    /**
     * Creates a map associating email template names with their corresponding email subject lines.
     *
     * @return A map of email template names and their corresponding subject lines.
     */
    private static Map<String, String> getEmailSubject() {
        Map<String, String> templateMap = new HashMap<>();
        templateMap.put(ApplicationConstants.Email.START_PRODUCTION,
                ApplicationConstants.EmailSubject.START_PRODUCTION);
        templateMap.put(ApplicationConstants.Email.FINISH_PRODUCTION,
                ApplicationConstants.EmailSubject.FINISH_PRODUCTION);
        templateMap.put(ApplicationConstants.Email.DISPATCH, ApplicationConstants.EmailSubject.DISPATCH);
        templateMap.put(ApplicationConstants.Email.DELIVERED, ApplicationConstants.EmailSubject.DELIVERED);
        templateMap.put(ApplicationConstants.Email.MEASUREMENT_EDITED,
                ApplicationConstants.EmailSubject.EDIT_MEASUREMENTS);
        return templateMap;
    }

    /**
     * Creates a map associating order-related email template names with their corresponding email subject lines.
     *
     * @return A map of order-related email template names and their corresponding subject lines.
     */
    private Map<String, String> getEmailSubjects() {
        Map<String, String> templateMap = new HashMap<>();
        templateMap.put(ApplicationConstants.Email.ORDER_PLACED, ApplicationConstants.EmailSubject.ORDER_PLACED);
        templateMap.put(ApplicationConstants.Email.ORDER_DISPATCHED, ApplicationConstants.EmailSubject.ORDER_DISPATCH);
        templateMap.put(ApplicationConstants.Email.ORDER_DELIVERED, ApplicationConstants.EmailSubject.ORDER_DELIVERED);
        return templateMap;
    }

    /**
     * Retrieves customer order status details from the `adminService` and generates an `ApiListResponse` object.
     *
     * @return An `ApiListResponse` object containing the customer order status details or an error message.
     */
    public List<CustomerOrderStatusDetailsDto> getCustomerOrderStatusDetails() {
        List<CustomerOrderStatusDetails> customerOrderStatusDetailsList = customerOrderStatusDetailsRepositoryI
                .findAllByOrderByCustomerOrderStatusDetailsIdDesc();
        return customerOrderStatusDetailsList.stream().map(customerOrderStatusDetails ->
                        modelMapper.map(customerOrderStatusDetails, CustomerOrderStatusDetailsDto.class))
                .collect(Collectors.toList());
    }
}
