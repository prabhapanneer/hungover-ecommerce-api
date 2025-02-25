package com.hungover.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.hungover.admin.repository.CustomerOrderStatusRepositoryI;
import com.hungover.common.constant.ApplicationConstants;
import com.hungover.common.domain.DomainObject;
import com.hungover.common.util.AppUtil;
import com.hungover.core.domain.customer.CustomerOrderStatus;
import com.hungover.core.dto.customer.CustomerDetailResponseDto;
import com.hungover.core.dto.customer.CustomerDto;
import com.hungover.core.dto.customer.CustomerMeasurementDto;
import com.hungover.core.dto.customer.CustomerOrderStatusDto;
import com.hungover.core.dto.order.AverageOrderValueResponseDto;
import com.hungover.core.dto.order.ConversionSummaryDto;
import com.hungover.core.dto.order.ImageDetailResponseDto;
import com.hungover.core.dto.order.Orders;
import com.hungover.core.dto.order.OrderChannelInformationDto;
import com.hungover.core.dto.order.OrderDetailsResponseDto;
import com.hungover.core.dto.order.OrderDto;
import com.hungover.core.dto.order.OrderKeyValueDto;
import com.hungover.core.dto.order.OrderKVDto;
import com.hungover.core.dto.order.OrderResponseDto;
import com.hungover.core.dto.order.ProductInfoDto;
import com.hungover.core.dto.order.VariantDetailsDto;
import com.hungover.core.dto.order.YearlyOrderResponseDto;
import com.hungover.core.dto.order.ConversionSummaryDto.FirstVisit;
import com.hungover.core.dto.order.OrderChannelInformationDto.ChannelDefinition;
import com.hungover.customer.repository.CustomerMeasurementRepositoryI;
import com.hungover.customer.service.CustomerService;
import com.hungover.product.service.ProductService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * Service class for handling order-related operations and Shopify API calls.
 */
@Service
public class OrderService {
    private final Logger orderServiceLogger = LoggerFactory.getLogger(this.getClass());
    private static final String NEWLINE = "\n";
    private static final String SHOPIFY_ENDPOINT_URL_QUESTIONMARK = "?";
    private static final String SHOPIFY_ENDPOINT_PARAM_PROCESSED_AT_MIN = "processed_at_min=";
    private static final String SHOPIFY_ENDPOINT_PARAM_PROCESSED_AT_MAX = "&processed_at_max=";
    private static final String SHOPIFY_ENDPOINT_LIMIT = "limit=250";
    private static final String SHOPIFY_ENDPOINT_JSON = ".json";
    private static final String DEFAULT_EMPTY = "";
    private static final String SPACE = " ";
    private static final String QUANTITY_ITEM = "item";
    private static final String TOTAL_PRICE_IN_RS = "RS ";
    private static final String EQUAL_TO = "=";
    private static final String RESPONSE_HINT_FOR_NEXT_PAGE = "rel=\"next\"";
    private static final String SPLIT_WITH_AND_SYMBOL = "&";
    private static final String REPLACE_WITH_GREATER_THAN = ">";
    private static final Integer DEFAULT_INTEGER_VALUE_ZERO = 0;
    private static final Double DEFAULT_DOUBLE_VALUE = 0.0;
    private static final String CONTENT_TYPE = "application/graphql";

    private ProductService productService;
    private CustomerOrderStatusRepositoryI customerOrderStatusRepository;
    private ModelMapper modelMapper;
    private CustomerService customerService;

    public OrderService(ProductService productService, CustomerOrderStatusRepositoryI customerOrderStatusRepository,
                        ModelMapper modelMapper, CustomerService customerService) {
        this.productService = productService;
        this.customerOrderStatusRepository = customerOrderStatusRepository;
        this.modelMapper = modelMapper;
        this.customerService = customerService;
    }

    /**
     * Retrieves today's order count based on the provided date range.
     *
     * @param yesterdayDate The date for yesterday.
     * @param todayDate     The date for today.
     * @return OrderKVDto containing the count of fulfilled and pending orders.
     * @throws IOException If an error occurs while processing the request.
     */
    public OrderKVDto getTodayOrder(String shopifyAccessToken, String yesterdayDate,
                                    String todayDate, String shopifyEndpoint) throws IOException {
        orderServiceLogger.info("Entered order service::::::::::::::::::");
        OrderKVDto orderKVDtoObj = new OrderKVDto();
        List<Orders> getOrderList = getTodayOrderListFromShopify(shopifyAccessToken, yesterdayDate,
                todayDate, shopifyEndpoint);
        int orderPaidCount = (int) getOrderList.stream()
                .filter(data -> ApplicationConstants.Orders.ORDER_FULFILLED_STATUS.equals(data.getFulfillment_status()))
                .count();
        int orderPendingCount = (int) getOrderList.stream()
                .filter(data -> ApplicationConstants.Orders.ORDER_PAID_STATUS.equals(data.getFinancial_status())
                        || ApplicationConstants.Orders.ORDER_PENDING_STATUS.equals(data.getFinancial_status()))
                .count();
        orderKVDtoObj.setFulfill(orderPaidCount);
        orderKVDtoObj.setPending(orderPendingCount);
        return orderKVDtoObj;
    }

    /**
     * Retrieves today's orders from Shopify API based on the provided date range.
     *
     * @param yesterdayDate The date for yesterday.
     * @param todayDate     The date for today.
     * @return List of Orders representing the orders fetched from Shopify.
     * @throws IOException If an error occurs while making the API call or processing the response.
     */
    public List<Orders> getTodayOrderListFromShopify(String shopifyAccessToken, String yesterdayDate,
                                                     String todayDate, String shopifyEndpoint) throws IOException {
        orderServiceLogger.info("Entered get today order details from shopify api call:::::::::::::::");
        HttpURLConnection httpConnection = getHttpURLConnection(shopifyEndpoint +
                SHOPIFY_ENDPOINT_URL_QUESTIONMARK + SHOPIFY_ENDPOINT_PARAM_PROCESSED_AT_MIN + yesterdayDate +
                SHOPIFY_ENDPOINT_PARAM_PROCESSED_AT_MAX + todayDate, shopifyAccessToken);
        try (InputStream httpConnectionInputStream = httpConnection.getInputStream();
             BufferedReader httpConnectionBufferReader = new BufferedReader(
                     new InputStreamReader(httpConnectionInputStream, StandardCharsets.UTF_8))) {
            StringBuilder sbNewLine = new StringBuilder();
            String strLine;
            while ((strLine = httpConnectionBufferReader.readLine()) != null) {
                sbNewLine.append(strLine).append(NEWLINE);
            }
            Gson gsonObject = new Gson();
            OrderResponseDto orderResponseDtoObj = gsonObject.fromJson(sbNewLine.toString(), OrderResponseDto.class);
            return orderResponseDtoObj.getOrders();
        }
    }

    HttpURLConnection getHttpURLConnection(String shopifyEndpoint, String authorization) throws IOException {
        URL urlConnector = new URL(shopifyEndpoint);
        HttpURLConnection httpConnection = (HttpURLConnection) urlConnector.openConnection();
        httpConnection.setRequestMethod(ApplicationConstants.ShopifyApiHeaders.GET_REQUEST_METHOD);
        httpConnection.setRequestProperty(ApplicationConstants.ShopifyApiHeaders.SHOPIFY_ACCESS_TOKEN, authorization);
        return httpConnection;
    }

    /**
     * Retrieves a list of OrderKeyValueDto objects containing details of all orders from Shopify.
     *
     * @return List of OrderKeyValueDto objects representing order details.
     * @throws IOException If there is an error while retrieving or processing the orders.
     */
    public List<OrderKeyValueDto> getAllOrderListFromShopify(String shopifyEndpoint, String shopifyAccessToken)
            throws IOException {
        orderServiceLogger.info("Entered get all order details from shopify api call:::::::::::::::");
        String pageInfo = "";
        boolean lastPage = true;
        List<OrderKeyValueDto> allOrdersList = new ArrayList<>();
        while (lastPage) {
            HttpURLConnection httpConnection = getHttpURLConnection(shopifyEndpoint +
                            SHOPIFY_ENDPOINT_URL_QUESTIONMARK + SHOPIFY_ENDPOINT_LIMIT + SPLIT_WITH_AND_SYMBOL + pageInfo,
                    shopifyAccessToken);
            int responseCode = httpConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader httpConnectionBufferReader = new BufferedReader(new InputStreamReader(
                        httpConnection.getInputStream()))) {
                    String strLine;
                    StringBuilder sbNewLine = new StringBuilder();
                    while ((strLine = httpConnectionBufferReader.readLine()) != null) {
                        sbNewLine.append(strLine);
                    }
                    JSONObject responseObject = new JSONObject(sbNewLine.toString());
                    JSONArray orders = responseObject.getJSONArray(DomainObject.Order.ORDERS);
                    List<OrderKeyValueDto> orderList = IntStream.range(0, orders.length())
                            .mapToObj(orders::getJSONObject)
                            .map(this::createOrderKeyValueDto)
                            .collect(Collectors.toList());
                    allOrdersList.addAll(orderList);
                    String linkHeader = httpConnection.getHeaderField(ApplicationConstants.ShopifyApiHeaders.LINK);
                    if (linkHeader == null || !linkHeader.contains(RESPONSE_HINT_FOR_NEXT_PAGE)) {
                        break;
                    }
                    String nextLink = productService.parseNextLink(linkHeader);
                    Map<String, String> params = productService.parseQueryParameters(nextLink);
                    String strPageInfo = params.get(ApplicationConstants.ShopifyApiHeaders.PAGE_INFO)
                            .replace(REPLACE_WITH_GREATER_THAN, DEFAULT_EMPTY);
                    pageInfo = ApplicationConstants.ShopifyApiHeaders.PAGE_INFO + EQUAL_TO + strPageInfo;
                }
            } else {
                break;
            }
            httpConnection.disconnect();
        }
        return allOrdersList;
    }

    /**
     * Maps the JSON object representing an order to an OrderKeyValueDto object.
     *
     * @param orderObj The JSON object representing an order from Shopify API.
     * @return OrderKeyValueDto object containing order details.
     */
    private OrderKeyValueDto createOrderKeyValueDto(JSONObject orderObj) {
        OrderKeyValueDto orderKeyValueDtoObj = new OrderKeyValueDto();
        orderKeyValueDtoObj.setOrderId(orderObj.optLong(DomainObject.Order.ORDER_ID));
        orderKeyValueDtoObj.setOrderNumber(orderObj.optInt(DomainObject.Order.NUMBER));
        orderKeyValueDtoObj.setCreatedAt(
                AppUtil.getDateFormatFromTimeDate(orderObj.optString(DomainObject.Order.CREATED_AT)));
        orderKeyValueDtoObj.setCustomerFullName(getCustomerFullName(
                orderObj.optJSONObject(DomainObject.Order.CUSTOMER)));
        orderKeyValueDtoObj.setCurrentTotalPrice(TOTAL_PRICE_IN_RS +
                orderObj.optString(DomainObject.Order.CURRENT_TOTAL_PRICE));
        orderKeyValueDtoObj.setFinancialStatus(mapFinancialStatus(
                orderObj.optString(DomainObject.Order.FINANCIAL_STATUS)));
        orderKeyValueDtoObj.setFulfillmentStatus(mapFulfillmentStatus(
                orderObj.optString(DomainObject.Order.FULFILLMENT_STATUS)));
        orderKeyValueDtoObj.setQuantity(getTotalQuantity(
                orderObj.optJSONArray(DomainObject.Order.LINE_ITEMS)));
        orderKeyValueDtoObj.setShippingLinesCode(getShippingLinesCode(
                orderObj.optJSONArray(DomainObject.Order.SHIPPING_LINES)));
        return orderKeyValueDtoObj;
    }

    /**
     * Gets the full name of the customer from the customer JSON object.
     *
     * @param customerObj The JSON object representing a customer from the order details.
     * @return Full name of the customer as a string.
     */
    private String getCustomerFullName(JSONObject customerObj) {
        if (Optional.ofNullable(customerObj).isPresent()) {
            return customerObj.optString(DomainObject.Order.CUSTOMER_FIRST_NAME) + SPACE +
                    customerObj.optString(DomainObject.Order.CUSTOMER_LAST_NAME);
        }
        return DEFAULT_EMPTY;
    }

    /**
     * Maps the financial status string to a more user-friendly representation.
     *
     * @param financialStatus The financial status string to be mapped.
     * @return Mapped financial status string.
     */
    private String mapFinancialStatus(String financialStatus) {
        return financialStatus.equals(ApplicationConstants.Orders.ORDER_PAID_STATUS) ||
                financialStatus.equals(ApplicationConstants.Orders.ORDER_PENDING_STATUS) ?
                ApplicationConstants.Orders.PAID_STATUS : ApplicationConstants.Orders.PAYMENT_PENDING_STATUS;
    }

    /**
     * Maps the fulfillment status string to a more user-friendly representation.
     *
     * @param fulfillmentStatus The fulfillment status string to be mapped.
     * @return Mapped fulfillment status string.
     */
    private String mapFulfillmentStatus(String fulfillmentStatus) {
        String strFulFillmentStatus;
        if (ApplicationConstants.Orders.ORDER_FULFILLED_STATUS.equals(fulfillmentStatus)) {
            strFulFillmentStatus = ApplicationConstants.Orders.FULFILLED_STATUS;
        } else {
            strFulFillmentStatus = ApplicationConstants.Orders.UNFULFILLED_STATUS;
        }
        return strFulFillmentStatus;
    }

    /**
     * Calculates and gets the total quantity of items in the order based on the line items.
     *
     * @param lineItems The JSON array representing line items in the order.
     * @return Total quantity of items in the order as a string.
     */
    private String getTotalQuantity(JSONArray lineItems) {
        int totalQuantity = 0;
        if (Optional.ofNullable(lineItems).isPresent()) {
            for (int i = 0; i < lineItems.length(); i++) {
                JSONObject lineItem = lineItems.optJSONObject(i);
                totalQuantity += lineItem.optInt(DomainObject.Order.QUANTITY);
            }
        }
        return totalQuantity + SPACE + QUANTITY_ITEM;
    }

    /**
     * Gets the shipping lines code from the JSON array of shipping lines in the order.
     *
     * @param shippingLines The JSON array representing shipping lines in the order.
     * @return Shipping lines code as a string.
     */
    private String getShippingLinesCode(JSONArray shippingLines) {
        if (Optional.ofNullable(shippingLines).isPresent() && shippingLines.length() > DEFAULT_INTEGER_VALUE_ZERO) {
            JSONObject firstShippingLine = shippingLines.optJSONObject(DEFAULT_INTEGER_VALUE_ZERO);
            return firstShippingLine.optString(DomainObject.Order.CODE);
        }
        return DEFAULT_EMPTY;
    }

    /**
     * Retrieves the yearly order details for the given last year and current year.
     *
     * @param lastYear    The last year as a string (e.g., "2022").
     * @param currentYear The current year as a string (e.g., "2023").
     * @return A {@link YearlyOrderResponseDto} containing the yearly order statistics.
     * @throws IOException if there is an error while fetching the data from Shopify.
     */
    public YearlyOrderResponseDto getYearlyOrderDetails(String shopifyEndpoint, String lastYear,
                                                        String currentYear, String shopifyAccessToken)
            throws IOException {
        YearlyOrderResponseDto yearlyOrderResponseDto = new YearlyOrderResponseDto();
        List<Orders> lastYearOrderDetailsList = getYearlyOrderDetailsFromShopify(shopifyEndpoint, lastYear, null,
                shopifyAccessToken);
        double wiOldPriceCount = lastYearOrderDetailsList.stream()
                .filter(order -> order.getFinancial_status().equals(ApplicationConstants.Orders.ORDER_PAID_STATUS))
                .mapToDouble(order -> Double.parseDouble(order.getCurrent_total_price()))
                .sum();
        int wiOldTotalOrderCount = (int) lastYearOrderDetailsList.stream()
                .filter(order -> order.getFinancial_status().equals(ApplicationConstants.Orders.ORDER_PAID_STATUS))
                .count();
        List<String> calendarMonthsList = getCalendarMonthsList();
        Map<String, Double> orderMonthMap = calendarMonthsList.stream().collect(Collectors.toMap(
                month -> month,
                month -> DEFAULT_DOUBLE_VALUE));
        Map<String, Integer> monthsBasedOrderMap = calendarMonthsList.stream().collect(Collectors.toMap(
                month -> month,
                month -> DEFAULT_INTEGER_VALUE_ZERO));
        List<Orders> currentYearOrderDetailsList = getYearlyOrderDetailsFromShopify(shopifyEndpoint, currentYear,
                null, shopifyAccessToken);
        double wiOrderNewPriceCount = currentYearOrderDetailsList.stream()
                .filter(order -> order.getFinancial_status().equals(ApplicationConstants.Orders.ORDER_PAID_STATUS))
                .mapToDouble(order -> Double.parseDouble(order.getCurrent_total_price()))
                .sum();
        int wiTotalOrderCount = (int) currentYearOrderDetailsList.stream()
                .filter(order -> order.getFinancial_status().equals(ApplicationConstants.Orders.ORDER_PAID_STATUS))
                .count();
        currentYearOrderDetailsList.forEach(data -> {
            if (ApplicationConstants.Orders.ORDER_PAID_STATUS.equals(data.getFinancial_status())) {
                double wiCurrentTotalPrice = Double.parseDouble(data.getCurrent_total_price());
                String createdMonth = AppUtil.getCreatedMonth(data.getCreated_at());
                orderMonthMap.put(createdMonth, orderMonthMap.get(createdMonth) + wiCurrentTotalPrice);
                monthsBasedOrderMap.put(createdMonth, monthsBasedOrderMap.get(createdMonth) + 1);
            }
        });
        int wiPercentage = (int) ((wiOrderNewPriceCount - wiOldPriceCount) / 100);
        int wiOrderPercentage = ((wiTotalOrderCount - wiOldTotalOrderCount) / 100);
        yearlyOrderResponseDto.setOrderData(orderMonthMap);
        yearlyOrderResponseDto.setPercentage(wiPercentage);
        yearlyOrderResponseDto.setMonthsBasedOrder(monthsBasedOrderMap);
        yearlyOrderResponseDto.setTotalOrder(wiTotalOrderCount);
        yearlyOrderResponseDto.setOrderPercentage(BigDecimal.valueOf(wiOrderPercentage));
        return yearlyOrderResponseDto;
    }

    /**
     * Fetches the yearly order details from Shopify for the given order date.
     *
     * @param fromDate The order date as a string (e.g., "2022-12-31T23:59:59").
     * @return A list of {@link Orders} representing the yearly order details.
     * @throws IOException if there is an error while fetching the data from Shopify.
     */
    public List<Orders> getYearlyOrderDetailsFromShopify(String shopifyEndpoint, String fromDate, String toDate,
                                                         String shopifyAccessToken) throws IOException {
        orderServiceLogger.info("Entered get order details from shopify api call:::::::::::::::");
        String pageInfo = null;
        List<Orders> allOrderList = new ArrayList<>();
        boolean lastPage = true;
        HttpURLConnection httpConnection = null;
        while (lastPage) {
            if (allOrderList.isEmpty()) {
                if (Optional.ofNullable(toDate).isEmpty()) {
                    httpConnection = getHttpURLConnection(shopifyEndpoint +
                            SHOPIFY_ENDPOINT_URL_QUESTIONMARK + SHOPIFY_ENDPOINT_PARAM_PROCESSED_AT_MIN + fromDate +
                            SPLIT_WITH_AND_SYMBOL + pageInfo, shopifyAccessToken);
                } else {
                    httpConnection = getHttpURLConnection(shopifyEndpoint +
                                    SHOPIFY_ENDPOINT_URL_QUESTIONMARK + SHOPIFY_ENDPOINT_PARAM_PROCESSED_AT_MIN + fromDate +
                                    SHOPIFY_ENDPOINT_PARAM_PROCESSED_AT_MAX + toDate + SPLIT_WITH_AND_SYMBOL + pageInfo,
                            shopifyAccessToken);
                }
            } else {
                httpConnection = getHttpURLConnection(shopifyEndpoint +
                        SHOPIFY_ENDPOINT_URL_QUESTIONMARK + pageInfo, shopifyAccessToken);
            }
            int responseCode = httpConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader httpConnectionBufferReader = new BufferedReader(new InputStreamReader(
                        httpConnection.getInputStream()))) {
                    String sbNewLine = httpConnectionBufferReader.lines().collect(Collectors.joining());
                    Gson gsonObject = new Gson();
                    OrderResponseDto orderResponseDtoObj = gsonObject.fromJson(sbNewLine, OrderResponseDto.class);
                    List<Orders> ordersList = orderResponseDtoObj.getOrders();
                    allOrderList.addAll(ordersList);
                    String linkHeader = httpConnection.getHeaderField(ApplicationConstants.ShopifyApiHeaders.LINK);
                    if (linkHeader == null || !linkHeader.contains(RESPONSE_HINT_FOR_NEXT_PAGE)) {
                        lastPage = false;
                    } else {
                        String nextLink = productService.parseNextLink(linkHeader);
                        if (Optional.ofNullable(nextLink).isPresent()) {
                            Map<String, String> params = productService.parseQueryParameters(nextLink);
                            String strPageInfo = params.get(ApplicationConstants.ShopifyApiHeaders.PAGE_INFO)
                                    .replace(REPLACE_WITH_GREATER_THAN, DEFAULT_EMPTY);
                            pageInfo = ApplicationConstants.ShopifyApiHeaders.PAGE_INFO + EQUAL_TO + strPageInfo;
                        }
                    }
                }
            } else {
                lastPage = false;
            }
            httpConnection.disconnect();
        }
        return allOrderList;
    }

    /**
     * Calculates and retrieves the average order value for a specific date range, number of days, and Shopify configuration.
     *
     * @param shopifyEndpoint    The Shopify endpoint for accessing order data.
     * @param fromDate           The start date of the date range for which the average order value is to be calculated.
     * @param toDate             The end date of the date range for which the average order value is to be calculated.
     * @param shopifyAccessToken The access token for authenticating with Shopify.
     * @param noOfDays           The number of days to consider when calculating the average order value.
     * @return An AverageOrderValueResponseDto containing the total average order value and percentage.
     * @throws IOException if there is an I/O error while retrieving Shopify order data.
     */
    public AverageOrderValueResponseDto getAverageValueOfOrders(String shopifyEndpoint, String fromDate, String toDate,
                                                                String shopifyAccessToken, Integer noOfDays) throws IOException {
        AverageOrderValueResponseDto averageOrderValueResponseDto = new AverageOrderValueResponseDto();
        List<Orders> getCurrentYearOrderDetails = getYearlyOrderDetailsFromShopify(shopifyEndpoint, fromDate, toDate,
                shopifyAccessToken);
        int wiTotalPaidOrderStatusCount = (int) getCurrentYearOrderDetails.stream()
                .filter(order -> order.getFinancial_status().equals(ApplicationConstants.Orders.ORDER_PAID_STATUS))
                .count();
        BigDecimal percentage = BigDecimal.valueOf(wiTotalPaidOrderStatusCount)
                .divide(BigDecimal.valueOf(noOfDays), 2, RoundingMode.HALF_UP);
        List<String> calendarMonthsList = getCalendarMonthsList();
        Map<String, Integer> monthsBasedOrderMap = calendarMonthsList.stream().collect(Collectors.toMap(
                month -> month,
                month -> DEFAULT_INTEGER_VALUE_ZERO));
        getCurrentYearOrderDetails.forEach(data -> {
            if (ApplicationConstants.Orders.ORDER_PAID_STATUS.equals(data.getFinancial_status())) {
                String createdMonth = AppUtil.getCreatedMonth(data.getCreated_at());
                monthsBasedOrderMap.put(createdMonth, monthsBasedOrderMap.get(createdMonth) + 1);
            }
        });
        averageOrderValueResponseDto.setTotalAverageOrderValue(wiTotalPaidOrderStatusCount);
        averageOrderValueResponseDto.setPercentage(percentage);
        averageOrderValueResponseDto.setMonthsBasedOrder(monthsBasedOrderMap);
        return averageOrderValueResponseDto;
    }

    /**
     * Fetches filtered order details based on the provided fromDate and toDate.
     *
     * @param fromDate The start date of the filter range.
     * @param toDate   The end date of the filter range.
     * @return A YearlyOrderResponseDto containing the filtered order details.
     * @throws IOException If there's an error while fetching the filtered order details.
     */
    public YearlyOrderResponseDto getFilterOrderDetails(String shopifyEndpoint, String fromDate, String toDate,
                                                        String shopifyAccessToken, Integer noOfDays) throws IOException {
        YearlyOrderResponseDto yearlyOrderResponseDto = new YearlyOrderResponseDto();
        List<Orders> filteredOrderDetailsList = getFilterOrderListFromShopify(shopifyEndpoint, fromDate,
                toDate, shopifyAccessToken);
        double wiOldPriceCount = filteredOrderDetailsList.stream()
                .filter(order -> order.getFinancial_status().equals(ApplicationConstants.Orders.ORDER_PAID_STATUS))
                .mapToDouble(order -> Double.parseDouble(order.getCurrent_total_price()))
                .sum();
        List<String> calendarMonthsList = getCalendarMonthsList();
        Map<String, Double> orderMonthMap = calendarMonthsList.stream().collect(Collectors.toMap(
                month -> month,
                month -> DEFAULT_DOUBLE_VALUE));
        Map<String, Integer> monthsBasedOrderMap = calendarMonthsList.stream().collect(Collectors.toMap(
                month -> month,
                month -> DEFAULT_INTEGER_VALUE_ZERO));
        int wiTotalOrderCount = (int) filteredOrderDetailsList.stream()
                .filter(order -> order.getFinancial_status().equals(ApplicationConstants.Orders.ORDER_PAID_STATUS))
                .count();
        filteredOrderDetailsList.forEach(data -> {
            if (ApplicationConstants.Orders.ORDER_PAID_STATUS.equals(data.getFinancial_status())) {
                double wiCurrentTotalPrice = Double.parseDouble(data.getCurrent_total_price());
                String createdMonth = AppUtil.getCreatedMonth(data.getCreated_at());
                orderMonthMap.put(createdMonth, orderMonthMap.get(createdMonth) + wiCurrentTotalPrice);
                monthsBasedOrderMap.put(createdMonth, monthsBasedOrderMap.get(createdMonth) + 1);
            }
        });
        int wiPercentage = (int) ((wiOldPriceCount) / noOfDays);
        BigDecimal wiOrderPercentage = BigDecimal.valueOf(wiTotalOrderCount)
                .divide(BigDecimal.valueOf(noOfDays), 2, RoundingMode.HALF_UP);
        yearlyOrderResponseDto.setOrderData(orderMonthMap);
        yearlyOrderResponseDto.setPercentage(wiPercentage);
        yearlyOrderResponseDto.setMonthsBasedOrder(monthsBasedOrderMap);
        yearlyOrderResponseDto.setTotalOrder(wiTotalOrderCount);
        yearlyOrderResponseDto.setOrderPercentage(wiOrderPercentage);
        return yearlyOrderResponseDto;
    }

    static List<String> getCalendarMonthsList() {
        List<String> calendarMonthsList = new ArrayList<>();
        calendarMonthsList.add(ApplicationConstants.CalendarMonths.JANUARY);
        calendarMonthsList.add(ApplicationConstants.CalendarMonths.FEBRUARY);
        calendarMonthsList.add(ApplicationConstants.CalendarMonths.MARCH);
        calendarMonthsList.add(ApplicationConstants.CalendarMonths.APRIL);
        calendarMonthsList.add(ApplicationConstants.CalendarMonths.MAY);
        calendarMonthsList.add(ApplicationConstants.CalendarMonths.JUNE);
        calendarMonthsList.add(ApplicationConstants.CalendarMonths.JULY);
        calendarMonthsList.add(ApplicationConstants.CalendarMonths.AUGUST);
        calendarMonthsList.add(ApplicationConstants.CalendarMonths.SEPTEMBER);
        calendarMonthsList.add(ApplicationConstants.CalendarMonths.OCTOBER);
        calendarMonthsList.add(ApplicationConstants.CalendarMonths.NOVEMBER);
        calendarMonthsList.add(ApplicationConstants.CalendarMonths.DECEMBER);
        return calendarMonthsList;
    }

    /**
     * Fetches filtered order details from the Shopify API based on the provided fromDate and toDate.
     *
     * @param fromDate The start date of the filter range.
     * @param toDate   The end date of the filter range.
     * @return A List of Orders containing the filtered order details.
     * @throws IOException If there's an error while fetching the filtered order details from the Shopify API.
     */
    public List<Orders> getFilterOrderListFromShopify(String shopifyEndpoint, String fromDate, String toDate,
                                                      String shopifyAccessToken) throws IOException {
        orderServiceLogger.info("Entered get filtered orders details from Shopify API call:::::::::::::::");
        String pageInfo = null;
        List<Orders> allOrderList = new ArrayList<>();
        boolean lastPage = true;
        while (lastPage) {
            String orderEndPoint = getOrderEndPoint(shopifyEndpoint, fromDate, toDate, pageInfo);
            HttpURLConnection httpConnection = getHttpURLConnection(orderEndPoint, shopifyAccessToken);
            int responseCode = httpConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader httpConnectionBufferReader = new BufferedReader(new InputStreamReader(
                        httpConnection.getInputStream()))) {
                    String sbNewLine = httpConnectionBufferReader.lines().collect(Collectors.joining());
                    Gson gsonObject = new Gson();
                    OrderResponseDto orderResponseDtoObj = gsonObject.fromJson(sbNewLine, OrderResponseDto.class);
                    List<Orders> ordersList = orderResponseDtoObj.getOrders();
                    allOrderList.addAll(ordersList);
                    String linkHeader = httpConnection.getHeaderField(ApplicationConstants.ShopifyApiHeaders.LINK);
                    if (linkHeader == null || !linkHeader.contains(RESPONSE_HINT_FOR_NEXT_PAGE)) {
                        lastPage = false;
                    } else {
                        String nextLink = productService.parseNextLink(linkHeader);
                        if (Optional.ofNullable(nextLink).isPresent()) {
                            Map<String, String> params = productService.parseQueryParameters(nextLink);
                            String strPageInfo = params.get(ApplicationConstants.ShopifyApiHeaders.PAGE_INFO)
                                    .replace(REPLACE_WITH_GREATER_THAN, DEFAULT_EMPTY);
                            pageInfo = ApplicationConstants.ShopifyApiHeaders.PAGE_INFO + EQUAL_TO + strPageInfo;
                        }
                    }
                }
            } else {
                lastPage = false;
            }
            httpConnection.disconnect();
        }
        return allOrderList;
    }

    String getOrderEndPoint(String shopifyEndpoint, String fromDate, String toDate, String pageInfo) {
        String orderEndPoint;
        if (Optional.ofNullable(pageInfo).isPresent()) {
            orderEndPoint = shopifyEndpoint + SHOPIFY_ENDPOINT_URL_QUESTIONMARK + pageInfo;
        } else {
            orderEndPoint = shopifyEndpoint + SHOPIFY_ENDPOINT_URL_QUESTIONMARK +
                    SHOPIFY_ENDPOINT_PARAM_PROCESSED_AT_MIN + fromDate +
                    SHOPIFY_ENDPOINT_PARAM_PROCESSED_AT_MAX + toDate +
                    SPLIT_WITH_AND_SYMBOL + SHOPIFY_ENDPOINT_LIMIT;
        }
        return orderEndPoint;
    }

    /**
     * Retrieves the order details for a given order ID from Shopify and other relevant data related to the order.
     *
     * @param orderId The unique identifier of the order.
     * @return An OrderDto containing the order details, total order count by customer, and conversion summary.
     * @throws ExecutionException   if there is an error while processing the request.
     * @throws InterruptedException if there is an error while processing the request.
     */
    public OrderDto getOrderDetailsByOrderId(String shopifyOrderDetailsByOrderIdEndPoint, String orderId,
                                             String shopifyAccessToken, String shopifyResetPasswordEndPoint,
                                             String shopifyGraphQueryEndPoint,
                                             String shopifyVariantDetailsByVariantIdEndPoint,
                                             String shopifyProductImageByProductIdAndImageIdEndPoint)
            throws ExecutionException, InterruptedException {
        OrderDto orderDtoObj = new OrderDto();
        CompletableFuture<Orders> getOrderDetailsByOrderIdFromShopify = CompletableFuture.supplyAsync(() -> {
            try {
                return getOrderDetailsByOrderIdFromShopify(shopifyOrderDetailsByOrderIdEndPoint, orderId,
                        shopifyAccessToken);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        orderDtoObj.setOrder(getOrderDetailsByOrderIdFromShopify.get());
        if (Optional.ofNullable(getOrderDetailsByOrderIdFromShopify.get().getCustomer().getId()).isPresent()) {
            CompletableFuture<CustomerDto> getTotalOrderCountByCustomerIdFromShopify = CompletableFuture.supplyAsync(
                    () -> {
                        try {
                            return getTotalOrderCountByCustomerIdFromShopify(shopifyResetPasswordEndPoint,
                                    String.valueOf(getOrderDetailsByOrderIdFromShopify.get().getCustomer().getId()),
                                    shopifyAccessToken);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
            orderDtoObj.setTotalOrdersCount(getTotalOrderCountByCustomerIdFromShopify.get().getOrders_count());
        }
        CompletableFuture<ConversionSummaryDto> getConversionSummary = CompletableFuture.supplyAsync(() ->
                getConversionSummary(shopifyGraphQueryEndPoint, orderId, shopifyAccessToken));
        orderDtoObj.setConversionSummary(getConversionSummary.get());
        List<ProductInfoDto> productInfoDtoList = new ArrayList<>();
        getOrderDetailsByOrderIdFromShopify.get().getLine_items().forEach(lineItems -> {
            CompletableFuture<VariantDetailsDto> getVariantDetailsDtoCompletableFuture = CompletableFuture.supplyAsync(() ->
            {
                try {
                    return getVariantDetails(shopifyVariantDetailsByVariantIdEndPoint, lineItems.getVariant_id(),
                            shopifyAccessToken);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            try {
                orderDtoObj.setWeight(getVariantDetailsDtoCompletableFuture.get().getWeight());
                orderDtoObj.setWeightUnit(getVariantDetailsDtoCompletableFuture.get().getWeight_unit());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            CompletableFuture<ProductInfoDto> getProductInfoDtoCompletableFuture = CompletableFuture.supplyAsync(() ->
            {
                try {
                    if (Optional.ofNullable(getVariantDetailsDtoCompletableFuture.get().getImage_id()).isPresent()) {
                        return getProductInfo(shopifyProductImageByProductIdAndImageIdEndPoint,
                                getVariantDetailsDtoCompletableFuture.get().getProduct_id(),
                                getVariantDetailsDtoCompletableFuture.get().getImage_id(),
                                shopifyAccessToken);
                    }
                } catch (IOException | InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
            try {
                if (Optional.ofNullable(getProductInfoDtoCompletableFuture.get()).isPresent()) {
                    productInfoDtoList.add(getProductInfoDtoCompletableFuture.get());
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
        orderDtoObj.setProductInfo(productInfoDtoList);

        CompletableFuture<OrderChannelInformationDto> getOrderChannelInformation = CompletableFuture.supplyAsync(() ->
                getOrderChannelInformation(shopifyGraphQueryEndPoint, orderId, shopifyAccessToken));
        orderDtoObj.setChannelInformation(getOrderChannelInformation.get());
        CompletableFuture<List<CustomerOrderStatusDto>> getCustomerOrderStatusListCompletableFuture =
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return getCustomerOrderStatusByOrderId(orderId, getOrderDetailsByOrderIdFromShopify.get());
                    } catch (ExecutionException | JsonProcessingException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
        orderDtoObj.setCustomerOrderStatus(getCustomerOrderStatusListCompletableFuture.get());

        CompletableFuture<CustomerMeasurementDto> getCustomerMeasurementListCompletableFuture =
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return customerService.getCustomerMeasurementByCustomerMeasurementId(
                                String.valueOf(getOrderDetailsByOrderIdFromShopify.get().getCustomer().getId()),
                                getOrderDetailsByOrderIdFromShopify.get().getLine_items()
                                        .get(0).getProperties().get(0).getValue());
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
        orderDtoObj.setIsToggleEnable(getCustomerMeasurementListCompletableFuture.get().getIsNewSize());
        return orderDtoObj;
    }

    /**
     * Retrieves the order details for a given order ID from Shopify.
     *
     * @param orderId The unique identifier of the order.
     * @return An Orders object containing the order details.
     * @throws IOException if there is an error while processing the Shopify API call.
     */
    public Orders getOrderDetailsByOrderIdFromShopify(String shopifyOrderDetailsByOrderIdEndPoint, String orderId,
                                                      String shopifyAccessToken) throws IOException {
        orderServiceLogger.info("Entered get order details by order id from shopify api call:::::::::::::::");
        HttpURLConnection httpConnection = getHttpURLConnection(
                shopifyOrderDetailsByOrderIdEndPoint + orderId + SHOPIFY_ENDPOINT_JSON, shopifyAccessToken);
        try (InputStream httpConnectionInputStream = httpConnection.getInputStream();
             BufferedReader httpConnectionBufferReader = new BufferedReader(
                     new InputStreamReader(httpConnectionInputStream, StandardCharsets.UTF_8))) {
            StringBuilder sbNewLine = new StringBuilder();
            String strLine;
            while ((strLine = httpConnectionBufferReader.readLine()) != null) {
                sbNewLine.append(strLine).append(NEWLINE);
            }
            Gson gsonObject = new Gson();
            OrderDetailsResponseDto orderObj = gsonObject.fromJson(sbNewLine.toString(), OrderDetailsResponseDto.class);
            return orderObj.getOrder();
        }
    }

    /**
     * Retrieves the total order count for a given customer ID from Shopify.
     *
     * @param customerId The unique identifier of the customer.
     * @return A CustomerDto object containing the customer details, including the total order count.
     * @throws IOException if there is an error while processing the Shopify API call.
     */
    public CustomerDto getTotalOrderCountByCustomerIdFromShopify(String shopifyResetPasswordEndPoint,
                                                                 String customerId, String shopifyAccessToken) throws IOException {
        orderServiceLogger.info("Entered get total order count by customer id from shopify api call:::::::::::::::");
        HttpURLConnection httpConnection = getHttpURLConnection(
                shopifyResetPasswordEndPoint + customerId + SHOPIFY_ENDPOINT_JSON, shopifyAccessToken);
        try (InputStream httpConnectionInputStream = httpConnection.getInputStream();
             BufferedReader httpConnectionBufferReader = new BufferedReader(
                     new InputStreamReader(httpConnectionInputStream, StandardCharsets.UTF_8))) {
            StringBuilder sbNewLine = new StringBuilder();
            String strLine;
            while ((strLine = httpConnectionBufferReader.readLine()) != null) {
                sbNewLine.append(strLine).append(NEWLINE);
            }
            Gson gsonObject = new Gson();
            CustomerDetailResponseDto customerResponseDtoObj = gsonObject.fromJson(sbNewLine.toString(),
                    CustomerDetailResponseDto.class);
            return customerResponseDtoObj.getCustomer();
        }
    }

    /**
     * Retrieves the conversion summary for a given order ID from Shopify using GraphQL.
     *
     * @param orderId The unique identifier of the order.
     * @return A ConversionSummaryDto object containing the conversion summary details.
     */
    public ConversionSummaryDto getConversionSummary(String shopifyGraphQueryEndPoint, String orderId,
                                                     String shopifyAccessToken) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.valueOf(CONTENT_TYPE));
        httpHeaders.set(ApplicationConstants.ShopifyApiHeaders.SHOPIFY_ACCESS_TOKEN, shopifyAccessToken);
        String query = String.format(ApplicationConstants.ShopifyQuery.ORDER_CONVERSION_SUMMARY_QUERY, orderId);
        HttpEntity<String> httpEntity = new HttpEntity<>(query, httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(shopifyGraphQueryEndPoint, HttpMethod.POST,
                httpEntity, String.class);
        JSONObject jsonResponse = new JSONObject(Objects.requireNonNull(responseEntity.getBody()));
        JSONObject conversionSummaryJsonObject = jsonResponse.getJSONObject(DomainObject.ConversionSummary.DATA)
                .getJSONObject(DomainObject.ConversionSummary.ORDER)
                .getJSONObject(DomainObject.ConversionSummary.CUSTOMER_JOURNEY_SUMMARY);
        return createConversionSummary(conversionSummaryJsonObject);
    }

    /**
     * Creates a ConversionSummaryDto object from the given JSON data representing the conversion summary.
     *
     * @param conversionSummaryJsonObject The JSON data containing the conversion summary details.
     * @return A ConversionSummaryDto object representing the conversion summary.
     */
    private ConversionSummaryDto createConversionSummary(JSONObject conversionSummaryJsonObject) {
        ConversionSummaryDto conversionSummaryDto = new ConversionSummaryDto();
        conversionSummaryDto.setCustomerOrderIndex(
                conversionSummaryJsonObject.getInt(DomainObject.ConversionSummary.CUSTOMER_ORDER_INDEX));
        conversionSummaryDto.setDaysToConversion(
                conversionSummaryJsonObject.optInt(DomainObject.ConversionSummary.DAYS_TO_CONVERSION, 0));
        conversionSummaryDto.setMomentsCount(
                conversionSummaryJsonObject.optInt(DomainObject.ConversionSummary.MOMENTS_COUNT));
        conversionSummaryDto.setReady(
                conversionSummaryJsonObject.optBoolean(DomainObject.ConversionSummary.READY));
        conversionSummaryDto.set__typename(
                conversionSummaryJsonObject.optString(DomainObject.ConversionSummary.TYPE_NAME));
        JSONObject firstVisitJson = conversionSummaryJsonObject.optJSONObject(DomainObject.ConversionSummary.
                FIRST_VISIT);
        if (firstVisitJson != null) {
            FirstVisit firstVisit = conversionSummaryDto.new FirstVisit();
            firstVisit.setId(firstVisitJson.optString(DomainObject.ConversionSummary.FIRST_VISIT_ID));
            firstVisit.setLandingPage(
                    firstVisitJson.optString(DomainObject.ConversionSummary.FIRST_VISIT_LANDING_PAGE));
            firstVisit.setReferralCode(
                    firstVisitJson.optString(DomainObject.ConversionSummary.FIRST_VISIT_REFERRAL_CODE));
            firstVisit.setReferralInfoHtml(
                    firstVisitJson.optString(DomainObject.ConversionSummary.FIRST_VISIT_REFERRAL_INFO_HTML));
            firstVisit.setSourceDescription(
                    firstVisitJson.optString(DomainObject.ConversionSummary.FIRST_VISIT_SOURCE_DESCRIPTION));
            firstVisit.set__typename(
                    firstVisitJson.optString(DomainObject.ConversionSummary.FIRST_VISIT_TYPE_NAME));
            firstVisit.setOccurredAt(
                    firstVisitJson.optString(DomainObject.ConversionSummary.FIRST_VISIT_OCCURRED_AT));
            conversionSummaryDto.setFirstVisit(firstVisit);
        }
        return conversionSummaryDto;
    }

    /**
     * Retrieves customer order status based on the provided order ID.
     *
     * @param orderId The order ID to retrieve customer order statuses for.
     * @return A list of customer order status DTOs associated with the given order ID.
     */
    public List<CustomerOrderStatusDto> getCustomerOrderStatusByOrderId(String orderId, Orders ordersObj) throws JsonProcessingException {
        List<CustomerOrderStatusDto> customerOrderStatusDtoList = new ArrayList<>();
        List<CustomerOrderStatus> customerOrderStatusList = customerOrderStatusRepository.findByOrderId(orderId);
        if (!customerOrderStatusList.isEmpty()) {
            customerOrderStatusList.forEach(customerOrderStatusObj -> {
                CustomerOrderStatusDto customerOrderStatusDto =
                        modelMapper.map(customerOrderStatusObj, CustomerOrderStatusDto.class);
                customerOrderStatusDtoList.add(customerOrderStatusDto);
            });
        } else {
            CustomerOrderStatusDto customerOrderStatusDtoObj = new CustomerOrderStatusDto();
            customerOrderStatusDtoObj.setOrderId(orderId);
            CustomerMeasurementDto customerMeasurementDtoObj = new CustomerMeasurementDto();

            String stringSizeNameValue = getValueByKey(ordersObj, DomainObject.CustomerMeasurements.SIZE_NAME);
            String stringShoulderWidthValue = getValueByKey(ordersObj,DomainObject.CustomerMeasurements.SHOULDER_WIDTH);
            String stringHalfChestWidthValue = getValueByKey(ordersObj,DomainObject.CustomerMeasurements.HALF_CHEST_WIDTH);
            String stringHalfBottomWidthValue = getValueByKey(ordersObj,DomainObject.CustomerMeasurements.HALF_BOTTOM_WIDTH);
            String stringNeckWidthValue = getValueByKey(ordersObj,DomainObject.CustomerMeasurements.NECK_WIDTH);
            String stringFrontNeckValue = getValueByKey(ordersObj,DomainObject.CustomerMeasurements.FRONT_NECK_DROP);
            String stringCbLengthValue = getValueByKey(ordersObj,DomainObject.CustomerMeasurements.CB_LENGTH);
            String stringSleeveLengthValue = getValueByKey(ordersObj,DomainObject.CustomerMeasurements.SLEEVE_LENGTH);
            String stringSleeveOpeningValue = getValueByKey(ordersObj,DomainObject.CustomerMeasurements.SLEEVE_OPENING);
            String stringArmHoleStraigthValue = getValueByKey(ordersObj,DomainObject.CustomerMeasurements.ARMHOLE_STRAIGTH);
            String stringInitialsValue = getValueByKey(ordersObj,DomainObject.CustomerMeasurements.INITIALS);

            customerMeasurementDtoObj.setName(stringSizeNameValue);
            customerMeasurementDtoObj.setShoulderWidth(stringShoulderWidthValue);
            customerMeasurementDtoObj.setHalfChestWidth(stringHalfChestWidthValue);
            customerMeasurementDtoObj.setHalfBottomWidth(stringHalfBottomWidthValue);
            customerMeasurementDtoObj.setNeckWidth(stringNeckWidthValue);
            customerMeasurementDtoObj.setFrontNeckDrop(stringFrontNeckValue);
            customerMeasurementDtoObj.setCbLength(stringCbLengthValue);
            customerMeasurementDtoObj.setSleeveLength(stringSleeveLengthValue);
            customerMeasurementDtoObj.setSleeveOpening(stringSleeveOpeningValue);
            customerMeasurementDtoObj.setArmholeStraight(stringArmHoleStraigthValue);
            customerMeasurementDtoObj.setInitial(stringInitialsValue);
            customerMeasurementDtoObj.setFit(String.valueOf(ordersObj.getLine_items().get(0).getVariant_id()));
            customerMeasurementDtoObj.setCustomerId(String.valueOf(ordersObj.getCustomer().getId()));
            customerOrderStatusDtoObj.setCustomerMeasurement(customerMeasurementDtoObj);
            customerOrderStatusDtoList.add(customerOrderStatusDtoObj);
        }
        return customerOrderStatusDtoList;
    }

    private static String getValueByKey(Orders orderDto, String key) {
        if (orderDto != null && orderDto.getLine_items().get(0).getProperties().get(0) != null) {
            List<Orders.Properties> properties = orderDto.getLine_items().get(0).getProperties();
            for (Orders.Properties property : properties) {
                if (key.equals(property.getName())) {
                    return property.getValue();
                }
            }
        }
        return null;
    }


    /**
     * Retrieves order channel information from a Shopify store using GraphQL.
     *
     * @param shopifyGraphQueryEndPoint The GraphQL endpoint URL for the Shopify store.
     * @param orderId                   The unique identifier of the order for which channel information is requested.
     * @param shopifyAccessToken        The access token required to authenticate the request to the Shopify API.
     * @return An OrderChannelInformationDto object containing channel information for the specified order.
     * @see OrderChannelInformationDto
     */
    public OrderChannelInformationDto getOrderChannelInformation(String shopifyGraphQueryEndPoint, String orderId,
                                                                 String shopifyAccessToken) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.valueOf(CONTENT_TYPE));
        httpHeaders.set(ApplicationConstants.ShopifyApiHeaders.SHOPIFY_ACCESS_TOKEN, shopifyAccessToken);
        String query = String.format(ApplicationConstants.ShopifyQuery.ORDER_CHANNEL_INFORMATION_QUERY, orderId);
        HttpEntity<String> httpEntity = new HttpEntity<>(query, httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(shopifyGraphQueryEndPoint, HttpMethod.POST,
                httpEntity, String.class);
        JSONObject jsonResponse = new JSONObject(Objects.requireNonNull(responseEntity.getBody()));
        JSONObject orderJsonObject = jsonResponse.getJSONObject(DomainObject.ConversionSummary.DATA)
                .getJSONObject(DomainObject.ConversionSummary.ORDER);
        JSONObject channelInformationJsonObject = null;
        if (!orderJsonObject.isNull(DomainObject.OrderChannelInformation.CHANNEL_INFORMATION)) {
            channelInformationJsonObject = orderJsonObject.
                    getJSONObject(DomainObject.OrderChannelInformation.CHANNEL_INFORMATION);
        }
        return createChannelInformation(channelInformationJsonObject);
    }

    /**
     * Creates an OrderChannelInformationDto object from a JSON representation of channel information.
     *
     * @param channelInformationJsonObject The JSON object containing channel information.
     * @return An OrderChannelInformationDto object populated with data from the JSON representation.
     * @see OrderChannelInformationDto
     */
    private OrderChannelInformationDto createChannelInformation(JSONObject channelInformationJsonObject) {
        if (channelInformationJsonObject!=null) {
            OrderChannelInformationDto orderChannelInformationDto = new OrderChannelInformationDto();
            orderChannelInformationDto.setId(
                    channelInformationJsonObject.getString(DomainObject.OrderChannelInformation.ID));
            orderChannelInformationDto.set__typename(
                    channelInformationJsonObject.optString(DomainObject.OrderChannelInformation.TYPE_NAME));
            JSONObject channelDefinitionJson = channelInformationJsonObject.optJSONObject(DomainObject.
                    OrderChannelInformation.CHANNEL_DEFINITION);
            if (channelDefinitionJson!=null) {
                ChannelDefinition channelDefinition = orderChannelInformationDto.new ChannelDefinition();
                channelDefinition.setId(channelDefinitionJson.optString(DomainObject.
                        OrderChannelInformation.CHANNEL_DEFINITION_ID));
                channelDefinition.setChannelName(channelDefinitionJson.optString(DomainObject.
                        OrderChannelInformation.CHANNEL_DEFINITION_CHANNEL_NAME));
                channelDefinition.setSvgIcon(channelDefinitionJson.optString(DomainObject.
                        OrderChannelInformation.CHANNEL_DEFINITION_SVG_ICON));
                channelDefinition.set__typename(
                        channelDefinitionJson.optString(DomainObject.OrderChannelInformation.CHANNEL_DEFINITION_TYPE_NAME));
                orderChannelInformationDto.setChannelDefinition(channelDefinition);
            }
            return orderChannelInformationDto;
        } else {
            return null;
        }
    }

    /**
     * Retrieves details of a product variant from the Shopify API based on a variant ID.
     *
     * @param shopifyVariantDetailsByVariantIdEndPoint The Shopify API endpoint for fetching variant details
     *                                                 by variant ID.
     * @param variantId                                The unique identifier of the product variant.
     * @param shopifyAccessToken                        The access token required to authenticate the request
     *                                                  to the Shopify API.
     * @return A {@code VariantDetailsDto} object containing the retrieved details of the product variant.
     * @throws IOException if there are issues with I/O operations while making HTTP requests.
     * @see VariantDetailsDto
     */
    public VariantDetailsDto getVariantDetails(String shopifyVariantDetailsByVariantIdEndPoint, Long variantId,
                                               String shopifyAccessToken) throws IOException {
        orderServiceLogger.info("Entered get variant details by variant id from shopify api call:::::::::::::::");
        return productService.getVariantDetailsByVariantId(shopifyVariantDetailsByVariantIdEndPoint, variantId,
                shopifyAccessToken);
    }

    /**
     * Retrieves product information, including image source, product ID, and variant IDs, from the Shopify API
     * based on a product ID and an image ID.
     *
     * @param shopifyProductImageByProductIdAndImageIdEndPoint The Shopify API endpoint for fetching product image
     *                                                         details by product and image IDs.
     * @param productId                                       The unique identifier of the product.
     * @param imageId                                         The unique identifier of the image associated with
     *                                                        the product.
     * @param shopifyAccessToken                               The access token required to authenticate the request
     *                                                         to the Shopify API.
     * @return A {@code ProductInfoDto} object containing the retrieved product information.
     * @throws IOException if there are issues with I/O operations while making HTTP requests.
     * @see ProductInfoDto
     */
    public ProductInfoDto getProductInfo(String shopifyProductImageByProductIdAndImageIdEndPoint, Long productId,
                                         Long imageId, String shopifyAccessToken ) throws IOException {
        ProductInfoDto productInfoDto = new ProductInfoDto();
        orderServiceLogger.info("Entered get image details by product id and image id from shopify api call:::::");
        HttpURLConnection productImageHttpConnection = getHttpURLConnection(String.format(
                shopifyProductImageByProductIdAndImageIdEndPoint, productId, imageId), shopifyAccessToken);
        try (InputStream productImageHttpConnectionInputStream = productImageHttpConnection.getInputStream();
             BufferedReader productImageHttpConnectionBufferReader = new BufferedReader(
                     new InputStreamReader(productImageHttpConnectionInputStream, StandardCharsets.UTF_8))) {
            StringBuilder productImageSbNewLine = new StringBuilder();
            String productImageStrLine;
            while ((productImageStrLine = productImageHttpConnectionBufferReader.readLine())!=null) {
                productImageSbNewLine.append(productImageStrLine).append(NEWLINE);
            }
            Gson productImageGsonObject = new Gson();
            ImageDetailResponseDto imageDetailResponseDto = productImageGsonObject.fromJson(
                    productImageSbNewLine.toString(), ImageDetailResponseDto.class);
            productInfoDto.setImgSrc(imageDetailResponseDto.getImage().getSrc());
            productInfoDto.setProductId(imageDetailResponseDto.getImage().getProduct_id());
            productInfoDto.setVariantId(imageDetailResponseDto.getImage().getVariant_ids());
        }
        return productInfoDto;
    }
}
