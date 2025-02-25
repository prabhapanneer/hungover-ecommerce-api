package com.hungover.order.service;


import com.hungover.common.apiresponse.ApiListResponse;
import com.hungover.common.apiresponse.SingleDataResponse;
import com.hungover.common.constant.ApplicationConstants;
import com.hungover.core.dto.order.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class OrderApiResponseServiceTest {

    @InjectMocks
    OrderApiResponseService orderApiResponseService;
    @Mock
    private OrderService orderService;
    @Mock
    private MessageSource messageSource;

    @Value("${shopifyAccessToken}")
    String shopifyAccessToken;
    @Value("${shopifyEndpoint}")
    String shopifyEndpoint;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        setPrivateField(orderApiResponseService, "shopifyAccessToken", "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");
        setPrivateField(orderApiResponseService, "shopifyEndpoint", "https://e4d27c.myshopify.com/admin/api/2022-07/orders.json");
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
    @DisplayName("Test getTodayOrder Success")
    void testGetTodayOrder_Success() throws IOException {
        String yesterdayDate = "2023-08-01";
        String todayDate = "2023-08-02";

        // Mock the behavior of the OrderService
        OrderKVDto mockOrderKVDto = new OrderKVDto();
        mockOrderKVDto.setFulfill(0);
        mockOrderKVDto.setPending(1);

        when(orderService.getTodayOrder(any(), any(), any(), any())).thenReturn(mockOrderKVDto);

        // Mock the behavior of the MessageSource
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Today order fetched successfully");

        SingleDataResponse singleDataResponse = orderApiResponseService.getTodayOrder(yesterdayDate, todayDate);

        Assertions.assertEquals(ApplicationConstants.Status.SUCCESS, singleDataResponse.getStatus().getSuccess());
        Assertions.assertEquals("Today order fetched successfully", singleDataResponse.getStatus().getMessage());
        Assertions.assertSame(mockOrderKVDto, singleDataResponse.getData());
    }

    @Test
    @DisplayName("Test getTodayOrder Not Found")
    void testGetTodayOrder_NotFound() throws IOException {
        String yesterdayDate = "2023-08-01";
        String todayDate = "2023-08-02";

        // Mock the behavior of the OrderService to return null
        when(orderService.getTodayOrder(any(), any(), any(), any())).thenReturn(null);

        // Mock the behavior of the MessageSource
        when(messageSource.getMessage(anyString(), any(), any()))
                .thenReturn("Today order not found");

        SingleDataResponse singleDataResponse = orderApiResponseService.getTodayOrder(yesterdayDate, todayDate);

        Assertions.assertEquals(ApplicationConstants.Status.FAIL, singleDataResponse.getStatus().getSuccess());
        Assertions.assertEquals("Today order not found", singleDataResponse.getStatus().getMessage());
        Assertions.assertNull(singleDataResponse.getData());
    }

    @Test
    @DisplayName("Test getAllOrder Success")
    void testGetAllOrder_Success() throws IOException {
        List<OrderKeyValueDto> orderKeyValueDtoList = new ArrayList<>();
        orderKeyValueDtoList.add(new OrderKeyValueDto(123135L, 123, "2023-08-24", "John Doe", "1500.00", "Paid", "Fulfilled", "4", "1245243"));
        orderKeyValueDtoList.add(new OrderKeyValueDto(1231356789L, 1234, "2023-08-24", "Jame Smith", "1400.00", "Paid", "Fulfilled", "3", "1243543254"));

        when(orderService.getAllOrderListFromShopify(any(), any())).thenReturn(orderKeyValueDtoList);

        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Order fetched successfully");

        ApiListResponse apiListResponse = orderApiResponseService.getAllOrder();

        Assertions.assertEquals(ApplicationConstants.Status.SUCCESS, apiListResponse.getStatus().getSuccess());
        Assertions.assertEquals("Order fetched successfully", apiListResponse.getStatus().getMessage());
        Assertions.assertSame(orderKeyValueDtoList, apiListResponse.getData());
    }

    @Test
    @DisplayName("Test getAllOrder Failure")
    void testGetAllOrder_Failure() throws IOException {

        when(orderService.getAllOrderListFromShopify(any(), any())).thenReturn(Collections.EMPTY_LIST);

        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Order not able to fetch");

        ApiListResponse apiListResponse = orderApiResponseService.getAllOrder();

        Assertions.assertEquals(ApplicationConstants.Status.FAIL, apiListResponse.getStatus().getSuccess());
        Assertions.assertEquals("Order not able to fetch", apiListResponse.getStatus().getMessage());
    }

    @Test
    @DisplayName("Test getYearlyOrderDetails Success")
    void testGetYearlyOrderDetails() throws IOException {
        String lastYear = "2021-01-01";
        String currentYear = "2022-01-01";

        YearlyOrderResponseDto yearlyOrderResponseDto = new YearlyOrderResponseDto();
        yearlyOrderResponseDto.setOrderPercentage(BigDecimal.valueOf(10));
        yearlyOrderResponseDto.setTotalOrder(5);
        yearlyOrderResponseDto.setPercentage(50);

        when(orderService.getYearlyOrderDetails(any(), any(), any(), any())).thenReturn(yearlyOrderResponseDto);

        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Yearly order details count fetched successfully");

        SingleDataResponse singleDataResponse = orderApiResponseService.getYearlyOrderDetails(lastYear, currentYear);

        Assertions.assertEquals(ApplicationConstants.Status.SUCCESS, singleDataResponse.getStatus().getSuccess());
        Assertions.assertEquals("Yearly order details count fetched successfully", singleDataResponse.getStatus().getMessage());
        Assertions.assertSame(yearlyOrderResponseDto, singleDataResponse.getData());
    }

    @Test
    @DisplayName("Test getYearlyOrderDetails Failure")
    void testGetYearlyOrderDetails_Failure() throws IOException {
        String lastYear = "2021-01-01";
        String currentYear = "2022-01-01";

        when(orderService.getYearlyOrderDetails(any(), any(), any(), any())).thenReturn(null);

        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Yearly order details count not able to fetch");

        SingleDataResponse singleDataResponse = orderApiResponseService.getYearlyOrderDetails(lastYear, currentYear);

        Assertions.assertEquals(ApplicationConstants.Status.FAIL, singleDataResponse.getStatus().getSuccess());
        Assertions.assertEquals("Yearly order details count not able to fetch", singleDataResponse.getStatus().getMessage());
        Assertions.assertNull(singleDataResponse.getData());
    }

    @Test
    @DisplayName("Test getAverageValueOfOrders Success")
    void testGetAverageValueOfOrders_Success() throws IOException {
        String currentYear = "2022-01-01";
        Integer noOfDays = 365;

        AverageOrderValueResponseDto averageOrderValueResponseDto = new AverageOrderValueResponseDto();
        averageOrderValueResponseDto.setPercentage(new BigDecimal("150.00"));
        averageOrderValueResponseDto.setTotalAverageOrderValue(10);

        when(orderService.getAverageValueOfOrders(any(), any(), any(), any(), any())).thenReturn(averageOrderValueResponseDto);

        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Average order details count fetched successfully");

        SingleDataResponse singleDataResponse = orderApiResponseService.getAverageValueOfOrders(currentYear, noOfDays);

        Assertions.assertEquals(ApplicationConstants.Status.SUCCESS, singleDataResponse.getStatus().getSuccess());
        Assertions.assertEquals("Average order details count fetched successfully", singleDataResponse.getStatus().getMessage());
        Assertions.assertEquals(averageOrderValueResponseDto, singleDataResponse.getData());
    }

    @Test
    @DisplayName("Test getAverageValueOfOrders Failure")
    void testGetAverageValueOfOrders_Failure() throws IOException {
        String currentYear = "2022-01-01";
        Integer noOfDays = 365;

        when(orderService.getAverageValueOfOrders(any(), any(), any(), any(), any())).thenReturn(null);

        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Average order details count not able to fetch");

        SingleDataResponse singleDataResponse = orderApiResponseService.getAverageValueOfOrders(currentYear, noOfDays);

        Assertions.assertEquals(ApplicationConstants.Status.FAIL, singleDataResponse.getStatus().getSuccess());
        Assertions.assertEquals("Average order details count not able to fetch", singleDataResponse.getStatus().getMessage());
        Assertions.assertNull(singleDataResponse.getData());
    }

    @Test
    @DisplayName("Test getOrderDetailsByOrderId Success")
    void testGetOrderDetailsByOrderId_Success() throws ExecutionException, InterruptedException, IOException {
        String orderId = "5275913289957";

        OrderDto orderDto = new OrderDto();
        orderDto.setTotalOrdersCount(5);

        when(orderService.getOrderDetailsByOrderId(any(), any(), any(), any(), any(), any(), any())).thenReturn(orderDto);

        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Order details fetched successfully");

        SingleDataResponse singleDataResponse = orderApiResponseService.getOrderDetailsByOrderId(orderId);

        Assertions.assertEquals(ApplicationConstants.Status.SUCCESS, singleDataResponse.getStatus().getSuccess());
        Assertions.assertEquals("Order details fetched successfully", singleDataResponse.getStatus().getMessage());
        Assertions.assertEquals(orderDto, singleDataResponse.getData());
    }

    @Test
    @DisplayName("Test getOrderDetailsByOrderId Failure")
    void testGetOrderDetailsByOrderId_Failure() throws ExecutionException, InterruptedException, IOException {
        String orderId = "5275913289957";

        when(orderService.getOrderDetailsByOrderId(any(), any(), any(), any(), any(), any(), any())).thenReturn(null);

        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Order details not able to fetch");

        SingleDataResponse singleDataResponse = orderApiResponseService.getOrderDetailsByOrderId(orderId);

        Assertions.assertEquals(ApplicationConstants.Status.FAIL, singleDataResponse.getStatus().getSuccess());
        Assertions.assertEquals("Order details not able to fetch", singleDataResponse.getStatus().getMessage());
        Assertions.assertNull(singleDataResponse.getData());
    }
}