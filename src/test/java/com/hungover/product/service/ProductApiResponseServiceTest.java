package com.hungover.product.service;

import com.hungover.common.apiresponse.ApiListResponse;
import com.hungover.common.apiresponse.SingleDataResponse;
import com.hungover.common.constant.ApplicationConstants;
import com.hungover.core.dto.order.VariantDetailsDto;
import com.hungover.core.dto.product.ProductDataDto;
import com.hungover.core.dto.product.ProductDataResponseDto;
import com.hungover.core.dto.product.ProductKeyValueDto;
import com.hungover.core.dto.product.TopProductKeyValueDto;
import com.hungover.core.dto.product.TopProductsResponseDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.springframework.context.MessageSource;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

class ProductApiResponseServiceTest {

    @InjectMocks
    ProductApiResponseService productApiResponseService;
    @Mock
    MessageSource messageSource;
    @Mock
    ProductService productService;
    @Mock
    private Logger productApiResponseServiceLogger;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        setPrivateField(productApiResponseService, "shopifyAccessToken", "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");
        setPrivateField(productApiResponseService, "shopifyProductEndPoint", "https://e4d27c.myshopify.com/admin/api/2022-07/products.json");
        setPrivateField(productApiResponseService, "shopifyGraphQueryEndPoint", "https://e4d27c.myshopify.com/admin/api/2023-07/graphql.json");
        setPrivateField(productApiResponseService, "shopifyVariantDetailsByVariantIdEndPoint", "https://e4d27c.myshopify.com/admin/api/2023-07/variants/");
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
    @DisplayName("Test Get All Products Success")
    void testGetAllProducts_Success() throws IOException, IOException {
        // Arrange
        List<ProductKeyValueDto> mockProductList = new ArrayList<>();
        mockProductList.add(new ProductKeyValueDto("Crew Neck Tee", "Paid", "inventory", "productType","Hungover",1));

        doNothing().when(productApiResponseServiceLogger).info(anyString());

        when(productService.getAllProductListFromShopify(anyString(), anyString())).thenReturn(mockProductList);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Products fetched successfully");

        // Act
        ApiListResponse apiListResponse = productApiResponseService.getAllProducts();

        // Assert
        Assertions.assertNotNull(apiListResponse);
        Assertions.assertEquals(ApplicationConstants.Status.SUCCESS, apiListResponse.getStatus().getSuccess());
        Assertions.assertEquals("Products fetched successfully", apiListResponse.getStatus().getMessage());
        Assertions.assertEquals(mockProductList, apiListResponse.getData());
        Assertions.assertEquals(mockProductList.size(), apiListResponse.getTotalResults());
    }

    @Test
    @DisplayName("Test Get All Products Fail")
    void testGetAllProducts_Fail() throws IOException {
        // Arrange
        List<ProductKeyValueDto> mockProductList = new ArrayList<>();

        when(productService.getAllProductListFromShopify(anyString(), anyString())).thenReturn(mockProductList);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Products details not able to fetch");

        // Act
        ApiListResponse apiListResponse = productApiResponseService.getAllProducts();

        // Assert
        Assertions.assertNotNull(apiListResponse);
        Assertions.assertEquals(ApplicationConstants.Status.FAIL, apiListResponse.getStatus().getSuccess());
        Assertions.assertEquals("Products details not able to fetch", apiListResponse.getStatus().getMessage());
        Assertions.assertNull(apiListResponse.getData());
    }

    @Test
    @DisplayName("Test Get Top Products Success")
    void testGetTopProducts_Success() {
        TopProductsResponseDTO topProductsResponseDTO = new TopProductsResponseDTO();
        List<List<TopProductKeyValueDto>> topProductsLists = new ArrayList<>();
        Map<String, Integer> productsMap = new HashMap<>();
        topProductsResponseDTO.setTopProducts(topProductsLists);
        topProductsResponseDTO.setProducts(productsMap);
        topProductsResponseDTO.setCount(3);

        doNothing().when(productApiResponseServiceLogger).info(anyString());

        when(productService.getTopProducts(anyString(), anyString(), anyString())).thenReturn(topProductsResponseDTO);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Top products fetched successfully");

        // Act
        SingleDataResponse singleDataResponse = productApiResponseService.getTopProducts();

        // Assert
        Assertions.assertNotNull(singleDataResponse);
        Assertions.assertEquals(ApplicationConstants.Status.SUCCESS, singleDataResponse.getStatus().getSuccess());
        Assertions.assertEquals("Top products fetched successfully", singleDataResponse.getStatus().getMessage());
        Assertions.assertEquals(topProductsResponseDTO, singleDataResponse.getData());
    }

    @Test
    @DisplayName("Test Get Top Products Fail")
    void testGetTopProducts_Fail() {
        when(productService.getTopProducts(anyString(), anyString(), anyString())).thenReturn(null);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Top products details not able to fetch");

        // Act
        SingleDataResponse singleDataResponse = productApiResponseService.getTopProducts();

        // Assert
        Assertions.assertNotNull(singleDataResponse);
        Assertions.assertEquals(ApplicationConstants.Status.FAIL, singleDataResponse.getStatus().getSuccess());
        Assertions.assertEquals("Top products details not able to fetch", singleDataResponse.getStatus().getMessage());
        Assertions.assertNull(singleDataResponse.getData());
    }

    @Test
    @DisplayName("Test Get Total Products Success")
    void testGetTotalProducts_Success() {
        ProductDataResponseDto productDataResponseDto = new ProductDataResponseDto();
        List<List<ProductDataDto>> totalProductsLists = new ArrayList<>();
        Map<String, Integer> productsMap = new HashMap<>();
        productDataResponseDto.setTotalProducts(totalProductsLists);
        productDataResponseDto.setProducts(productsMap);
        productDataResponseDto.setCount(3);
        productDataResponseDto.setPercentage(new BigDecimal(10));

        doNothing().when(productApiResponseServiceLogger).info(anyString());

        when(productService.getTotalProducts(anyString(), anyString(), any(), anyString(), anyString(), anyString())).thenReturn(productDataResponseDto);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Total products fetched successfully");

        // Act
        SingleDataResponse singleDataResponse = productApiResponseService.getTotalProducts("2022-12-31", "2021-12-31", 240);

        // Assert
        Assertions.assertNotNull(singleDataResponse);
        Assertions.assertEquals(ApplicationConstants.Status.SUCCESS, singleDataResponse.getStatus().getSuccess());
        Assertions.assertEquals("Total products fetched successfully", singleDataResponse.getStatus().getMessage());
        Assertions.assertEquals(productDataResponseDto, singleDataResponse.getData());
    }

    @Test
    @DisplayName("Test Get Total Products Fail")
    void testGetTotalProducts_Fail() {
        when(productService.getTotalProducts(anyString(), anyString(), any(), anyString(), anyString(), anyString())).thenReturn(null);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Total products details not able to fetch");

        // Act
        SingleDataResponse singleDataResponse = productApiResponseService.getTotalProducts("2022-12-31", "2021-12-31", 240);

        // Assert
        Assertions.assertNotNull(singleDataResponse);
        Assertions.assertEquals(ApplicationConstants.Status.FAIL, singleDataResponse.getStatus().getSuccess());
        Assertions.assertEquals("Total products details not able to fetch", singleDataResponse.getStatus().getMessage());
        Assertions.assertNull(singleDataResponse.getData());
    }

    @Test
    @DisplayName("Test Get variant details by variant id Success")
    void testGetVariantDetailsByVariantId() throws IOException {
        VariantDetailsDto variantDetailsDto = new VariantDetailsDto();
        doNothing().when(productApiResponseServiceLogger).info(anyString());
        when(productService.getVariantDetailsByVariantId(anyString(), any(), anyString()))
                .thenReturn(variantDetailsDto);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Product variant details fetched successfully");

        //Act
        SingleDataResponse singleDataResponse =
                productApiResponseService.getVariantDetailsByVariantId(Long.valueOf("43457309901029"));

        // Assert
        Assertions.assertNotNull(singleDataResponse);
        Assertions.assertEquals(ApplicationConstants.Status.SUCCESS, singleDataResponse.getStatus().getSuccess());
        Assertions.assertEquals("Product variant details fetched successfully",
                singleDataResponse.getStatus().getMessage());
    }
}