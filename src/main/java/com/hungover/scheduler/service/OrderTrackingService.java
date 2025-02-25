package com.hungover.scheduler.service;

import com.google.gson.Gson;
import com.hungover.admin.repository.CustomerOrderStatusRepositoryI;
import com.hungover.admin.service.AdminService;
import com.hungover.common.constant.ApplicationConstants;
import com.hungover.common.domain.DomainObject;
import com.hungover.core.domain.customer.CustomerOrderStatus;
import com.hungover.core.dto.customer.CustomerOrderStatusDto;
import com.hungover.core.dto.order.OrderTrackingDetailsResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service class for handling order-tracking related operations and DHL API calls.
 */
@Service
public class OrderTrackingService {

    private final Logger orderTrackingServiceLogger = LoggerFactory.getLogger(this.getClass());

    private static final String ACCEPT = "application/json";
    private static final String DHL_ENDPOINT_PARAM = "&language=en&offset=0&limit=5";
    private static final String NEWLINE = "\n";

    private CustomerOrderStatusRepositoryI customerOrderStatusRepositoryI;
    private AdminService adminService;

    @Value("${dhlOrderTrackingStatusEndPoint}")
    String dhlOrderTrackingStatusEndPoint;
    @Value("${dhlAccessToken}")
    String dhlAccessToken;

    public OrderTrackingService(CustomerOrderStatusRepositoryI customerOrderStatusRepositoryI,
                                AdminService adminService) {
        this.customerOrderStatusRepositoryI = customerOrderStatusRepositoryI;
        this.adminService = adminService;
    }

    /**
     * Scheduled method to track and update order status for orders with DISPATCH status using DHL API.
     * This method retrieves order tracking information from DHL for eligible orders and updates their status.
     * Scheduled method with a cron expression that runs at the beginning of every hour.
     * This method performs a specific task or job on a regular hourly schedule.
     */
    @Scheduled(cron = "0 0/15 * * * ?")
    public void orderTrackingDetails() {
        orderTrackingServiceLogger.info("Entered order tracking service:::::::::::::::::::");
        List<CustomerOrderStatus> updatedCustomerOrderStatusList = new ArrayList<>();
        List<CustomerOrderStatus> customerOrderStatusList = customerOrderStatusRepositoryI.
                findByOrderStatusAndOrderTrackingNumberIsNotNull(DomainObject.CustomerOrderStatus.DISPATCHED);
        customerOrderStatusList.forEach(customerOrderStatus -> {
            orderTrackingServiceLogger.info("Entered DHL api call::::::::::::::::::");
            try {
                HttpURLConnection httpConnection = getHttpURLConnection(dhlOrderTrackingStatusEndPoint +
                        customerOrderStatus.getOrderTrackingNumber() + DHL_ENDPOINT_PARAM);
                try (InputStream httpConnectionInputStream = httpConnection.getInputStream();
                     BufferedReader httpConnectionBufferReader = new BufferedReader(
                             new InputStreamReader(httpConnectionInputStream, StandardCharsets.UTF_8))) {
                    StringBuilder sbNewLine = new StringBuilder();
                    String strLine;
                    while ((strLine = httpConnectionBufferReader.readLine())!=null) {
                        sbNewLine.append(strLine).append(NEWLINE);
                    }
                    Gson gsonObject = new Gson();
                    OrderTrackingDetailsResponseDto orderResponseDtoObj = gsonObject.fromJson(sbNewLine.toString(),
                            OrderTrackingDetailsResponseDto.class);
                    String strOrderStatus = null;
                    if (Boolean.TRUE.equals(customerOrderStatus.getIsFitSample())) {
                        strOrderStatus = "Edit Measurements";
                    } else {
                        strOrderStatus = "Order Delivered";
                    }
                    customerOrderStatus.setOrderStatus(strOrderStatus);
                    if (Optional.ofNullable(customerOrderStatus.getAddressInformation()).isPresent()) {
                        String stringDeliveredStatus = orderResponseDtoObj.getShipments().get(0).getStatus().getStatus();
                        String stringUpdatedStatus = stringDeliveredStatus.substring(0, 1).toUpperCase()
                                + stringDeliveredStatus.substring(1).toLowerCase();
                        CustomerOrderStatusDto customerOrderStatusDto = adminService
                                .updateCustomerOrderStatus(customerOrderStatus.getCustomerOrderStatusId(),
                                        stringUpdatedStatus, null,
                                        customerOrderStatus.getAddressInformation());
                    }
                }
                updatedCustomerOrderStatusList.addAll(customerOrderStatusList);
                customerOrderStatusRepositoryI.saveAll(updatedCustomerOrderStatusList);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Creates and configures an HttpURLConnection for making an HTTP GET request to a specified DHL API endpoint.
     *
     * @param dhlEndpoint   The DHL API endpoint URL to connect to.
     * @return An HttpURLConnection configured for the GET request to the specified DHL endpoint.
     * @throws IOException if there is an I/O error while establishing the connection.
     */
    HttpURLConnection getHttpURLConnection(String dhlEndpoint) throws IOException {
        URL urlConnector = new URL(dhlEndpoint);
        HttpURLConnection httpConnection = (HttpURLConnection) urlConnector.openConnection();
        httpConnection.setRequestMethod(ApplicationConstants.DhlHeaders.GET_REQUEST_METHOD);
        httpConnection.setRequestProperty(ApplicationConstants.DhlHeaders.ACCEPT, ACCEPT);
        httpConnection.setRequestProperty(ApplicationConstants.DhlHeaders.API_KEY, dhlAccessToken);
        return httpConnection;
    }
}
