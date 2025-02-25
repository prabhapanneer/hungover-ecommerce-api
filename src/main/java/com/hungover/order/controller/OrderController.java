package com.hungover.order.controller;

import com.hungover.common.apiresponse.ApiListResponse;
import com.hungover.common.apiresponse.SingleDataResponse;
import com.hungover.order.service.OrderApiResponseService;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Controller class for managing Orders.
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/order")
public class OrderController {

    private OrderApiResponseService orderApiResponseService;
    private static final String ENDPOINT_VERSION = "v2";

    public OrderController(OrderApiResponseService orderApiResponseService) {
        super();
        this.orderApiResponseService = orderApiResponseService;
    }

    /**
     * Retrieves today's orders based on the provided date range.
     *
     * @param yesterdayDate The date for yesterday.
     * @param todayDate     The date for today.
     * @return ResponseEntity containing the response data of today's orders.
     * @throws IOException If an error occurs while processing the request.
     */
    @GetMapping(value = ENDPOINT_VERSION+"/today")
    @ApiOperation(value = "Get today order", nickname = "Get today order", notes = "This endpoint for Get today order",
            produces = "application/json", consumes = "application/json")
    public ResponseEntity<SingleDataResponse> getTodayOrder(
            @RequestParam(name = "yesterdayDate") String yesterdayDate,
            @RequestParam(name = "todayDate") String todayDate) throws IOException {
        return new ResponseEntity<>(orderApiResponseService.getTodayOrder(yesterdayDate, todayDate), HttpStatus.OK);
    }

    /**
     * Retrieves all orders.
     *
     * @return ResponseEntity containing the response data of all orders.
     * @throws IOException If an error occurs while processing the request.
     */
    @GetMapping(value = ENDPOINT_VERSION+"/all")
    @ApiOperation(value = "Get all orders", nickname = "Get all orders", notes = "This endpoint for Get all orders",
            produces = "application/json", consumes = "application/json")
    public ResponseEntity<ApiListResponse> getOrders() throws IOException {
        return new ResponseEntity<>(orderApiResponseService.getAllOrder(), HttpStatus.OK);
    }

    /**
     * Retrieves the yearly order details for the given last year and current year and returns it as a response.
     *
     * @param lastYear The last year as a string (e.g., "2022").
     * @param currentYear The current year as a string (e.g., "2023").
     * @return A {@link SingleDataResponse} containing the yearly order details.
     * @throws IOException if there is an error while fetching the data from Shopify.
     */
    @GetMapping(value = ENDPOINT_VERSION+"/yearly")
    @ApiOperation(value = "Get yearly order details", nickname = "Get yearly order details",
            notes = "This endpoint for Get yearly order details", produces = "application/json",
            consumes = "application/json")
    public ResponseEntity<SingleDataResponse> getYearlyOrderDetails(
            @RequestParam(name = "lastYear") String lastYear,
            @RequestParam(name = "currentYear") String currentYear) throws IOException {
        return new ResponseEntity<>(orderApiResponseService.getYearlyOrderDetails(lastYear, currentYear),
                HttpStatus.OK);
    }

    /**
     * Retrieves the total average order details for a specific date range and number of days.
     *
     * @param fromDate   The start date of the date range for which the average order details are to be calculated.
     * @param noOfDays   The number of days to consider when calculating the average order details.
     * @return A ResponseEntity containing a SingleDataResponse with the average order details.
     * @throws IOException if there is an I/O error while processing the request.
     */
    @GetMapping(value = ENDPOINT_VERSION+"/average")
    @ApiOperation(value = "Get total average order details", nickname = "Get total average order details",
            notes = "This endpoint for Get total average order details", produces = "application/json",
            consumes = "application/json")
    public ResponseEntity<SingleDataResponse> getAverageValueOfOrders(
            @RequestParam(name = "fromDate") String fromDate,
            @RequestParam(name = "noOfDays") Integer noOfDays) throws IOException {
        return new ResponseEntity<>(orderApiResponseService.getAverageValueOfOrders(fromDate, noOfDays), HttpStatus.OK);
    }

    /**
     * Retrieves the order details for a given order ID.
     *
     * @param orderId The unique identifier of the order.
     * @return A ResponseEntity containing a SingleDataResponse with the order details as the response data.
     * @throws ExecutionException if there is an error while processing the request.
     * @throws InterruptedException if there is an error while processing the request.
     */
    @GetMapping(value = ENDPOINT_VERSION+"/{orderId}")
    @ApiOperation(value = "Get order details by order id", nickname = "Get order details by order id",
            notes = "This endpoint for Get order details by order id", produces = "application/json",
            consumes = "application/json")
    public ResponseEntity<SingleDataResponse> getOrderDetailsByOrderId(@PathVariable(name = "orderId") String orderId)
            throws ExecutionException, InterruptedException, IOException {
        return new ResponseEntity<>(orderApiResponseService.getOrderDetailsByOrderId(orderId), HttpStatus.OK);
    }
}
