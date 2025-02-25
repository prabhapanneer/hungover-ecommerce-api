package com.hungover.order.controller;

import com.hungover.common.apiresponse.ApiListResponse;
import com.hungover.common.apiresponse.SingleDataResponse;
import com.hungover.common.constant.ApplicationConstants;
import com.hungover.core.dto.order.*;
import com.hungover.order.service.OrderApiResponseService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class OrderControllerTest {

    @InjectMocks
    OrderController orderController;
    @Mock
    private OrderApiResponseService orderApiResponseService;
    @Mock
    private MessageSource messageSource;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Controller Test for Get Today Order Endpoint")
    void testGetTodayOrder() throws IOException {
        String yesterdayDate = "2023-08-01";
        String todayDate = "2023-08-02";

        SingleDataResponse singleDataResponse = new SingleDataResponse();

        OrderKVDto orderKVDto = new OrderKVDto();

        singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS, "Today order fetched successfully", orderKVDto);
        ResponseEntity<SingleDataResponse> expectedSingleDataResponseResponseEntity = new ResponseEntity<>(singleDataResponse, HttpStatus.OK);

        when(orderApiResponseService.getTodayOrder(yesterdayDate, todayDate)).thenReturn(singleDataResponse);

        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Today order fetched successfully");

        ResponseEntity<SingleDataResponse> actualSingleDataResponseResponseEntity = orderController.getTodayOrder(yesterdayDate, todayDate);

        verify(orderApiResponseService).getTodayOrder(yesterdayDate, todayDate);
        Assertions.assertEquals(expectedSingleDataResponseResponseEntity, actualSingleDataResponseResponseEntity);
    }

    @Test
    @DisplayName("Controller Test for Get All Orders Endpoint")
    void testGetOrders() throws IOException {

        ApiListResponse apiListResponse = new ApiListResponse();

        List<OrderKeyValueDto> orderKeyValueDtoList = new ArrayList<>();
        orderKeyValueDtoList.add(new OrderKeyValueDto(123135L, 123, "2023-08-24", "John Doe", "1500.00", "Paid", "Fulfilled", "4", "1245243"));
        orderKeyValueDtoList.add(new OrderKeyValueDto(1231356789L, 1234, "2023-08-24", "Jame Smith", "1400.00", "Paid", "Fulfilled", "3", "1243543254"));

        apiListResponse.setResponse(ApplicationConstants.Status.SUCCESS, "Order fetched successfully", orderKeyValueDtoList);
        ResponseEntity<ApiListResponse> expectedApiListResponseResponseEntity = new ResponseEntity<>(apiListResponse, HttpStatus.OK);

        when(orderApiResponseService.getAllOrder()).thenReturn(apiListResponse);

        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Order fetched successfully");

        ResponseEntity<ApiListResponse> actualApiListResponseResponseEntity = orderController.getOrders();

        verify(orderApiResponseService).getAllOrder();
        Assertions.assertEquals(expectedApiListResponseResponseEntity, actualApiListResponseResponseEntity);

    }

    @Test
    @DisplayName("Controller Test for Get Yearly Order Details Endpoint")
    void testGetYearlyOrderDetails() throws IOException {
        String lastYear = "2021-01-01";
        String currentYear = "2022-01-01";

        SingleDataResponse singleDataResponse = new SingleDataResponse();

        YearlyOrderResponseDto yearlyOrderResponseDto = new YearlyOrderResponseDto();
        yearlyOrderResponseDto.setOrderPercentage(BigDecimal.valueOf(10));
        yearlyOrderResponseDto.setTotalOrder(5);
        yearlyOrderResponseDto.setPercentage(50);

        singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS, "Yearly order details count fetched successfully", yearlyOrderResponseDto);
        ResponseEntity<SingleDataResponse> exceptedSingleDataResponseResponseEntity = new ResponseEntity<>(singleDataResponse, HttpStatus.OK);

        when(orderApiResponseService.getYearlyOrderDetails(lastYear, currentYear)).thenReturn(singleDataResponse);

        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Yearly order details count fetched successfully");

        ResponseEntity<SingleDataResponse> actualSingleDataResponseResponseEntity = orderController.getYearlyOrderDetails(lastYear, currentYear);

        verify(orderApiResponseService).getYearlyOrderDetails(lastYear, currentYear);
        Assertions.assertEquals(exceptedSingleDataResponseResponseEntity, actualSingleDataResponseResponseEntity);
    }

    @Test
    @DisplayName("Controller Test for Get Average Value Of Orders Endpoint")
    void testGetAverageValueOfOrders() throws IOException {
        String lastYear = "2021-01-01";
        Integer noOfDays = 365;

        SingleDataResponse singleDataResponse = new SingleDataResponse();

        AverageOrderValueResponseDto averageOrderValueResponseDto = new AverageOrderValueResponseDto();
        averageOrderValueResponseDto.setPercentage(new BigDecimal("150.00"));
        averageOrderValueResponseDto.setTotalAverageOrderValue(10);

        singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS, "Average order details count fetched successfully", averageOrderValueResponseDto);
        ResponseEntity<SingleDataResponse> exceptedSingleDataResponseResponseEntity = new ResponseEntity<>(singleDataResponse, HttpStatus.OK);

        when(orderApiResponseService.getAverageValueOfOrders(lastYear, noOfDays)).thenReturn(singleDataResponse);

        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Average order details count fetched successfully");

        ResponseEntity<SingleDataResponse> actualSingleDataResponseResponseEntity = orderController.getAverageValueOfOrders(lastYear, noOfDays);

        verify(orderApiResponseService).getAverageValueOfOrders(lastYear, noOfDays);
        Assertions.assertEquals(exceptedSingleDataResponseResponseEntity, actualSingleDataResponseResponseEntity);
    }

    @Test
    @DisplayName("Controller Test for Get Order Details By OrderId Endpoint")
    void testGetOrderDetailsByOrderId() throws ExecutionException, InterruptedException, IOException {
        String orderId = "5275913289957";

        SingleDataResponse singleDataResponse = new SingleDataResponse();

        OrderDto orderDto = new OrderDto();
        orderDto.setTotalOrdersCount(5);

        singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS, "Order details fetched successfully", orderDto);
        ResponseEntity<SingleDataResponse> exceptedSingleDataResponseResponseEntity = new ResponseEntity<>(singleDataResponse, HttpStatus.OK);

        when(orderApiResponseService.getOrderDetailsByOrderId(orderId)).thenReturn(singleDataResponse);

        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Order details fetched successfully");

        ResponseEntity<SingleDataResponse> actualSingleDataResponseResponseEntity = orderController.getOrderDetailsByOrderId(orderId);

        verify(orderApiResponseService).getOrderDetailsByOrderId(orderId);
        Assertions.assertEquals(exceptedSingleDataResponseResponseEntity, actualSingleDataResponseResponseEntity);
    }
}