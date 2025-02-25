package com.hungover.customer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.hungover.admin.repository.CustomerOrderStatusRepositoryI;
import com.hungover.common.constant.ApplicationConstants;
import com.hungover.common.exception.RecordNotFoundException;
import com.hungover.common.exception.UniqueRecordException;
import com.hungover.common.util.AppUtil;
import com.hungover.core.domain.customer.*;
import com.hungover.core.domain.notification.Notification;
import com.hungover.core.dto.customer.CustomerCountResponseDto;
import com.hungover.core.dto.customer.CustomerCreationKVDto;
import com.hungover.core.dto.customer.CustomerDetailKeyValueResponseDto;
import com.hungover.core.dto.customer.CustomerDetailsKeyValueDto;
import com.hungover.core.dto.customer.CustomerDto;
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
import com.hungover.customer.repository.CustomerFeedbackRepositoryI;
import com.hungover.customer.repository.CustomerMeasurementFeedbackRepositoryI;
import com.hungover.customer.repository.CustomerMeasurementRepositoryI;
import com.hungover.customer.repository.CustomerOtpRepository;
import com.hungover.customer.repository.CustomerWishlistRepositoryI;
import com.hungover.email.notification.service.EmailNotificationService;
import com.hungover.util.AdminMapperUtil;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

class CustomerServiceTest {

    @InjectMocks
    private CustomerService customerService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private CustomerWishlistRepositoryI customerWishlistRepository;

    @Mock
    private CustomerFeedbackRepositoryI customerFeedbackRepositoryI;

    @Mock
    private CustomerMeasurementRepositoryI customerMeasurementRepositoryI;

    @Mock
    private CustomerMeasurementFeedbackRepositoryI customerMeasurementFeedbackRepositoryI;

    @Mock
    private CustomerOrderStatusRepositoryI customerOrderStatusRepositoryI;
    @Mock
    private CustomerOtpRepository customerOtpRepository;
    @Mock
    private EmailNotificationService emailNotificationService;
    @Mock
    private Logger customerServiceLogger;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private MessageSource messageSource;
    @Mock
    private AdminMapperUtil adminMapperUtil;

    @Value("${shopifyResetPasswordEndPoint}")
    String shopifyResetPasswordEndPoint;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should get customer wishlist by customer id")
    void getCustomerWishlistByCustomerId_Success() {
        // Arrange
        String customerId = "123";

        CustomerWishlist customerWishlistDataObj1 = new CustomerWishlist();
        customerWishlistDataObj1.setCustomerId(customerId);
        customerWishlistDataObj1.setVariantId("456");

        CustomerWishlist customerWishlistDataObj2 = new CustomerWishlist();
        customerWishlistDataObj2.setCustomerId(customerId);
        customerWishlistDataObj2.setVariantId("789");

        List<CustomerWishlist> customerWishlistDataList = new ArrayList<>();
        customerWishlistDataList.add(customerWishlistDataObj1);
        customerWishlistDataList.add(customerWishlistDataObj2);

        CustomerWishlistDto expectedCustomerWishlistDto1 = new CustomerWishlistDto();
        expectedCustomerWishlistDto1.setCustomerId(customerId);
        expectedCustomerWishlistDto1.setVariantId("456");

        CustomerWishlistDto expectedCustomerWishlistDto2 = new CustomerWishlistDto();
        expectedCustomerWishlistDto2.setCustomerId(customerId);
        expectedCustomerWishlistDto2.setVariantId("789");

        when(customerWishlistRepository.findByCustomerId(customerId))
                .thenReturn(customerWishlistDataList);

        when(modelMapper.map(customerWishlistDataObj1, CustomerWishlistDto.class))
                .thenReturn(expectedCustomerWishlistDto1);

        when(modelMapper.map(customerWishlistDataObj2, CustomerWishlistDto.class))
                .thenReturn(expectedCustomerWishlistDto2);

        // Act
        List<CustomerWishlistDto> actualCustomerWishlistDtoList = customerService.getCustomerWishlistByCustomerId(customerId);

        // Assert
        Assertions.assertEquals(2, actualCustomerWishlistDtoList.size());
        Assertions.assertEquals(expectedCustomerWishlistDto1, actualCustomerWishlistDtoList.get(0));
        Assertions.assertEquals(expectedCustomerWishlistDto2, actualCustomerWishlistDtoList.get(1));
        verify(customerWishlistRepository).findByCustomerId(customerId);
        verify(modelMapper).map(customerWishlistDataObj1, CustomerWishlistDto.class);
        verify(modelMapper).map(customerWishlistDataObj2, CustomerWishlistDto.class);
    }

    @Test
    @DisplayName("Should throw RecordNotFoundException when customer wishlist is empty")
    void getCustomerWishlistByCustomerId_RecordNotFound() {
        // Arrange
        String customerId = "123";

        when(customerWishlistRepository.findByCustomerId(customerId))
                .thenReturn(Collections.emptyList());

        when(messageSource.getMessage(eq("api.customer.id.not.found"),
                any(), any(Locale.class)))
                .thenReturn("Customer not found");

        // Act and Assert
        Assertions.assertThrows(RecordNotFoundException.class, () -> customerService.getCustomerWishlistByCustomerId(customerId));

        verify(customerWishlistRepository).findByCustomerId(customerId);
        verifyNoMoreInteractions(modelMapper);
    }

    @Test
    @DisplayName("Should get all customer wishlist")
    void getAllCustomerWishlist_Success() {
        // Arrange
        String variantId1 = "123";
        String variantId2 = "456";

        CustomerWishlist customerWishlistDataObj1 = new CustomerWishlist();
        customerWishlistDataObj1.setCustomerId("123");
        customerWishlistDataObj1.setVariantId(variantId1);

        CustomerWishlist customerWishlistDataObj2 = new CustomerWishlist();
        customerWishlistDataObj2.setCustomerId("456");
        customerWishlistDataObj2.setVariantId(variantId2);

        List<CustomerWishlist> customerWishlistDataList = new ArrayList<>();
        customerWishlistDataList.add(customerWishlistDataObj1);
        customerWishlistDataList.add(customerWishlistDataObj2);

        CustomerWishlistKVDto expectedCustomerWishlistKVDto1 = new CustomerWishlistKVDto();
        expectedCustomerWishlistKVDto1.setColor("Pink");
        expectedCustomerWishlistKVDto1.setVariantId(variantId1);

        CustomerWishlistKVDto expectedCustomerWishlistKVDto2 = new CustomerWishlistKVDto();
        expectedCustomerWishlistKVDto2.setColor("Blue");
        expectedCustomerWishlistKVDto2.setVariantId(variantId2);

        Map<String, List<CustomerWishlist>> customerWishListDtoMap = new HashMap<>();
        customerWishListDtoMap.put(variantId1, Collections.singletonList(customerWishlistDataObj1));
        customerWishListDtoMap.put(variantId2, Collections.singletonList(customerWishlistDataObj2));

        Map<String, Long> lcustomerWishListDtoMap = new HashMap<>();
        lcustomerWishListDtoMap.put(variantId1, 1L);
        lcustomerWishListDtoMap.put(variantId2, 1L);

        when(customerWishlistRepository.findAll())
                .thenReturn(customerWishlistDataList);

        when(modelMapper.map(customerWishlistDataObj1, CustomerWishlistKVDto.class))
                .thenReturn(expectedCustomerWishlistKVDto1);

        when(modelMapper.map(customerWishlistDataObj2, CustomerWishlistKVDto.class))
                .thenReturn(expectedCustomerWishlistKVDto2);

        // Act
        List<CustomerWishlistKVDto> actualCustomerWishlistKVDtoList = customerService.getAllCustomerWishlist();

        // Assert
        Assertions.assertEquals(2, actualCustomerWishlistKVDtoList.size());
        Assertions.assertEquals(expectedCustomerWishlistKVDto1, actualCustomerWishlistKVDtoList.get(0));
        Assertions.assertEquals(expectedCustomerWishlistKVDto2, actualCustomerWishlistKVDtoList.get(1));
        verify(customerWishlistRepository).findAll();
        verify(modelMapper).map(customerWishlistDataObj1, CustomerWishlistKVDto.class);
        verify(modelMapper).map(customerWishlistDataObj2, CustomerWishlistKVDto.class);
    }

    @Test
    @DisplayName("Should get customer wishlist by variant id")
    void getCustomerWishlistByVariantId_Success() {
        // Arrange
        String variantId = "456";

        CustomerWishlist customerWishlistDataObj1 = new CustomerWishlist();
        customerWishlistDataObj1.setCustomerId("123");
        customerWishlistDataObj1.setVariantId(variantId);

        CustomerWishlist customerWishlistDataObj2 = new CustomerWishlist();
        customerWishlistDataObj2.setCustomerId("456");
        customerWishlistDataObj2.setVariantId(variantId);

        List<CustomerWishlist> customerWishlistDataList = new ArrayList<>();
        customerWishlistDataList.add(customerWishlistDataObj1);
        customerWishlistDataList.add(customerWishlistDataObj2);

        CustomerWishlistDto expectedCustomerWishlistDto1 = new CustomerWishlistDto();
        expectedCustomerWishlistDto1.setCustomerId("123");
        expectedCustomerWishlistDto1.setVariantId(variantId);

        CustomerWishlistDto expectedCustomerWishlistDto2 = new CustomerWishlistDto();
        expectedCustomerWishlistDto2.setCustomerId("456");
        expectedCustomerWishlistDto2.setVariantId(variantId);

        when(customerWishlistRepository.findByVariantId(variantId))
                .thenReturn(customerWishlistDataList);

        when(modelMapper.map(customerWishlistDataObj1, CustomerWishlistDto.class))
                .thenReturn(expectedCustomerWishlistDto1);

        when(modelMapper.map(customerWishlistDataObj2, CustomerWishlistDto.class))
                .thenReturn(expectedCustomerWishlistDto2);

        // Act
        List<CustomerWishlistDto> actualCustomerWishlistDtoList = customerService.getCustomerWishlistByVariantId(variantId);

        // Assert
        Assertions.assertEquals(2, actualCustomerWishlistDtoList.size());
        Assertions.assertEquals(expectedCustomerWishlistDto1, actualCustomerWishlistDtoList.get(0));
        Assertions.assertEquals(expectedCustomerWishlistDto2, actualCustomerWishlistDtoList.get(1));
        verify(customerWishlistRepository).findByVariantId(variantId);
        verify(modelMapper).map(customerWishlistDataObj1, CustomerWishlistDto.class);
        verify(modelMapper).map(customerWishlistDataObj2, CustomerWishlistDto.class);
    }

    @Test
    @DisplayName("Should throw RecordNotFoundException when customer wishlist by variant id is empty")
    void getCustomerWishlistByVariantId_RecordNotFound() {
        // Arrange
        String variantId = "456";

        when(customerWishlistRepository.findByVariantId(variantId)).thenReturn(Collections.emptyList());

        when(messageSource.getMessage(eq("api.customer.variant.id.not.found"), any(), any(Locale.class)))
                .thenReturn("Variant not found");

        // Act and Assert
        Assertions.assertThrows(RecordNotFoundException.class, () -> customerService.getCustomerWishlistByVariantId(variantId));

        verify(customerWishlistRepository).findByVariantId(variantId);
        verifyNoMoreInteractions(modelMapper);
    }

    @Test
    @DisplayName("Should get customer feedback list")
    void getCustomerFeedback_Success() {

        List<CustomerFeedback> customerFeedbackList = new ArrayList<>();
        CustomerFeedback customerFeedback = new CustomerFeedback();
        customerFeedback.setCustomerEmail("sampath.pk@cloudnowtech.com");
        customerFeedback.setCustomerName("Test Sampt");
        customerFeedback.setCustomerId("6949011390693");
        customerFeedback.setComments("Hello yes");
        customerFeedback.setCreatedDate(new Date());
        customerFeedbackList.add(customerFeedback);

        CustomerFeedbackKVDto customerFeedbackKVDto = new CustomerFeedbackKVDto();
        customerFeedbackKVDto.setCustomerName("Test Sampt");
        customerFeedbackKVDto.setComments("Hello yes");

        Mockito.when(customerFeedbackRepositoryI.findAllByOrderByCreatedDateDesc()).thenReturn(customerFeedbackList);
        when(modelMapper.map(any(), any())).thenReturn(customerFeedbackKVDto);

        List<CustomerFeedbackKVDto> customerFeedbackKVDtoList = customerService.getCustomerFeedback();

        Assertions.assertEquals(customerFeedbackKVDto.getComments(), customerFeedbackKVDtoList.get(0).getComments());

    }

    @Test
    @DisplayName("Should get customer measurement list by customer id")
    void getCustomerMeasurementByCustomerId_Success() {
        // Arrange
        String customerId = "123";

        CustomerMeasurement customerMeasurementDataObj1 = new CustomerMeasurement();
        customerMeasurementDataObj1.setCustomerId(customerId);
        customerMeasurementDataObj1.setName("Jane");

        CustomerMeasurement customerMeasurementDataObj2 = new CustomerMeasurement();
        customerMeasurementDataObj2.setCustomerId(customerId);
        customerMeasurementDataObj2.setName("John");

        List<CustomerMeasurement> customerMeasurementDataList = new ArrayList<>();
        customerMeasurementDataList.add(customerMeasurementDataObj1);
        customerMeasurementDataList.add(customerMeasurementDataObj2);

        CustomerMeasurementDto expectedCustomerMeasurementDto1 = new CustomerMeasurementDto();
        expectedCustomerMeasurementDto1.setCustomerId(customerId);
        expectedCustomerMeasurementDto1.setCustomerName("Jane");

        CustomerMeasurementDto expectedCustomerMeasurementDto2 = new CustomerMeasurementDto();
        expectedCustomerMeasurementDto2.setCustomerId(customerId);
        expectedCustomerMeasurementDto2.setCustomerName("John");

        when(customerMeasurementRepositoryI.findByCustomerId(customerId))
                .thenReturn(customerMeasurementDataList);

        when(modelMapper.map(customerMeasurementDataObj1, CustomerMeasurementDto.class))
                .thenReturn(expectedCustomerMeasurementDto1);

        when(modelMapper.map(customerMeasurementDataObj2, CustomerMeasurementDto.class))
                .thenReturn(expectedCustomerMeasurementDto2);

        // Act
        List<CustomerMeasurementDto> actualCustomerMeasurementDtoList = customerService.getCustomerMeasurementByCustomerId(customerId);

        // Assert
        Assertions.assertEquals(2, actualCustomerMeasurementDtoList.size());
        Assertions.assertEquals(expectedCustomerMeasurementDto1, actualCustomerMeasurementDtoList.get(0));
        Assertions.assertEquals(expectedCustomerMeasurementDto2, actualCustomerMeasurementDtoList.get(1));
        verify(customerMeasurementRepositoryI).findByCustomerId(customerId);
        verify(modelMapper).map(customerMeasurementDataObj1, CustomerMeasurementDto.class);
        verify(modelMapper).map(customerMeasurementDataObj2, CustomerMeasurementDto.class);
    }

    @Test
    @DisplayName("Should throw RecordNotFoundException when customer measurement not found")
    void getCustomerMeasurementByCustomerId_RecordNotFoundException() {
        // Arrange
        String customerId = "123";

        when(customerMeasurementRepositoryI.findByCustomerId(customerId))
                .thenReturn(Collections.emptyList());

        // Act and Assert
        Assertions.assertThrows(RecordNotFoundException.class, () -> customerService.getCustomerMeasurementByCustomerId(customerId));

        verify(customerMeasurementRepositoryI).findByCustomerId(customerId);
        verifyNoInteractions(modelMapper); // No mapping should be attempted
    }

    @Test
    @DisplayName("Should delete customer measurement by ID and return DTO")
    void deleteCustomerMeasurementById_Success() {
        // Arrange
        Integer customerMeasurementId = 123;
        CustomerMeasurement customerMeasurement = new CustomerMeasurement();
        customerMeasurement.setCustomerMeasurementId(customerMeasurementId);

        when(customerMeasurementRepositoryI.findById(customerMeasurementId))
                .thenReturn(Optional.of(customerMeasurement));

        when(modelMapper.map(customerMeasurement, CustomerMeasurementDto.class))
                .thenReturn(new CustomerMeasurementDto());

        // Act
        CustomerMeasurementDto customerMeasurementDto = customerService.deleteCustomerMeasurementById(customerMeasurementId);

        // Assert
        Assertions.assertNotNull(customerMeasurementDto);
        verify(customerMeasurementRepositoryI).deleteById(customerMeasurementId);
        verify(modelMapper).map(customerMeasurement, CustomerMeasurementDto.class);
    }

    @Test
    @DisplayName("Should throw RecordNotFoundException when customer measurement ID not found")
    void deleteCustomerMeasurementById_RecordNotFoundException() {
        // Arrange
        Integer customerMeasurementId = 123;

        when(customerMeasurementRepositoryI.findById(customerMeasurementId))
                .thenReturn(Optional.empty());

        // Act and Assert
        Assertions.assertThrows(RecordNotFoundException.class,
                () -> customerService.deleteCustomerMeasurementById(customerMeasurementId));

        verify(customerMeasurementRepositoryI).findById(customerMeasurementId);
        verifyNoInteractions(modelMapper); // No mapping should be attempted
    }

    @Test
    @DisplayName("Should save customer measurement and return DTO")
    void saveMeasurement_Success() throws ParseException {
        // Arrange
        CustomerMeasurementDto customerMeasurementDto = new CustomerMeasurementDto();
        CustomerMeasurement customerMeasurement = new CustomerMeasurement();
        customerMeasurement.setName("Test Measurement");
        customerMeasurement.setCustomerId("testCustomer");

        when(adminMapperUtil.convertFromCustomerMeasurementDto(customerMeasurementDto)).thenReturn(customerMeasurement);

        when(customerMeasurementRepositoryI.findByNameAndCustomerId(
                customerMeasurement.getName(), customerMeasurement.getCustomerId()))
                .thenReturn(null); // No existing measurement found

        when(customerMeasurementRepositoryI.save(customerMeasurement))
                .thenReturn(customerMeasurement); // Return saved measurement

        when(adminMapperUtil.convertToCustomerMeasurementDto(customerMeasurement)).thenReturn(customerMeasurementDto);

        // Act
        CustomerMeasurementDto savedCustomerMeasurementDto = customerService.saveMeasurement(customerMeasurementDto);

        // Assert
        Assertions.assertNotNull(savedCustomerMeasurementDto);
        verify(customerMeasurementRepositoryI)
                .findByNameAndCustomerId(customerMeasurement.getName(), customerMeasurement.getCustomerId());
        verify(customerMeasurementRepositoryI).save(customerMeasurement);
    }

    @Test
    @DisplayName("Should throw UniqueRecordException when customer measurement already exists")
    void saveMeasurement_UniqueRecordException() {
        // Arrange
        CustomerMeasurementDto customerMeasurementDto = new CustomerMeasurementDto();
        CustomerMeasurement customerMeasurement = new CustomerMeasurement();
        customerMeasurement.setName("Test Measurement");
        customerMeasurement.setCustomerId("testCustomer");

        when(adminMapperUtil.convertFromCustomerMeasurementDto(customerMeasurementDto)).thenReturn(customerMeasurement);

        when(customerMeasurementRepositoryI.findByNameAndCustomerId(
                customerMeasurement.getName(), customerMeasurement.getCustomerId()))
                .thenReturn(customerMeasurement); // Existing measurement found

        // Act and Assert
        Assertions.assertThrows(UniqueRecordException.class, () -> customerService.saveMeasurement(customerMeasurementDto));

        verify(customerMeasurementRepositoryI).findByNameAndCustomerId(customerMeasurement.getName(), customerMeasurement.getCustomerId());
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


        when(customerMeasurementRepositoryI.findById(customerMeasurementId)).thenReturn(Optional.of(existingMeasurement));
        when(messageSource.getMessage(any(), any(), any())).thenReturn("Customer measurement id not found");
        when(adminMapperUtil.convertFromCustomerMeasurementDto(customerMeasurementInputDto)).thenReturn(existingMeasurement);
        when(customerMeasurementRepositoryI.save(existingMeasurement)).thenReturn(existingMeasurement);
        when(adminMapperUtil.convertToCustomerMeasurementDto(existingMeasurement)).thenReturn(customerMeasurementInputDto);

        CustomerMeasurementDto updatedCustomerMeasurementDto = customerService.updateCustomerMeasurement(customerMeasurementInputDto, customerMeasurementId);
        Assertions.assertEquals(customerMeasurementInputDto.getCustomerEmail(), updatedCustomerMeasurementDto.getCustomerEmail());
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
        Assertions.assertThrows(RecordNotFoundException.class, () -> customerService.updateCustomerMeasurement(customerMeasurementInputDto, 123));

        // Verify
        verify(customerMeasurementRepositoryI).findById(123);
    }

    @Test
    @DisplayName("Should save customer wishlist and return DTO")
    void saveCustomerWishlist_Success() {
        // Arrange
        CustomerWishlistDto customerWishlistDto = new CustomerWishlistDto();
        CustomerWishlist customerWishlist = new CustomerWishlist();
        customerWishlist.setCustomerId("testCustomer");
        customerWishlist.setVariantId("testVariant");

        when(modelMapper.map(customerWishlistDto, CustomerWishlist.class))
                .thenReturn(customerWishlist);

        when(customerWishlistRepository.save(customerWishlist))
                .thenReturn(customerWishlist); // Return saved wishlist

        when(modelMapper.map(customerWishlist, CustomerWishlistDto.class))
                .thenReturn(customerWishlistDto);

        // Act
        CustomerWishlistDto savedCustomerWishlistDto = customerService.saveCustomerWishlist(customerWishlistDto);

        // Assert
        Assertions.assertNotNull(savedCustomerWishlistDto);
        verify(modelMapper).map(customerWishlistDto, CustomerWishlist.class);
        verify(customerWishlistRepository).save(customerWishlist);
        verify(modelMapper).map(customerWishlist, CustomerWishlistDto.class);
    }

    @Test
    @DisplayName("Should get customer wishlist by customer id and variant id and return DTO")
    void getCustomerWishlistByCustomerIdAndVariantId_Success() {
        // Arrange
        String customerId = "testCustomer";
        String variantId = "testVariant";
        CustomerWishlist existingCustomerWishlistObj = new CustomerWishlist();
        existingCustomerWishlistObj.setCustomerId(customerId);
        existingCustomerWishlistObj.setVariantId(variantId);

        when(customerWishlistRepository.findByCustomerIdAndVariantId(customerId, variantId))
                .thenReturn(existingCustomerWishlistObj);

        CustomerWishlistDto expectedCustomerWishlistDto = new CustomerWishlistDto();
        when(modelMapper.map(existingCustomerWishlistObj, CustomerWishlistDto.class))
                .thenReturn(expectedCustomerWishlistDto);

        // Act
        CustomerWishlistDto customerWishlistDto = customerService.getCustomerWishlistByCustomerIdAndVariantId(customerId, variantId);

        // Assert
        Assertions.assertNotNull(customerWishlistDto);
        Assertions.assertEquals(expectedCustomerWishlistDto, customerWishlistDto);
        verify(customerWishlistRepository).findByCustomerIdAndVariantId(customerId, variantId);
        verify(modelMapper).map(existingCustomerWishlistObj, CustomerWishlistDto.class);
    }

    @Test
    @DisplayName("Should throw RecordNotFoundException when customer wishlist not found")
    void getCustomerWishlistByCustomerIdAndVariantId_RecordNotFound() {
        // Arrange
        String customerId = "nonExistentCustomer";
        String variantId = "nonExistentVariant";

        when(customerWishlistRepository.findByCustomerIdAndVariantId(customerId, variantId))
                .thenReturn(null); // Simulate not found

        // Act and Assert
        Assertions.assertThrows(RecordNotFoundException.class, () -> customerService.getCustomerWishlistByCustomerIdAndVariantId(customerId, variantId));
        verify(customerWishlistRepository).findByCustomerIdAndVariantId(customerId, variantId);
    }

    @Test
    @DisplayName("Should delete customer wishlist by customer id and variant id and return DTO")
    void deleteCustomerWishlistByCustomerIdAndVariantId_Success() {
        // Arrange
        String customerId = "testCustomer";
        String variantId = "testVariant";
        CustomerWishlist existingCustomerWishlistObj = new CustomerWishlist();
        existingCustomerWishlistObj.setCustomerId(customerId);
        existingCustomerWishlistObj.setVariantId(variantId);

        when(customerWishlistRepository.findByCustomerIdAndVariantId(customerId, variantId))
                .thenReturn(existingCustomerWishlistObj);

        CustomerWishlistDto expectedCustomerWishlistDto = new CustomerWishlistDto();
        when(modelMapper.map(existingCustomerWishlistObj, CustomerWishlistDto.class))
                .thenReturn(expectedCustomerWishlistDto);

        // Act
        CustomerWishlistDto customerWishlistDto = customerService.deleteCustomerWishlistByCustomerIdAndVariantId(customerId, variantId);

        // Assert
        Assertions.assertNotNull(customerWishlistDto);
        Assertions.assertEquals(expectedCustomerWishlistDto, customerWishlistDto);
        verify(customerWishlistRepository).findByCustomerIdAndVariantId(customerId, variantId);
        verify(customerWishlistRepository).deleteByCustomerIdAndVariantId(customerId, variantId);
        verify(modelMapper).map(existingCustomerWishlistObj, CustomerWishlistDto.class);
    }

    @Test
    @DisplayName("Should throw RecordNotFoundException when customer wishlist not found for deletion")
    void deleteCustomerWishlistByCustomerIdAndVariantId_RecordNotFound() {
        // Arrange
        String customerId = "nonExistentCustomer";
        String variantId = "nonExistentVariant";

        when(customerWishlistRepository.findByCustomerIdAndVariantId(customerId, variantId))
                .thenReturn(null); // Simulate not found

        // Act and Assert
        Assertions.assertThrows(RecordNotFoundException.class, () -> customerService.deleteCustomerWishlistByCustomerIdAndVariantId(customerId, variantId));
        verify(customerWishlistRepository).findByCustomerIdAndVariantId(customerId, variantId);
        verify(customerWishlistRepository, never()).deleteByCustomerIdAndVariantId(customerId, variantId);
    }

    @Test
    @DisplayName("Save Customer OTP - Existing Customer")
    void testSaveCustomerOtp_ExistingCustomer() {
        // Prepare test data
        String customerEmail = "test@example.com";
        CustomerOtp existingCustomerOtpObj = new CustomerOtp();
        existingCustomerOtpObj.setCustomerEmail(customerEmail);

        // Mock the behavior of customerOtpRepository
        when(customerOtpRepository.findByCustomerEmail(customerEmail)).thenReturn(existingCustomerOtpObj);

        // Mock the behavior of customerOtpRepository.save
        CustomerOtp savedCustomerOtpObj = new CustomerOtp();
        savedCustomerOtpObj.setCode(123); // Set the expected OTP code
        when(customerOtpRepository.save(any())).thenReturn(savedCustomerOtpObj);

        // Mock the behavior of modelMapper
        when(modelMapper.map(savedCustomerOtpObj, CustomerOtpDto.class)).thenReturn(new CustomerOtpDto());
        when(emailNotificationService.sendLoginIssueMailForAdmin(any(Notification.class))).thenReturn(null);

        // Perform the method call
        CustomerOtpDto customerOtpDto = customerService.saveCustomerOtp(customerEmail);

        // Verify interactions and assertions
        verify(customerOtpRepository).findByCustomerEmail(customerEmail);
        verify(customerOtpRepository).save(any(CustomerOtp.class));
        verify(emailNotificationService).sendLoginIssueMailForAdmin(any(Notification.class));
        Assertions.assertNotNull(customerOtpDto);
    }

    @Test
    @DisplayName("Save Customer OTP - New Customer")
    void testSaveCustomerOtp_NewCustomer() {
        // Arrange
        String customerEmail = "new@example.com";

        when(customerOtpRepository.findByCustomerEmail(customerEmail)).thenReturn(null);

        CustomerOtp savedCustomerOtp = new CustomerOtp();
        when(customerOtpRepository.save(any(CustomerOtp.class))).thenReturn(savedCustomerOtp);

        when(modelMapper.map(savedCustomerOtp, CustomerOtpDto.class)).thenReturn(new CustomerOtpDto());

        // Act
        CustomerOtpDto customerOtpDto = customerService.saveCustomerOtp(customerEmail);

        // Assert
        Assertions.assertNotNull(customerOtpDto);

        // Verify that sendOtpCodeForCustomer was called
        verify(emailNotificationService).sendLoginIssueMailForAdmin(any(Notification.class));
    }

    @Test
    @DisplayName("Validate Customer OTP - Existing OTP")
    void testValidateCustomerOtp_ExistingOTP() {
        // Arrange
        CustomerOtpDto customerOtpDto = new CustomerOtpDto();
        customerOtpDto.setCustomerEmail("test@example.com");
        customerOtpDto.setCode(1234);

        CustomerOtp existingCustomerOtp = new CustomerOtp();
        when(customerOtpRepository.findByCustomerEmailAndCode("test@example.com", 1234))
                .thenReturn(existingCustomerOtp);

        CustomerOtpDto mappedCustomerOtpDto = new CustomerOtpDto();
        when(modelMapper.map(existingCustomerOtp, CustomerOtpDto.class)).thenReturn(mappedCustomerOtpDto);

        // Act
        CustomerOtpDto validatedCustomerOtpDto = customerService.validateCustomerOtp(customerOtpDto);

        // Assert
        Assertions.assertNotNull(validatedCustomerOtpDto);
        Assertions.assertSame(mappedCustomerOtpDto, validatedCustomerOtpDto);
        verify(customerOtpRepository).findByCustomerEmailAndCode("test@example.com", 1234);
    }

    @Test
    @DisplayName("Validate Customer OTP - Invalid OTP")
    void testValidateCustomerOtp_InvalidOTP() {
        // Arrange
        CustomerOtpDto customerOtpDto = new CustomerOtpDto();
        customerOtpDto.setCustomerEmail("test@example.com");
        customerOtpDto.setCode(1234);

        when(customerOtpRepository.findByCustomerEmailAndCode("test@example.com", 1234))
                .thenReturn(null);

        // Act and Assert
        Assertions.assertThrows(RecordNotFoundException.class, () -> customerService.validateCustomerOtp(customerOtpDto));

        verify(customerOtpRepository).findByCustomerEmailAndCode("test@example.com", 1234);
    }

    @Test
    @DisplayName("Get Customer Feedback Filter")
    void testGetCustomerFeedbackFilter() throws ParseException {
        // Arrange
        String fromDate = "2023-07-01";
        String toDate = "2023-07-31";

        Date createdFromDateObj = new SimpleDateFormat("yyyy-MM-dd").parse(fromDate);
        Date createdToDateObj = new SimpleDateFormat("yyyy-MM-dd").parse(toDate);

        CustomerFeedback customerFeedback = new CustomerFeedback();
        customerFeedback.setCustomerName("John Doe");
        customerFeedback.setCreatedDate(new Date());

        List<CustomerFeedback> customerFeedbackList = new ArrayList<>();
        customerFeedbackList.add(customerFeedback);

        when(customerFeedbackRepositoryI.findByCreatedDateBetweenOrderByCreatedDateDesc(
                createdFromDateObj, createdToDateObj)).thenReturn(customerFeedbackList);

        CustomerFeedbackKVDto mappedCustomerFeedbackKVDto = new CustomerFeedbackKVDto();
        when(modelMapper.map(any(CustomerFeedback.class), eq(CustomerFeedbackKVDto.class)))
                .thenReturn(mappedCustomerFeedbackKVDto);

        // Act
        List<CustomerFeedbackKVDto> customerFeedbackKVDtoList = customerService.getCustomerFeedbackFilter(fromDate, toDate);

        // Assert
        Assertions.assertNotNull(customerFeedbackKVDtoList);
        Assertions.assertEquals(1, customerFeedbackKVDtoList.size());
        Assertions.assertSame(mappedCustomerFeedbackKVDto, customerFeedbackKVDtoList.get(0));
        verify(customerFeedbackRepositoryI).findByCreatedDateBetweenOrderByCreatedDateDesc(createdFromDateObj, createdToDateObj);
        verify(modelMapper).map(any(CustomerFeedback.class), eq(CustomerFeedbackKVDto.class));
    }

    @Test
    @DisplayName("Should get all customer list from Shopify and process HTTP response with next page")
    void testGetAllCustomerListFromShopify_Success() throws Exception {
        // Arrange
        String shopifyCustomerEndPoint = "https://example.com/shopify/customers";
        String shopifyAccessToken = "your-access-token";

        doNothing().when(customerServiceLogger).info(anyString());

        HttpURLConnection httpConnectionMock = mock(HttpURLConnection.class);
        when(httpConnectionMock.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);

        InputStream inputStream = getInputStream();
        when(httpConnectionMock.getInputStream()).thenReturn(inputStream);

        int responseCode = HttpURLConnection.HTTP_OK;
        when(httpConnectionMock.getResponseCode()).thenReturn(responseCode);

        String linkHeader = "<https://example.com/shopify/customers?page_info=abc>; rel=\"next\"";
        when(httpConnectionMock.getHeaderField(ApplicationConstants.ShopifyApiHeaders.LINK))
                .thenReturn(linkHeader);

        List<CustomerKeyValueDto> allCustomerList = new ArrayList<>();

        URL urlConnectorMock = mock(URL.class);
        whenNew(URL.class).withArguments(anyString()).thenReturn(urlConnectorMock);
        when(urlConnectorMock.openConnection()).thenReturn(httpConnectionMock);

        // Act
        Method privateMethod = CustomerService.class.getDeclaredMethod("processHttpResponseForCustomerKeyValueDtoList", HttpURLConnection.class, List.class);
        privateMethod.setAccessible(true);
        boolean shouldBreak = false;

        customerService.processHttpResponseForCustomerKeyValueDtoList(httpConnectionMock, allCustomerList);

        when(customerService.parseNextPageInfo(linkHeader)).thenReturn(null);

        List<CustomerKeyValueDto> actualCustomerList = customerService.getAllCustomerListFromShopify(
                shopifyCustomerEndPoint, shopifyAccessToken);

        // Assert
        Assertions.assertNotNull(actualCustomerList);
        Assertions.assertFalse(shouldBreak);
        Assertions.assertEquals(2, allCustomerList.size());
    }

    private static InputStream getInputStream() {
        String jsonContent = "{\n" +
                "    \"customers\": [\n" +
                "        {\n" +
                "            \"id\": 7251638386917,\n" +
                "            \"email\": \"johndoe@gmail.com\",\n" +
                "            \"created_at\": \"2023-08-18T11:03:06+05:30\",\n" +
                "            \"first_name\": John,\n" +
                "            \"last_name\": Doe,\n" +
                "            \"orders_count\": 3,\n" +
                "            \"total_spent\": \"1500.00\",\n" +
                "            \"verified_email\": true,\n" +
                "            \"phone\": 1234567890\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": 7251638059237,\n" +
                "            \"email\": \"minhuihuang2011@gmail.com\",\n" +
                "            \"created_at\": \"2023-08-17T12:59:10+05:30\",\n" +
                "            \"first_name\": Jane,\n" +
                "            \"last_name\": Smith,\n" +
                "            \"orders_count\": 4,\n" +
                "            \"total_spent\": \"1400.00\",\n" +
                "            \"verified_email\": true,\n" +
                "            \"phone\": 0987654321\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        InputStream inputStream = new ByteArrayInputStream(jsonContent.getBytes());
        return inputStream;
    }

    @Test
    @DisplayName("Should reset customer login password successfully")
    void testCustomerLoginPasswordReset_Success() {
        // Arrange
        ResetPasswordDto resetPasswordDto = new ResetPasswordDto();
        resetPasswordDto.setId(123L);
        resetPasswordDto.setPassword("NewPassword");
        resetPasswordDto.setPassword_confirmation("NewPassword");

        CustomerResetPasswordDto customerResetPasswordDto = new CustomerResetPasswordDto();
        customerResetPasswordDto.setCustomer(resetPasswordDto);

        Long customerId = 123L;
        String shopifyAccessToken = "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";

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set(ApplicationConstants.ShopifyApiHeaders.SHOPIFY_ACCESS_TOKEN, shopifyAccessToken);
        HttpEntity<CustomerResetPasswordDto> requestEntity = new HttpEntity<>(customerResetPasswordDto, httpHeaders);

        String apiUrl = shopifyResetPasswordEndPoint + customerId + ".json";

        ResponseEntity<CustomerResetPasswordDto> responseEntity = new ResponseEntity<>(customerResetPasswordDto, HttpStatus.OK);
        when(restTemplate.exchange(apiUrl, HttpMethod.PUT, requestEntity, CustomerResetPasswordDto.class))
                .thenReturn(responseEntity);

        // Act
        ResetPasswordDto resetPasswordDtoResponse = customerService.customerLoginPasswordReset(customerResetPasswordDto, customerId, shopifyAccessToken);

        // Assert
        Assertions.assertNotNull(resetPasswordDtoResponse);
        Assertions.assertEquals(customerResetPasswordDto.getCustomer(), resetPasswordDtoResponse);

        // Verify interactions
        verify(restTemplate, times(1)).exchange(apiUrl, HttpMethod.PUT, requestEntity, CustomerResetPasswordDto.class);
    }

    @Test
    @DisplayName("Should get filtered customer count response")
    void testGetFilterCustomerList_Success() throws Exception {
        // Arrange
        String fromDate = "2023-01-01";
        String toDate = "2023-06-30";
        String shopifyCustomerEndPoint = "https://example.com/shopify/customers";
        String shopifyAccessToken = "your-access-token";

        doNothing().when(customerServiceLogger).info(anyString());

        HttpURLConnection httpConnectionMock = mock(HttpURLConnection.class);
        when(httpConnectionMock.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);

        InputStream inputStream = getInputStream();
        when(httpConnectionMock.getInputStream()).thenReturn(inputStream);

        int responseCode = HttpURLConnection.HTTP_OK;
        when(httpConnectionMock.getResponseCode()).thenReturn(responseCode);

        CustomerDto customer1 = new CustomerDto();
        customer1.setCreated_at(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").parse("2023-01-15T10:30:00Z"));

        CustomerDto customer2 = new CustomerDto();
        customer2.setCreated_at(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").parse("2023-03-20T08:45:00Z"));

        List<CustomerDto> filterCustomerList = new ArrayList<>();
        filterCustomerList.add(customer1);
        filterCustomerList.add(customer2);

        List<String> calendarMonthsList = CustomerService.getCalendarMonthsList();
        Map<String, Double> customerYearlyDataMap = calendarMonthsList.stream().collect(Collectors.toMap(
                month -> month,
                month -> 0.0 ));

        filterCustomerList.forEach(data -> {
            String createdMonth = AppUtil.getCreatedMonth(data.getCreated_at());
            customerYearlyDataMap.put(createdMonth, customerYearlyDataMap.get(createdMonth) + 1);
        });

        URL urlConnectorMock = mock(URL.class);
        whenNew(URL.class).withArguments(anyString()).thenReturn(urlConnectorMock);
        when(urlConnectorMock.openConnection()).thenReturn(httpConnectionMock);

        Method privateMethod = CustomerService.class.getDeclaredMethod("processHttpResponseForCustomerKeyValueDtoList", HttpURLConnection.class, List.class);
        privateMethod.setAccessible(true);
        boolean shouldBreak = false;
        customerService.processHttpResponseForCustomerDtoList(httpConnectionMock, filterCustomerList);

        customerService.getFilterCustomerListFromShopify(shopifyCustomerEndPoint, fromDate, toDate, shopifyAccessToken);

        String linkHeader = "<https://example.com/shopify/customers?page_info=abc>; rel=\"next\"";
        when(httpConnectionMock.getHeaderField(ApplicationConstants.ShopifyApiHeaders.LINK))
                .thenReturn(linkHeader);
        // Act
        CustomerCountResponseDto customerCountResponseDto = customerService.getFilterCustomerList(shopifyCustomerEndPoint, fromDate, toDate);

        // Assert
        Assertions.assertNotNull(customerCountResponseDto);
        Assertions.assertNotNull(customerCountResponseDto.getCustomerCountByYear());
        Assertions.assertFalse(customerCountResponseDto.getCustomerCountByYear().isEmpty());
        Assertions.assertNotNull(customerCountResponseDto.getPercentChange());
        Assertions.assertFalse(shouldBreak);
    }

    @Test
    @DisplayName("Should calculate customer count by month for given years")
    void testGetCustomerCountByMonthForYears_LastYear() throws Exception {
        // Arrange
        int lastYear = 2021;
        int currentYear = 2022;
        String shopifyCustomerEndPoint = "https://example.com/shopify/customers";
        String shopifyAccessToken = "your-access-token";

        doNothing().when(customerServiceLogger).info(anyString());

        // Mocking dependencies
        CustomerService customerServiceSpy = Mockito.spy(customerService);
        HttpURLConnection httpConnectionMock = mock(HttpURLConnection.class);
        when(httpConnectionMock.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);

        InputStream inputStreamMock = getInputStream();
        when(httpConnectionMock.getInputStream()).thenReturn(inputStreamMock);

        int responseCode = HttpURLConnection.HTTP_OK;
        when(httpConnectionMock.getResponseCode()).thenReturn(responseCode);

        // Stub fetchCustomerDataFromShopify for both years
        doAnswer(invocation -> {
            Map<String, Double> monthlyCustomerDataMap = invocation.getArgument(1);
            monthlyCustomerDataMap.put("January", 10.0);
            monthlyCustomerDataMap.put("February", 15.0);
            return null; // Since the method returns void, return null here
        }).when(customerServiceSpy).fetchCustomerDataFromShopify(eq(shopifyCustomerEndPoint), any(), eq(lastYear), eq(shopifyAccessToken));

        doAnswer(invocation -> {
            Map<String, Double> monthlyCustomerDataMap = invocation.getArgument(1);
            monthlyCustomerDataMap.put("January", 10.0);
            monthlyCustomerDataMap.put("February", 15.0);
            return null; // Since the method returns void, return null here
        }).when(customerServiceSpy).fetchCustomerDataFromShopify(eq(shopifyCustomerEndPoint), any(), eq(currentYear), eq(shopifyAccessToken));

        String linkHeader = "<https://example.com/shopify/customers?page_info=abc>; rel=\"next\"";
        when(httpConnectionMock.getHeaderField(ApplicationConstants.ShopifyApiHeaders.LINK))
                .thenReturn(linkHeader);

        URL urlConnectorMock = mock(URL.class);
        whenNew(URL.class).withArguments(anyString()).thenReturn(urlConnectorMock);
        when(urlConnectorMock.openConnection()).thenReturn(httpConnectionMock);

        Map<String, Double> monthlyCustomerDataMap = new HashMap<>();
        monthlyCustomerDataMap.put("January", 10.0);
        monthlyCustomerDataMap.put("February", 15.0);

        int year = 2023;

        // Stub processHttpResponseForCustomer for both years
        Method privateMethod = CustomerService.class.getDeclaredMethod("processHttpResponseForCustomer", HttpURLConnection.class, Map.class, int.class);
        privateMethod.setAccessible(true);
        boolean shouldBreak = false;

        customerServiceSpy.processHttpResponseForCustomer(httpConnectionMock, monthlyCustomerDataMap, year);

        when(customerServiceSpy.parseNextPageInfo(linkHeader)).thenReturn(null);

        // Act
        CustomerCountResponseDto customerCountResponseDto = customerServiceSpy.getCustomerCountByMonthForYears(shopifyCustomerEndPoint, lastYear, currentYear, shopifyAccessToken);

        // Assert
        Assertions.assertNotNull(customerCountResponseDto);
        Map<String, Map<String, Double>> customerCountByYearMap = customerCountResponseDto.getCustomerCountByYear();
        Assertions.assertNotNull(customerCountByYearMap);

        Map<String, Double> lastYearCountsMap = customerCountByYearMap.get(Integer.toString(lastYear));
        Assertions.assertNotNull(lastYearCountsMap);

        Map<String, Double> currentYearCountsMap = customerCountByYearMap.get(Integer.toString(currentYear));
        Assertions.assertNotNull(currentYearCountsMap);
        Assertions.assertFalse(shouldBreak);
    }

    @Test
    @DisplayName("Should calculate customer count by month for given years")
    void testGetCustomerCountByMonthForYears_CurrentYear() throws Exception {
        // Arrange
        int lastYear = 2021;
        int currentYear = 2022;
        String shopifyCustomerEndPoint = "https://example.com/shopify/customers";
        String shopifyAccessToken = "your-access-token";

        Map<String, Double> customerLastYearDataMap = new HashMap<>();
        customerLastYearDataMap.put("January", 10.0);
        customerLastYearDataMap.put("February", 15.0);

        Map<String, Double> CustomerCurrentYearDataMap = new HashMap<>();
        CustomerCurrentYearDataMap.put("January", 20.0);
        CustomerCurrentYearDataMap.put("February", 25.0);

        doNothing().when(customerServiceLogger).info(anyString());

        HttpURLConnection httpConnectionMock = mock(HttpURLConnection.class);
        when(httpConnectionMock.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);

        InputStream inputStream = getInputStream();
        when(httpConnectionMock.getInputStream()).thenReturn(inputStream);

        int responseCode = HttpURLConnection.HTTP_OK;
        when(httpConnectionMock.getResponseCode()).thenReturn(responseCode);

        CustomerService.getCalendarMonthsList();

        CustomerService customerServiceSpy = Mockito.spy(customerService);

        doAnswer(invocation -> {
            Map<String, Double> monthlyCustomerDataMap = invocation.getArgument(1);
            monthlyCustomerDataMap.put("January", 10.0);
            monthlyCustomerDataMap.put("February", 15.0);
            return null; // Since the method returns void, return null here
        }).when(customerServiceSpy).fetchCustomerDataFromShopify(eq(shopifyCustomerEndPoint), any(), eq(lastYear), eq(shopifyAccessToken));

        doAnswer(invocation -> {
            Map<String, Double> monthlyCustomerDataMap = invocation.getArgument(1);
            monthlyCustomerDataMap.put("January", 10.0);
            monthlyCustomerDataMap.put("February", 15.0);
            return null; // Since the method returns void, return null here
        }).when(customerServiceSpy).fetchCustomerDataFromShopify(eq(shopifyCustomerEndPoint), any(), eq(currentYear), eq(shopifyAccessToken));

        String linkHeader = "<https://example.com/shopify/customers?page_info=abc>; rel=\"next\"";
        when(httpConnectionMock.getHeaderField(ApplicationConstants.ShopifyApiHeaders.LINK))
                .thenReturn(linkHeader);

        // Act
        customerService.parseNextPageInfo(linkHeader);
        CustomerCountResponseDto customerCountResponseDto = customerService.getCustomerCountByMonthForYears(shopifyCustomerEndPoint, lastYear, currentYear, shopifyAccessToken);

        // Assert
        Assertions.assertNotNull(customerCountResponseDto);
        Map<String, Map<String, Double>> customerCountByYearMap = customerCountResponseDto.getCustomerCountByYear();
        Assertions.assertNotNull(customerCountByYearMap);

        Map<String, Double> lastYearCountsMap = customerCountByYearMap.get(Integer.toString(lastYear));
        Assertions.assertNotNull(lastYearCountsMap);

        Map<String, Double> currentYearCountsMap = customerCountByYearMap.get(Integer.toString(currentYear));
        Assertions.assertNotNull(currentYearCountsMap);
    }

    @Test
    void testCreateCustomer_Failure() {
        // Prepare mock data and dependencies
        String shopifyCustomerCreationEndPoint = "https://e4d27c.myshopify.com/admin/api/2023-07/customers.json";
        String shopifyAccessToken = "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";
        List<CustomerKvDto.Addressess> addressess = new ArrayList<>();
        CustomerKvDto customerKvDto = new CustomerKvDto("John", "J", "john@gmail.com", true, addressess, "Password@123", "Password@123", true, true);
        CustomerCreationKVDto customerCreationKVDto = new CustomerCreationKVDto();
        customerCreationKVDto.setCustomer(customerKvDto);
        HttpHeaders mockHttpHeaders = new HttpHeaders();
        mockHttpHeaders.setContentType(MediaType.APPLICATION_JSON);
        mockHttpHeaders.set(ApplicationConstants.ShopifyApiHeaders.SHOPIFY_ACCESS_TOKEN, shopifyAccessToken);
        HttpEntity<String> mockHttpEntity = new HttpEntity<>(anyString(), mockHttpHeaders);
        HttpClientErrorException mockException = new HttpClientErrorException(HttpStatus.BAD_REQUEST);

        // Mock the behavior of the RestTemplate to throw an exception
        when(restTemplate.exchange(shopifyCustomerCreationEndPoint, any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
                .thenThrow(mockException);

        // Mock the behavior of the messageSource
        when(messageSource.getMessage(eq("api.customer.saved.exception"), isNull(), eq(Locale.ENGLISH)))
                .thenReturn("Email id already exist");

        // Call the method under test (it should throw UniqueRecordException)
        Assertions.assertThrows(UniqueRecordException.class, () -> customerService.createCustomer(customerCreationKVDto, shopifyCustomerCreationEndPoint, shopifyAccessToken));
    }

    @Test
    void testUpdateCustomerDetails_Success() {
        String shopifyUpdateCustomerDetailsEndPoint = "https://e4d27c.myshopify.com/admin/api/2023-07/customers/7320574099685.json";
        CustomerDetailsKeyValueDto customerDetailsKeyValueDto = new CustomerDetailsKeyValueDto();
        customerDetailsKeyValueDto.setId(7320574099685L);
        customerDetailsKeyValueDto.setFirst_name("Karthik");
        customerDetailsKeyValueDto.setLast_name("K");
        customerDetailsKeyValueDto.setPhone("7358545729");
        CustomerDetailKeyValueResponseDto customerDto = new CustomerDetailKeyValueResponseDto();
        customerDto.setCustomer(customerDetailsKeyValueDto);
        ResponseEntity<String> mockResponseEntity = new ResponseEntity<>("{ \"customerId\": \"123\" }", HttpStatus.OK);

        // Mock the behavior of the RestTemplate
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(mockResponseEntity);

        // Call the method under test
        CustomerDetailKeyValueResponseDto result = customerService.updateCustomerDetails(
                customerDto.toString(), shopifyUpdateCustomerDetailsEndPoint, "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", new Gson());

        // Verify the expected result
        Assertions.assertNotNull(result);
    }

    @Test
    void testUpdateCustomerDetails_Failure() {
        String shopifyUpdateCustomerDetailsEndPoint = "https://e4d27c.myshopify.com/admin/api/2023-07/customers/7320574099685.json";
        CustomerDetailKeyValueResponseDto customerDto = getCustomerDetailKeyValueResponseDto();
        HttpHeaders mockHttpHeaders = new HttpHeaders();
        mockHttpHeaders.setContentType(MediaType.APPLICATION_JSON);
        mockHttpHeaders.set(ApplicationConstants.ShopifyApiHeaders.SHOPIFY_ACCESS_TOKEN, "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");
        HttpEntity<String> mockHttpEntity = new HttpEntity<>(anyString(), mockHttpHeaders);
        HttpClientErrorException mockException = new HttpClientErrorException(HttpStatus.BAD_REQUEST);

        // Mock the behavior of the RestTemplate to throw an exception
        when(restTemplate.exchange(shopifyUpdateCustomerDetailsEndPoint, any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
                .thenThrow(mockException);

        // Mock the behavior of the messageSource
        when(messageSource.getMessage(eq("api.customer.updation.exception"), isNull(), eq(Locale.ENGLISH)))
                .thenReturn("Email id already exist");

        // Call the method under test (it should throw UniqueRecordException)
        Assertions.assertThrows(UniqueRecordException.class, () -> customerService.updateCustomerDetails(customerDto.toString(), shopifyUpdateCustomerDetailsEndPoint, "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", new Gson()));
    }

    private static CustomerDetailKeyValueResponseDto getCustomerDetailKeyValueResponseDto() {
        CustomerDetailKeyValueResponseDto customerDto = new CustomerDetailKeyValueResponseDto();
        CustomerDetailsKeyValueDto customerDetailsKeyValueDto = new CustomerDetailsKeyValueDto();
        customerDetailsKeyValueDto.setId(7320574099685L);
        customerDetailsKeyValueDto.setFirst_name("Karthik");
        customerDetailsKeyValueDto.setLast_name("K");
        customerDetailsKeyValueDto.setEmail("karthik.r@cloudnowtech.com");
        customerDetailsKeyValueDto.setPhone("7358545729");
        customerDto.setCustomer(customerDetailsKeyValueDto);
        return customerDto;
    }

    @Test
    @DisplayName("Test getCustomerMeasurementByCustomerMeasurementId returns correct CustomerMeasurementDto")
    void testGetCustomerMeasurementByCustomerMeasurementIdReturnsCorrectDto() {
        // Arrange
        String customerId = "123";
        String sizeName = "XL";

        CustomerMeasurement customerMeasurementObj = new CustomerMeasurement();
        customerMeasurementObj.setCustomerId("1");
        customerMeasurementObj.setCustomerId(customerId);
        customerMeasurementObj.setName(sizeName);

        CustomerMeasurementDto expectedDto = new CustomerMeasurementDto();
        expectedDto.setCustomerId("1");
        expectedDto.setCustomerId(customerId);
        expectedDto.setName(sizeName);

        when(customerMeasurementRepositoryI.findByCustomerIdAndName(customerId, sizeName))
                .thenReturn(customerMeasurementObj);
        when(modelMapper.map(customerMeasurementObj, CustomerMeasurementDto.class))
                .thenReturn(expectedDto);

        // Act
        CustomerMeasurementDto actualDto = customerService.getCustomerMeasurementByCustomerMeasurementId(customerId, sizeName);

        // Assert
        assertEquals(expectedDto, actualDto);
    }

    @Test
    @DisplayName("Test getCustomerMeasurementByCustomerMeasurementId throws RecordNotFoundException when customer measurement not found")
    void testGetCustomerMeasurementByCustomerMeasurementIdThrowsRecordNotFoundExceptionWhenMeasurementNotFound() {
        // Arrange
        String customerId = "123";
        String sizeName = "XL";

        when(customerMeasurementRepositoryI.findByCustomerIdAndName(customerId, sizeName))
                .thenReturn(null);
        when(messageSource.getMessage("api.customer.measurement.not.found", null, Locale.ENGLISH))
                .thenReturn("Customer measurement not found");

        // Act and Assert
        RecordNotFoundException exception = assertThrows(RecordNotFoundException.class, () -> {
            customerService.getCustomerMeasurementByCustomerMeasurementId(customerId, sizeName);
        });
        assertEquals("Customer measurement not found", exception.getMessage());
    }

    @Test
    @DisplayName("Test getCustomerMeasurementFeedbackByOrderId returns correct CustomerMeasurementFeedbackDto")
    void testGetCustomerMeasurementFeedbackByOrderIdReturnsCorrectDto() {
        // Arrange
        String orderId = "123";

        CustomerMeasurementFeedBack customerMeasurementFeedBack = new CustomerMeasurementFeedBack();
        customerMeasurementFeedBack.setOrderId("1");
        customerMeasurementFeedBack.setOrderId(orderId);

        CustomerMeasurementFeedbackDto expectedDto = new CustomerMeasurementFeedbackDto();
        expectedDto.setOrderId("1");
        expectedDto.setOrderId(orderId);

        when(customerMeasurementFeedbackRepositoryI.getCustomerMeasurementFeedbackByOrderId(orderId))
                .thenReturn(customerMeasurementFeedBack);
        when(modelMapper.map(customerMeasurementFeedBack, CustomerMeasurementFeedbackDto.class))
                .thenReturn(expectedDto);

        // Act
        CustomerMeasurementFeedbackDto actualDto = customerService.getCustomerMeasurementFeedbackByOrderId(orderId);

        // Assert
        verify(customerMeasurementFeedbackRepositoryI).getCustomerMeasurementFeedbackByOrderId(orderId);
        verify(modelMapper).map(customerMeasurementFeedBack, CustomerMeasurementFeedbackDto.class);
        assertEquals(expectedDto, actualDto);
    }
    @Test
    @Disabled
    @DisplayName("Test saveCustomerMeasurementFeedback returns correct CustomerMeasurementFeedbackDto")
    void testSaveCustomerMeasurementFeedbackReturnsCorrectDto() throws ParseException, JsonProcessingException {
        // Arrange
        CustomerMeasurementFeedbackDto customerMeasurementFeedbackDto = new CustomerMeasurementFeedbackDto();
        customerMeasurementFeedbackDto.setFeedback("{\"customerId\": \"123\", \"rating\": 5}");

        CustomerMeasurement existingCustomerMeasurement = new CustomerMeasurement();

        CustomerMeasurementFeedBack customerMeasurementFeedBack = new CustomerMeasurementFeedBack();
        customerMeasurementFeedBack.setOrderId("1");
        customerMeasurementFeedBack.setFeedback(customerMeasurementFeedbackDto.getFeedback());
        customerMeasurementFeedBack.setCreatedBy("123");
        customerMeasurementFeedBack.setCreatedDate(AppUtil.getTodayDate());

        when(adminMapperUtil.convertFromCustomerMeasurementFeedbackDto(customerMeasurementFeedbackDto))
                .thenReturn(customerMeasurementFeedBack);
        when(customerMeasurementFeedbackRepositoryI.save(customerMeasurementFeedBack))
                .thenReturn(customerMeasurementFeedBack);
        when(adminMapperUtil.convertFromCustomerMeasurementFeedBack(customerMeasurementFeedBack))
                .thenReturn(customerMeasurementFeedbackDto);
        when(customerMeasurementRepositoryI.findByCustomerIdAndName(anyString(), anyString())).thenReturn(existingCustomerMeasurement);

        // Act
        CustomerMeasurementFeedbackDto savedDto = customerService.
                saveCustomerMeasurementFeedback(customerMeasurementFeedbackDto);

        // Assert
        verify(adminMapperUtil).convertFromCustomerMeasurementFeedbackDto(customerMeasurementFeedbackDto);
        verify(customerMeasurementFeedbackRepositoryI).save(customerMeasurementFeedBack);
        verify(adminMapperUtil).convertFromCustomerMeasurementFeedBack(customerMeasurementFeedBack);
        assertEquals(customerMeasurementFeedbackDto, savedDto);
    }
    @Test
    @Disabled
    @DisplayName("Test updateCustomerMeasurementFeedbackByCustomerMeasurementId updates feedback and customer measurement")
    void testUpdateCustomerMeasurementFeedbackByCustomerMeasurementIdUpdatesFeedbackAndMeasurement()
            throws ParseException, JsonProcessingException {
        // Arrange
        Boolean isApproved = true;
        Integer loggedInUserId = 1;

        CustomerMeasurementFeedBack existingCustomerMeasurementFeedback = new CustomerMeasurementFeedBack();
        existingCustomerMeasurementFeedback.setOrderId("1");
        existingCustomerMeasurementFeedback.setIsApproved(false);
        existingCustomerMeasurementFeedback.setFeedback("{\"customerId\": \"123\", \"sizeName\": \"M\", " +
             "\"shoulderWidth\": \"10\", \"halfChestWidth\": \"20\", \"neckWidth\": \"15\", \"frontNeckDrop\": \"5\"," +
             " \"cbLength\": \"25\", \"sleeveLength\": \"30\", \"sleeveOpening\": \"8\", \"armHoleStraight\": \"12\"}");

        CustomerMeasurement customerMeasurement = new CustomerMeasurement();
        customerMeasurement.setCustomerId("123");
        customerMeasurement.setName("M");

        CustomerMeasurementFeedbackDto expectedDto = new CustomerMeasurementFeedbackDto();
        expectedDto.setOrderId("1");
        expectedDto.setFeedback(existingCustomerMeasurementFeedback.getFeedback());
        expectedDto.setIsApproved(isApproved);

        when(customerMeasurementFeedbackRepositoryI.findById(any()))
                .thenReturn(Optional.of(existingCustomerMeasurementFeedback));

        CustomerOrderStatus customerOrderStatus = new CustomerOrderStatus();
        customerOrderStatus.setCustomerOrderStatusId(1);
        Mockito.when(customerOrderStatusRepositoryI.findByOrderId(Mockito.anyString()))
                .thenReturn(Collections.singletonList(customerOrderStatus));
        when(adminMapperUtil.convertFromCustomerMeasurementFeedBack(existingCustomerMeasurementFeedback))
                .thenReturn(expectedDto);
        when(customerMeasurementRepositoryI.findByCustomerIdAndName("123", "M"))
                .thenReturn(customerMeasurement);
        // Act
        CustomerMeasurementFeedbackDto actualDto = customerService.
                updateCustomerMeasurementFeedbackByCustomerMeasurementId(1,
                        expectedDto, loggedInUserId);

        // Assert
        verify(customerMeasurementFeedbackRepositoryI).save(existingCustomerMeasurementFeedback);
        verify(adminMapperUtil).convertFromCustomerMeasurementFeedBack(existingCustomerMeasurementFeedback);

    }
}
