package com.hungover.product.controller;

import com.hungover.common.apiresponse.ApiListResponse;
import com.hungover.common.apiresponse.SingleDataResponse;
import com.hungover.product.service.ProductApiResponseService;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * Controller class for handling product-related API endpoints.
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/product")
public class ProductController {

    private ProductApiResponseService productApiResponseService;
    private static final String ENDPOINT_VERSION = "v2";

    public ProductController(ProductApiResponseService productApiResponseService) {
        this.productApiResponseService = productApiResponseService;
    }

    /**
     * Get all products API endpoint.
     *
     * @return ResponseEntity containing the API response with the list of all products.
     * @throws IOException if there is an error during the API call or data retrieval.
     */
    @GetMapping(value = ENDPOINT_VERSION+"/all")
    @ApiOperation(value = "Get all products", nickname = "Get all products",
            notes = "This endpoint for Get all products", produces = "application/json", consumes = "application/json")
    public ResponseEntity<ApiListResponse> getAllProducts() throws IOException {
        return new ResponseEntity<>(productApiResponseService.getAllProducts(), HttpStatus.OK);
    }

    /**
     * Handles the HTTP GET request to retrieve the top products and returns the response
     * in a ResponseEntity with the SingleDataResponse object.
     *
     * @return ResponseEntity containing the SingleDataResponse object with the top products data.
     */
    @GetMapping(value = ENDPOINT_VERSION+"/top")
    @ApiOperation(value = "Get top products", nickname = "Get top products",
            notes = "This endpoint for Get top products", produces = "application/json", consumes = "application/json")
    public ResponseEntity<SingleDataResponse> getTopProducts() {
        return new ResponseEntity<>(productApiResponseService.getTopProducts(), HttpStatus.OK);
    }

    /**
     * Retrieves the total number of products based on the provided parameters.
     *
     * @param currentYear The current year for which the total products are to be retrieved.
     * @param lastYear    The last year for which the total products are to be retrieved.
     * @param noOfDays    The number of days to consider while calculating the total products.
     * @return A ResponseEntity containing the SingleDataResponse with the total product data.
     */
    @GetMapping(value = ENDPOINT_VERSION+"/total")
    @ApiOperation(value = "Get total products", nickname = "Get total products",
            notes = "This endpoint for Get total products", produces = "application/json",
            consumes = "application/json")
    public ResponseEntity<SingleDataResponse> getTotalProducts(
            @RequestParam(value = "currentYear") String currentYear,
            @RequestParam(value = "lastYear") String lastYear,
            @RequestParam(value = "noOfDays") Integer noOfDays) {
        return new ResponseEntity<>(productApiResponseService.getTotalProducts(currentYear, lastYear, noOfDays),
                HttpStatus.OK);
    }

    /**
     * Retrieves details for a specific product variant by its ID.
     *
     * @param variantId The ID of the product variant for which details are requested.
     * @return A ResponseEntity containing a SingleDataResponse object with the variant details.
     * @throws IOException If an I/O error occurs while retrieving the variant details.
     */
    @GetMapping(value = ENDPOINT_VERSION+"/variant/{variantId}")
    @ApiOperation(value = "Get product variant details", nickname = "Get product variant details",
            notes = "This endpoint for Get product variant details", produces = "application/json",
            consumes = "application/json")
    public ResponseEntity<SingleDataResponse> getVariantDetailsByVariantId(
            @PathVariable(value = "variantId") Long variantId) throws IOException {
        return new ResponseEntity<>(productApiResponseService.getVariantDetailsByVariantId(variantId),
                HttpStatus.OK);
    }
}
