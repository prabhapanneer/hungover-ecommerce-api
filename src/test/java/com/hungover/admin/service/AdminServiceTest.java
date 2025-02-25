package com.hungover.admin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hungover.admin.repository.CustomerOrderStatusCustomImpl;
import com.hungover.admin.repository.CustomerOrderStatusRepositoryI;
import com.hungover.common.exception.RecordNotFoundException;
import com.hungover.common.exception.UniqueRecordException;
import com.hungover.core.domain.customer.CustomerMeasurement;
import com.hungover.core.domain.customer.CustomerOrderStatus;
import com.hungover.core.domain.notification.Notification;
import com.hungover.core.dto.customer.CustomerMeasurementDto;
import com.hungover.core.dto.customer.CustomerOrderStatusDto;
import com.hungover.customer.repository.CustomerMeasurementRepositoryI;
import com.hungover.email.notification.service.AdminEmailNotificationService;
import com.hungover.email.notification.service.EmailNotificationService;
import com.hungover.util.AdminMapperUtil;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;

import java.io.StringWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
class AdminServiceTest {
    @InjectMocks
    private AdminService adminService;
    @Mock
    private AdminMapperUtil adminMapperUtil;
    @Mock
    private AdminEmailNotificationService adminEmailNotificationService;
    @Mock
    private EmailNotificationService emailNotificationService;
    @Mock
    private CustomerMeasurementRepositoryI customerMeasurementRepositoryI;
    @Mock
    private MessageSource messageSource;
    @Mock
    private VelocityEngine velocityEngine;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private CustomerOrderStatusRepositoryI customerOrderStatusRepository;
    @Mock
    private CustomerOrderStatusCustomImpl customerOrderStatusCustomImpl;

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

        CustomerMeasurement customerMeasurement = new CustomerMeasurement();
        customerMeasurement.setName("Chest");
        customerMeasurement.setCustomerId(String.valueOf(1L));
        customerMeasurement.setCustomerEmail("john@example.com");

        when(adminMapperUtil.convertFromCustomerMeasurementDto(customerMeasurementDto))
                .thenReturn(customerMeasurement);
        when(customerMeasurementRepositoryI.findByNameAndCustomerId(customerMeasurementDto.getName(),
                customerMeasurementDto.getCustomerId()))
                .thenReturn(null); // No existing measurement with the same name and customerId.
        when(customerMeasurementRepositoryI.save(customerMeasurement)).thenReturn(customerMeasurement);
        when(adminMapperUtil.convertToCustomerMeasurementDto(customerMeasurement)).thenReturn(customerMeasurementDto);

        CustomerMeasurementDto savedCustomerMeasurementDto = adminService
                .saveCustomerMeasurement(customerMeasurementDto, true);
        verify(adminEmailNotificationService, times(1))
                .sendUpdatedCustomerMeasurementToCustomer("John Doe", "john@example.com");

        Assertions.assertEquals(customerMeasurementDto.getCustomerEmail(),
                savedCustomerMeasurementDto.getCustomerEmail());
    }

    @Test
    void testSaveCustomerMeasurementWithExistingCustomerMeasurement() {

        CustomerMeasurementDto customerMeasurementDto = new CustomerMeasurementDto();
        customerMeasurementDto.setName("Chest");
        customerMeasurementDto.setCustomerId(String.valueOf(1L));
        customerMeasurementDto.setCustomerName("John Doe");
        customerMeasurementDto.setCustomerEmail("john@example.com");
        customerMeasurementDto.setAdminName("Admin");

        CustomerMeasurement customerMeasurement = new CustomerMeasurement();
        customerMeasurement.setName("Chest");
        customerMeasurement.setCustomerId(String.valueOf(1L));
        customerMeasurement.setCustomerEmail("john@example.com");

        when(adminMapperUtil.convertFromCustomerMeasurementDto(customerMeasurementDto))
                .thenReturn(customerMeasurement);
        when(customerMeasurementRepositoryI.findByNameAndCustomerId(customerMeasurementDto.getName(),
                customerMeasurementDto.getCustomerId()))
                .thenReturn(customerMeasurement);
        UniqueRecordException exception = Assertions.assertThrows(UniqueRecordException.class, () ->
                adminService.saveCustomerMeasurement(customerMeasurementDto, true));
        when(customerMeasurementRepositoryI.save(customerMeasurement)).thenReturn(customerMeasurement);
        when(adminMapperUtil.convertToCustomerMeasurementDto(customerMeasurement)).thenReturn(customerMeasurementDto);
    }

    @Test
    void testUpdateCustomerMeasurement_ExistingMeasurement() throws ParseException {

        Integer customerMeasurementId = 1;
        CustomerMeasurementDto customerMeasurementInputDto = new CustomerMeasurementDto();
        customerMeasurementInputDto.setName("Chest");
        customerMeasurementInputDto.setCustomerId(String.valueOf(1L));
        customerMeasurementInputDto.setCustomerName("John Doe");
        customerMeasurementInputDto.setCustomerEmail("john@example.com");
        customerMeasurementInputDto.setAdminName("Admin");

        CustomerMeasurement existingMeasurement = new CustomerMeasurement();
        existingMeasurement.setName("Chest");
        existingMeasurement.setCustomerId(String.valueOf(1L));
        existingMeasurement.setCustomerEmail("john@example.com");

        List<CustomerOrderStatus> customerOrderStatusList = new ArrayList<>();

        when(customerMeasurementRepositoryI.findById(customerMeasurementId))
                .thenReturn(Optional.of(existingMeasurement));
        when(messageSource.getMessage(any(), any(), any())).thenReturn("Customer measurement id not found");
        when(adminMapperUtil.convertFromCustomerMeasurementDto(customerMeasurementInputDto))
                .thenReturn(existingMeasurement);
        when(customerMeasurementRepositoryI.save(existingMeasurement)).thenReturn(existingMeasurement);
        when(adminMapperUtil.convertToCustomerMeasurementDto(existingMeasurement))
                .thenReturn(customerMeasurementInputDto);
        when(customerOrderStatusCustomImpl.customerMeasurementSizeName("testName"))
                .thenReturn(customerOrderStatusList);

        CustomerMeasurementDto updatedCustomerMeasurementDto = adminService
                .updateCustomerMeasurement(customerMeasurementInputDto, customerMeasurementId);
        Assertions.assertEquals(customerMeasurementInputDto.getCustomerEmail(),
                updatedCustomerMeasurementDto.getCustomerEmail());
    }

    @Test
    @DisplayName("Should throw RecordNotFoundException when customer measurement ID is not found")
    void updateCustomerMeasurement_RecordNotFound() {
        // Arrange
        CustomerMeasurementDto customerMeasurementInputDto = new CustomerMeasurementDto();
        customerMeasurementInputDto.setName("Chest");
        customerMeasurementInputDto.setCustomerId(String.valueOf(1L));
        customerMeasurementInputDto.setCustomerName("John Doe");
        customerMeasurementInputDto.setCustomerEmail("john@example.com");
        customerMeasurementInputDto.setAdminName("Admin");

        when(customerMeasurementRepositoryI.findById(anyInt())).thenReturn(Optional.empty());
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Measurement ID not found");

        // Act and Assert
        Assertions.assertThrows(RecordNotFoundException.class, () -> adminService.updateCustomerMeasurement(customerMeasurementInputDto, 123));

        // Verify
        verify(customerMeasurementRepositoryI).findById(123);
    }

    @Test
    void testSendLoginIssueMailForAdmin() {
        String userName = "monika";
        String userEmail = "monika.n@cloudnowtech.com";
        String userMessage = "Testing";

        Template template = mock(Template.class);
        when(velocityEngine.getTemplate("emailtemplate/loginissue.vm")).thenReturn(template);
        StringWriter stringWriter = new StringWriter();
        doNothing().when(template).merge(any(VelocityContext.class), eq(stringWriter));

        adminService.sendLoginIssueMailForAdmin(userName, userEmail, userMessage);
        verify(emailNotificationService).sendLoginIssueMailForAdmin(any(Notification.class));
    }

    @Test
    @Disabled
    @DisplayName("Save Customer Order Status - Success")
    void testSaveCustomerOrderStatus_Success() throws JsonProcessingException {
        String orderStatus = "Start Production"; // Set your new order status
        String userName = "TestUser"; // Set your username
        String userEmail = "test@example.com"; // Set your user email
        String orderNumber = "00340434292135100186";
        String orderDate = "16041998";
        String addressInformationDto ="{\n" +
                "  \"userName\": \"Aadhithyan Anbuchezlian\",\n" +
                "  \"userEmail\": \"aadhithyan.a@cloudnowtech.com\",\n" +
                "  \"orderStatus\": \"Delivered\",\n" +
                "  \"orderNumber\": \"#123\",\n" +
                "  \"orderDate\": \"29/12/2022\",\n" +
                "  \"shippingInformationDto\": {\n" +
                "    \"customerName\": \"Aadhithyan Anbuchezlian\",\n" +
                "    \"plotNumber\": \"1/1648A,Bharathakovil Street,Jolarpet\",\n" +
                "    \"pinCode\": \"635852\",\n" +
                "    \"country\": \"India\",\n" +
                "    \"state\": \"TanmilNadu\",\n" +
                "    \"phoneNumber\": \"8798789098\"\n" +
                "  },\n" +
                "  \"orderCartDtoList\": [\n" +
                "    {\n" +
                "      \"teeType\": \"Fullhand t-shirt\",\n" +
                "      \"pocketType\": \"With Pocket\",\n" +
                "      \"sleeveType\": \"Half-sleeve\",\n" +
                "      \"color\": \"Blue\",\n" +
                "      \"quantityCount\": \"1\",\n" +
                "      \"image\": \"https://cdn.shopify.com/s/files/1/0659/7548/4645/files/shot-2_without-sleeve_henley_with-pocket_brick.jpg?v=1695628820\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"teeType\": \"Halfhand t-shirt\",\n" +
                "      \"pocketType\": \"Without Pocket\",\n" +
                "      \"sleeveType\": \"full-sleeve\",\n" +
                "      \"color\": \"white\",\n" +
                "      \"quantityCount\": \"2\",\n" +
                "      \"image\": \"https://cdn.shopify.com/s/files/1/0659/7548/4645/files/shot-2_without-sleeve_henley_with-pocket_brick.jpg?v=1695628820\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"teeType\": \"Fullhand t-shirt\",\n" +
                "      \"pocketType\": \"With Pocket\",\n" +
                "      \"sleeveType\": \"Half-sleeve\",\n" +
                "      \"color\": \"Blue\",\n" +
                "      \"quantityCount\": \"3\",\n" +
                "      \"image\": \"https://cdn.shopify.com/s/files/1/0659/7548/4645/files/shot-2_without-sleeve_henley_with-pocket_brick.jpg?v=1695628820\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"teeType\": \"Halfhand t-shirt\",\n" +
                "      \"pocketType\": \"Without Pocket\",\n" +
                "      \"sleeveType\": \"full-sleeve\",\n" +
                "      \"color\": \"white\",\n" +
                "      \"quantityCount\": \"4\",\n" +
                "      \"image\": \"https://cdn.shopify.com/s/files/1/0659/7548/4645/files/shot-2_without-sleeve_henley_with-pocket_brick.jpg?v=1695628820\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        // Arrange
        List<CustomerOrderStatusDto> customerOrderStatusDtoInputList = new ArrayList<>();
        CustomerOrderStatusDto customerOrderStatusDto = new CustomerOrderStatusDto();
        customerOrderStatusDto.setCustomerOrderStatusId(1);
        customerOrderStatusDto.setOrderId("12344568763234");
        customerOrderStatusDto.setOrderStatus("StartProduction");
        customerOrderStatusDto.setIsFitSample(Boolean.TRUE);
        customerOrderStatusDto.setOrderName("orderName");
        CustomerMeasurementDto customerMeasurementDto = new CustomerMeasurementDto();
        customerMeasurementDto.setName("testName");
        customerOrderStatusDto.setCustomerMeasurement(customerMeasurementDto);
        customerOrderStatusDtoInputList.add(customerOrderStatusDto);

        List<CustomerOrderStatus> savedCustomerOrderStatusList = new ArrayList<>();
        CustomerOrderStatus customerOrderStatus = new CustomerOrderStatus();
        customerOrderStatus.setCustomerOrderStatusId(1);
        customerOrderStatus.setOrderId("12344568763234");
        customerOrderStatus.setOrderStatus("Start Production");
        customerOrderStatus.setIsFitSample(Boolean.TRUE);
        savedCustomerOrderStatusList.add(customerOrderStatus);

        when(modelMapper.map(customerOrderStatusDto, CustomerOrderStatus.class)).thenReturn(customerOrderStatus);
        when(customerOrderStatusRepository.saveAll(anyList())).thenReturn(savedCustomerOrderStatusList);
        when(modelMapper.map(customerOrderStatus, CustomerOrderStatusDto.class)).thenReturn(customerOrderStatusDto);

        // Act
        List<CustomerOrderStatusDto> savedCustomerOrderStatusDtoList = adminService.saveCustomerOrderStatus(
                customerOrderStatusDtoInputList,addressInformationDto);

        // Assert
        Assertions.assertNotNull(savedCustomerOrderStatusDtoList);
        Assertions.assertEquals(1, savedCustomerOrderStatusDtoList.size());
        Assertions.assertEquals(1, savedCustomerOrderStatusDtoList.get(0).getCustomerOrderStatusId());
    }

    @Test
    @DisplayName("Save Customer Order Status - Null List")
    void testSaveCustomerOrderStatus_NullList() {
        String addressInformationDto ="{\n" +
                "  \"userName\": \"Aadhithyan Anbuchezlian\",\n" +
                "  \"userEmail\": \"aadhithyan.a@cloudnowtech.com\",\n" +
                "  \"orderStatus\": \"Start Production\",\n" +
                "  \"orderNumber\": \"#123\",\n" +
                "  \"orderDate\": \"29/12/2022\",\n" +
                "  \"shippingInformationDto\": {\n" +
                "    \"customerName\": \"Aadhithyan Anbuchezlian\",\n" +
                "    \"plotNumber\": \"1/1648A,Bharathakovil Street,Jolarpet\",\n" +
                "    \"pinCode\": \"635852\",\n" +
                "    \"country\": \"India\",\n" +
                "    \"state\": \"TanmilNadu\",\n" +
                "    \"phoneNumber\": \"8798789098\"\n" +
                "  },\n" +
                "  \"orderCartDtoList\": [\n" +
                "    {\n" +
                "      \"teeType\": \"Fullhand t-shirt\",\n" +
                "      \"pocketType\": \"With Pocket\",\n" +
                "      \"sleeveType\": \"Half-sleeve\",\n" +
                "      \"color\": \"Blue\",\n" +
                "      \"quantityCount\": \"1\",\n" +
                "      \"image\": \"https://cdn.shopify.com/s/files/1/0659/7548/4645/files/shot-2_without-sleeve_henley_with-pocket_brick.jpg?v=1695628820\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"teeType\": \"Halfhand t-shirt\",\n" +
                "      \"pocketType\": \"Without Pocket\",\n" +
                "      \"sleeveType\": \"full-sleeve\",\n" +
                "      \"color\": \"white\",\n" +
                "      \"quantityCount\": \"2\",\n" +
                "      \"image\": \"https://cdn.shopify.com/s/files/1/0659/7548/4645/files/shot-2_without-sleeve_henley_with-pocket_brick.jpg?v=1695628820\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"teeType\": \"Fullhand t-shirt\",\n" +
                "      \"pocketType\": \"With Pocket\",\n" +
                "      \"sleeveType\": \"Half-sleeve\",\n" +
                "      \"color\": \"Blue\",\n" +
                "      \"quantityCount\": \"3\",\n" +
                "      \"image\": \"https://cdn.shopify.com/s/files/1/0659/7548/4645/files/shot-2_without-sleeve_henley_with-pocket_brick.jpg?v=1695628820\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"teeType\": \"Halfhand t-shirt\",\n" +
                "      \"pocketType\": \"Without Pocket\",\n" +
                "      \"sleeveType\": \"full-sleeve\",\n" +
                "      \"color\": \"white\",\n" +
                "      \"quantityCount\": \"4\",\n" +
                "      \"image\": \"https://cdn.shopify.com/s/files/1/0659/7548/4645/files/shot-2_without-sleeve_henley_with-pocket_brick.jpg?v=1695628820\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        // Act & Assert
        Assertions.assertThrows(NullPointerException.class, () -> adminService.saveCustomerOrderStatus(
                        null,addressInformationDto));
    }


    @Test
    @DisplayName("Update Customer Order Status - When Not Exists")
    void testUpdateCustomerOrderStatusWhenNotExists() {
        // Mock the data and behavior for the "else" scenario
        Integer customerOrderStatusId = 1;
        String orderStatus = "NewStatus";
        String trackumber = "122";
        String addressInformationDto ="{\n" +
                "  \"userName\": \"Aadhithyan Anbuchezlian\",\n" +
                "  \"userEmail\": \"aadhithyan.a@cloudnowtech.com\",\n" +
                "  \"orderStatus\": \"Start Production\",\n" +
                "  \"orderNumber\": \"123\",\n" +
                "  \"orderDate\": \"29/12/2022\",\n" +
                "  \"shippingInformationDto\": {\n" +
                "    \"customerName\": \"Aadhithyan Anbuchezlian\",\n" +
                "    \"plotNumber\": \"1/1648A,Bharathakovil Street,Jolarpet\",\n" +
                "    \"pinCode\": \"635852\",\n" +
                "    \"country\": \"India\",\n" +
                "    \"state\": \"TanmilNadu\",\n" +
                "    \"phoneNumber\": \"8798789098\"\n" +
                "  },\n" +
                "  \"orderCartDtoList\": [\n" +
                "    {\n" +
                "      \"teeType\": \"Fullhand t-shirt\",\n" +
                "      \"pocketType\": \"With Pocket\",\n" +
                "      \"sleeveType\": \"Half-sleeve\",\n" +
                "      \"color\": \"Blue\",\n" +
                "      \"quantityCount\": \"1\",\n" +
                "      \"image\": \"https://cdn.shopify.com/s/files/1/0659/7548/4645/files/shot-2_without-sleeve_henley_with-pocket_brick.jpg?v=1695628820\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"teeType\": \"Halfhand t-shirt\",\n" +
                "      \"pocketType\": \"Without Pocket\",\n" +
                "      \"sleeveType\": \"full-sleeve\",\n" +
                "      \"color\": \"white\",\n" +
                "      \"quantityCount\": \"2\",\n" +
                "      \"image\": \"https://cdn.shopify.com/s/files/1/0659/7548/4645/files/shot-2_without-sleeve_henley_with-pocket_brick.jpg?v=1695628820\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"teeType\": \"Fullhand t-shirt\",\n" +
                "      \"pocketType\": \"With Pocket\",\n" +
                "      \"sleeveType\": \"Half-sleeve\",\n" +
                "      \"color\": \"Blue\",\n" +
                "      \"quantityCount\": \"3\",\n" +
                "      \"image\": \"https://cdn.shopify.com/s/files/1/0659/7548/4645/files/shot-2_without-sleeve_henley_with-pocket_brick.jpg?v=1695628820\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"teeType\": \"Halfhand t-shirt\",\n" +
                "      \"pocketType\": \"Without Pocket\",\n" +
                "      \"sleeveType\": \"full-sleeve\",\n" +
                "      \"color\": \"white\",\n" +
                "      \"quantityCount\": \"4\",\n" +
                "      \"image\": \"https://cdn.shopify.com/s/files/1/0659/7548/4645/files/shot-2_without-sleeve_henley_with-pocket_brick.jpg?v=1695628820\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        when(customerOrderStatusRepository.findById(customerOrderStatusId)).thenReturn(Optional.empty());

        when(messageSource.getMessage("api.customer.order.status.id.not.found",
                new String[]{customerOrderStatusId.toString()}, Locale.ENGLISH))
                .thenReturn("Error message");

        // Call the service method and expect an exception
        Assertions.assertThrows(RecordNotFoundException.class, () -> adminService.updateCustomerOrderStatus(customerOrderStatusId, orderStatus,trackumber,addressInformationDto));
    }

}