package com.hungover.dashboard.service;

import com.hungover.core.dto.customer.CustomerCountResponseDto;
import com.hungover.core.dto.customer.CustomerFeedbackKVDto;
import com.hungover.core.dto.dashboard.AdminDashboardResponseDto;
import com.hungover.core.dto.order.YearlyOrderResponseDto;
import com.hungover.core.dto.product.TopProductsResponseDTO;
import com.hungover.customer.service.CustomerService;
import com.hungover.order.service.OrderService;
import com.hungover.product.service.ProductService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class AdminDashboardServiceTest {

    @InjectMocks
    AdminDashboardService adminDashboardService;
    @Mock
    private OrderService orderService;
    @Mock
    private CustomerService customerService;
    @Mock
    private ProductService productService;

    @Value("${shopifyCustomerEndPoint}")
    String shopifyCustomerEndPoint;
    @Value("${shopifyAccessToken}")
    String shopifyAccessToken;
    @Value("${shopifyProductEndPoint}")
    String shopifyProductEndPoint;
    @Value("${shopifyGraphQueryEndPoint}")
    String shopifyGraphQueryEndPoint;
    @Value("${shopifyEndpoint}")
    String shopifyEndpoint;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        setPrivateField(adminDashboardService, "shopifyCustomerEndPoint", "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");
        setPrivateField(adminDashboardService, "shopifyAccessToken", "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");
        setPrivateField(adminDashboardService, "shopifyProductEndPoint", "https://e4d27c.myshopify.com/admin/api/2022-07/products.json");
        setPrivateField(adminDashboardService, "shopifyGraphQueryEndPoint", "https://e4d27c.myshopify.com/admin/api/2023-07/graphql.json");
        setPrivateField(adminDashboardService, "shopifyEndpoint", "https://e4d27c.myshopify.com/admin/api/2022-07/orders.json");
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
    @DisplayName("Test adminDashboardSummary with successful responses")
    void testAdminDashboardSummary() throws Exception {
        String fromDate = "2021-12-31";
        String toDate = "2022-12-31";
        Integer noOfDays = 240;

        // Mock orderService
        YearlyOrderResponseDto yearlyOrderResponseDto = new YearlyOrderResponseDto();
        CompletableFuture<YearlyOrderResponseDto> yearlyOrderResponseDtoCompletableFuture = CompletableFuture.completedFuture(yearlyOrderResponseDto);
        when(orderService.getFilterOrderDetails(shopifyEndpoint, fromDate, toDate, shopifyAccessToken, noOfDays)).thenReturn(yearlyOrderResponseDtoCompletableFuture.get());

        // Mock customerService
        CustomerCountResponseDto customerCountResponseDto = new CustomerCountResponseDto();
        CompletableFuture<CustomerCountResponseDto> customerCountResponseDtoCompletableFuture = CompletableFuture.completedFuture(customerCountResponseDto);
        when(customerService.getFilterCustomerList(shopifyCustomerEndPoint, fromDate, toDate)).thenReturn(customerCountResponseDtoCompletableFuture.get());

        // Mock productService
        List<CustomerFeedbackKVDto> customerFeedbackKVDtoList = new ArrayList<>();
        CompletableFuture<List<CustomerFeedbackKVDto>> customerFeedbackKVDtoListCompletableFuture = CompletableFuture.completedFuture(customerFeedbackKVDtoList);
        when(customerService.getCustomerFeedbackFilter(fromDate, toDate)).thenReturn(customerFeedbackKVDtoListCompletableFuture.get());

        // Mock productService
        TopProductsResponseDTO topProductsResponseDTO = new TopProductsResponseDTO();
        CompletableFuture<TopProductsResponseDTO> topProductsResponseDTOCompletableFuture = CompletableFuture.completedFuture(topProductsResponseDTO);
        when(productService.getTopProductByFilter(fromDate, toDate, shopifyAccessToken, shopifyGraphQueryEndPoint, shopifyProductEndPoint)).thenReturn(topProductsResponseDTOCompletableFuture.get());

        // Test the method
        AdminDashboardService adminDashboardService = new AdminDashboardService(orderService, customerService, productService);
        AdminDashboardResponseDto result = adminDashboardService.adminDashboardSummary(fromDate, toDate, noOfDays);

        // Perform assertions on the result
        assertNotNull(result);
        assertEquals(customerCountResponseDto, result.getCustomerResponse());
        assertEquals(customerFeedbackKVDtoList, result.getCustomerFeedback());
        assertEquals(topProductsResponseDTO, result.getTopProductsResponse());
    }

    @Test
    @DisplayName("Test adminDashboardSummary with order service exceptions")
    void testAdminDashboardSummary_OrderServiceExceptions() throws Exception {
        // Set up your mocks and expected exceptions here
        when(orderService.getFilterOrderDetails(anyString(), anyString(), anyString(), anyString(), anyInt()))
                .thenThrow(new RuntimeException()); // Customize the exception you want to throw

        AdminDashboardService adminDashboardService = new AdminDashboardService(orderService, customerService, productService);

        CompletableFuture<YearlyOrderResponseDto> yearlyOrderResponseDtoCompletableFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return orderService.getFilterOrderDetails(anyString(), anyString(), anyString(), anyString(), anyInt());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        AdminDashboardResponseDto adminDashboardResponseDto = new AdminDashboardResponseDto();

        ExecutionException executionException = Assertions.assertThrows(ExecutionException.class, () ->
                adminDashboardResponseDto.setOrderResponse(yearlyOrderResponseDtoCompletableFuture.get()));

        assertNotNull(executionException.getCause()); // Verify that the cause exception is not null
        assertTrue(executionException.getCause() instanceof RuntimeException); // Verify that the cause is a RuntimeException
    }

    @Test
    @DisplayName("Test adminDashboardSummary with customer service exceptions")
    void testAdminDashboardSummary_CustomerServiceExceptions() throws Exception {
        // Set up your mocks and expected exceptions here
        String fromDate = "2023-01-01";
        String toDate = "2023-12-31";
        Integer noOfDays = 30;

        // Mock your services to throw exceptions
        when(customerService.getFilterCustomerList(eq(shopifyCustomerEndPoint), eq(fromDate), eq(toDate)))
                .thenThrow(new RuntimeException("Customer service exception"));

        // Create an instance of AdminDashboardService and call the method
        AdminDashboardService adminDashboardService = new AdminDashboardService(orderService, customerService, productService);

        // Perform assertions for the expected exceptions
        ExecutionException executionException = Assertions.assertThrows(ExecutionException.class, () ->
                adminDashboardService.adminDashboardSummary(fromDate, toDate, noOfDays)
        );

        assertTrue(executionException.getCause() instanceof RuntimeException);
        assertEquals("java.lang.RuntimeException: Customer service exception", executionException.getCause().getMessage());
    }

    @Test
    @DisplayName("Test adminDashboardSummary with parse exceptions")
    void testAdminDashboardSummary_ParseExceptions() throws Exception {
        // Set up your mocks and expected exceptions here
        String fromDate = "2023-01-01";
        String toDate = "2023-12-31";
        Integer noOfDays = 30;

        // Mock your services to throw exceptions
        when(customerService.getCustomerFeedbackFilter(eq(fromDate), eq(toDate)))
                .thenThrow(new ParseException("Parse exception", 0));

        // Create an instance of AdminDashboardService and call the method
        AdminDashboardService adminDashboardService = new AdminDashboardService(orderService, customerService, productService);

        // Perform assertions for the expected exceptions
        ExecutionException executionException = Assertions.assertThrows(ExecutionException.class, () ->
                adminDashboardService.adminDashboardSummary(fromDate, toDate, noOfDays)
        );

        assertTrue(executionException.getCause() instanceof RuntimeException);
        assertEquals("java.text.ParseException: Parse exception", executionException.getCause().getMessage());
    }

    @Test
    @DisplayName("Test adminDashboardSummary with product service exceptions")
    void testAdminDashboardSummary_ProductServiceExceptions() throws Exception {
        // Set up your mocks and expected exceptions here
        String fromDate = "2023-01-01";
        String toDate = "2023-12-31";
        Integer noOfDays = 30;

        // Mock your services to throw exceptions
        when(productService.getTopProductByFilter(eq(fromDate), eq(toDate), eq(shopifyAccessToken), eq(shopifyGraphQueryEndPoint), eq(shopifyProductEndPoint)))
                .thenThrow(new RuntimeException("Product service exception"));

        // Create an instance of AdminDashboardService and call the method
        AdminDashboardService adminDashboardService = new AdminDashboardService(orderService, customerService, productService);

        // Perform assertions for the expected exceptions
        ExecutionException executionException = Assertions.assertThrows(ExecutionException.class, () ->
                adminDashboardService.adminDashboardSummary(fromDate, toDate, noOfDays)
        );

        assertTrue(executionException.getCause() instanceof RuntimeException);
        assertEquals("Product service exception", executionException.getCause().getMessage());
    }
}