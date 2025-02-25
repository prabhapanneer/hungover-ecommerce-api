package com.hungover.product.service;

import com.google.gson.Gson;
import com.hungover.common.constant.ApplicationConstants;
import com.hungover.common.domain.DomainObject;
import com.hungover.core.dto.order.VariantDetailsDto;
import com.hungover.core.dto.order.VariantDetailsResponseDto;
import com.hungover.core.dto.product.ProductDataDto;
import com.hungover.core.dto.product.ProductDataResponseDto;
import com.hungover.core.dto.product.ProductKeyValueDto;
import com.hungover.core.dto.product.TopProductKeyValueDto;
import com.hungover.core.dto.product.TopProductsResponseDTO;
import org.json.JSONArray;
import org.json.JSONObject;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * Service class responsible for handling product-related operations.
 */
@Service
public class ProductService {
    private final Logger productServiceLogger = LoggerFactory.getLogger(this.getClass());

    private static final String DEFAULT_EMPTY = "";
    private static final String EQUAL_TO = "=";
    private static final String RESPONSE_HINT_FOR_NEXT_PAGE = "rel=\"next\"";
    private static final String SPLIT_WITH_COMMA = ",";
    private static final String SPLIT_WITH_SEMI_COLON = ";";
    private static final String SPLIT_WITH_AND_SYMBOL = "&";
    private static final String REPLACE_WITH_GREATER_THAN = ">";
    private static final String REPLACE_WITH_LESSER_THAN = "<";
    private static final String NULL = "null";
    private static final String SHOPIFY_ENDPOINT_URL_QUESTIONMARK = "?";
    private static final String SHOPIFY_ENDPOINT_LIMIT = "limit=250";
    private static final String CONTENT_TYPE = "application/graphql";
    private static final String RESPONSE_DATA = "data";
    private static final String SHOPIFY_QL_QUERY = "shopifyqlQuery";
    private static final String TABLE_DATA = "tableData";
    private static final String ROW_DATA = "rowData";
    private static final String SHOPIFY_ENDPOINT_JSON = ".json";
    private static final String NEWLINE = "\n";

    /**
     * Retrieves a list of product data from the Shopify API by making paginated API calls.
     *
     * @return A list of {@link ProductKeyValueDto} objects containing product data.
     * @throws IOException if there's an error while making the API calls or processing the response.
     */
    public List<ProductKeyValueDto> getAllProductListFromShopify(String shopifyAccessToken,
                                                                 String shopifyProductEndPoint) throws IOException {
        productServiceLogger.info("Entered get product details from Shopify API call:::::::::::::::");
        String pageInfo = "";
        List<ProductKeyValueDto> allProductList = new ArrayList<>();
        while (true) {
            HttpURLConnection httpConnection = getHttpURLConnection(pageInfo, shopifyAccessToken,
                    shopifyProductEndPoint);
            int responseCode = httpConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader httpConnectionBufferReader = new BufferedReader(new InputStreamReader(
                        httpConnection.getInputStream()))) {
                    String responseBody = httpConnectionBufferReader.lines().collect(Collectors.joining());
                    JSONObject responseObject = new JSONObject(responseBody);
                    JSONArray products = responseObject.getJSONArray(DomainObject.Product.PRODUCTS);
                    List<ProductKeyValueDto> productList = IntStream.range(0, products.length())
                            .mapToObj(products::getJSONObject)
                            .map(this::createProductKeyValueDto)
                            .collect(Collectors.toList());
                    allProductList.addAll(productList);
                    String linkHeader = httpConnection.getHeaderField(ApplicationConstants.ShopifyApiHeaders.LINK);
                    if (linkHeader == null || !linkHeader.contains(RESPONSE_HINT_FOR_NEXT_PAGE)) {
                        break;
                    }
                    String nextLink = parseNextLink(linkHeader);
                    Map<String, String> params = parseQueryParameters(nextLink);
                    String strPageInfo = params.get(ApplicationConstants.ShopifyApiHeaders.PAGE_INFO)
                            .replace(REPLACE_WITH_GREATER_THAN, DEFAULT_EMPTY);
                    pageInfo = ApplicationConstants.ShopifyApiHeaders.PAGE_INFO + EQUAL_TO + strPageInfo;
                }
            } else {
                break;
            }
            httpConnection.disconnect();
        }
        return allProductList;
    }

     HttpURLConnection getHttpURLConnection(String pageInfo, String authorization,
                                                   String shopifyProductEndPoint) throws IOException {
        URL urlConnector = new URL(shopifyProductEndPoint + SHOPIFY_ENDPOINT_URL_QUESTIONMARK +
                SHOPIFY_ENDPOINT_LIMIT + SPLIT_WITH_AND_SYMBOL + pageInfo);
        HttpURLConnection httpConnection = (HttpURLConnection) urlConnector.openConnection();
        httpConnection.setRequestMethod(ApplicationConstants.ShopifyApiHeaders.GET_REQUEST_METHOD);
        httpConnection.setRequestProperty(ApplicationConstants.ShopifyApiHeaders.SHOPIFY_ACCESS_TOKEN,
                authorization);
        return httpConnection;
    }

    /**
     * Creates a {@link ProductKeyValueDto} object from the JSON representation of a product obtained from the API.
     *
     * @param productObj The JSON object representing a product's data.
     * @return A {@link ProductKeyValueDto} object containing product data.
     */
    private ProductKeyValueDto createProductKeyValueDto(JSONObject productObj) {
        ProductKeyValueDto productKeyValueDto = new ProductKeyValueDto();
        productKeyValueDto.setTitle(productObj.optString(DomainObject.Product.TITLE, DEFAULT_EMPTY));
        productKeyValueDto.setStatus(productObj.optString(DomainObject.Product.STATUS));
        JSONArray variants = productObj.optJSONArray(DomainObject.Product.VARIANTS);
        if (Optional.ofNullable(variants).isPresent()) {
            int inventoryQuantity = IntStream.range(0, variants.length())
                    .mapToObj(variants::getJSONObject)
                    .filter(Objects::nonNull)
                    .mapToInt(variantObj -> variantObj.optInt(DomainObject.Product.INVENTORY_QUANTITY, 0))
                    .sum();
            productKeyValueDto.setInventory(inventoryQuantity +
                    ApplicationConstants.Products.INVENTORY_MESSAGE_CONTENT + variants.length() +
                    ApplicationConstants.Products.VARIANTS);
            productKeyValueDto.setInventoryQuantity(inventoryQuantity);
        } else {
            productKeyValueDto.setInventory(DEFAULT_EMPTY);
            productKeyValueDto.setInventoryQuantity(0);
        }
        productKeyValueDto.setProductType(productObj.optString(DomainObject.Product.TYPE, DEFAULT_EMPTY));
        productKeyValueDto.setVendor(productObj.optString(DomainObject.Product.VENDOR, DEFAULT_EMPTY));
        return productKeyValueDto;
    }

    /**
     * Parses the "Link" header in the HTTP response to extract the URL of the next page of results.
     *
     * @param linkHeader The "Link" header containing the URL of the next page.
     * @return The URL of the next page of results, or {@code null} if there's no next page.
     */
    public String parseNextLink(String linkHeader) {
        String[] links = linkHeader.split(SPLIT_WITH_COMMA);
        for (String link : links) {
            if (link.contains(RESPONSE_HINT_FOR_NEXT_PAGE)) {
                return link.split(SPLIT_WITH_SEMI_COLON)[0].trim();
            }
        }
        return null;
    }

    /**
     * Parses the query parameters from a URL and returns them as a map.
     *
     * @param link The URL containing the query parameters.
     * @return A {@link Map} containing the query parameters and their values.
     */
    public Map<String, String> parseQueryParameters(String link) {
        Map<String, String> params = new HashMap<>();
        String[] queryParams = link.substring(link.indexOf(SHOPIFY_ENDPOINT_URL_QUESTIONMARK) + 1).
                split(SPLIT_WITH_AND_SYMBOL);
        for (String queryParam : queryParams) {
            String[] param = queryParam.split(EQUAL_TO);
            params.put(param[0], param[1]);
        }
        return params;
    }

    /**
     * Get the top products based on sales and inventory quantity.
     *
     * @return A TopProductsResponseDTO object containing the top products, their sales, and inventory quantity.
     */
    public TopProductsResponseDTO getTopProducts(String shopifyAccessToken, String shopifyGraphQueryEndPoint,
                                                 String shopifyProductEndPoint) {
        TopProductsResponseDTO topProductsResponseDTO = new TopProductsResponseDTO();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.valueOf(CONTENT_TYPE));
        httpHeaders.set(ApplicationConstants.ShopifyApiHeaders.SHOPIFY_ACCESS_TOKEN, shopifyAccessToken);
        String query = ApplicationConstants.ShopifyQuery.TOP_PRODUCT_QUERY;
        HttpEntity<String> httpEntity = new HttpEntity<>(query, httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(shopifyGraphQueryEndPoint, HttpMethod.POST,
                httpEntity, String.class);
        JSONObject jsonResponse = new JSONObject(responseEntity.getBody());
        List<List<String>> strList = new ArrayList<>();
        if (jsonResponse.has(RESPONSE_DATA) && jsonResponse.getJSONObject(RESPONSE_DATA).has(SHOPIFY_QL_QUERY)) {
            JSONObject tableData = jsonResponse.getJSONObject(RESPONSE_DATA).
                    getJSONObject(SHOPIFY_QL_QUERY).getJSONObject(TABLE_DATA);
            strList = extractTableData(tableData.getJSONArray(ROW_DATA));
        }
        Map<String, Integer> quantityList = new HashMap<>();
        for (List<String> entry : strList) {
            String productId = entry.get(0);
            int sales = Integer.parseInt(entry.get(2));
            quantityList.put(productId, quantityList.getOrDefault(productId, 0) + sales);
        }
        Map<String, Integer> products = new HashMap<>();
        List<List<TopProductKeyValueDto>> topProductList = fetchAllProductsData(restTemplate, httpEntity,
                quantityList, products, shopifyProductEndPoint);
        Comparator<List<TopProductKeyValueDto>> outerListComparator = (list1, list2) -> {
            String sales1 = list1.get(0).getSales();
            String sales2 = list2.get(0).getSales();
            return Integer.parseInt(sales1) - Integer.parseInt(sales2);
        };
        topProductList.sort(outerListComparator);
        Collections.reverse(topProductList);
        topProductsResponseDTO.setTopProducts(topProductList);
        topProductsResponseDTO.setProducts(products);
        topProductsResponseDTO.setCount(products.size());
        return topProductsResponseDTO;
    }

    /**
     * Fetch all products data from Shopify API and process it to get the required information.
     *
     * @param restTemplate The RestTemplate instance to make API calls.
     * @param entity       The HttpEntity containing headers and body for the API call.
     * @param quantityList A map containing product IDs as keys and their corresponding sales quantities as values.
     * @param products     A map containing product titles as keys and their corresponding
     *                     inventory quantities as values.
     * @return A List of List of TopProductKeyValueDto containing the processed products' data.
     */
    private List<List<TopProductKeyValueDto>> fetchAllProductsData(RestTemplate restTemplate, HttpEntity<String> entity,
        Map<String, Integer> quantityList, Map<String, Integer> products, String shopifyProductEndPoint) {
        List<List<TopProductKeyValueDto>> allProducts = new ArrayList<>();
        String pageInfo = "";
        boolean lastPage = true;
        do {
            String url = shopifyProductEndPoint + SHOPIFY_ENDPOINT_URL_QUESTIONMARK + SHOPIFY_ENDPOINT_LIMIT + pageInfo;
            ResponseEntity<String> productsResponse = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            JSONObject productsJson = new JSONObject(productsResponse.getBody());
            List<List<TopProductKeyValueDto>> productsData = extractProductsData(
                    productsJson.getJSONArray(DomainObject.Product.PRODUCTS), quantityList, products);
            allProducts.addAll(productsData);
            if (productsResponse.getHeaders().containsKey(ApplicationConstants.ShopifyApiHeaders.LINK)) {
                List<String> links = productsResponse.getHeaders().get(ApplicationConstants.ShopifyApiHeaders.LINK);
                for (String link : links) {
                    if (link.contains(RESPONSE_HINT_FOR_NEXT_PAGE)) {
                        String next = link.substring(link.indexOf(REPLACE_WITH_LESSER_THAN) + 1,
                                link.indexOf(REPLACE_WITH_GREATER_THAN));
                        String[] params = next.substring(next.indexOf(SHOPIFY_ENDPOINT_URL_QUESTIONMARK) + 1).
                                split(SPLIT_WITH_AND_SYMBOL);
                        for (String param : params) {
                            String[] parts = param.split(EQUAL_TO);
                            if (parts[0].equals(ApplicationConstants.ShopifyApiHeaders.PAGE_INFO)) {
                                pageInfo = SPLIT_WITH_AND_SYMBOL + ApplicationConstants.ShopifyApiHeaders.PAGE_INFO +
                                        EQUAL_TO + parts[1];
                                break;
                            }
                        }
                        break;
                    }
                }
            } else {
                lastPage = false;
            }
        } while (lastPage);

        return allProducts;
    }

    /**
     * Extracts the data from a JSONArray containing rows of a table and converts it into a list of lists of strings.
     *
     * @param rowData The JSONArray containing the rows of the table.
     * @return A List of List of Strings representing the table data.
     */
     List<List<String>> extractTableData(JSONArray rowData) {
        List<List<String>> list = new ArrayList<>();
        for (int i = 0; i < rowData.length(); i++) {
            JSONArray row = rowData.getJSONArray(i);
            List<String> entry = new ArrayList<>();
            for (int j = 0; j < row.length(); j++) {
                entry.add(row.getString(j));
            }
            list.add(entry);
        }
        return list;
    }

    /**
     * Extracts product data from a JSONArray and converts it into a list of lists of TopProductKeyValueDto objects.
     *
     * @param productsJson The JSONArray containing product data.
     * @param quantityList A Map containing the quantity of each product.
     * @param products A Map to store product titles and their corresponding inventory quantities.
     * @return A List of List of TopProductKeyValueDto objects representing the product data.
     */
    private List<List<TopProductKeyValueDto>> extractProductsData(JSONArray productsJson,
        Map<String, Integer> quantityList, Map<String, Integer> products) {
        List<List<TopProductKeyValueDto>> allProducts = new ArrayList<>();
        for (int i = 0; i < productsJson.length(); i++) {
            JSONObject product = productsJson.getJSONObject(i);
            Long productId = product.getLong(DomainObject.Product.ID);
            int inventoryQuantity = 0;
            JSONArray variants = product.getJSONArray(DomainObject.Product.VARIANTS);
            for (int j = 0; j < variants.length(); j++) {
                inventoryQuantity += variants.getJSONObject(j).getInt(DomainObject.Product.INVENTORY_QUANTITY);
            }
            JSONObject firstVariantJsonObject = variants.getJSONObject(0);
            JSONObject imageSrc = product.getJSONObject(DomainObject.Product.IMAGE);
            Map<Long, Integer> filteredQuantityList = new HashMap<>();
            for (Map.Entry<String, Integer> entry : quantityList.entrySet()) {
                Long strProductId = Long.valueOf(entry.getKey());
                int wiQuantityCount = entry.getValue();

                if (strProductId.equals(productId)) {
                    filteredQuantityList.put(strProductId, wiQuantityCount);
                }
            }
            if (!product.getString(DomainObject.Product.TYPE).equals(DomainObject.Product.GIFT_CARDS)) {
                List<TopProductKeyValueDto> topProductKeyValueDtoList = new ArrayList<>();
                TopProductKeyValueDto topProductKeyValueDtoObj = new TopProductKeyValueDto();
                products.put(product.getString(DomainObject.Product.TITLE), inventoryQuantity);
                topProductKeyValueDtoObj.setTitle(product.getString(DomainObject.Product.TITLE));
                topProductKeyValueDtoObj.setInventoryQuantity(String.valueOf(inventoryQuantity));
                topProductKeyValueDtoObj.setSales(String.valueOf(filteredQuantityList.get(productId)));
                topProductKeyValueDtoObj.setImageSrc(imageSrc.getString(DomainObject.Product.IMAGE_SRC));
                topProductKeyValueDtoObj.setPrice(firstVariantJsonObject.getString(DomainObject.Product.PRICE));
                topProductKeyValueDtoList.add(topProductKeyValueDtoObj);
                allProducts.add(topProductKeyValueDtoList);
            }
        }
        return allProducts;
    }

    /**
     * Retrieves the total number of products based on the provided parameters.
     *
     * @param currentYear The current year for which the total products are to be retrieved.
     * @param lastYear    The last year for which the total products are to be retrieved.
     * @param noOfDays    The number of days to consider while calculating the total products.
     * @return A ProductDataResponseDto containing the product data with the total products,
     * count, and percentage difference.
     */
    public ProductDataResponseDto getTotalProducts(String lastYear, String currentYear, Integer noOfDays,
        String shopifyAccessToken, String shopifyProductEndPoint, String shopifyGraphQueryEndPoint) {
        ProductDataResponseDto productDataResponseDto = new ProductDataResponseDto();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.valueOf(CONTENT_TYPE));
        httpHeaders.set(ApplicationConstants.ShopifyApiHeaders.SHOPIFY_ACCESS_TOKEN, shopifyAccessToken);
        HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Integer> quantityList = getQuantityList(currentYear, shopifyGraphQueryEndPoint, shopifyAccessToken);
        Map<String, Integer> quantityListOld = getQuantityList(lastYear, noOfDays, shopifyAccessToken,
                shopifyGraphQueryEndPoint);
        Map<String, Integer> products = new HashMap<>();
        List<List<ProductDataDto>> allProductsData = fetchAllProductsData(restTemplate, httpEntity,
                quantityList, quantityListOld, products, shopifyProductEndPoint);
        Comparator<List<ProductDataDto>> outerListComparator = (list1, list2) -> {
            String strQuantityFirstListOfFirstValue = String.valueOf(list1.get(0).getQuantity());
            String strQuantitySecondListOfFirstValue = String.valueOf(list2.get(0).getQuantity());
            return Integer.parseInt(strQuantityFirstListOfFirstValue) -
                    Integer.parseInt(strQuantitySecondListOfFirstValue);
        };
        allProductsData.sort(outerListComparator);
        Collections.reverse(allProductsData);
        int quantityListTotal = quantityList.values().stream().mapToInt(Integer::intValue).sum();
        int quantityListOldTotal = quantityListOld.values().stream().mapToInt(Integer::intValue).sum();
        BigDecimal difference = BigDecimal.valueOf(quantityListTotal).subtract(
                BigDecimal.valueOf(quantityListOldTotal));
        BigDecimal percentage = difference.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        productDataResponseDto.setTotalProducts(allProductsData);
        productDataResponseDto.setProducts(products);
        productDataResponseDto.setCount(products.size());
        productDataResponseDto.setPercentage(percentage);
        return productDataResponseDto;
    }

    /**
     * Retrieves the quantity data for products in the current year from Shopify.
     *
     * @param currentYear The current year for which to retrieve the quantity data.
     * @return A map containing product titles as keys and their corresponding quantities as values.
     */
    private Map<String, Integer> getQuantityList(String currentYear, String shopifyGraphQueryEndPoint,
                                                 String shopifyAccessToken) {
        String query = String.format(ApplicationConstants.ShopifyQuery.TOTAL_PRODUCT_CURRENT_YEAR_QUERY,
                currentYear);
        Map<String, Integer> quantityList = new HashMap<>();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.valueOf(CONTENT_TYPE));
        httpHeaders.set(ApplicationConstants.ShopifyApiHeaders.SHOPIFY_ACCESS_TOKEN, shopifyAccessToken);
        HttpEntity<String> httpEntity = new HttpEntity<>(query, httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(shopifyGraphQueryEndPoint, HttpMethod.POST,
                httpEntity, String.class);
        JSONObject responseObject = new JSONObject(responseEntity.getBody());
        List<List<String>> strList = new ArrayList<>();
        if (responseObject.has(RESPONSE_DATA) && responseObject.getJSONObject(RESPONSE_DATA).has(SHOPIFY_QL_QUERY)) {
            JSONObject tableData = responseObject.getJSONObject(RESPONSE_DATA).
                    getJSONObject(SHOPIFY_QL_QUERY).getJSONObject(TABLE_DATA);
            strList = extractTableData(tableData.getJSONArray(ROW_DATA));
        }
        for (List<String> entry : strList) {
            String productTitle = entry.get(0);
            int quantityPurchased = Integer.parseInt(entry.get(2));
            quantityList.put(productTitle, quantityPurchased);
        }
        return quantityList;
    }

    /**
     * Retrieves the quantity data for products in the last year with the specified number of days from Shopify.
     *
     * @param lastYear The last year for which to retrieve the quantity data.
     * @param noOfDays The number of days to consider for the last year.
     * @return A map containing product titles as keys and their corresponding quantities as values for the last year.
     */
    private Map<String, Integer> getQuantityList(String lastYear, int noOfDays, String shopifyAccessToken,
                                                 String shopifyGraphQueryEndPoint) {
        String query = String.format(ApplicationConstants.ShopifyQuery.TOTAL_PRODUCT_LAST_YEAR_QUERY,
                lastYear, noOfDays);
        Map<String, Integer> quantityListOld = new HashMap<>();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.valueOf(CONTENT_TYPE));
        httpHeaders.set(ApplicationConstants.ShopifyApiHeaders.SHOPIFY_ACCESS_TOKEN, shopifyAccessToken);
        HttpEntity<String> httpEntity = new HttpEntity<>(query, httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(shopifyGraphQueryEndPoint, HttpMethod.POST,
                httpEntity, String.class);
        JSONObject responseObject = new JSONObject(responseEntity.getBody());
        List<List<String>> strList = new ArrayList<>();
        if (responseObject.has(RESPONSE_DATA) && responseObject.getJSONObject(RESPONSE_DATA).has(SHOPIFY_QL_QUERY)) {
            JSONObject tableData = responseObject.getJSONObject(RESPONSE_DATA).
                    getJSONObject(SHOPIFY_QL_QUERY).getJSONObject(TABLE_DATA);
            strList = extractTableData(tableData.getJSONArray(ROW_DATA));
        }
        for (List<String> entry : strList) {
            String productTitle = entry.get(0);
            int quantityPurchased = Integer.parseInt(entry.get(2));
            quantityListOld.put(productTitle, quantityPurchased);
        }
        return quantityListOld;
    }

    /**
     * Fetches all products' data from Shopify API in paginated format.
     *
     * @param restTemplate The RestTemplate used to send HTTP requests.
     * @param entity       The HttpEntity containing request headers and the query for Shopify API.
     * @param quantityList The map containing the quantity data for the current year.
     * @param quantityListOld The map containing the quantity data for the last year.
     * @param products     The map containing the merged product data.
     * @return A list of lists containing ProductDataDto objects for all products.
     */
     List<List<ProductDataDto>> fetchAllProductsData(RestTemplate restTemplate, HttpEntity<String> entity,
        Map<String, Integer> quantityList, Map<String, Integer> quantityListOld, Map<String, Integer> products,
        String shopifyProductEndPoint) {
        List<List<ProductDataDto>> allProducts = new ArrayList<>();
        String pageInfo = "";
        boolean lastPage = true;
        do {
            String url = shopifyProductEndPoint + SHOPIFY_ENDPOINT_URL_QUESTIONMARK + SHOPIFY_ENDPOINT_LIMIT + pageInfo;
            ResponseEntity<String> productsResponse = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            JSONObject productsJson = new JSONObject(productsResponse.getBody());
            List<List<ProductDataDto>> productsData = mergeAndSortData(
                    productsJson.getJSONArray(DomainObject.Product.PRODUCTS), quantityList, quantityListOld, products);
            allProducts.addAll(productsData);

            if (productsResponse.getHeaders().containsKey(ApplicationConstants.ShopifyApiHeaders.LINK)) {
                List<String> links = productsResponse.getHeaders().get(ApplicationConstants.ShopifyApiHeaders.LINK);
                for (String link : links) {
                    if (link.contains(RESPONSE_HINT_FOR_NEXT_PAGE)) {
                        String next = link.substring(link.indexOf(REPLACE_WITH_LESSER_THAN) + 1,
                                link.indexOf(REPLACE_WITH_GREATER_THAN));
                        String[] params = next.substring(next.indexOf(SHOPIFY_ENDPOINT_URL_QUESTIONMARK) + 1).
                                split(SPLIT_WITH_AND_SYMBOL);
                        for (String param : params) {
                            String[] parts = param.split(EQUAL_TO);
                            if (parts[0].equals(ApplicationConstants.ShopifyApiHeaders.PAGE_INFO)) {
                                pageInfo = SPLIT_WITH_AND_SYMBOL + ApplicationConstants.ShopifyApiHeaders.PAGE_INFO +
                                        EQUAL_TO + parts[1];
                                break;
                            }
                        }
                        break;
                    }
                }
            } else {
                lastPage = false;
            }
        } while (lastPage);

        return allProducts;
    }

    /**
     * Merges and sorts the product data from different sources.
     *
     * @param productsJson      The JSON array containing product data from Shopify API.
     * @param quantityList      The map containing the quantity data for the current year.
     * @param quantityListOld   The map containing the quantity data for the last year.
     * @param products          The map containing the merged product data.
     * @return A list of lists containing ProductDataDto objects with merged and sorted data.
     */
    private List<List<ProductDataDto>> mergeAndSortData(JSONArray productsJson, Map<String, Integer> quantityList,
                                                         Map<String, Integer> quantityListOld,
                                                        Map<String, Integer> products) {
        List<List<ProductDataDto>> allProducts = new ArrayList<>();
        for (int i = 0; i < productsJson.length(); i++) {
            JSONObject productObject = productsJson.getJSONObject(i);
            Long productId = productObject.getLong(DomainObject.Product.ID);
            int inventoryQuantity = 0;
            JSONArray variants = productObject.getJSONArray(DomainObject.Product.VARIANTS);
            for (int j = 0; j < variants.length(); j++) {
                inventoryQuantity += variants.getJSONObject(j).getInt(DomainObject.Product.INVENTORY_QUANTITY);
            }
            JSONObject imageSrc = productObject.getJSONObject(DomainObject.Product.IMAGE);
            Map<Long, Integer> filteredQuantityList = new HashMap<>();
            for (Map.Entry<String, Integer> entry : quantityList.entrySet()) {
                Long strProductId = Long.valueOf(entry.getKey());
                int wiQuantityCount = entry.getValue();

                if (strProductId.equals(productId)) {
                    filteredQuantityList.put(strProductId, wiQuantityCount);
                }
            }
            Map<Long, Integer> filteredOldQuantityList = new HashMap<>();
            for (Map.Entry<String, Integer> entry : quantityListOld.entrySet()) {
                Long strProductId = Long.valueOf(entry.getKey());
                int wiQuantityCount = entry.getValue();
                if (strProductId.equals(productId)) {
                    filteredOldQuantityList.put(strProductId, wiQuantityCount);
                }
            }
            if (!productObject.getString(DomainObject.Product.TYPE).equals(DomainObject.Product.GIFT_CARDS) &&
                    filteredOldQuantityList.size()>0 && filteredQuantityList.size()>0) {
                List<ProductDataDto> productDataDtoList = new ArrayList<>();
                ProductDataDto productDataDto = new ProductDataDto();
                products.put(productObject.getString(DomainObject.Product.TITLE), filteredQuantityList.get(productId));
                productDataDto.setTitle(productObject.getString(DomainObject.Product.TITLE));
                productDataDto.setInventoryQuantity(String.valueOf(inventoryQuantity));
                productDataDto.setQuantity(filteredQuantityList.get(productId));
                productDataDto.setImageSrc(imageSrc.getString(DomainObject.Product.IMAGE_SRC));
                productDataDto.setOldQuantity(filteredOldQuantityList.get(productId));
                productDataDtoList.add(productDataDto);
                allProducts.add(productDataDtoList);
            }
        }
        return allProducts;
    }

    /**
     * Retrieves top products based on the specified date range.
     *
     * @param fromDate The start date of the filter range.
     * @param toDate   The end date of the filter range.
     * @return A {@link TopProductsResponseDTO} containing information about the top products.
     */
    public TopProductsResponseDTO getTopProductByFilter(String fromDate, String toDate, String shopifyAccessToken,
        String shopifyGraphQueryEndPoint, String shopifyProductEndPoint) {
        TopProductsResponseDTO topProductsResponseDTO = new TopProductsResponseDTO();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.valueOf(CONTENT_TYPE));
        httpHeaders.set(ApplicationConstants.ShopifyApiHeaders.SHOPIFY_ACCESS_TOKEN, shopifyAccessToken);
        String query = String.format(ApplicationConstants.ShopifyQuery.TOP_PRODUCT_FILTER_QUERY,
                fromDate, toDate);
        HttpEntity<String> httpEntity = new HttpEntity<>(query, httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(shopifyGraphQueryEndPoint, HttpMethod.POST,
                httpEntity, String.class);
        JSONObject jsonResponse = new JSONObject(responseEntity.getBody());
        List<List<String>> strList = new ArrayList<>();
        if (jsonResponse.has(RESPONSE_DATA) && jsonResponse.getJSONObject(RESPONSE_DATA).has(SHOPIFY_QL_QUERY)) {
            JSONObject tableData = jsonResponse.getJSONObject(RESPONSE_DATA).
                    getJSONObject(SHOPIFY_QL_QUERY).getJSONObject(TABLE_DATA);
            strList = extractTableData(tableData.getJSONArray(ROW_DATA));
        }
        Map<String, Integer> quantityList = new HashMap<>();
        for (List<String> entry : strList) {
            String productId = entry.get(0);
            int sales = Integer.parseInt(entry.get(2));
            quantityList.put(productId, quantityList.getOrDefault(productId, 0) + sales);
        }
        Map<String, Integer> products = new HashMap<>();
        List<List<TopProductKeyValueDto>> topProductList = fetchAllProductsData(restTemplate, httpEntity,
                quantityList, products, shopifyProductEndPoint);
        Comparator<List<TopProductKeyValueDto>> outerListComparator = (list1, list2) -> {
            String sales1 = list1.get(0).getSales();
            String sales2 = list2.get(0).getSales();
            if (NULL.equals(sales1) && NULL.equals(sales2)) {
                return 0;
            } else {
                return Integer.parseInt(sales1) - Integer.parseInt(sales2);
            }
        };
        topProductList.sort(outerListComparator);
        Collections.reverse(topProductList);
        topProductsResponseDTO.setTopProducts(topProductList);
        topProductsResponseDTO.setProducts(products);
        topProductsResponseDTO.setCount(products.size());
        return topProductsResponseDTO;
    }

    /**
     * Retrieves variant details for a specific variant ID from a Shopify endpoint using the provided access token.
     *
     * @param shopifyVariantDetailsByVariantIdEndPoint The Shopify endpoint URL for fetching variant details.
     * @param variantId                               The ID of the variant for which details are requested.
     * @param shopifyAccessToken                       The access token for authentication with the Shopify API.
     * @return A VariantDetailsDto object containing the details of the requested variant.
     * @throws IOException If an I/O error occurs while communicating with the Shopify endpoint.
     */
    public VariantDetailsDto getVariantDetailsByVariantId(String shopifyVariantDetailsByVariantIdEndPoint,
                                                          Long variantId, String shopifyAccessToken)
            throws IOException {
        HttpURLConnection variantDetailsHttpConnection = getHttpURLConnection(
                shopifyVariantDetailsByVariantIdEndPoint + variantId + SHOPIFY_ENDPOINT_JSON,
                shopifyAccessToken);
        try {
            int responseCode = variantDetailsHttpConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream variantDetailsHttpConnectionInputStream =
                             variantDetailsHttpConnection.getInputStream();
                     BufferedReader variantDetailsHttpConnectionBufferReader = new BufferedReader(
                             new InputStreamReader(variantDetailsHttpConnectionInputStream, StandardCharsets.UTF_8))) {
                    StringBuilder variantDetailsSbNewLine = new StringBuilder();
                    String variantDetailsStrLine;
                    while ((variantDetailsStrLine = variantDetailsHttpConnectionBufferReader.readLine()) != null) {
                        variantDetailsSbNewLine.append(variantDetailsStrLine).append(NEWLINE);
                    }
                    Gson gsonObject = new Gson();
                    VariantDetailsResponseDto variantDetailsResponseDto = gsonObject.fromJson(
                            variantDetailsSbNewLine.toString(), VariantDetailsResponseDto.class);
                    return variantDetailsResponseDto.getVariant();
                }
            } else {
                return new VariantDetailsDto();
            }
        } finally {
            variantDetailsHttpConnection.disconnect();
        }
    }

    /**
     * Establishes an HTTP connection using the provided Shopify endpoint URL and authorization token.
     *
     * @param shopifyEndpoint The Shopify endpoint URL to connect to.
     * @param authorization   The authorization token used for the connection.
     * @return An HttpURLConnection object configured with the specified endpoint and authorization token.
     * @throws IOException If an I/O error occurs while establishing the connection.
     */
    HttpURLConnection getHttpURLConnection(String shopifyEndpoint, String authorization) throws IOException {
        URL urlConnector = new URL(shopifyEndpoint);
        HttpURLConnection httpConnection = (HttpURLConnection) urlConnector.openConnection();
        httpConnection.setRequestMethod(ApplicationConstants.ShopifyApiHeaders.GET_REQUEST_METHOD);
        httpConnection.setRequestProperty(ApplicationConstants.ShopifyApiHeaders.SHOPIFY_ACCESS_TOKEN, authorization);
        return httpConnection;
    }
}
