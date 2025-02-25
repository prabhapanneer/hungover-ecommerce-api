package com.hungover.product.service;


import com.hungover.common.constant.ApplicationConstants;
import com.hungover.core.dto.order.VariantDetailsDto;
import com.hungover.core.dto.product.ProductDataResponseDto;
import com.hungover.core.dto.product.ProductKeyValueDto;
import com.hungover.core.dto.product.TopProductsResponseDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductServiceTest {

    @InjectMocks
    ProductService productService;
    @Mock
    private Logger productServiceLogger;
    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllProductListFromShopify_Success() throws IOException {
        // Mock input parameters
        String shopifyAccessToken = "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";
        String shopifyProductEndPoint = "https://e4d27c.myshopify.com/admin/api/2022-07/products.json";
        String pageInfo = "page_info_value";
        String linkHeader = "<https://example.com/shopify/products?page_info=" + pageInfo + ">; rel=\"next\"";
        String nextLink = "https://example.com/shopify/products?page_info=new_page_info";

        doNothing().when(productServiceLogger).info(anyString());

        // Mocked data
        HttpURLConnection mockHttpConnection = mock(HttpURLConnection.class);
        when(mockHttpConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);

        InputStream inputStream = getInputStream();
        when(mockHttpConnection.getInputStream()).thenReturn(inputStream);

        int responseCode = HttpURLConnection.HTTP_OK;
        when(mockHttpConnection.getResponseCode()).thenReturn(responseCode);

        when(mockHttpConnection.getHeaderField(ApplicationConstants.ShopifyApiHeaders.LINK)).thenReturn(linkHeader);

        // Mock other methods
        ProductService productServiceMock = mock(ProductService.class);
        when(productServiceMock.getHttpURLConnection(pageInfo, shopifyAccessToken, shopifyProductEndPoint)).thenReturn(mockHttpConnection);
        when(productServiceMock.parseNextLink(linkHeader)).thenReturn(nextLink);

        // Perform the actual method call
        List<ProductKeyValueDto> productKeyValueDtoList = productService.getAllProductListFromShopify(shopifyAccessToken, shopifyProductEndPoint);

        // Assertions
        Assertions.assertNotNull(productKeyValueDtoList);
    }

    private static InputStream getInputStream() {
        String jsonContent = "{\n" +
                "    \"products\": [\n" +
                "        {\n" +
                "            \"id\": 8017100177637,\n" +
                "            \"title\": \"Crew Neck Tee\",\n" +
                "            \"vendor\": \"Hungover\",\n" +
                "            \"product_type\": \"\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": 8030154162405,\n" +
                "            \"title\": \"Henley\",\n" +
                "            \"vendor\": \"Hungover\",\n" +
                "            \"product_type\": \"\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": 8030383964389,\n" +
                "            \"title\": \"Hungover Gift Card\",\n" +
                "            \"vendor\": \"gift card\",\n" +
                "            \"product_type\": \"Gift Cards\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": 8017098834149,\n" +
                "            \"title\": \"Polo\",\n" +
                "            \"vendor\": \"Hungover\",\n" +
                "            \"product_type\": \"\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": 8017050566885,\n" +
                "            \"title\": \"V-Neck Tee\",\n" +
                "            \"vendor\": \"Hungover\",\n" +
                "            \"product_type\": \"\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        return new ByteArrayInputStream(jsonContent.getBytes());
    }

    @Test
    @DisplayName("Test Get Top Products")
    void testGetTopProducts() {
        // Mock the response entity from restTemplate.exchange
        String shopifyAccessToken = "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";
        String shopifyGraphQueryEndPoint = "https://e4d27c.myshopify.com/admin/api/2023-07/graphql.json";
        String shopifyProductEndPoint = "https://e4d27c.myshopify.com/admin/api/2022-07/products.json";

        String productsJsonResponseBodyStr = "{...your JSON response here...}";
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(ApplicationConstants.ShopifyApiHeaders.LINK, "your-link-here");
        ResponseEntity<String> stringResponseEntity = new ResponseEntity<>(productsJsonResponseBodyStr, httpHeaders, HttpStatus.OK);
        when(restTemplate.exchange(any(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
                .thenReturn(stringResponseEntity);

        // Call the method under test
        TopProductsResponseDTO topProductsResponseDTO = productService.getTopProducts(shopifyAccessToken, shopifyGraphQueryEndPoint, shopifyProductEndPoint);

        // Add assertions to verify the behavior and result
        Assertions.assertNotNull(topProductsResponseDTO);
        Assertions.assertNotNull(topProductsResponseDTO.getTopProducts());
        Assertions.assertNotNull(topProductsResponseDTO.getProducts());
        Assertions.assertTrue(topProductsResponseDTO.getCount() >= 0); // Assuming count is non-negative
    }

    @Test
    @DisplayName("Test getTotalProducts")
    void testGetTotalProducts_ValidInputs() {
        // Mocking RestTemplate response
        String shopifyAccessToken = "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";
        String shopifyGraphQueryEndPoint = "https://e4d27c.myshopify.com/admin/api/2023-07/graphql.json";
        String shopifyProductEndPoint = "https://e4d27c.myshopify.com/admin/api/2022-07/products.json";
        String productsJsonResponseBodyStr = "{...your JSON response here...}";
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(ApplicationConstants.ShopifyApiHeaders.LINK, "your-link-here");

        ResponseEntity<String> stringResponseEntity = new ResponseEntity<>(productsJsonResponseBodyStr, httpHeaders, HttpStatus.OK);
        when(restTemplate.exchange(eq(shopifyProductEndPoint), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(stringResponseEntity);

        // Call the method under test
        ProductDataResponseDto productDataResponseDto = productService.getTotalProducts("2022-12-31", "2021-12-31", 240, shopifyAccessToken, shopifyProductEndPoint, shopifyGraphQueryEndPoint);

        // Add assertions based on your expected behavior and response
        Assertions.assertNotNull(productDataResponseDto);
    }

    @Test
    @DisplayName("Test getTopProductByFilter Success")
    void testGetTopProductByFilter_Success() throws NoSuchMethodException {
        String shopifyAccessToken = "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";
        String shopifyGraphQueryEndPoint = "https://e4d27c.myshopify.com/admin/api/2023-07/graphql.json";
        String shopifyProductEndPoint = "https://e4d27c.myshopify.com/admin/api/2022-07/products.json";

        // Mock response from Shopify API
        String productJsonResponseBodyStr = "{ \"response_data\": { \"shopify_ql_query\": { \"table_data\": { \"row_data\": [] } } } }";
        ResponseEntity<String> stringResponseEntity = new ResponseEntity<>(productJsonResponseBodyStr, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(stringResponseEntity);

        Method privateMethod = ProductService.class.getDeclaredMethod("fetchAllProductsData", RestTemplate.class, HttpEntity.class,
                Map.class, Map.class, String.class);
        privateMethod.setAccessible(true);

        // Call the method to test
        TopProductsResponseDTO topProductsResponseDTO = productService.getTopProductByFilter("2023-01-01", "2023-12-31",
                shopifyAccessToken, shopifyGraphQueryEndPoint, shopifyProductEndPoint);

        // Assertions
        Assertions.assertNotNull(topProductsResponseDTO);
        Assertions.assertEquals(4, topProductsResponseDTO.getCount());
    }

    @Test
    @DisplayName("Test getVariantDetailsByVariantId Success")
    void testGetVariantDetailsByVariantId() throws IOException {
        String shopifyVariantDetailsByVariantIdEndPoint="https://e4d27c.myshopify.com/admin/api/2023-07/variants/";
        Long variantId = Long.valueOf("43457309901029");
        String shopifyAccessToken = "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";

        ProductService productServiceSpy = Mockito.spy(productService);

        VariantDetailsDto variantDetailsDto = new VariantDetailsDto();

        // Mock necessary dependencies (for example, HttpURLConnection)
        HttpURLConnection mockedHttpURLConnection = mock(HttpURLConnection.class);
        when(mockedHttpURLConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);

        doReturn(variantDetailsDto).when(productServiceSpy).getVariantDetailsByVariantId(
                shopifyVariantDetailsByVariantIdEndPoint, variantId, shopifyAccessToken);

        // Call the method to test
        VariantDetailsDto result = productService.getVariantDetailsByVariantId(
                shopifyVariantDetailsByVariantIdEndPoint, variantId, shopifyAccessToken);

        Assertions.assertNotNull(result);
    }
}