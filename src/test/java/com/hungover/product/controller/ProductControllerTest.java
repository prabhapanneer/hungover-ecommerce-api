package com.hungover.product.controller;

import com.hungover.common.apiresponse.ApiListResponse;
import com.hungover.common.apiresponse.SingleDataResponse;
import com.hungover.common.constant.ApplicationConstants;
import com.hungover.core.dto.order.VariantDetailsDto;
import com.hungover.core.dto.product.ProductDataResponseDto;
import com.hungover.core.dto.product.ProductKeyValueDto;
import com.hungover.core.dto.product.TopProductsResponseDTO;
import com.hungover.product.service.ProductApiResponseService;
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
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductControllerTest {

    @InjectMocks
    ProductController productController;
    @Mock
    ProductApiResponseService productApiResponseService;
    @Mock
    MessageSource messageSource;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Controller Test for Get All Products Endpoint")
    void getAllProducts() throws IOException {
        ApiListResponse apiListResponse = new ApiListResponse();
        List<ProductKeyValueDto> productKeyValueDtoList = new ArrayList<>();

        apiListResponse.setResponse(ApplicationConstants.Status.SUCCESS, "Products fetched successfully", productKeyValueDtoList);
        ResponseEntity<ApiListResponse> expectedApiListResponseResponseEntity = new ResponseEntity<>(apiListResponse, HttpStatus.OK);

        when(productApiResponseService.getAllProducts()).thenReturn(apiListResponse);

        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Products fetched successfully");

        ResponseEntity<ApiListResponse> actualApiListResponseResponseEntity = productController.getAllProducts();

        verify(productApiResponseService).getAllProducts();
        Assertions.assertEquals(expectedApiListResponseResponseEntity, actualApiListResponseResponseEntity);

    }

    @Test
    @DisplayName("Controller Test for Get Top Products Endpoint")
    void getTopProducts() {
        SingleDataResponse singleDataResponse = new SingleDataResponse();
        TopProductsResponseDTO topProductsResponseDTO = new TopProductsResponseDTO();

        singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS, "Top products fetched successfully", topProductsResponseDTO);
        ResponseEntity<SingleDataResponse> expectedSingleDataResponseResponseEntity = new ResponseEntity<>(singleDataResponse, HttpStatus.OK);

        when(productApiResponseService.getTopProducts()).thenReturn(singleDataResponse);

        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Top products fetched successfully");

        ResponseEntity<SingleDataResponse> actualSingleDataResponseResponseEntity = productController.getTopProducts();

        verify(productApiResponseService).getTopProducts();
        Assertions.assertEquals(expectedSingleDataResponseResponseEntity, actualSingleDataResponseResponseEntity);
    }

    @Test
    @DisplayName("Controller Test for Get Total Products Endpoint")
    void getTotalProducts() {
        SingleDataResponse singleDataResponse = new SingleDataResponse();
        ProductDataResponseDto totalProductsResponseDTO = new ProductDataResponseDto();

        singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS, "Total products fetched successfully", totalProductsResponseDTO);
        ResponseEntity<SingleDataResponse> expectedSingleDataResponseResponseEntity = new ResponseEntity<>(singleDataResponse, HttpStatus.OK);

        when(productApiResponseService.getTotalProducts(anyString(), anyString(), any())).thenReturn(singleDataResponse);

        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Total products fetched successfully");

        ResponseEntity<SingleDataResponse> actualSingleDataResponseResponseEntity = productController.getTotalProducts("2022-12-31", "2021-12-31", 240);

        verify(productApiResponseService).getTotalProducts(anyString(), anyString(), any());
        Assertions.assertEquals(expectedSingleDataResponseResponseEntity, actualSingleDataResponseResponseEntity);
    }

    @Test
    @DisplayName("Controller Test for get variant details by variant id")
    void testGetVariantDetailsByVariantId() throws IOException {
        SingleDataResponse singleDataResponse = new SingleDataResponse();
        VariantDetailsDto variantDetailsDto = new VariantDetailsDto();

        singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS, "Product variant details fetched successfully", variantDetailsDto);
        ResponseEntity<SingleDataResponse> expectedSingleDataResponseResponseEntity = new ResponseEntity<>(singleDataResponse, HttpStatus.OK);

        when(productApiResponseService.getVariantDetailsByVariantId(Long.valueOf("43457309901029"))).thenReturn(singleDataResponse);
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Product variant details fetched successfully");

        ResponseEntity<SingleDataResponse> actualSingleDataResponseResponseEntity = productController.getVariantDetailsByVariantId(Long.valueOf("43457309901029"));

        verify(productApiResponseService).getVariantDetailsByVariantId(any());
        Assertions.assertEquals(expectedSingleDataResponseResponseEntity, actualSingleDataResponseResponseEntity);
    }
}