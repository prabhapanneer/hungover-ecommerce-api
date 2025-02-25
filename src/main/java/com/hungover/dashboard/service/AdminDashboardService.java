package com.hungover.dashboard.service;

import com.hungover.core.dto.customer.CustomerCountResponseDto;
import com.hungover.core.dto.customer.CustomerFeedbackKVDto;
import com.hungover.core.dto.dashboard.AdminDashboardResponseDto;
import com.hungover.core.dto.order.AverageOrderValueResponseDto;
import com.hungover.core.dto.order.YearlyOrderResponseDto;
import com.hungover.core.dto.product.TopProductsResponseDTO;
import com.hungover.customer.service.CustomerService;
import com.hungover.order.service.OrderService;
import com.hungover.product.service.ProductService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Service class providing methods for fetching and aggregating data for the Admin Dashboard.
 */
@Service
public class AdminDashboardService {

    private OrderService orderService;
    private CustomerService customerService;
    private ProductService productService;

    @Value("${shopifyCustomerEndPoint}")
    String shopifyCustomerEndPoint;
    @Value("${shopifyEndpoint}")
    String shopifyEndpoint;
    @Value("${shopifyAccessToken}")
    String shopifyAccessToken;
    @Value("${shopifyProductEndPoint}")
    String shopifyProductEndPoint;
    @Value("${shopifyGraphQueryEndPoint}")
    String shopifyGraphQueryEndPoint;

    public AdminDashboardService(OrderService orderService, CustomerService customerService,
                                 ProductService productService) {
        this.orderService = orderService;
        this.customerService = customerService;
        this.productService = productService;
    }

    /**
     * Generates the summary data for the Admin Dashboard using asynchronous operations.
     *
     * @param fromDate The start date for filtering data.
     * @param toDate   The end date for filtering data.
     * @param noOfDays  The number of days to consider for certain data components.
     * @return The AdminDashboardResponseDto containing the aggregated dashboard data.
     * @throws ExecutionException if there is an error while fetching the data.
     * @throws InterruptedException if there is an error while fetching the data.
     */
    public AdminDashboardResponseDto adminDashboardSummary(String fromDate, String toDate, Integer noOfDays)
            throws ExecutionException, InterruptedException {
        AdminDashboardResponseDto adminDashboardResponseDto = new AdminDashboardResponseDto();
        CompletableFuture<YearlyOrderResponseDto> getOrderResponse = CompletableFuture.supplyAsync(() -> {
            try {
                return orderService.getFilterOrderDetails(shopifyEndpoint, fromDate, toDate, shopifyAccessToken,
                        noOfDays);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        adminDashboardResponseDto.setOrderResponse(getOrderResponse.get());
        CompletableFuture<CustomerCountResponseDto> getCustomerResponse = CompletableFuture.supplyAsync(() -> {
            try {
                return customerService.getFilterCustomerList(shopifyCustomerEndPoint, fromDate, toDate);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        adminDashboardResponseDto.setCustomerResponse(getCustomerResponse.get());
        CompletableFuture<List<CustomerFeedbackKVDto>> getCustomerFeedback = CompletableFuture.supplyAsync(() -> {
            try {
                return customerService.getCustomerFeedbackFilter(fromDate, toDate);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });
        adminDashboardResponseDto.setCustomerFeedback(getCustomerFeedback.get());
        CompletableFuture<TopProductsResponseDTO> getTopProductsResponse = CompletableFuture.supplyAsync(() ->
                productService.getTopProductByFilter(fromDate, toDate, shopifyAccessToken, shopifyGraphQueryEndPoint,
                        shopifyProductEndPoint));
        adminDashboardResponseDto.setTopProductsResponse(getTopProductsResponse.get());
        CompletableFuture<AverageOrderValueResponseDto> getAverageOrderValueResponse = CompletableFuture.supplyAsync(
                () -> {
            try {
                return orderService.getAverageValueOfOrders(shopifyEndpoint, fromDate, toDate, shopifyAccessToken,
                        noOfDays);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        adminDashboardResponseDto.setAverageOrderValueResponse(getAverageOrderValueResponse.get());
        return adminDashboardResponseDto;
    }
}
