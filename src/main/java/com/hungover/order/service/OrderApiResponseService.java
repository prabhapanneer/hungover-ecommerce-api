package com.hungover.order.service;

import com.hungover.common.apiresponse.ApiListResponse;
import com.hungover.common.apiresponse.SingleDataResponse;
import com.hungover.common.constant.ApplicationConstants;
import com.hungover.core.dto.order.AverageOrderValueResponseDto;
import com.hungover.core.dto.order.OrderDto;
import com.hungover.core.dto.order.OrderKVDto;
import com.hungover.core.dto.order.OrderKeyValueDto;
import com.hungover.core.dto.order.YearlyOrderResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Service class for handling order-related API responses.
 */
@Service
public class OrderApiResponseService {
    private final Logger orderApiResponseServiceLogger = LoggerFactory.getLogger(this.getClass());

    private OrderService orderService;
    private MessageSource messageSource;

    @Value("${shopifyAccessToken}")
    String shopifyAccessToken;
    @Value("${shopifyEndpoint}")
    String shopifyEndpoint;
    @Value("${shopifyOrderDetailsByOrderIdEndPoint}")
    String shopifyOrderDetailsByOrderIdEndPoint;
    @Value("${shopifyResetPasswordEndPoint}")
    String shopifyResetPasswordEndPoint;
    @Value("${shopifyGraphQueryEndPoint}")
    String shopifyGraphQueryEndPoint;
    @Value("${shopifyVariantDetailsByVariantIdEndPoint}")
    String shopifyVariantDetailsByVariantIdEndPoint;
    @Value("${shopifyProductImageByProductIdAndImageIdEndPoint}")
    String shopifyProductImageByProductIdAndImageIdEndPoint;

    public OrderApiResponseService(OrderService orderService, MessageSource messageSource) {
        super();
        this.orderService = orderService;
        this.messageSource = messageSource;
    }

    /**
     * Retrieves today's order based on the provided date range.
     *
     * @param yesterdayDate The date for yesterday.
     * @param todayDate     The date for today.
     * @return SingleDataResponse containing the response data of today's order.
     * @throws IOException If an error occurs while processing the request.
     */
    public SingleDataResponse getTodayOrder(String yesterdayDate, String todayDate) throws IOException {
        orderApiResponseServiceLogger.info("Entered get today order ApiResponse Service:::::::::::::::::::::::");
        SingleDataResponse singleDataResponse = new SingleDataResponse();
        OrderKVDto orderKVDto = orderService.getTodayOrder(shopifyAccessToken, yesterdayDate,
                todayDate, shopifyEndpoint);
        if (Optional.ofNullable(orderKVDto).isPresent()) {
            singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                    messageSource.getMessage("api.get.today.order.success", null, Locale.ENGLISH),
                    orderKVDto);
        } else {
            singleDataResponse.setResponse(ApplicationConstants.Status.FAIL,
                    messageSource.getMessage("api.get.today.order.not.found", null, Locale.ENGLISH),
                    null);
        }
        return singleDataResponse;
    }

    /**
     * Retrieves all orders.
     *
     * @return ApiListResponse containing the response data of all orders.
     * @throws IOException If an error occurs while processing the request.
     */
    public ApiListResponse getAllOrder() throws IOException {
        orderApiResponseServiceLogger.info("Entered get all order ApiResponse Service:::::::::::::::::::::::");
        ApiListResponse apiListResponse = new ApiListResponse();
        List<OrderKeyValueDto> orderKeyValueDtoList = orderService.
                getAllOrderListFromShopify(shopifyEndpoint, shopifyAccessToken);
        if (!(orderKeyValueDtoList.isEmpty())) {
            apiListResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                    messageSource.getMessage("api.get.all.order.success", null, Locale.ENGLISH),
                    orderKeyValueDtoList);
            apiListResponse.setTotalResults(orderKeyValueDtoList.size());
        } else {
            apiListResponse.setResponse(ApplicationConstants.Status.FAIL,
                    messageSource.getMessage("api.get.all.order.fail", null, Locale.ENGLISH),
                    orderKeyValueDtoList);
        }
        return apiListResponse;
    }

    /**
     * Retrieves the yearly order details for the given last year and current year and returns it as a response.
     *
     * @param lastYear The last year as a string (e.g., "2022").
     * @param currentYear The current year as a string (e.g., "2023").
     * @return A {@link SingleDataResponse} containing the yearly order details.
     * @throws IOException if there is an error while fetching the data from Shopify.
     */
    public SingleDataResponse getYearlyOrderDetails(String lastYear, String currentYear) throws IOException {
        orderApiResponseServiceLogger.info("Entered get yearly order details ApiResponse Service::::::::::::::::::");
        SingleDataResponse singleDataResponse = new SingleDataResponse();
        YearlyOrderResponseDto yearlyOrderResponseDto = orderService.getYearlyOrderDetails(shopifyEndpoint, lastYear,
                currentYear, shopifyAccessToken);
        if (Optional.ofNullable(yearlyOrderResponseDto).isPresent()) {
            singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                    messageSource.getMessage("api.get.yearly.order.detail.count.success",
                            null, Locale.ENGLISH), yearlyOrderResponseDto);
        } else {
            singleDataResponse.setResponse(ApplicationConstants.Status.FAIL,
                    messageSource.getMessage("api.get.yearly.order.detail.count.fail",
                            null, Locale.ENGLISH), null);
        }
        return singleDataResponse;
    }

    /**
     * Calculates and retrieves the total average order details for a specific date range and number of days.
     *
     * @param fromDate           The start date of the date range for which the average
     *                           order details are to be calculated.
     * @param noOfDays           The number of days to consider when calculating the average order details.
     * @return A SingleDataResponse containing the average order details or an error message.
     * @throws IOException if there is an I/O error while processing the request.
     */
    public SingleDataResponse getAverageValueOfOrders(String fromDate, Integer noOfDays) throws IOException {
        orderApiResponseServiceLogger.info("Entered get total average order details ApiResponse Service:::::::::");
        SingleDataResponse singleDataResponse = new SingleDataResponse();
        AverageOrderValueResponseDto averageOrderValueResponseDto = orderService.getAverageValueOfOrders(
                shopifyEndpoint, fromDate, null, shopifyAccessToken, noOfDays);
        if (Optional.ofNullable(averageOrderValueResponseDto).isPresent()) {
            singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                    messageSource.getMessage("api.get.total.average.order.detail.count.success", null,
                            Locale.ENGLISH), averageOrderValueResponseDto);
        } else {
            singleDataResponse.setResponse(ApplicationConstants.Status.FAIL,
                    messageSource.getMessage("api.get.total.average.order.detail.count.fail", null,
                            Locale.ENGLISH), null);
        }
        return singleDataResponse;
    }

    /**
     * Retrieves the order details for a given order ID.
     *
     * @param orderId The unique identifier of the order.
     * @return A SingleDataResponse containing the order details as the response data.
     * @throws ExecutionException if there is an error while processing the request.
     * @throws InterruptedException if there is an error while processing the request.
     */
    public SingleDataResponse getOrderDetailsByOrderId(String orderId) throws ExecutionException, InterruptedException,
            IOException {
        orderApiResponseServiceLogger.info("Entered get order details by order id ApiResponse Service:::::::::");
        SingleDataResponse singleDataResponse = new SingleDataResponse();
        OrderDto orderDtoObj = orderService.getOrderDetailsByOrderId(shopifyOrderDetailsByOrderIdEndPoint, orderId,
        shopifyAccessToken, shopifyResetPasswordEndPoint, shopifyGraphQueryEndPoint,
        shopifyVariantDetailsByVariantIdEndPoint, shopifyProductImageByProductIdAndImageIdEndPoint);
        if (Optional.ofNullable(orderDtoObj).isPresent()) {
            singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                    messageSource.getMessage("api.get.order.details.by.order.id.success", null,
                            Locale.ENGLISH), orderDtoObj);
        } else {
            singleDataResponse.setResponse(ApplicationConstants.Status.FAIL,
                    messageSource.getMessage("api.get.order.details.by.order.id.fail", null,
                            Locale.ENGLISH), null);
        }
        return singleDataResponse;
    }
}
