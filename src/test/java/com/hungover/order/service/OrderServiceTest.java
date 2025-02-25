package com.hungover.order.service;

import com.hungover.admin.repository.CustomerOrderStatusRepositoryI;
import com.hungover.common.constant.ApplicationConstants;
import com.hungover.core.domain.customer.CustomerMeasurement;
import com.hungover.core.domain.customer.CustomerOrderStatus;
import com.hungover.core.dto.customer.CustomerDto;
import com.hungover.core.dto.customer.CustomerMeasurementDto;
import com.hungover.core.dto.customer.CustomerOrderStatusDto;
import com.hungover.core.dto.order.*;
import com.hungover.customer.repository.CustomerMeasurementRepositoryI;
import com.hungover.customer.service.CustomerService;
import com.hungover.product.service.ProductService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    @InjectMocks
    OrderService orderService;
    @Mock
    ProductService productService;
    @Mock
    private Logger orderServiceLogger;
    @Mock
    private CustomerOrderStatusRepositoryI customerOrderStatusRepository;
    @Mock
    private CustomerMeasurementRepositoryI customerMeasurementRepositoryI;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private CustomerService customerService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should get today's order details")
    void testGetTodayOrder() throws Exception {
        // Arrange
        String yesterdayDate = "2023-08-01";
        String todayDate = "2023-08-02";

        String shopifyAccessToken = "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";
        String shopifyEndpoint = "https://e4d27c.myshopify.com/admin/api/2022-07/orders.json";

        doNothing().when(orderServiceLogger).info(anyString());

        // Create a mock order list for today
        List<Orders> mockOrderList = getOrdersList();

        OrderService orderServiceMock = mock(OrderService.class);
        when(orderServiceMock.getTodayOrderListFromShopify(shopifyAccessToken, yesterdayDate, todayDate,
                shopifyEndpoint)).thenReturn(mockOrderList);

        // Act
        OrderKVDto orderKVDto = orderService.getTodayOrder(shopifyAccessToken, yesterdayDate, todayDate,
                shopifyEndpoint);

        // Assert
        Assertions.assertNotNull(orderKVDto);
        Assertions.assertEquals(0, orderKVDto.getFulfill());
        Assertions.assertEquals(1, orderKVDto.getPending());
    }

    private static List<Orders> getOrdersList() {
        Orders orderObj1 = new Orders();
        orderObj1.setFulfillment_status(ApplicationConstants.Orders.ORDER_FULFILLED_STATUS);
        orderObj1.setFinancial_status(ApplicationConstants.Orders.ORDER_PAID_STATUS);

        Orders orderObj2 = new Orders();
        orderObj2.setFulfillment_status(ApplicationConstants.Orders.ORDER_FULFILLED_STATUS);
        orderObj2.setFinancial_status(ApplicationConstants.Orders.ORDER_PENDING_STATUS);

        List<Orders> mockOrderList = new ArrayList<>();
        mockOrderList.add(orderObj1);
        mockOrderList.add(orderObj2);
        return mockOrderList;
    }

    @Test
    @DisplayName("Should retrieve all order details from Shopify API")
    void testGetAllOrderListFromShopify() throws IOException {
        // Arrange
        String shopifyAccessToken = "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";
        String shopifyEndpoint = "https://e4d27c.myshopify.com/admin/api/2022-07/orders.json";
        String pageInfo = "page_info_value";
        String linkHeader = "<https://example.com/shopify/orders?page_info=" + pageInfo + ">; rel=\"next\"";
        String nextLink = "https://example.com/shopify/orders?page_info=new_page_info";

        doNothing().when(orderServiceLogger).info(anyString());

        HttpURLConnection httpConnectionMock = mock(HttpURLConnection.class);
        when(httpConnectionMock.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(httpConnectionMock.getHeaderField(ApplicationConstants.ShopifyApiHeaders.LINK)).thenReturn(linkHeader);

        InputStream inputStreamMock = getInputStream();
        when(httpConnectionMock.getInputStream()).thenReturn(inputStreamMock);

        orderService.getHttpURLConnection(shopifyEndpoint, shopifyAccessToken);
        when(productService.parseNextLink(linkHeader)).thenReturn(nextLink);
        when(productService.parseQueryParameters(nextLink)).thenReturn(Map.of(ApplicationConstants.ShopifyApiHeaders
                .PAGE_INFO, "new_page_info"));

        ProductService productServiceMock = mock(ProductService.class);
        when(productServiceMock.parseNextLink(linkHeader)).thenReturn(nextLink);
        when(productServiceMock.parseQueryParameters(nextLink)).thenReturn(Map.of(ApplicationConstants
                .ShopifyApiHeaders.PAGE_INFO, "new_page_info"));

        // Act
        List<OrderKeyValueDto> orderKeyValueDtoList = orderService.getAllOrderListFromShopify(shopifyEndpoint,
                shopifyAccessToken);

        // Assert
        Assertions.assertNotNull(orderKeyValueDtoList);
    }

    private static InputStream getInputStream() {
        String jsonContent = "{\n" +
                "    \"orders\": [\n" +
                "        {\n" +
                "            \"id\": 5275913289957,\n" +
                "            \"admin_graphql_api_id\": \"gid://shopify/Order/5275913289957\",\n" +
                "            \"app_id\": 580111,\n" +
                "            \"browser_ip\": \"49.206.128.134\",\n" +
                "            \"buyer_accepts_marketing\": false,\n" +
                "            \"cancel_reason\": null,\n" +
                "            \"cancelled_at\": null,\n" +
                "            \"cart_token\": \"4999f95d02b923c0e7d01b6ff032118a\",\n" +
                "            \"checkout_id\": 34642162942181,\n" +
                "            \"checkout_token\": \"7e9b574d36b521a0b02312a2f6e47d28\",\n" +
                "            \"client_details\": {\n" +
                "                \"accept_language\": \"en-IN\",\n" +
                "                \"browser_height\": null,\n" +
                "                \"browser_ip\": \"49.206.128.134\",\n" +
                "                \"browser_width\": null,\n" +
                "                \"session_hash\": null,\n" +
                "                \"user_agent\": \"Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36\"\n" +
                "            },\n" +
                "            \"closed_at\": null,\n" +
                "            \"confirmed\": true,\n" +
                "            \"contact_email\": \"savvy_jim@yahoo.co.in\",\n" +
                "            \"created_at\": \"2023-06-06T15:03:49+05:30\",\n" +
                "            \"currency\": \"INR\",\n" +
                "            \"current_subtotal_price\": \"1500.00\",\n" +
                "            \"current_subtotal_price_set\": {\n" +
                "                \"shop_money\": {\n" +
                "                    \"amount\": \"1500.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                },\n" +
                "                \"presentment_money\": {\n" +
                "                    \"amount\": \"1500.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"current_total_discounts\": \"0.00\",\n" +
                "            \"current_total_discounts_set\": {\n" +
                "                \"shop_money\": {\n" +
                "                    \"amount\": \"0.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                },\n" +
                "                \"presentment_money\": {\n" +
                "                    \"amount\": \"0.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"current_total_duties_set\": null,\n" +
                "            \"current_total_price\": \"1500.00\",\n" +
                "            \"current_total_price_set\": {\n" +
                "                \"shop_money\": {\n" +
                "                    \"amount\": \"1500.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                },\n" +
                "                \"presentment_money\": {\n" +
                "                    \"amount\": \"1500.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"current_total_tax\": \"0.00\",\n" +
                "            \"current_total_tax_set\": {\n" +
                "                \"shop_money\": {\n" +
                "                    \"amount\": \"0.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                },\n" +
                "                \"presentment_money\": {\n" +
                "                    \"amount\": \"0.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"customer_locale\": \"en-IN\",\n" +
                "            \"device_id\": null,\n" +
                "            \"discount_codes\": [],\n" +
                "            \"email\": \"johndoe@gmail.com\",\n" +
                "            \"estimated_taxes\": false,\n" +
                "            \"financial_status\": \"paid\",\n" +
                "            \"fulfillment_status\": null,\n" +
                "            \"gateway\": \"Razorpay Secure\",\n" +
                "            \"landing_site\": \"/\",\n" +
                "            \"landing_site_ref\": null,\n" +
                "            \"location_id\": null,\n" +
                "            \"merchant_of_record_app_id\": null,\n" +
                "            \"name\": \"#1139\",\n" +
                "            \"note\": \"✘ Orders Create : Customer : Invalid Template Match\",\n" +
                "            \"note_attributes\": [],\n" +
                "            \"number\": 139,\n" +
                "            \"order_number\": 1139,\n" +
                "            \"order_status_url\": \"https://hungover.in/65975484645/orders/" +
                "79c96f611dec1f870f5899ddecea2aef/authenticate?key=6908f8b073a4d102f1f1009f6e1c2266\",\n" +
                "            \"original_total_duties_set\": null,\n" +
                "            \"payment_gateway_names\": [\n" +
                "                \"Razorpay Secure\"\n" +
                "            ],\n" +
                "            \"phone\": null,\n" +
                "            \"presentment_currency\": \"INR\",\n" +
                "            \"processed_at\": \"2023-06-06T15:03:47+05:30\",\n" +
                "            \"processing_method\": \"offsite\",\n" +
                "            \"reference\": \"01013c56e76f66df87d120566e2b9546\",\n" +
                "            \"referring_site\": \"https://www.google.com/\",\n" +
                "            \"source_identifier\": \"01013c56e76f66df87d120566e2b9546\",\n" +
                "            \"source_name\": \"web\",\n" +
                "            \"source_url\": null,\n" +
                "            \"subtotal_price\": \"1500.00\",\n" +
                "            \"subtotal_price_set\": {\n" +
                "                \"shop_money\": {\n" +
                "                    \"amount\": \"1500.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                },\n" +
                "                \"presentment_money\": {\n" +
                "                    \"amount\": \"1500.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"tags\": \"\",\n" +
                "            \"tax_lines\": [],\n" +
                "            \"taxes_included\": true,\n" +
                "            \"test\": false,\n" +
                "            \"token\": \"79c96f611dec1f870f5899ddecea2aef\",\n" +
                "            \"total_discounts\": \"0.00\",\n" +
                "            \"total_discounts_set\": {\n" +
                "                \"shop_money\": {\n" +
                "                    \"amount\": \"0.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                },\n" +
                "                \"presentment_money\": {\n" +
                "                    \"amount\": \"0.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"total_line_items_price\": \"1500.00\",\n" +
                "            \"total_line_items_price_set\": {\n" +
                "                \"shop_money\": {\n" +
                "                    \"amount\": \"1500.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                },\n" +
                "                \"presentment_money\": {\n" +
                "                    \"amount\": \"1500.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"total_outstanding\": \"0.00\",\n" +
                "            \"total_price\": \"1500.00\",\n" +
                "            \"total_price_set\": {\n" +
                "                \"shop_money\": {\n" +
                "                    \"amount\": \"1500.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                },\n" +
                "                \"presentment_money\": {\n" +
                "                    \"amount\": \"1500.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"total_shipping_price_set\": {\n" +
                "                \"shop_money\": {\n" +
                "                    \"amount\": \"0.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                },\n" +
                "                \"presentment_money\": {\n" +
                "                    \"amount\": \"0.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"total_tax\": \"0.00\",\n" +
                "            \"total_tax_set\": {\n" +
                "                \"shop_money\": {\n" +
                "                    \"amount\": \"0.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                },\n" +
                "                \"presentment_money\": {\n" +
                "                    \"amount\": \"0.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"total_tip_received\": \"0.00\",\n" +
                "            \"total_weight\": 0,\n" +
                "            \"updated_at\": \"2023-06-06T15:03:56+05:30\",\n" +
                "            \"user_id\": null,\n" +
                "            \"billing_address\": {\n" +
                "                \"first_name\": \"john\",\n" +
                "                \"address1\": \"A201 , Vandana Tejyesh Apartment\",\n" +
                "                \"phone\": \"9916925325\",\n" +
                "                \"city\": \"Bangalore\",\n" +
                "                \"zip\": \"560087\",\n" +
                "                \"province\": \"Karnataka\",\n" +
                "                \"country\": \"India\",\n" +
                "                \"last_name\": \"George\",\n" +
                "                \"address2\": \"Panathur Main Road\",\n" +
                "                \"company\": null,\n" +
                "                \"latitude\": null,\n" +
                "                \"longitude\": null,\n" +
                "                \"name\": \"John Doe\",\n" +
                "                \"country_code\": \"IN\",\n" +
                "                \"province_code\": \"KA\"\n" +
                "            },\n" +
                "            \"customer\": {\n" +
                "                \"id\": 7009303429349,\n" +
                "                \"email\": \"johndoe@gmail.com\",\n" +
                "                \"accepts_marketing\": true,\n" +
                "                \"created_at\": \"2023-03-19T23:42:06+05:30\",\n" +
                "                \"updated_at\": \"2023-06-06T15:03:49+05:30\",\n" +
                "                \"first_name\": \"John\",\n" +
                "                \"last_name\": null,\n" +
                "                \"state\": \"enabled\",\n" +
                "                \"note\": null,\n" +
                "                \"verified_email\": true,\n" +
                "                \"multipass_identifier\": null,\n" +
                "                \"tax_exempt\": false,\n" +
                "                \"phone\": \"+919916925325\",\n" +
                "                \"email_marketing_consent\": {\n" +
                "                    \"state\": \"subscribed\",\n" +
                "                    \"opt_in_level\": \"single_opt_in\",\n" +
                "                    \"consent_updated_at\": \"2023-03-23T11:42:38+05:30\"\n" +
                "                },\n" +
                "                \"sms_marketing_consent\": {\n" +
                "                    \"state\": \"not_subscribed\",\n" +
                "                    \"opt_in_level\": \"single_opt_in\",\n" +
                "                    \"consent_updated_at\": null,\n" +
                "                    \"consent_collected_from\": \"OTHER\"\n" +
                "                },\n" +
                "                \"tags\": \"\",\n" +
                "                \"currency\": \"INR\",\n" +
                "                \"accepts_marketing_updated_at\": \"2023-03-23T11:42:38+05:30\",\n" +
                "                \"marketing_opt_in_level\": \"single_opt_in\",\n" +
                "                \"tax_exemptions\": [],\n" +
                "                \"admin_graphql_api_id\": \"gid://shopify/Customer/7009303429349\",\n" +
                "                \"default_address\": {\n" +
                "                    \"id\": 8819787071717,\n" +
                "                    \"customer_id\": 7009303429349,\n" +
                "                    \"first_name\": \"John\",\n" +
                "                    \"last_name\": \"Doe\",\n" +
                "                    \"company\": null,\n" +
                "                    \"address1\": \"A201 , Vandana Tejyesh Apartment\",\n" +
                "                    \"address2\": \"Panathur Main Road\",\n" +
                "                    \"city\": \"Bangalore\",\n" +
                "                    \"province\": \"Karnataka\",\n" +
                "                    \"country\": \"India\",\n" +
                "                    \"zip\": \"560087\",\n" +
                "                    \"phone\": \"9916925325\",\n" +
                "                    \"name\": \"Jim George\",\n" +
                "                    \"province_code\": \"KA\",\n" +
                "                    \"country_code\": \"IN\",\n" +
                "                    \"country_name\": \"India\",\n" +
                "                    \"default\": true\n" +
                "                }\n" +
                "            },\n" +
                "            \"discount_applications\": [],\n" +
                "            \"fulfillments\": [],\n" +
                "            \"line_items\": [\n" +
                "                {\n" +
                "                    \"id\": 13181004742885,\n" +
                "                    \"admin_graphql_api_id\": \"gid://shopify/LineItem/13181004742885\",\n" +
                "                    \"fulfillable_quantity\": 1,\n" +
                "                    \"fulfillment_service\": \"manual\",\n" +
                "                    \"fulfillment_status\": null,\n" +
                "                    \"gift_card\": false,\n" +
                "                    \"grams\": 0,\n" +
                "                    \"name\": \"Crew Neck Tee - No Pocket / Short Sleeve / Heather Grey\",\n" +
                "                    \"price\": \"1500.00\",\n" +
                "                    \"price_set\": {\n" +
                "                        \"shop_money\": {\n" +
                "                            \"amount\": \"1500.00\",\n" +
                "                            \"currency_code\": \"INR\"\n" +
                "                        },\n" +
                "                        \"presentment_money\": {\n" +
                "                            \"amount\": \"1500.00\",\n" +
                "                            \"currency_code\": \"INR\"\n" +
                "                        }\n" +
                "                    },\n" +
                "                    \"product_exists\": true,\n" +
                "                    \"product_id\": 8017100177637,\n" +
                "                    \"properties\": [\n" +
                "                        {\n" +
                "                            \"name\": \"Size Name\",\n" +
                "                            \"value\": \"Jim\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"name\": \"_Shoulder Width\",\n" +
                "                            \"value\": \"47 cm\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"name\": \"_1/2 Chest Width\",\n" +
                "                            \"value\": \"57.5 cm\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"name\": \"_1/2 Bottom Width\",\n" +
                "                            \"value\": \"55 cm\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"name\": \"_Neck Width\",\n" +
                "                            \"value\": \"19 cm\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"name\": \"_Front Neck Drop\",\n" +
                "                            \"value\": \"9 cm\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"name\": \"_CB Length\",\n" +
                "                            \"value\": \"72 cm\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"name\": \"_Sleeve Length\",\n" +
                "                            \"value\": \"23.5 cm\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"name\": \"_Sleeve Opening\",\n" +
                "                            \"value\": \"17 cm\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"name\": \"_Armhole Straight\",\n" +
                "                            \"value\": \"26 cm\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"name\": \"_Initials\",\n" +
                "                            \"value\": \"\"\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"quantity\": 1,\n" +
                "                    \"requires_shipping\": true,\n" +
                "                    \"sku\": null,\n" +
                "                    \"taxable\": false,\n" +
                "                    \"title\": \"Crew Neck Tee\",\n" +
                "                    \"total_discount\": \"0.00\",\n" +
                "                    \"total_discount_set\": {\n" +
                "                        \"shop_money\": {\n" +
                "                            \"amount\": \"0.00\",\n" +
                "                            \"currency_code\": \"INR\"\n" +
                "                        },\n" +
                "                        \"presentment_money\": {\n" +
                "                            \"amount\": \"0.00\",\n" +
                "                            \"currency_code\": \"INR\"\n" +
                "                        }\n" +
                "                    },\n" +
                "                    \"variant_id\": 43457384579301,\n" +
                "                    \"variant_inventory_management\": \"shopify\",\n" +
                "                    \"variant_title\": \"No Pocket / Short Sleeve / Heather Grey\",\n" +
                "                    \"vendor\": \"Hungover\",\n" +
                "                    \"tax_lines\": [],\n" +
                "                    \"duties\": [],\n" +
                "                    \"discount_allocations\": []\n" +
                "                }\n" +
                "            ],\n" +
                "            \"payment_terms\": null,\n" +
                "            \"refunds\": [],\n" +
                "            \"shipping_address\": {\n" +
                "                \"first_name\": \"Jim\",\n" +
                "                \"address1\": \"A201 , Vandana Tejyesh Apartment\",\n" +
                "                \"phone\": \"9916925325\",\n" +
                "                \"city\": \"Bangalore\",\n" +
                "                \"zip\": \"560087\",\n" +
                "                \"province\": \"Karnataka\",\n" +
                "                \"country\": \"India\",\n" +
                "                \"last_name\": \"George\",\n" +
                "                \"address2\": \"Panathur Main Road\",\n" +
                "                \"company\": null,\n" +
                "                \"latitude\": null,\n" +
                "                \"longitude\": null,\n" +
                "                \"name\": \"Jim George\",\n" +
                "                \"country_code\": \"IN\",\n" +
                "                \"province_code\": \"KA\"\n" +
                "            },\n" +
                "            \"shipping_lines\": [\n" +
                "                {\n" +
                "                    \"id\": 4316004712677,\n" +
                "                    \"carrier_identifier\": \"650f1a14fa979ec5c74d063e968411d4\",\n" +
                "                    \"code\": \"Standard\",\n" +
                "                    \"delivery_category\": null,\n" +
                "                    \"discounted_price\": \"0.00\",\n" +
                "                    \"discounted_price_set\": {\n" +
                "                        \"shop_money\": {\n" +
                "                            \"amount\": \"0.00\",\n" +
                "                            \"currency_code\": \"INR\"\n" +
                "                        },\n" +
                "                        \"presentment_money\": {\n" +
                "                            \"amount\": \"0.00\",\n" +
                "                            \"currency_code\": \"INR\"\n" +
                "                        }\n" +
                "                    },\n" +
                "                    \"phone\": null,\n" +
                "                    \"price\": \"0.00\",\n" +
                "                    \"price_set\": {\n" +
                "                        \"shop_money\": {\n" +
                "                            \"amount\": \"0.00\",\n" +
                "                            \"currency_code\": \"INR\"\n" +
                "                        },\n" +
                "                        \"presentment_money\": {\n" +
                "                            \"amount\": \"0.00\",\n" +
                "                            \"currency_code\": \"INR\"\n" +
                "                        }\n" +
                "                    },\n" +
                "                    \"requested_fulfillment_service_id\": null,\n" +
                "                    \"source\": \"shopify\",\n" +
                "                    \"title\": \"Standard\",\n" +
                "                    \"tax_lines\": [],\n" +
                "                    \"discount_allocations\": []\n" +
                "                }\n" +
                "            ]\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": 5274489323749,\n" +
                "            \"admin_graphql_api_id\": \"gid://shopify/Order/5274489323749\",\n" +
                "            \"app_id\": 580111,\n" +
                "            \"browser_ip\": \"49.205.240.140\",\n" +
                "            \"buyer_accepts_marketing\": false,\n" +
                "            \"cancel_reason\": null,\n" +
                "            \"cancelled_at\": null,\n" +
                "            \"cart_token\": \"a960fb351ff25a92311cf39451cba051\",\n" +
                "            \"checkout_id\": 34639426519269,\n" +
                "            \"checkout_token\": \"2546cf73ba32fe571a299e5379466a3d\",\n" +
                "            \"client_details\": {\n" +
                "                \"accept_language\": \"en-IN\",\n" +
                "                \"browser_height\": null,\n" +
                "                \"browser_ip\": \"49.205.240.140\",\n" +
                "                \"browser_width\": null,\n" +
                "                \"session_hash\": null,\n" +
                "                \"user_agent\": \"Mozilla/5.0 (iPhone; CPU iPhone OS 16_5 like Mac OS X) " +
                "AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.5 Mobile/15E148 Safari/604.1\"\n" +
                "            },\n" +
                "            \"closed_at\": null,\n" +
                "            \"confirmed\": true,\n" +
                "            \"contact_email\": \"spatwari@gmail.com\",\n" +
                "            \"created_at\": \"2023-06-04T23:38:23+05:30\",\n" +
                "            \"currency\": \"INR\",\n" +
                "            \"current_subtotal_price\": \"3000.00\",\n" +
                "            \"current_subtotal_price_set\": {\n" +
                "                \"shop_money\": {\n" +
                "                    \"amount\": \"3000.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                },\n" +
                "                \"presentment_money\": {\n" +
                "                    \"amount\": \"3000.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"current_total_discounts\": \"0.00\",\n" +
                "            \"current_total_discounts_set\": {\n" +
                "                \"shop_money\": {\n" +
                "                    \"amount\": \"0.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                },\n" +
                "                \"presentment_money\": {\n" +
                "                    \"amount\": \"0.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"current_total_duties_set\": null,\n" +
                "            \"current_total_price\": \"3000.00\",\n" +
                "            \"current_total_price_set\": {\n" +
                "                \"shop_money\": {\n" +
                "                    \"amount\": \"3000.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                },\n" +
                "                \"presentment_money\": {\n" +
                "                    \"amount\": \"3000.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"current_total_tax\": \"0.00\",\n" +
                "            \"current_total_tax_set\": {\n" +
                "                \"shop_money\": {\n" +
                "                    \"amount\": \"0.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                },\n" +
                "                \"presentment_money\": {\n" +
                "                    \"amount\": \"0.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"customer_locale\": \"en\",\n" +
                "            \"device_id\": null,\n" +
                "            \"discount_codes\": [],\n" +
                "            \"email\": \"spatwari@gmail.com\",\n" +
                "            \"estimated_taxes\": false,\n" +
                "            \"financial_status\": \"paid\",\n" +
                "            \"fulfillment_status\": null,\n" +
                "            \"gateway\": \"Razorpay Secure\",\n" +
                "            \"landing_site\": \"/\",\n" +
                "            \"landing_site_ref\": null,\n" +
                "            \"location_id\": null,\n" +
                "            \"merchant_of_record_app_id\": null,\n" +
                "            \"name\": \"#1138\",\n" +
                "            \"note\": \"✘ Orders Create : Customer : Invalid Template Match\",\n" +
                "            \"note_attributes\": [],\n" +
                "            \"number\": 138,\n" +
                "            \"order_number\": 1138,\n" +
                "            \"order_status_url\": \"https://hungover.in/65975484645/orders/" +
                "08eb5a9c3be9bfa92c56c01e44450d3d/authenticate?key=7adf22694abf6205050244622e0ceb72\",\n" +
                "            \"original_total_duties_set\": null,\n" +
                "            \"payment_gateway_names\": [\n" +
                "                \"Razorpay Secure\"\n" +
                "            ],\n" +
                "            \"phone\": null,\n" +
                "            \"presentment_currency\": \"INR\",\n" +
                "            \"processed_at\": \"2023-06-04T23:38:22+05:30\",\n" +
                "            \"processing_method\": \"offsite\",\n" +
                "            \"reference\": \"853e280ee63454bacdb3a6b4075e57d3\",\n" +
                "            \"referring_site\": \"https://www.google.co.in/\",\n" +
                "            \"source_identifier\": \"853e280ee63454bacdb3a6b4075e57d3\",\n" +
                "            \"source_name\": \"web\",\n" +
                "            \"source_url\": null,\n" +
                "            \"subtotal_price\": \"3000.00\",\n" +
                "            \"subtotal_price_set\": {\n" +
                "                \"shop_money\": {\n" +
                "                    \"amount\": \"3000.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                },\n" +
                "                \"presentment_money\": {\n" +
                "                    \"amount\": \"3000.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"tags\": \"\",\n" +
                "            \"tax_lines\": [],\n" +
                "            \"taxes_included\": true,\n" +
                "            \"test\": false,\n" +
                "            \"token\": \"08eb5a9c3be9bfa92c56c01e44450d3d\",\n" +
                "            \"total_discounts\": \"0.00\",\n" +
                "            \"total_discounts_set\": {\n" +
                "                \"shop_money\": {\n" +
                "                    \"amount\": \"0.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                },\n" +
                "                \"presentment_money\": {\n" +
                "                    \"amount\": \"0.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"total_line_items_price\": \"3000.00\",\n" +
                "            \"total_line_items_price_set\": {\n" +
                "                \"shop_money\": {\n" +
                "                    \"amount\": \"3000.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                },\n" +
                "                \"presentment_money\": {\n" +
                "                    \"amount\": \"3000.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"total_outstanding\": \"0.00\",\n" +
                "            \"total_price\": \"3000.00\",\n" +
                "            \"total_price_set\": {\n" +
                "                \"shop_money\": {\n" +
                "                    \"amount\": \"3000.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                },\n" +
                "                \"presentment_money\": {\n" +
                "                    \"amount\": \"3000.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"total_shipping_price_set\": {\n" +
                "                \"shop_money\": {\n" +
                "                    \"amount\": \"0.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                },\n" +
                "                \"presentment_money\": {\n" +
                "                    \"amount\": \"0.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"total_tax\": \"0.00\",\n" +
                "            \"total_tax_set\": {\n" +
                "                \"shop_money\": {\n" +
                "                    \"amount\": \"0.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                },\n" +
                "                \"presentment_money\": {\n" +
                "                    \"amount\": \"0.00\",\n" +
                "                    \"currency_code\": \"INR\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"total_tip_received\": \"0.00\",\n" +
                "            \"total_weight\": 0,\n" +
                "            \"updated_at\": \"2023-06-04T23:38:30+05:30\",\n" +
                "            \"user_id\": null,\n" +
                "            \"billing_address\": {\n" +
                "                \"first_name\": \"Sanjay\",\n" +
                "                \"address1\": \"151A MLA Colony Lane no 11 Road no 12 Banjara Hills\",\n" +
                "                \"phone\": \"9848072000\",\n" +
                "                \"city\": \"Hyderabad\",\n" +
                "                \"zip\": \"500034\",\n" +
                "                \"province\": \"Telangana\",\n" +
                "                \"country\": \"India\",\n" +
                "                \"last_name\": \"Patwari\",\n" +
                "                \"address2\": null,\n" +
                "                \"company\": null,\n" +
                "                \"latitude\": 17.4119347,\n" +
                "                \"longitude\": 78.4337122,\n" +
                "                \"name\": \"Sanjay Patwari\",\n" +
                "                \"country_code\": \"IN\",\n" +
                "                \"province_code\": \"TS\"\n" +
                "            },\n" +
                "            \"customer\": {\n" +
                "                \"id\": 6683481669861,\n" +
                "                \"email\": \"spatwari@gmail.com\",\n" +
                "                \"accepts_marketing\": true,\n" +
                "                \"created_at\": \"2022-09-24T18:09:31+05:30\",\n" +
                "                \"updated_at\": \"2023-06-04T23:38:23+05:30\",\n" +
                "                \"first_name\": \"Sanjay Patwari\",\n" +
                "                \"last_name\": null,\n" +
                "                \"state\": \"enabled\",\n" +
                "                \"note\": null,\n" +
                "                \"verified_email\": true,\n" +
                "                \"multipass_identifier\": null,\n" +
                "                \"tax_exempt\": false,\n" +
                "                \"phone\": \"+919848072000\",\n" +
                "                \"email_marketing_consent\": {\n" +
                "                    \"state\": \"subscribed\",\n" +
                "                    \"opt_in_level\": \"single_opt_in\",\n" +
                "                    \"consent_updated_at\": \"2023-03-01T14:21:02+05:30\"\n" +
                "                },\n" +
                "                \"sms_marketing_consent\": {\n" +
                "                    \"state\": \"not_subscribed\",\n" +
                "                    \"opt_in_level\": \"single_opt_in\",\n" +
                "                    \"consent_updated_at\": null,\n" +
                "                    \"consent_collected_from\": \"OTHER\"\n" +
                "                },\n" +
                "                \"tags\": \"\",\n" +
                "                \"currency\": \"INR\",\n" +
                "                \"accepts_marketing_updated_at\": \"2023-03-01T14:21:02+05:30\",\n" +
                "                \"marketing_opt_in_level\": \"single_opt_in\",\n" +
                "                \"tax_exemptions\": [],\n" +
                "                \"admin_graphql_api_id\": \"gid://shopify/Customer/6683481669861\",\n" +
                "                \"default_address\": {\n" +
                "                    \"id\": 8940307939557,\n" +
                "                    \"customer_id\": 6683481669861,\n" +
                "                    \"first_name\": \"Sanjay\",\n" +
                "                    \"last_name\": \"Patwari\",\n" +
                "                    \"company\": null,\n" +
                "                    \"address1\": \"Myscape Courtyard , Financial Dist, Nanakramguda\",\n" +
                "                    \"address2\": \"Villa21A\",\n" +
                "                    \"city\": \"Hyderabad\",\n" +
                "                    \"province\": \"Telangana\",\n" +
                "                    \"country\": \"India\",\n" +
                "                    \"zip\": \"500032\",\n" +
                "                    \"phone\": \"99089 43000\",\n" +
                "                    \"name\": \"Sanjay Patwari\",\n" +
                "                    \"province_code\": \"TS\",\n" +
                "                    \"country_code\": \"IN\",\n" +
                "                    \"country_name\": \"India\",\n" +
                "                    \"default\": true\n" +
                "                }\n" +
                "            },\n" +
                "            \"discount_applications\": [],\n" +
                "            \"fulfillments\": [],\n" +
                "            \"line_items\": [\n" +
                "                {\n" +
                "                    \"id\": 13178041434341,\n" +
                "                    \"admin_graphql_api_id\": \"gid://shopify/LineItem/13178041434341\",\n" +
                "                    \"fulfillable_quantity\": 1,\n" +
                "                    \"fulfillment_service\": \"manual\",\n" +
                "                    \"fulfillment_status\": null,\n" +
                "                    \"gift_card\": false,\n" +
                "                    \"grams\": 0,\n" +
                "                    \"name\": \"Crew Neck Tee - No Pocket / Short Sleeve / Sky Blue\",\n" +
                "                    \"price\": \"1500.00\",\n" +
                "                    \"price_set\": {\n" +
                "                        \"shop_money\": {\n" +
                "                            \"amount\": \"1500.00\",\n" +
                "                            \"currency_code\": \"INR\"\n" +
                "                        },\n" +
                "                        \"presentment_money\": {\n" +
                "                            \"amount\": \"1500.00\",\n" +
                "                            \"currency_code\": \"INR\"\n" +
                "                        }\n" +
                "                    },\n" +
                "                    \"product_exists\": true,\n" +
                "                    \"product_id\": 8017100177637,\n" +
                "                    \"properties\": [\n" +
                "                        {\n" +
                "                            \"name\": \"Size Name\",\n" +
                "                            \"value\": \"SP two\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"name\": \"_Shoulder Width\",\n" +
                "                            \"value\": \"17.40 INCH\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"name\": \"_1/2 Chest Width\",\n" +
                "                            \"value\": \"20.40 INCH\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"name\": \"_1/2 Bottom Width\",\n" +
                "                            \"value\": \"20.40 INCH\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"name\": \"_Neck Width\",\n" +
                "                            \"value\": \"7.20 INCH\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"name\": \"_Front Neck Drop\",\n" +
                "                            \"value\": \"1.52 INCH\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"name\": \"_CB Length\",\n" +
                "                            \"value\": \"26.60 INCH\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"name\": \"_Sleeve Length\",\n" +
                "                            \"value\": \"8.80 INCH\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"name\": \"_Sleeve Opening\",\n" +
                "                            \"value\": \"6.40 INCH\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"name\": \"_Armhole Straight\",\n" +
                "                            \"value\": \"9.20 INCH\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"name\": \"_Initials\",\n" +
                "                            \"value\": \"S,P\"\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"quantity\": 1,\n" +
                "                    \"requires_shipping\": true,\n" +
                "                    \"sku\": null,\n" +
                "                    \"taxable\": false,\n" +
                "                    \"title\": \"Crew Neck Tee\",\n" +
                "                    \"total_discount\": \"0.00\",\n" +
                "                    \"total_discount_set\": {\n" +
                "                        \"shop_money\": {\n" +
                "                            \"amount\": \"0.00\",\n" +
                "                            \"currency_code\": \"INR\"\n" +
                "                        },\n" +
                "                        \"presentment_money\": {\n" +
                "                            \"amount\": \"0.00\",\n" +
                "                            \"currency_code\": \"INR\"\n" +
                "                        }\n" +
                "                    },\n" +
                "                    \"variant_id\": 44065016185061,\n" +
                "                    \"variant_inventory_management\": \"shopify\",\n" +
                "                    \"variant_title\": \"No Pocket / Short Sleeve / Sky Blue\",\n" +
                "                    \"vendor\": \"Hungover\",\n" +
                "                    \"tax_lines\": [],\n" +
                "                    \"duties\": [],\n" +
                "                    \"discount_allocations\": []\n" +
                "                },\n" +
                "                {\n" +
                "                    \"id\": 13178041467109,\n" +
                "                    \"admin_graphql_api_id\": \"gid://shopify/LineItem/13178041467109\",\n" +
                "                    \"fulfillable_quantity\": 1,\n" +
                "                    \"fulfillment_service\": \"manual\",\n" +
                "                    \"fulfillment_status\": null,\n" +
                "                    \"gift_card\": false,\n" +
                "                    \"grams\": 0,\n" +
                "                    \"name\": \"Crew Neck Tee - No Pocket / Short Sleeve / White\",\n" +
                "                    \"price\": \"1500.00\",\n" +
                "                    \"price_set\": {\n" +
                "                        \"shop_money\": {\n" +
                "                            \"amount\": \"1500.00\",\n" +
                "                            \"currency_code\": \"INR\"\n" +
                "                        },\n" +
                "                        \"presentment_money\": {\n" +
                "                            \"amount\": \"1500.00\",\n" +
                "                            \"currency_code\": \"INR\"\n" +
                "                        }\n" +
                "                    },\n" +
                "                    \"product_exists\": true,\n" +
                "                    \"product_id\": 8017100177637,\n" +
                "                    \"properties\": [\n" +
                "                        {\n" +
                "                            \"name\": \"Size Name\",\n" +
                "                            \"value\": \"SP two\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"name\": \"_Shoulder Width\",\n" +
                "                            \"value\": \"17.40 INCH\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"name\": \"_1/2 Chest Width\",\n" +
                "                            \"value\": \"20.40 INCH\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"name\": \"_1/2 Bottom Width\",\n" +
                "                            \"value\": \"20.40 INCH\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"name\": \"_Neck Width\",\n" +
                "                            \"value\": \"7.20 INCH\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"name\": \"_Front Neck Drop\",\n" +
                "                            \"value\": \"1.52 INCH\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"name\": \"_CB Length\",\n" +
                "                            \"value\": \"26.60 INCH\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"name\": \"_Sleeve Length\",\n" +
                "                            \"value\": \"8.80 INCH\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"name\": \"_Sleeve Opening\",\n" +
                "                            \"value\": \"6.40 INCH\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"name\": \"_Armhole Straight\",\n" +
                "                            \"value\": \"9.20 INCH\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"name\": \"_Initials\",\n" +
                "                            \"value\": \"S,P\"\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"quantity\": 1,\n" +
                "                    \"requires_shipping\": true,\n" +
                "                    \"sku\": null,\n" +
                "                    \"taxable\": false,\n" +
                "                    \"title\": \"Crew Neck Tee\",\n" +
                "                    \"total_discount\": \"0.00\",\n" +
                "                    \"total_discount_set\": {\n" +
                "                        \"shop_money\": {\n" +
                "                            \"amount\": \"0.00\",\n" +
                "                            \"currency_code\": \"INR\"\n" +
                "                        },\n" +
                "                        \"presentment_money\": {\n" +
                "                            \"amount\": \"0.00\",\n" +
                "                            \"currency_code\": \"INR\"\n" +
                "                        }\n" +
                "                    },\n" +
                "                    \"variant_id\": 44076666192101,\n" +
                "                    \"variant_inventory_management\": \"shopify\",\n" +
                "                    \"variant_title\": \"No Pocket / Short Sleeve / White\",\n" +
                "                    \"vendor\": \"Hungover\",\n" +
                "                    \"tax_lines\": [],\n" +
                "                    \"duties\": [],\n" +
                "                    \"discount_allocations\": []\n" +
                "                }\n" +
                "            ],\n" +
                "            \"payment_terms\": null,\n" +
                "            \"refunds\": [],\n" +
                "            \"shipping_address\": {\n" +
                "                \"first_name\": \"Sanjay\",\n" +
                "                \"address1\": \"Myscape Courtyard , Financial Dist, Nanakramguda\",\n" +
                "                \"phone\": \"99089 43000\",\n" +
                "                \"city\": \"Hyderabad\",\n" +
                "                \"zip\": \"500032\",\n" +
                "                \"province\": \"Telangana\",\n" +
                "                \"country\": \"India\",\n" +
                "                \"last_name\": \"Patwari\",\n" +
                "                \"address2\": \"Villa21A\",\n" +
                "                \"company\": null,\n" +
                "                \"latitude\": 17.4110569,\n" +
                "                \"longitude\": 78.3397736,\n" +
                "                \"name\": \"Sanjay Patwari\",\n" +
                "                \"country_code\": \"IN\",\n" +
                "                \"province_code\": \"TS\"\n" +
                "            },\n" +
                "            \"shipping_lines\": [\n" +
                "                {\n" +
                "                    \"id\": 4314886701285,\n" +
                "                    \"carrier_identifier\": \"650f1a14fa979ec5c74d063e968411d4\",\n" +
                "                    \"code\": \"Standard\",\n" +
                "                    \"delivery_category\": null,\n" +
                "                    \"discounted_price\": \"0.00\",\n" +
                "                    \"discounted_price_set\": {\n" +
                "                        \"shop_money\": {\n" +
                "                            \"amount\": \"0.00\",\n" +
                "                            \"currency_code\": \"INR\"\n" +
                "                        },\n" +
                "                        \"presentment_money\": {\n" +
                "                            \"amount\": \"0.00\",\n" +
                "                            \"currency_code\": \"INR\"\n" +
                "                        }\n" +
                "                    },\n" +
                "                    \"phone\": null,\n" +
                "                    \"price\": \"0.00\",\n" +
                "                    \"price_set\": {\n" +
                "                        \"shop_money\": {\n" +
                "                            \"amount\": \"0.00\",\n" +
                "                            \"currency_code\": \"INR\"\n" +
                "                        },\n" +
                "                        \"presentment_money\": {\n" +
                "                            \"amount\": \"0.00\",\n" +
                "                            \"currency_code\": \"INR\"\n" +
                "                        }\n" +
                "                    },\n" +
                "                    \"requested_fulfillment_service_id\": null,\n" +
                "                    \"source\": \"shopify\",\n" +
                "                    \"title\": \"Standard\",\n" +
                "                    \"tax_lines\": [],\n" +
                "                    \"discount_allocations\": []\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        return new ByteArrayInputStream(jsonContent.getBytes());
    }

    @Test
    @DisplayName("Should calculate yearly order details")
    void testGetYearlyOrderDetails() throws IOException {
        // Arrange
        String lastYear = "2022-01-01"; // Change to the desired last year
        String currentYear = "2023-01-01"; // Change to the desired current year

        String shopifyAccessToken = "your-access-token";
        String shopifyEndpoint = "https://example.myshopify.com/admin/api/2022-07/orders.json";

        Orders orderObj1 = new Orders();
        orderObj1.setName("John Doe");
        orderObj1.setEmail("johndoe@gmail.com");

        Orders orderObj2 = new Orders();
        orderObj2.setName("Jame Smith");
        orderObj2.setEmail("jamesmith@gmail.com");

        List<Orders> mockLastYearOrderList = new ArrayList<>();
        mockLastYearOrderList.add(orderObj1);
        mockLastYearOrderList.add(orderObj2);

        List<Orders> mockCurrentYearOrderList = new ArrayList<>();
        mockCurrentYearOrderList.add(orderObj1);
        mockCurrentYearOrderList.add(orderObj2);

        OrderService orderServiceSpy = spy(orderService);
        doReturn(mockLastYearOrderList).when(orderServiceSpy).getYearlyOrderDetailsFromShopify(shopifyEndpoint,
                lastYear, null, shopifyAccessToken);
        doReturn(mockCurrentYearOrderList).when(orderServiceSpy).getYearlyOrderDetailsFromShopify(shopifyEndpoint,
                currentYear, null, shopifyAccessToken);

        // Act
        YearlyOrderResponseDto yearlyOrderResponseDto = orderService.getYearlyOrderDetails(shopifyEndpoint, lastYear,
                currentYear, shopifyAccessToken);

        // Assert
        Assertions.assertNotNull(yearlyOrderResponseDto);
    }

    @Test
    @DisplayName("Test average order value calculation with orders")
    void testGetAverageValueOfOrders() throws IOException {
        // Arrange
        String fromDate = "2022-01-01";
        Integer noOfDays = 365;
        String shopifyAccessToken = "your-access-token";
        String shopifyEndpoint = "https://example.myshopify.com/admin/api/2022-07/orders.json";

        Orders orderObj1 = new Orders();
        orderObj1.setName("John Doe");
        orderObj1.setEmail("johndoe@gmail.com");

        Orders orderObj2 = new Orders();
        orderObj2.setName("Jame Smith");
        orderObj2.setEmail("jamesmith@gmail.com");

        List<Orders> mockOrderDetailsList = new ArrayList<>();
        mockOrderDetailsList.add(orderObj1);
        mockOrderDetailsList.add(orderObj2);

        OrderService orderServiceSpy = spy(orderService);
        doReturn(mockOrderDetailsList).when(orderServiceSpy).getYearlyOrderDetailsFromShopify(shopifyEndpoint,
                fromDate, null, shopifyAccessToken);

        // Act
        AverageOrderValueResponseDto averageOrderValueResponseDto = orderService
                .getAverageValueOfOrders(shopifyEndpoint, fromDate, null, shopifyAccessToken, noOfDays);

        // Assert
        Assertions.assertNotNull(averageOrderValueResponseDto);
    }

    @Test
    @Disabled
    @DisplayName("Test getOrderDetailsByOrderId")
    void testGetOrderDetailsByOrderId() throws ExecutionException, InterruptedException, IOException {
        String orderId = "5275913289957";
        String customerId = "7009303429349";
        String shopifyOrderDetailsByOrderIdEndPoint = "https://e4d27c.myshopify.com/admin/api/2022-07/orders/";
        String shopifyAccessToken = "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";
        String shopifyResetPasswordEndPoint = "https://e4d27c.myshopify.com/admin/api/2022-07/customers/";
        String shopifyGraphQueryEndPoint = "https://e4d27c.myshopify.com/admin/api/2023-07/graphql.json";
        String shopifyVariantDetailsByVariantIdEndPoint="https://e4d27c.myshopify.com/admin/api/2023-07/variants/";
        String shopifyProductImageByProductIdAndImageIdEndPoint="https://e4d27c.myshopify.com/admin/api/2023-07/products/%s/images/%s.json";

        Orders orders = new Orders();
        orders.setId(5275913289957L);
        orders.setName("John Doe");
        orders.setEmail("johndoe@gmail.com");

        CustomerDto customerDto = new CustomerDto();
        customerDto.setId(7009303429349L);
        customerDto.setFirst_name("Jame");
        customerDto.setLast_name("Smith");

        ConversionSummaryDto conversionSummaryDto = getConversionSummaryDto();

        List<CustomerOrderStatusDto> customerOrderStatusDtoList = new ArrayList<>();
        CustomerOrderStatusDto customerOrderStatusDto = new CustomerOrderStatusDto();
        customerOrderStatusDto.setCustomerOrderStatusId(1);
        customerOrderStatusDto.setOrderId("12344568763234");
        customerOrderStatusDto.setOrderStatus("StartProduction");
        customerOrderStatusDto.setIsFitSample(Boolean.TRUE);
        customerOrderStatusDtoList.add(customerOrderStatusDto);

        List<CustomerOrderStatus> savedCustomerOrderStatusList = new ArrayList<>();
        CustomerOrderStatus customerOrderStatus = new CustomerOrderStatus();
        customerOrderStatus.setCustomerOrderStatusId(1);
        customerOrderStatus.setOrderId("12344568763234");
        customerOrderStatus.setOrderStatus("StartProduction");
        customerOrderStatus.setIsFitSample(Boolean.TRUE);
        savedCustomerOrderStatusList.add(customerOrderStatus);

        CustomerMeasurementDto customerMeasurementDtoObj = new CustomerMeasurementDto();
        customerMeasurementDtoObj.setCustomerId("1");
        customerMeasurementDtoObj.setCustomerMeasurementId(1);
        customerMeasurementDtoObj.setCustomerEmail("email");
        customerMeasurementDtoObj.setCustomerName("mame");
        customerMeasurementDtoObj.setAdminName("name");
        customerMeasurementDtoObj.setName("anme");
        customerMeasurementDtoObj.setType("type");

        CustomerMeasurement customerMeasurement = new CustomerMeasurement();
        customerMeasurement.setCustomerId("1");
        customerMeasurement.setCustomerMeasurementId(1);
        customerMeasurement.setCustomerEmail("email");
        customerMeasurement.setName("anme");
        customerMeasurement.setType("type");

        OrderService orderServiceSpy = spy(orderService);

        CompletableFuture<Orders> getOrderDetailsFuture = CompletableFuture.completedFuture(orders);
        doReturn(getOrderDetailsFuture.get()).when(orderServiceSpy)
                .getOrderDetailsByOrderIdFromShopify(shopifyOrderDetailsByOrderIdEndPoint, orderId, shopifyAccessToken);

        CompletableFuture<CustomerDto> getTotalOrderCountFuture = CompletableFuture.completedFuture(customerDto);
        doReturn(getTotalOrderCountFuture.get()).when(orderServiceSpy)
                .getTotalOrderCountByCustomerIdFromShopify(shopifyResetPasswordEndPoint, customerId, shopifyAccessToken);

        CompletableFuture<ConversionSummaryDto> getConversionSummaryFuture = CompletableFuture
                .completedFuture(conversionSummaryDto);
        doReturn(getConversionSummaryFuture.get()).when(orderServiceSpy)
                .getConversionSummary(shopifyGraphQueryEndPoint, orderId, shopifyAccessToken);

        CompletableFuture<List<CustomerOrderStatusDto>> getCustomerOrderStatusListCompletableFuture = CompletableFuture
                .completedFuture(customerOrderStatusDtoList);
        when(customerOrderStatusRepository.findByOrderId(orderId)).thenReturn(savedCustomerOrderStatusList);
        when(modelMapper.map(any(CustomerOrderStatus.class), eq(CustomerOrderStatusDto.class)))
                .thenReturn(customerOrderStatusDtoList.get(0));
        Orders ordersObj = new Orders();
        doReturn(getCustomerOrderStatusListCompletableFuture.get()).when(orderServiceSpy)
                .getCustomerOrderStatusByOrderId(orderId,ordersObj);

        CompletableFuture<CustomerMeasurementDto> getCustomerMeasurementDtoCompletableFuture = CompletableFuture
                .completedFuture(customerMeasurementDtoObj);
        when(customerMeasurementRepositoryI.findByCustomerIdAndName(anyString(),anyString()))
                .thenReturn(customerMeasurement);
        when(modelMapper.map(any(CustomerMeasurement.class),eq(CustomerMeasurementDto.class)))
                .thenReturn(customerMeasurementDtoObj);
        CustomerService customerServiceMock = spy(customerService);
        doReturn(getCustomerMeasurementDtoCompletableFuture.get()).when(customerServiceMock)
                .getCustomerMeasurementByCustomerMeasurementId("12","name");

        // Act
        OrderDto orderDto = orderService.getOrderDetailsByOrderId(shopifyOrderDetailsByOrderIdEndPoint, orderId,
                shopifyAccessToken, shopifyResetPasswordEndPoint, shopifyGraphQueryEndPoint,
                shopifyVariantDetailsByVariantIdEndPoint, shopifyProductImageByProductIdAndImageIdEndPoint);

        // Assert
        Assertions.assertNotNull(orderDto);
        Assertions.assertEquals(5, orderDto.getTotalOrdersCount());
        Assertions.assertEquals(conversionSummaryDto, orderDto.getConversionSummary());
    }

    private static ConversionSummaryDto getConversionSummaryDto() {
        ConversionSummaryDto.FirstVisit firstVisit = new ConversionSummaryDto().getFirstVisit();
        firstVisit.setId("gid://shopify/CustomerVisit/12228152525029");
        firstVisit.setOccurredAt("2023-05-22T11:33:08Z");
        firstVisit.setLandingPage("https://hungover.in/");
        firstVisit.setReferralInfoHtml("Store visit was direct");
        firstVisit.setSourceDescription("1st session was direct to your store");
        firstVisit.set__typename("CustomerVisit");
        firstVisit.setReferralCode("");

        return new ConversionSummaryDto(5, 15, firstVisit, 7,
                true, "CustomerJourneySummary");
    }

    @Test
    @DisplayName("Test getFilterOrderDetails - Valid Data")
    void testGetFilterOrderDetails_ValidData() throws IOException {
        String fromDate = "2023-08-01";
        String toDate = "2023-08-31";
        Integer noOfDays = 31;
        String shopifyAccessToken = "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";
        String shopifyEndpoint="https://e4d27c.myshopify.com/admin/api/2022-07/orders.json";

        // Prepare mock data for filteredOrderDetailsList
        List<Orders> mockFilteredOrderDetailsList = new ArrayList<>();

        OrderService orderServiceSpy = spy(orderService);
        doReturn(mockFilteredOrderDetailsList).when(orderServiceSpy).getFilterOrderListFromShopify(shopifyEndpoint,
                fromDate, toDate, shopifyAccessToken);

        // Call the method under test
        YearlyOrderResponseDto yearlyOrderResponseDto = orderService.getFilterOrderDetails(shopifyEndpoint, fromDate,
                toDate, shopifyAccessToken, noOfDays);

        // Assert
        Assertions.assertNotNull(yearlyOrderResponseDto);
    }
}