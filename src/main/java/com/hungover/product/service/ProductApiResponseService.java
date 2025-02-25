package com.hungover.product.service;

import com.hungover.common.apiresponse.ApiListResponse;
import com.hungover.common.apiresponse.SingleDataResponse;
import com.hungover.common.constant.ApplicationConstants;
import com.hungover.core.dto.order.VariantDetailsDto;
import com.hungover.core.dto.product.ProductDataResponseDto;
import com.hungover.core.dto.product.ProductKeyValueDto;
import com.hungover.core.dto.product.TopProductsResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Service class responsible for handling API responses related to products.
 */
@Service
public class ProductApiResponseService {
    private final Logger productApiResponseServiceLogger = LoggerFactory.getLogger(this.getClass());

    private ProductService productService;
    private MessageSource messageSource;

    @Value("${shopifyAccessToken}")
    String shopifyAccessToken;
    @Value("${shopifyProductEndPoint}")
    String shopifyProductEndPoint;
    @Value("${shopifyGraphQueryEndPoint}")
    String shopifyGraphQueryEndPoint;
    @Value("${shopifyVariantDetailsByVariantIdEndPoint}")
    String shopifyVariantDetailsByVariantIdEndPoint;

    public ProductApiResponseService(ProductService productService, MessageSource messageSource) {
        this.productService = productService;
        this.messageSource = messageSource;
    }

    /**
     * Retrieves a list of all products and prepares the API response.
     *
     * @return ApiListResponse containing the list of products.
     * @throws IOException if there is an error during the API call or data retrieval.
     */
    public ApiListResponse getAllProducts() throws IOException {
        productApiResponseServiceLogger.info("Entered into Get all products api response service");
        ApiListResponse apiListResponse = new ApiListResponse();
        List<ProductKeyValueDto> productKeyValueDtoList = productService.
                getAllProductListFromShopify(shopifyAccessToken, shopifyProductEndPoint);
        if (!(productKeyValueDtoList.isEmpty())) {
            apiListResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                    messageSource.getMessage("api.get.all.product.success", null, Locale.ENGLISH),
                    productKeyValueDtoList);
            apiListResponse.setTotalResults(productKeyValueDtoList.size());
        } else {
            apiListResponse.setResponse(ApplicationConstants.Status.FAIL,
                    messageSource.getMessage("api.get.all.product.fail", null, Locale.ENGLISH),
                    null);
        }
        return apiListResponse;
    }

    /**
     * Retrieves the top products data from the ProductService and creates a SingleDataResponse object
     * containing the response data.
     *
     * @return SingleDataResponse object containing the top products data.
     */
    public SingleDataResponse getTopProducts() {
        productApiResponseServiceLogger.info("Entered into Get top products api response service");
        SingleDataResponse singleDataResponse = new SingleDataResponse();
        TopProductsResponseDTO topProductsResponseDTO = productService.
                getTopProducts(shopifyAccessToken, shopifyGraphQueryEndPoint, shopifyProductEndPoint);
        if (Optional.ofNullable(topProductsResponseDTO).isPresent()) {
            singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                    messageSource.getMessage("api.get.top.product.success", null, Locale.ENGLISH),
                    topProductsResponseDTO);
        } else {
            singleDataResponse.setResponse(ApplicationConstants.Status.FAIL,
                    messageSource.getMessage("api.get.top.product.fail", null, Locale.ENGLISH), null);
        }
        return singleDataResponse;
    }

    /**
     * Retrieves the total number of products based on the provided parameters.
     *
     * @param currentYear The current year for which the total products are to be retrieved.
     * @param lastYear    The last year for which the total products are to be retrieved.
     * @param noOfDays    The number of days to consider while calculating the total products.
     * @return A SingleDataResponse containing the product data with the response status and message.
     */
    public SingleDataResponse getTotalProducts(String currentYear, String lastYear, Integer noOfDays) {
        productApiResponseServiceLogger.info("Entered into Get total products api response service");
        SingleDataResponse singleDataResponse = new SingleDataResponse();
        ProductDataResponseDto totalProductsResponseDTO = productService.getTotalProducts(currentYear, lastYear,
                noOfDays, shopifyAccessToken, shopifyProductEndPoint, shopifyGraphQueryEndPoint);
        if (Optional.ofNullable(totalProductsResponseDTO).isPresent()) {
            singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                    messageSource.getMessage("api.get.total.product.success", null, Locale.ENGLISH),
                    totalProductsResponseDTO);
        } else {
            singleDataResponse.setResponse(ApplicationConstants.Status.FAIL,
                    messageSource.getMessage("api.get.total.product.fail", null, Locale.ENGLISH),
                    null);
        }
        return singleDataResponse;
    }

    /**
     * Retrieves variant details for a specific variant ID and creates a SingleDataResponse
     * object containing the details.
     *
     * @param variantId The ID of the variant for which details are requested.
     * @return A SingleDataResponse object containing the variant details wrapped in a success response status.
     * @throws IOException If an I/O error occurs while retrieving the variant details.
     */
    public SingleDataResponse getVariantDetailsByVariantId(Long variantId) throws IOException {
        productApiResponseServiceLogger.info("Entered into Get product variant api response service");
        SingleDataResponse singleDataResponse = new SingleDataResponse();
        VariantDetailsDto variantDetailsDto = productService.getVariantDetailsByVariantId(
                shopifyVariantDetailsByVariantIdEndPoint, variantId, shopifyAccessToken);
        singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                messageSource.getMessage("api.get.product.variant.success", null, Locale.ENGLISH),
                variantDetailsDto);
        return singleDataResponse;
    }
}
