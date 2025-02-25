package com.hungover.scheduler.service;

import com.hungover.admin.repository.CustomerOrderStatusRepositoryI;
import com.hungover.common.domain.DomainObject;
import com.hungover.core.domain.customer.CustomerOrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;


class OrderTrackingServiceTest {

    @InjectMocks
    OrderTrackingService orderTrackingService;
    @Mock
    private CustomerOrderStatusRepositoryI customerOrderStatusRepository;

    @Value("${dhlAccessToken}")
    String dhlAccessToken;
    @Value("${dhlOrderTrackingStatusEndPoint}")
    String dhlOrderTrackingStatusEndPoint;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        setPrivateField(orderTrackingService, "dhlAccessToken", "demo-key");
        setPrivateField(orderTrackingService, "dhlOrderTrackingStatusEndPoint", "https://api-test.dhl.com/track/shipments?trackingNumber=");
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
    void testOrderTrackingDetails() throws IOException {
        // Prepare mock data and dependencies
        List<CustomerOrderStatus> customerOrderStatusList = createMockCustomerOrderStatusList();
        HttpURLConnection mockHttpConnection = mock(HttpURLConnection.class);
        InputStream mockInputStream = new ByteArrayInputStream(
                "{ \"shipments\": [ { \"status\": { \"status\": \"Shipped\" } } ] }".getBytes(StandardCharsets.UTF_8)
        );

        // Mock behaviors of your dependencies
        when(customerOrderStatusRepository.findByOrderStatusAndOrderTrackingNumberIsNotNull(DomainObject.CustomerOrderStatus.DISPATCHED))
                .thenReturn(customerOrderStatusList);
        when(mockHttpConnection.getInputStream()).thenReturn(mockInputStream);

        // Call the method under test
        orderTrackingService.orderTrackingDetails();

        verify(customerOrderStatusRepository).saveAll(customerOrderStatusList);
    }

    private List<CustomerOrderStatus> createMockCustomerOrderStatusList() {
        List<CustomerOrderStatus> mockCustomerOrderStatusList = new ArrayList<>();
        CustomerOrderStatus customerOrderStatus = new CustomerOrderStatus();
        customerOrderStatus.setCustomerOrderStatusId(87);
        customerOrderStatus.setOrderId("5400592744677");
        customerOrderStatus.setCustomerMeasurement(null);
        customerOrderStatus.setOrderStatus("Dispatch");
        customerOrderStatus.setOrderTrackingNumber("00340434292135100186");
        mockCustomerOrderStatusList.add(customerOrderStatus);
        return mockCustomerOrderStatusList;
    }
}