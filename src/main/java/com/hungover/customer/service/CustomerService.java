package com.hungover.customer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.hungover.admin.repository.CustomerOrderStatusRepositoryI;
import com.hungover.admin.service.AdminService;
import com.hungover.common.constant.ApplicationConstants;
import com.hungover.common.domain.DomainObject;
import com.hungover.common.exception.RecordNotFoundException;
import com.hungover.common.exception.UniqueRecordException;
import com.hungover.common.util.AppUtil;
import com.hungover.core.domain.customer.CustomerFeedback;
import com.hungover.core.domain.customer.CustomerMeasurement;
import com.hungover.core.domain.customer.CustomerMeasurementFeedBack;
import com.hungover.core.domain.customer.CustomerOrderStatus;
import com.hungover.core.domain.customer.CustomerOtp;
import com.hungover.core.domain.customer.CustomerWishlist;
import com.hungover.core.domain.notification.Notification;
import com.hungover.core.dto.customer.CustomerCountResponseDto;
import com.hungover.core.dto.customer.CustomerCreationKVDto;
import com.hungover.core.dto.customer.CustomerDetailKeyValueResponseDto;
import com.hungover.core.dto.customer.CustomerDto;
import com.hungover.core.dto.customer.CustomerFeedbackKVDto;
import com.hungover.core.dto.customer.CustomerKeyValueDto;
import com.hungover.core.dto.customer.CustomerKvDto;
import com.hungover.core.dto.customer.CustomerMeasurementDto;
import com.hungover.core.dto.customer.CustomerMeasurementFeedbackDto;
import com.hungover.core.dto.customer.CustomerOrderStatusDto;
import com.hungover.core.dto.customer.CustomerOtpDto;
import com.hungover.core.dto.customer.CustomerResetPasswordDto;
import com.hungover.core.dto.customer.CustomerResponseDto;
import com.hungover.core.dto.customer.CustomerWishlistDto;
import com.hungover.core.dto.customer.CustomerWishlistKVDto;
import com.hungover.core.dto.customer.ResetPasswordDto;
import com.hungover.customer.repository.CustomerFeedbackRepositoryI;
import com.hungover.customer.repository.CustomerMeasurementFeedbackRepositoryI;
import com.hungover.customer.repository.CustomerMeasurementRepositoryI;
import com.hungover.customer.repository.CustomerOtpRepository;
import com.hungover.customer.repository.CustomerWishlistRepositoryI;
import com.hungover.email.notification.service.EmailNotificationService;
import com.hungover.util.AdminMapperUtil;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.io.StringWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Service class for handling customer-related operations.
 */
@Service
public class CustomerService {

    private final Logger customerServiceLogger = LoggerFactory.getLogger(this.getClass());
    private static final String DEFAULT_EMPTY = "";
    private static final String SHOPIFY_ENDPOINT_URL_QUESTIONMARK = "?";
    private static final String SHOPIFY_ENDPOINT_PARAM_PROCESSED_AT_MIN = "processed_at_min=";
    private static final String SHOPIFY_ENDPOINT_PARAM_PROCESSED_AT_MAX = "&processed_at_max=";
    private static final String SHOPIFY_ENDPOINT_LIMIT = "limit=250";
    private static final String SHOPIFY_ENDPOINT_REQUIRED_FIELDS = "fields=id,email,first_name,last_name,phone," +
            "address,orders_count,verified_email,total_spent,created_at&";
    private static final String EQUAL_TO = "=";
    private static final String RESPONSE_HINT_FOR_NEXT_PAGE = "rel=\"next\"";
    private static final String SPLIT_WITH_COMMA = ",";
    private static final String SPLIT_WITH_SEMI_COLON = ";";
    private static final String SPLIT_WITH_AND_SYMBOL = "&";
    private static final String REPLACE_WITH_GREATER_THAN = ">";
    private static final String SHOPIFY_ENDPOINT_JSON = ".json";
    private static final Integer RANDOM_GENERATION_CODE_MAX_VALUE = 999999;
    private static final Integer RANDOM_GENERATION_CODE_MIN_VALUE = 100000;
    private static final Double DEFAULT_DOUBLE_VALUE = 0.0;
    private static final String DEFAULT_PASSWORD = "Password@123";
    private static final String DEFAULT_COUNTRY_CODE = "IN";
    private Random random = new Random();
    private CustomerWishlistRepositoryI customerWishlistRepository;
    private ModelMapper modelMapper;
    private CustomerFeedbackRepositoryI customerFeedbackRepository;
    private CustomerMeasurementRepositoryI customerMeasurementRepository;
    private CustomerMeasurementFeedbackRepositoryI customerMeasurementFeedbackRepositoryI;
    private CustomerOrderStatusRepositoryI customerOrderStatusRepositoryI;
    private MessageSource messageSource;
    private final RestTemplate restTemplate;
    private CustomerOtpRepository customerOtpRepository;
    private EmailNotificationService emailNotificationService;
    private VelocityEngine velocityEngine;
    private AdminMapperUtil adminMapperUtil;
    private AdminService adminService;

    @Value("${shopifyAccessToken}")
    String shopifyAccessToken;

    @Value("${shopifyResetPasswordEndPoint}")
    String shopifyResetPasswordEndPoint;

    @Value("${from.email}")
    private String fromEmail;

    public CustomerService(CustomerWishlistRepositoryI customerWishlistRepository, ModelMapper modelMapper,
                           CustomerFeedbackRepositoryI customerFeedbackRepository,
                           CustomerMeasurementRepositoryI customerMeasurementRepository,
                           MessageSource messageSource, RestTemplate restTemplate,
                           CustomerOtpRepository customerOtpRepository,
                           EmailNotificationService emailNotificationService, AdminMapperUtil adminMapperUtil,
                           CustomerMeasurementFeedbackRepositoryI customerMeasurementFeedbackRepositoryI,
                           CustomerOrderStatusRepositoryI customerOrderStatusRepositoryI, AdminService adminService) {
        super();
        this.customerWishlistRepository = customerWishlistRepository;
        this.modelMapper = modelMapper;
        this.customerFeedbackRepository = customerFeedbackRepository;
        this.customerMeasurementRepository = customerMeasurementRepository;
        this.messageSource = messageSource;
        this.restTemplate = restTemplate;
        this.customerOtpRepository = customerOtpRepository;
        this.emailNotificationService = emailNotificationService;
        this.adminMapperUtil = adminMapperUtil;
        this.customerMeasurementFeedbackRepositoryI = customerMeasurementFeedbackRepositoryI;
        this.customerOrderStatusRepositoryI = customerOrderStatusRepositoryI;
        this.adminService = adminService;
        velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        velocityEngine.init();
    }

    /**
     * Get customer wishlist based on customer id.
     *
     * @param customerId The customer id for which to retrieve the wishlist.
     * @return List of CustomerWishlistDto containing the customer wishlist.
     */
    public List<CustomerWishlistDto> getCustomerWishlistByCustomerId(String customerId) {
        List<CustomerWishlistDto> customerWishlistDtoList = new ArrayList<>();
        List<CustomerWishlist> customerWishlist = customerWishlistRepository.findByCustomerId(customerId);
        if (!customerWishlist.isEmpty()) {
            customerWishlist.forEach(customerWishListDataObj -> {
                CustomerWishlistDto customerWishlistDtoObj;
                customerWishlistDtoObj = modelMapper.map(customerWishListDataObj, CustomerWishlistDto.class);
                customerWishlistDtoList.add(customerWishlistDtoObj);
            });
        } else {
            throw new RecordNotFoundException(messageSource.getMessage("api.customer.id.not.found",
                    new String[]{customerId}, Locale.ENGLISH));
        }
        return customerWishlistDtoList;
    }

    /**
     * Get all customer wishlists grouped by variant id.
     *
     * @return List of CustomerWishlistKVDto containing all customer wishlists grouped by variant id.
     */
    public List<CustomerWishlistKVDto> getAllCustomerWishlist() {
        Set<CustomerWishlistKVDto> customerWishlistKVDtoSet = new HashSet<>();
        List<CustomerWishlist> listCustomerWishlist =
                (List<CustomerWishlist>) customerWishlistRepository.findAll();
        Map<String, List<CustomerWishlist>> customerWishListDtoMap = listCustomerWishlist.
                stream().collect(Collectors.groupingBy(CustomerWishlist::getVariantId));
        Map<String, Long> lcustomerWishListDtoMap = listCustomerWishlist.
                stream().collect(Collectors.groupingBy(CustomerWishlist::getVariantId,
                        Collectors.counting()));
        for (Map.Entry<String, List<CustomerWishlist>> entry : customerWishListDtoMap.entrySet()) {
            String variantId = entry.getKey();
            List<CustomerWishlist> customerWishlist = entry.getValue();
            Long lCountOfSameVariantId = lcustomerWishListDtoMap.getOrDefault(variantId, 0L);
            int totalCountOfSameVariantId = Math.toIntExact(lCountOfSameVariantId);
            customerWishlist.forEach(data -> {
                CustomerWishlistKVDto customerWishlistKVDtoObj;
                customerWishlistKVDtoObj = modelMapper.map(data, CustomerWishlistKVDto.class);
                customerWishlistKVDtoObj.setTotal(totalCountOfSameVariantId);
                customerWishlistKVDtoSet.add(customerWishlistKVDtoObj);
            });
        }
        return new ArrayList<>(customerWishlistKVDtoSet);
    }

    /**
     * Get customer wishlist based on variant id.
     *
     * @param variantId The variant id for which to retrieve the wishlist.
     * @return List of CustomerWishlistDto containing the customer wishlist.
     */
    public List<CustomerWishlistDto> getCustomerWishlistByVariantId(String variantId) {
        List<CustomerWishlistDto> customerWishlistDtoList = new ArrayList<>();
        List<CustomerWishlist> customerWishlist = customerWishlistRepository.findByVariantId(variantId);
        if (!(customerWishlist.isEmpty())) {
            customerWishlist.forEach(customerWishListDataObj -> {
                CustomerWishlistDto customerWishlistDtoObj;
                customerWishlistDtoObj = modelMapper.map(customerWishListDataObj, CustomerWishlistDto.class);
                customerWishlistDtoList.add(customerWishlistDtoObj);
            });
        } else {
            throw new RecordNotFoundException(messageSource.getMessage("api.customer.variant.id.not.found",
                    new String[]{variantId}, Locale.ENGLISH));
        }
        return customerWishlistDtoList;
    }

    /**
     * Get customer feedback.
     *
     * @return List of CustomerFeedbackKVDto containing customer feedback data.
     */
    public List<CustomerFeedbackKVDto> getCustomerFeedback() {
        List<CustomerFeedbackKVDto> customerFeedbackKVDtoList = new ArrayList<>();
        List<CustomerFeedback> customerFeedbackList = customerFeedbackRepository.findAllByOrderByCreatedDateDesc();
        customerFeedbackList.forEach(data -> {
            if (Optional.ofNullable(data.getCustomerName()).isPresent()) {
                CustomerFeedbackKVDto customerFeedbackKVDto;
                customerFeedbackKVDto = modelMapper.map(data, CustomerFeedbackKVDto.class);
                String strCustomerFeedbackCreatedDate =
                        AppUtil.getStringDateFromDate(data.getCreatedDate());
                customerFeedbackKVDto.setCreatedDate(
                        AppUtil.getDateFormatWithOutTimeDate(strCustomerFeedbackCreatedDate));
                customerFeedbackKVDtoList.add(customerFeedbackKVDto);
            }
        });
        return customerFeedbackKVDtoList;
    }

    /**
     * Get customer measurement based on customer id.
     *
     * @param customerId The customer id for which to retrieve the measurements.
     * @return List of CustomerMeasurementDto containing the customer measurements.
     */
    public List<CustomerMeasurementDto> getCustomerMeasurementByCustomerId(String customerId) {
        List<CustomerMeasurementDto> customerMeasurementDtoList = new ArrayList<>();
        List<CustomerMeasurement> customerMeasurementList =
                customerMeasurementRepository.findByCustomerId(customerId);
        if (!(customerMeasurementList.isEmpty())) {
            customerMeasurementList.forEach(data -> {
                CustomerMeasurementDto customerMeasurementDtoObj;
                customerMeasurementDtoObj = modelMapper.map(data, CustomerMeasurementDto.class);
                customerMeasurementDtoList.add(customerMeasurementDtoObj);
            });
        } else {
            throw new RecordNotFoundException(messageSource.getMessage("api.customer.id.not.found",
                    new String[]{customerId}, Locale.ENGLISH));
        }
        return customerMeasurementDtoList;
    }

    public CustomerMeasurementDto getCustomerMeasurementByCustomerMeasurementId(String customerId,String sizeName) {
        CustomerMeasurement customerMeasurementObj = customerMeasurementRepository
                .findByCustomerIdAndName(customerId,sizeName);
        if (Optional.ofNullable(customerMeasurementObj).isPresent()) {
            return modelMapper.map(customerMeasurementObj,CustomerMeasurementDto.class);
        } else {
            throw new RecordNotFoundException(messageSource.getMessage("api.customer.measurement.not.found",
                    null, Locale.ENGLISH));
        }
    }

    /**
     * Delete customer measurement based on customer measurement id.
     *
     * @param customerMeasurementId The id of the customer measurement to be deleted.
     * @return CustomerMeasurementDto containing the deleted customer measurement data.
     */
    public CustomerMeasurementDto deleteCustomerMeasurementById(Integer customerMeasurementId) {
        CustomerMeasurementDto customerMeasurementDto;
        Optional<CustomerMeasurement> customerMeasurement =
                customerMeasurementRepository.findById(customerMeasurementId);
        if (customerMeasurement.isPresent()) {
            customerMeasurementRepository.deleteById(customerMeasurementId);
            customerMeasurementDto = modelMapper.map(customerMeasurement.get(), CustomerMeasurementDto.class);
        } else {
            throw new RecordNotFoundException(messageSource.getMessage("api.customer.measurement.id.not.found",
                    new String[]{customerMeasurementId.toString()}, Locale.ENGLISH));
        }
        return customerMeasurementDto;
    }

    /**
     * Save customer measurement.
     *
     * @param customerMeasurementDto The customer measurement data to be saved.
     * @return CustomerMeasurementDto containing the saved customer measurement data.
     * @throws ParseException        If an error occurs while parsing dates.
     * @throws UniqueRecordException If a measurement with the same name and customer id already exists.
     */
    public CustomerMeasurementDto saveMeasurement(CustomerMeasurementDto customerMeasurementDto)
            throws ParseException {
        CustomerMeasurementDto savedCustomerMeasurementDto;
        CustomerMeasurement customerMeasurement = adminMapperUtil.convertFromCustomerMeasurementDto(
                customerMeasurementDto);
        customerMeasurement.setCreatedDate(AppUtil.getTodayDate());
        customerMeasurement.setCreatedBy(customerMeasurementDto.getCustomerId());
        CustomerMeasurement existingCustomerMeasurement = customerMeasurementRepository.findByNameAndCustomerId(
                customerMeasurement.getName(), customerMeasurement.getCustomerId());
        if (Optional.ofNullable(existingCustomerMeasurement).isPresent()) {
            throw new UniqueRecordException(messageSource.getMessage("api.customer.measurement.already.exist",
                    null, Locale.ENGLISH));
        } else {
            CustomerMeasurement savedCustomerMeasurementObj = customerMeasurementRepository.save(customerMeasurement);
            savedCustomerMeasurementDto = adminMapperUtil.convertToCustomerMeasurementDto(savedCustomerMeasurementObj);
        }
        return savedCustomerMeasurementDto;
    }

    /**
     * Update customer measurement by customer measurement ID and return the updated data as a DTO.
     *
     * @param customerMeasurementDto The updated customer measurement data.
     * @param customerMeasurementId  The ID of the customer measurement to be updated.
     * @return CustomerMeasurementDto containing the updated customer measurement data.
     * @throws ParseException If there is an error in parsing the data.
     */
    public CustomerMeasurementDto updateCustomerMeasurement(CustomerMeasurementDto customerMeasurementDto,
                                                            Integer customerMeasurementId) throws ParseException {
        CustomerMeasurementDto updatedCustomerMeasurementDto;
        Optional<CustomerMeasurement> existingCustomerMeasurementObj = customerMeasurementRepository.
                findById(customerMeasurementId);
        if (existingCustomerMeasurementObj.isPresent()) {
            CustomerMeasurement customerMeasurementObj = adminMapperUtil.convertFromCustomerMeasurementDto(
                    customerMeasurementDto);
            customerMeasurementObj.setUpdatedDate(AppUtil.getTodayDate());
            customerMeasurementObj.setUpdatedBy(customerMeasurementDto.getCustomerId());
            CustomerMeasurement updatedCustomerMeasurementObj = customerMeasurementRepository.
                    save(customerMeasurementObj);
            updatedCustomerMeasurementDto = adminMapperUtil.convertToCustomerMeasurementDto(
                    updatedCustomerMeasurementObj);
        } else {
            throw new RecordNotFoundException(messageSource.getMessage("api.customer.measurement.id.not.found",
                    new String[]{customerMeasurementId.toString()}, Locale.ENGLISH));
        }
        return updatedCustomerMeasurementDto;
    }

    /**
     * Save customer wishlist.
     *
     * @param customerWishlistDto The customer wishlist data to be saved.
     * @return CustomerWishlistDto containing the saved customer wishlist data.
     */
    public CustomerWishlistDto saveCustomerWishlist(CustomerWishlistDto customerWishlistDto) {
        CustomerWishlist customerWishlistObj = modelMapper.map(customerWishlistDto, CustomerWishlist.class);
        CustomerWishlist savedCustomerWishlistObj = customerWishlistRepository.save(customerWishlistObj);
        return modelMapper.map(savedCustomerWishlistObj, CustomerWishlistDto.class);
    }

    /**
     * Get customer wishlist based on customer id and variant id.
     *
     * @param customerId The customer id for which to retrieve the wishlist.
     * @param variantId  The variant id for which to retrieve the wishlist.
     * @return CustomerWishlistDto containing the customer wishlist.
     */
    public CustomerWishlistDto getCustomerWishlistByCustomerIdAndVariantId(String customerId, String variantId) {
        CustomerWishlist existingCustomerWishlistObj = customerWishlistRepository.
                findByCustomerIdAndVariantId(customerId, variantId);
        if (Optional.ofNullable(existingCustomerWishlistObj).isPresent()) {
            return modelMapper.map(existingCustomerWishlistObj, CustomerWishlistDto.class);
        } else {
            throw new RecordNotFoundException(messageSource.getMessage(
                    "api.get.customer.wishlist.customerId.and.variantId.fail",
                    new String[]{customerId, variantId}, Locale.ENGLISH));
        }
    }

    /**
     * Delete customer wishlist based on customer id and variant id.
     *
     * @param customerId The customer id for which to delete the wishlist.
     * @param variantId  The variant id for which to delete the wishlist.
     * @return CustomerWishlistDto containing the deleted customer wishlist data.
     */
    public CustomerWishlistDto deleteCustomerWishlistByCustomerIdAndVariantId(String customerId, String variantId) {
        CustomerWishlist existingCustomerWishlistObj = customerWishlistRepository.
                findByCustomerIdAndVariantId(customerId, variantId);
        if (Optional.ofNullable(existingCustomerWishlistObj).isPresent()) {
            customerWishlistRepository.deleteByCustomerIdAndVariantId(customerId, variantId);
            return modelMapper.map(existingCustomerWishlistObj, CustomerWishlistDto.class);
        } else {
            throw new RecordNotFoundException(messageSource.getMessage(
                    "api.get.customer.wishlist.customerId.and.variantId.fail",
                    new String[]{customerId, variantId}, Locale.ENGLISH));
        }
    }

    /**
     * Retrieves a list of customer data from the Shopify API by making paginated API calls.
     *
     * @return A list of {@link CustomerKeyValueDto} objects containing customer data.
     * @throws IOException if there's an error while making the API calls or processing the response.
     */
    public List<CustomerKeyValueDto> getAllCustomerListFromShopify(String shopifyCustomerEndPoint,
                                                                   String shopifyAccessToken) throws IOException {
        customerServiceLogger.info("Entered get customer details from Shopify API call:::::::::::::::");
        String pageInfo = "";
        List<CustomerKeyValueDto> allCustomerList = new ArrayList<>();
        boolean shouldBreak = false;
        while (!shouldBreak) {
            URL urlConnector = new URL(buildUrlWithPageInfo(shopifyCustomerEndPoint, pageInfo));
            HttpURLConnection httpConnection = createHttpConnection(urlConnector, shopifyAccessToken);
            int responseCode = httpConnection.getResponseCode();
            if (responseCode==HttpURLConnection.HTTP_OK) {
                processHttpResponseForCustomerKeyValueDtoList(httpConnection, allCustomerList);
                String linkHeader = httpConnection.getHeaderField(ApplicationConstants.ShopifyApiHeaders.LINK);
                if (linkHeader==null || !linkHeader.contains(RESPONSE_HINT_FOR_NEXT_PAGE)) {
                    shouldBreak = true;
                } else {
                    pageInfo = parseNextPageInfo(linkHeader);
                }
            } else {
                shouldBreak = true;
            }
            httpConnection.disconnect();
        }
        return allCustomerList;
    }

    /**
     * Builds a URL with the provided base URL and page information for Shopify API call.
     *
     * @param baseUrl  The base URL for the Shopify API.
     * @param pageInfo The page information to be included in the URL.
     * @return A URL string constructed by combining the base URL, required fields, limit, and page info.
     */
    private String buildUrlWithPageInfo(String baseUrl, String pageInfo) {
        return baseUrl + SHOPIFY_ENDPOINT_URL_QUESTIONMARK + SHOPIFY_ENDPOINT_REQUIRED_FIELDS +
                SHOPIFY_ENDPOINT_LIMIT + SPLIT_WITH_AND_SYMBOL + pageInfo;
    }

    /**
     * Creates an HTTP connection with the provided URL and Shopify access token.
     *
     * @param urlConnector       The URL to which the connection will be established.
     * @param shopifyAccessToken The Shopify access token to be set as a request property.
     * @return An instance of HttpURLConnection for the established connection.
     * @throws IOException If an I/O error occurs while opening the connection.
     */
    private HttpURLConnection createHttpConnection(URL urlConnector, String shopifyAccessToken) throws IOException {
        HttpURLConnection httpConnection = (HttpURLConnection) urlConnector.openConnection();
        httpConnection.setRequestMethod(ApplicationConstants.ShopifyApiHeaders.GET_REQUEST_METHOD);
        httpConnection.setRequestProperty(ApplicationConstants.ShopifyApiHeaders.SHOPIFY_ACCESS_TOKEN,
                shopifyAccessToken);
        return httpConnection;
    }

    /**
     * Processes the HTTP response from the given HttpURLConnection to extract customer data and populate the list.
     *
     * @param httpConnection  The HttpURLConnection from which to read the response.
     * @param allCustomerList The list to populate with CustomerKeyValueDto objects.
     * @throws IOException If an I/O error occurs while reading the response.
     */
    void processHttpResponseForCustomerKeyValueDtoList(HttpURLConnection httpConnection,
                                                       List<CustomerKeyValueDto> allCustomerList) throws IOException {
        try (BufferedReader httpConnectionBufferReader = new BufferedReader(new InputStreamReader(
                httpConnection.getInputStream()))) {
            String sbNewLine = httpConnectionBufferReader.lines().collect(Collectors.joining());
            JSONObject responseObject = new JSONObject(sbNewLine);
            JSONArray customers = responseObject.getJSONArray(DomainObject.Customer.CUSTOMERS);
            List<CustomerKeyValueDto> customerList = IntStream.range(0, customers.length())
                    .mapToObj(customers::getJSONObject)
                    .map(this::createCustomerKeyValueDto)
                    .collect(Collectors.toList());
            List<CustomerKeyValueDto> filterCustomerList = customerList.stream().
                    filter(customerObj -> Objects.nonNull(customerObj.getFirst_name())).collect(Collectors.toList());
            allCustomerList.addAll(filterCustomerList);
        }
    }

    /**
     * Creates a {@link CustomerKeyValueDto} object from the JSON representation of a customer obtained from the API.
     *
     * @param customerObj The JSON object representing a customer's data.
     * @return A {@link CustomerKeyValueDto} object containing customer data.
     */
    public CustomerKeyValueDto createCustomerKeyValueDto(JSONObject customerObj) {
        customerServiceLogger.info("Entered customer key value dto::::::::::::::::::");
        CustomerKeyValueDto customerKeyValueDto = new CustomerKeyValueDto();
        if (!customerObj.isNull(DomainObject.Customer.FIRST_NAME)) {
            customerKeyValueDto.setFirst_name(customerObj.optString(DomainObject.Customer.FIRST_NAME, DEFAULT_EMPTY));
            customerKeyValueDto.setVerified_email(customerObj.optBoolean(
                    DomainObject.Customer.VERIFIED_EMAIL, false));
            customerKeyValueDto.setPhone(customerObj.optString(DomainObject.Customer.PHONE, DEFAULT_EMPTY));
            customerKeyValueDto.setOrders(customerObj.optInt(DomainObject.Customer.ORDERS_COUNT, 0) +
                    ApplicationConstants.Orders.ORDERS);
            customerKeyValueDto.setOrderPrice(ApplicationConstants.Orders.PRICE_IN_RUPEES +
                    customerObj.optDouble(DomainObject.Customer.TOTAL_SPENT, 0.0));
            customerKeyValueDto.setId(customerObj.optLong(DomainObject.Customer.ID, 0L));
            customerKeyValueDto.setEmail(customerObj.optString(DomainObject.Customer.EMAIL, DEFAULT_EMPTY));
        }
        return customerKeyValueDto;
    }

    /**
     * Parses the "Link" header in the HTTP response to extract the URL of the next page of results.
     *
     * @param linkHeader The "Link" header containing the URL of the next page.
     * @return The URL of the next page of results, or {@code null} if there's no next page.
     */
    String parseNextLink(String linkHeader) {
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
    Map<String, String> parseQueryParameters(String link) {
        Map<String, String> params = new HashMap<>();
        String[] queryParams = link.substring(
                link.indexOf(SHOPIFY_ENDPOINT_URL_QUESTIONMARK) + 1).split(SPLIT_WITH_AND_SYMBOL);
        for (String queryParam : queryParams) {
            String[] param = queryParam.split(EQUAL_TO);
            params.put(param[0], param[1]);
        }
        return params;
    }

    /**
     * Retrieves customer count data by month for the specified years from the Shopify API.
     *
     * @param lastYear    The last year for which to fetch customer count data.
     * @param currentYear The current year for which to fetch customer count data.
     * @return A CustomerCountResponseDto containing the customer count data for the specified years.
     * @throws IOException If an I/O error occurs while making API calls.
     */
    public CustomerCountResponseDto getCustomerCountByMonthForYears(String shopifyCustomerEndPoint, int lastYear,
                                                                    int currentYear, String shopifyAccessToken)
            throws IOException {
        CustomerCountResponseDto customerCountResponseDtoObj = new CustomerCountResponseDto();
        Map<String, Map<String, Double>> customerCountYearData = new HashMap<>();
        List<String> calendarMonthsList = getCalendarMonthsList();
        Map<String, Double> lastYearData = calendarMonthsList.stream().collect(Collectors.toMap(
                month -> month,
                month -> DEFAULT_DOUBLE_VALUE));
        Map<String, Double> currentYearData = calendarMonthsList.stream().collect(Collectors.toMap(
                month -> month,
                month -> DEFAULT_DOUBLE_VALUE));
        fetchCustomerDataFromShopify(shopifyCustomerEndPoint, lastYearData, lastYear, shopifyAccessToken);
        fetchCustomerDataFromShopify(shopifyCustomerEndPoint, currentYearData, currentYear, shopifyAccessToken);
        customerCountYearData.put(Integer.toString(lastYear), lastYearData);
        customerCountYearData.put(Integer.toString(currentYear), currentYearData);
        double lastYearCount = lastYearData.values().stream().mapToDouble(Double::doubleValue).sum();
        double currentYearCount = currentYearData.values().stream().mapToDouble(Double::doubleValue).sum();
        BigDecimal difference = BigDecimal.valueOf(currentYearCount - lastYearCount);
        BigDecimal percentage = difference.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        customerCountResponseDtoObj.setCustomerCountByYear(customerCountYearData);
        customerCountResponseDtoObj.setPercentChange(percentage);
        return customerCountResponseDtoObj;
    }

    /**
     * Fetches customer data from the Shopify API and processes it to count the customers for the specified year.
     *
     * @param url          The URL of the Shopify API endpoint.
     * @param customerData The map to store the customer count data.
     * @param year         The year for which to count the customers.
     * @throws IOException If an I/O error occurs while making API calls.
     */
    void fetchCustomerDataFromShopify(String url, Map<String, Double> customerData,
                                      int year, String shopifyAccessToken) throws IOException {
        String authorization = shopifyAccessToken;
        String pageInfo = "";
        boolean shouldBreak = false;
        while (!shouldBreak) {
            HttpURLConnection httpConnection = createHttpConnection(url, pageInfo, authorization);
            int responseCode = httpConnection.getResponseCode();
            if (responseCode==HttpURLConnection.HTTP_OK) {
                processHttpResponseForCustomer(httpConnection, customerData, year);
                String linkHeader = httpConnection.getHeaderField(ApplicationConstants.ShopifyApiHeaders.LINK);
                if (linkHeader==null || !linkHeader.contains(RESPONSE_HINT_FOR_NEXT_PAGE)) {
                    shouldBreak = true;
                } else {
                    pageInfo = parseNextPageInfo(linkHeader);
                }
            } else {
                shouldBreak = true;
            }
            httpConnection.disconnect();
        }
    }

    /**
     * Creates an HttpURLConnection for making a GET request to the specified URL with query parameters.
     *
     * @param url           The base URL for the connection.
     * @param pageInfo      The page information to include in the URL.
     * @param authorization The authorization token to set as a request property.
     * @return The created HttpURLConnection.
     * @throws IOException If an I/O error occurs while creating the connection.
     */
    private HttpURLConnection createHttpConnection(String url, String pageInfo, String authorization)
            throws IOException {
        URL urlConnector = new URL(url + SHOPIFY_ENDPOINT_URL_QUESTIONMARK +
                SHOPIFY_ENDPOINT_REQUIRED_FIELDS + SHOPIFY_ENDPOINT_LIMIT + SPLIT_WITH_AND_SYMBOL + pageInfo);
        HttpURLConnection httpConnection = (HttpURLConnection) urlConnector.openConnection();
        httpConnection.setRequestMethod(ApplicationConstants.ShopifyApiHeaders.GET_REQUEST_METHOD);
        httpConnection.setRequestProperty(ApplicationConstants.ShopifyApiHeaders.SHOPIFY_ACCESS_TOKEN,
                authorization);
        return httpConnection;
    }

    /**
     * Processes the HTTP response from a HttpURLConnection for customer data and updates the customerData map.
     *
     * @param httpConnection The HttpURLConnection to process the response from.
     * @param customerData   The map to update with customer data.
     * @param year           The target year for filtering customer data.
     * @throws IOException If an I/O error occurs while processing the response.
     */
    void processHttpResponseForCustomer(HttpURLConnection httpConnection, Map<String, Double> customerData, int year)
            throws IOException {
        try (BufferedReader httpConnectionBufferReader = new BufferedReader(new InputStreamReader(
                httpConnection.getInputStream()))) {
            String sbNewLine = httpConnectionBufferReader.lines().collect(Collectors.joining());
            Gson gsonObject = new Gson();
            CustomerResponseDto customerResponseDtoObj =
                    gsonObject.fromJson(sbNewLine, CustomerResponseDto.class);
            List<CustomerDto> customerList = customerResponseDtoObj.getCustomers();
            for (CustomerDto customerDto : customerList) {
                processCustomerData(customerDto, customerData, year);
            }
        }
    }

    /**
     * Processes customer data and updates the customerData map based on the customer's creation year and month.
     *
     * @param customerDto  The CustomerDto containing customer information.
     * @param customerData The map to update with customer data.
     * @param year         The target year for filtering customer data.
     */
    void processCustomerData(CustomerDto customerDto, Map<String, Double> customerData, int year) {
        String createdAt = AppUtil.getStringDateFromDate(customerDto.getCreated_at());
        int customerYear = Integer.parseInt(createdAt.substring(0, 4));
        if (customerYear==year) {
            String createdMonth = AppUtil.getCreatedMonth(customerDto.getCreated_at());
            customerData.putIfAbsent(createdMonth, 0.0);
            customerData.put(createdMonth, customerData.get(createdMonth) + 1);
        }
    }

    /**
     * Parses the next page information from the link header and returns it.
     *
     * @param linkHeader The link header containing pagination information.
     * @return The next page information as a formatted string, or an empty string if not present.
     */
    String parseNextPageInfo(String linkHeader) {
        String nextLink = parseNextLink(linkHeader);
        if (Optional.ofNullable(nextLink).isPresent()) {
            Map<String, String> params = parseQueryParameters(nextLink);
            return ApplicationConstants.ShopifyApiHeaders.PAGE_INFO +
                    EQUAL_TO +
                    params.get(ApplicationConstants.ShopifyApiHeaders.PAGE_INFO).
                            replace(REPLACE_WITH_GREATER_THAN, DEFAULT_EMPTY);
        }
        return "";
    }

    /**
     * Perform a password reset for a customer on Shopify.
     *
     * @param resetPasswordDto The {@link CustomerResetPasswordDto} containing the new password and confirmation.
     * @param customerId       The ID of the customer for whom the password needs to be reset.
     * @return The {@link ResetPasswordDto} containing the updated customer's information after the password reset.
     */
    public ResetPasswordDto customerLoginPasswordReset(CustomerResetPasswordDto resetPasswordDto, Long customerId,
                                                       String shopifyAccessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(ApplicationConstants.ShopifyApiHeaders.SHOPIFY_ACCESS_TOKEN, shopifyAccessToken);
        HttpEntity<CustomerResetPasswordDto> requestEntity = new HttpEntity<>(resetPasswordDto, headers);
        String apiUrl = shopifyResetPasswordEndPoint + customerId + SHOPIFY_ENDPOINT_JSON;
        restTemplate.exchange(apiUrl, HttpMethod.PUT, requestEntity, CustomerResetPasswordDto.class);
        return resetPasswordDto.getCustomer();
    }

    /**
     * Generates and saves an OTP (One-Time Password) for the customer with the given email.
     *
     * @param customerEmail The email of the customer for whom the OTP is generated.
     * @return A {@link CustomerOtpDto} containing the details of the saved OTP.
     */
    public CustomerOtpDto saveCustomerOtp(String customerEmail) {
        new CustomerOtp();
        CustomerOtp savedCustomerOtpObj;
        CustomerOtp existingCustomerOtpObj = customerOtpRepository.findByCustomerEmail(customerEmail);
        int randomOtpCode = random.nextInt(
                RANDOM_GENERATION_CODE_MAX_VALUE - RANDOM_GENERATION_CODE_MIN_VALUE + 1) +
                RANDOM_GENERATION_CODE_MIN_VALUE;
        if (Optional.ofNullable(existingCustomerOtpObj).isPresent()) {
            existingCustomerOtpObj.setCode(randomOtpCode);
            savedCustomerOtpObj = customerOtpRepository.save(existingCustomerOtpObj);
        } else {
            CustomerOtp customerOtpObj = new CustomerOtp();
            customerOtpObj.setCustomerEmail(customerEmail);
            customerOtpObj.setCode(randomOtpCode);
            savedCustomerOtpObj = customerOtpRepository.save(customerOtpObj);
        }
        sendOtpCodeForCustomer(savedCustomerOtpObj);
        return modelMapper.map(savedCustomerOtpObj, CustomerOtpDto.class);
    }

    /**
     * Sends the generated OTP code to the customer via email.
     *
     * @param customerOtp The {@link CustomerOtp} object containing the OTP code and customer email.
     */
    public void sendOtpCodeForCustomer(CustomerOtp customerOtp) {
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put(ApplicationConstants.Email.CUSTOMER_OTP_CODE, customerOtp.getCode());
        Template templateObj = velocityEngine.getTemplate("emailtemplate/customerotp.vm");
        StringWriter stringWriter = new StringWriter();
        templateObj.merge(velocityContext, stringWriter);
        Notification emailNotification = new Notification(fromEmail, customerOtp.getCustomerEmail(),
                ApplicationConstants.EmailSubject.LOGIN_OTP, stringWriter.toString());
        emailNotificationService.sendLoginIssueMailForAdmin(emailNotification);
    }

    /**
     * Validates the provided OTP code for a customer.
     *
     * @param customerOtpDto The {@link CustomerOtpDto} object containing the customer email and OTP code.
     * @return The {@link CustomerOtpDto} object representing the validated customer OTP.
     * @throws RecordNotFoundException if the provided OTP code does not match the one stored in the database.
     */
    public CustomerOtpDto validateCustomerOtp(CustomerOtpDto customerOtpDto) {
        CustomerOtpDto existingCustomerOtpDtoObj;
        CustomerOtp existingCustomerOtpObj = customerOtpRepository.findByCustomerEmailAndCode(
                customerOtpDto.getCustomerEmail(), customerOtpDto.getCode());
        if (Optional.ofNullable(existingCustomerOtpObj).isPresent()) {
            existingCustomerOtpDtoObj = modelMapper.map(existingCustomerOtpObj, CustomerOtpDto.class);
        } else {
            throw new RecordNotFoundException(
                    messageSource.getMessage("api.customer.otp.code.not.found", null, Locale.ENGLISH));
        }
        return existingCustomerOtpDtoObj;
    }

    /**
     * Retrieves filtered customer count data based on the provided date range.
     *
     * @param fromDate The start date of the filter range.
     * @param toDate   The end date of the filter range.
     * @return A {@link CustomerCountResponseDto} object containing customer count data
     * for each month within the filter range.
     * @throws IOException    If an error occurs while retrieving the data.
     * @throws ParseException If an error occurs while retrieving the data.
     */
    public CustomerCountResponseDto getFilterCustomerList(
            String shopifyCustomerEndPoint, String fromDate, String toDate) throws IOException, ParseException {
        CustomerCountResponseDto customerCountResponseDtoObj = new CustomerCountResponseDto();
        Map<String, Map<String, Double>> customerCountYearData = new HashMap<>();
        List<String> calendarMonthsList = getCalendarMonthsList();
        Map<String, Double> yearData = calendarMonthsList.stream().collect(Collectors.toMap(
                month -> month,
                month -> DEFAULT_DOUBLE_VALUE));
        List<CustomerDto> filterCustomerList =
                getFilterCustomerListFromShopify(shopifyCustomerEndPoint, fromDate, toDate, shopifyAccessToken);
        filterCustomerList.forEach(data -> {
            String createdMonth = AppUtil.getCreatedMonth(data.getCreated_at());
            yearData.put(createdMonth, yearData.get(createdMonth) + 1);
        });
        String getYearFromStrDate = AppUtil.getYearFromStrDate(toDate);
        customerCountYearData.put(getYearFromStrDate, yearData);
        double yearCount = yearData.values().stream().mapToDouble(Double::doubleValue).sum();
        BigDecimal percentage = BigDecimal.valueOf(yearCount).
                divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        customerCountResponseDtoObj.setCustomerCountByYear(customerCountYearData);
        customerCountResponseDtoObj.setPercentChange(percentage);
        return customerCountResponseDtoObj;
    }

    /**
     * Retrieves a list of calendar months.
     *
     * @return A list containing the names of all calendar months.
     */
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
     * Retrieves filtered customer details from the Shopify API based on the provided date range.
     *
     * @param fromDate The start date of the filter range.
     * @param toDate   The end date of the filter range.
     * @return A list of {@link CustomerDto} objects representing the filtered customer details.
     * @throws IOException If an error occurs while retrieving the data.
     */
    public List<CustomerDto> getFilterCustomerListFromShopify(String shopifyCustomerEndPoint, String fromDate,
                                                       String toDate, String shopifyAccessToken) throws IOException {
        customerServiceLogger.info("Entered get filtered customer details from Shopify API call:::::::::::::::");
        String pageInfo = null;
        List<CustomerDto> allCustomerList = new ArrayList<>();
        boolean shouldBreak = false;
        while (!shouldBreak) {
            String orderEndPoint = buildOrderEndPoint(shopifyCustomerEndPoint, fromDate, toDate, pageInfo);
            HttpURLConnection httpConnection = createHttpConnection(orderEndPoint, shopifyAccessToken);
            int responseCode = httpConnection.getResponseCode();
            if (responseCode==HttpURLConnection.HTTP_OK) {
                processHttpResponseForCustomerDtoList(httpConnection, allCustomerList);
                String linkHeader = httpConnection.getHeaderField(ApplicationConstants.ShopifyApiHeaders.LINK);
                if (linkHeader==null || !linkHeader.contains(RESPONSE_HINT_FOR_NEXT_PAGE)) {
                    shouldBreak = true;
                } else {
                    pageInfo = parseNextPageInfo(linkHeader);
                }
            } else {
                shouldBreak = true;
            }
            httpConnection.disconnect();
        }
        return allCustomerList;
    }

    /**
     * Builds the endpoint URL for querying customer data from Shopify API.
     *
     * @param fromDate The start date for filtering customer data.
     * @param toDate   The end date for filtering customer data.
     * @param pageInfo The pagination information for the API request.
     * @return The constructed endpoint URL.
     */
    private String buildOrderEndPoint(String shopifyCustomerEndPoint, String fromDate, String toDate, String pageInfo) {
        if (Optional.ofNullable(pageInfo).isPresent()) {
            return shopifyCustomerEndPoint + SHOPIFY_ENDPOINT_URL_QUESTIONMARK + pageInfo;
        } else {
            return shopifyCustomerEndPoint + SHOPIFY_ENDPOINT_URL_QUESTIONMARK +
                    SHOPIFY_ENDPOINT_PARAM_PROCESSED_AT_MIN + fromDate +
                    SHOPIFY_ENDPOINT_PARAM_PROCESSED_AT_MAX + toDate +
                    SPLIT_WITH_AND_SYMBOL + SHOPIFY_ENDPOINT_LIMIT;
        }
    }

    /**
     * Creates an HTTP connection for the given URL with the specified authorization token.
     *
     * @param url           The URL to establish the connection to.
     * @param authorization The authorization token to be included in the request header.
     * @return An HttpURLConnection object representing the established connection.
     * @throws IOException If an I/O error occurs while creating the connection.
     */
    HttpURLConnection createHttpConnection(String url, String authorization) throws IOException {
        URL urlConnector = new URL(url);
        HttpURLConnection httpConnection = (HttpURLConnection) urlConnector.openConnection();
        httpConnection.setRequestMethod(ApplicationConstants.ShopifyApiHeaders.GET_REQUEST_METHOD);
        httpConnection.setRequestProperty(ApplicationConstants.ShopifyApiHeaders.SHOPIFY_ACCESS_TOKEN, authorization);
        return httpConnection;
    }

    /**
     * Processes the HTTP response from a connection and populates a list with CustomerDto objects.
     *
     * @param httpConnection  The HttpURLConnection representing the established connection.
     * @param allCustomerList The list to which the processed CustomerDto objects will be added.
     * @throws IOException If an I/O error occurs while processing the response.
     */
    void processHttpResponseForCustomerDtoList(HttpURLConnection httpConnection, List<CustomerDto> allCustomerList)
            throws IOException {
        try (BufferedReader httpConnectionBufferReader = new BufferedReader(new InputStreamReader(
                httpConnection.getInputStream()))) {
            String sbNewLine = httpConnectionBufferReader.lines().collect(Collectors.joining());
            Gson gsonObject = new Gson();
            CustomerResponseDto customerResponseDtoObj = gsonObject.fromJson(sbNewLine, CustomerResponseDto.class);
            List<CustomerDto> customerList = customerResponseDtoObj.getCustomers();
            allCustomerList.addAll(customerList);
        }
    }

    /**
     * Retrieves filtered customer feedback within the specified date range.
     *
     * @param fromDate The start date of the filter range.
     * @param toDate   The end date of the filter range.
     * @return A list of {@link CustomerFeedbackKVDto} objects representing the filtered customer feedback.
     * @throws ParseException If there is an error parsing the date strings.
     */
    public List<CustomerFeedbackKVDto> getCustomerFeedbackFilter(String fromDate, String toDate) throws ParseException {
        List<CustomerFeedbackKVDto> customerFeedbackKVDtoList = new ArrayList<>();
        Date createdFromDateObj = AppUtil.getDateFromStrDate(fromDate);
        Date createdToDateObj = AppUtil.getDateFromStrDate(toDate);
        List<CustomerFeedback> customerFeedbackList = customerFeedbackRepository.
                findByCreatedDateBetweenOrderByCreatedDateDesc(createdFromDateObj, createdToDateObj);
        customerFeedbackList.forEach(data -> {
            if (Optional.ofNullable(data.getCustomerName()).isPresent()) {
                CustomerFeedbackKVDto customerFeedbackKVDto = new CustomerFeedbackKVDto();
                customerFeedbackKVDto = modelMapper.map(data, CustomerFeedbackKVDto.class);
                String strCustomerFeedbackCreatedDate =
                        AppUtil.getStringDateFromDate(data.getCreatedDate());
                customerFeedbackKVDto.setCreatedDate(
                        AppUtil.getDateFormatWithOutTimeDate(strCustomerFeedbackCreatedDate));
                customerFeedbackKVDtoList.add(customerFeedbackKVDto);
            }
        });
        return customerFeedbackKVDtoList;
    }

    /**
     * Creates a customer using the provided CustomerCreationKVDto object and sends a POST request to a
     * Shopify API endpoint.
     *
     * @param customerCreationKVDto The DTO (Data Transfer Object) containing customer information to be created.
     * @return A CustomerCreationKVDto object representing the created customer or the response from the Shopify API.
     */
    public CustomerCreationKVDto createCustomer(CustomerCreationKVDto customerCreationKVDto,
                                                String shopifyCustomerCreationEndPoint, String shopifyAccessToken) {
        customerCreationKVDto.getCustomer().setVerified_email(Boolean.TRUE);
        List<CustomerKvDto.Addressess> addressessList = new ArrayList<>();
        CustomerKvDto.Addressess addressess = new CustomerKvDto.Addressess();
        addressess.setCountry(DEFAULT_COUNTRY_CODE);
        addressessList.add(addressess);
        customerCreationKVDto.getCustomer().setAddressess(addressessList);
        customerCreationKVDto.getCustomer().setPassword(DEFAULT_PASSWORD);
        customerCreationKVDto.getCustomer().setPassword_confirmation(DEFAULT_PASSWORD);
        customerCreationKVDto.getCustomer().setSend_email_welcome(Boolean.TRUE);
        customerCreationKVDto.getCustomer().setAccepts_marketing(Boolean.TRUE);
        Gson gson = new Gson();
        String customerCreationJsonPayload = gson.toJson(customerCreationKVDto);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set(ApplicationConstants.ShopifyApiHeaders.SHOPIFY_ACCESS_TOKEN, shopifyAccessToken);
        HttpEntity<String> httpEntity = new HttpEntity<>(customerCreationJsonPayload, httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(shopifyCustomerCreationEndPoint,
                    HttpMethod.POST, httpEntity, String.class);
            JSONObject jsonResponse = new JSONObject(responseEntity.getBody());
            return gson.fromJson(jsonResponse.toString(), CustomerCreationKVDto.class);
        } catch (HttpClientErrorException httpClientErrorException) {
            throw new UniqueRecordException(messageSource.getMessage("api.customer.saved.exception", null,
                    Locale.ENGLISH));
        }
    }

    /**
     * Updates customer details on a Shopify platform using the provided JSON payload, access token, and Gson instance.
     *
     * @param customerUpdationJsonPayload The JSON payload containing updated customer details.
     * @param shopifyUpdateCustomerDetailsEndPoint The Shopify API endpoint for updating customer details.
     * @param shopifyAccessToken The access token for authentication with the Shopify API.
     * @param gson The Gson instance for JSON serialization and deserialization.
     * @return A CustomerDetailKeyValueResponseDto containing the updated customer details.
     * @throws UniqueRecordException if an exception occurs during the update process.
     */
    public CustomerDetailKeyValueResponseDto updateCustomerDetails(String customerUpdationJsonPayload,
        String shopifyUpdateCustomerDetailsEndPoint, String shopifyAccessToken, Gson gson) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set(ApplicationConstants.ShopifyApiHeaders.SHOPIFY_ACCESS_TOKEN, shopifyAccessToken);
        HttpEntity<String> httpEntity = new HttpEntity<>(customerUpdationJsonPayload, httpHeaders);
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(shopifyUpdateCustomerDetailsEndPoint,
                    HttpMethod.PUT, httpEntity, String.class);
            JSONObject jsonResponse = new JSONObject(responseEntity.getBody());
            return gson.fromJson(jsonResponse.toString(), CustomerDetailKeyValueResponseDto.class);
        } catch (HttpClientErrorException httpClientErrorException) {
            throw new UniqueRecordException(messageSource.getMessage("api.customer.updation.exception", null,
                    Locale.ENGLISH));
        }
    }

    /**
     * Save customer measurement feedback and return the updated DTO.
     *
     * @param customerMeasurementFeedbackDto The DTO containing feedback data to be saved.
     * @return Updated CustomerMeasurementFeedbackDto after saving.
     * @throws ParseException If there is an issue with date parsing.
     * @throws JsonProcessingException If there is an issue with JSON processing.
     */
    public CustomerMeasurementFeedbackDto saveCustomerMeasurementFeedback(
            CustomerMeasurementFeedbackDto customerMeasurementFeedbackDto) throws ParseException,
            JsonProcessingException {
        CustomerMeasurementFeedBack customerMeasurementFeedBack = adminMapperUtil.
                convertFromCustomerMeasurementFeedbackDto( customerMeasurementFeedbackDto);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode feedbackJsonNode = objectMapper.readTree(customerMeasurementFeedBack.getFeedback());
        customerMeasurementFeedBack.setCreatedBy(feedbackJsonNode.findPath("customerId").textValue());
        customerMeasurementFeedBack.setCreatedDate(AppUtil.getTodayDate());
       CustomerMeasurementFeedBack savedcustomerMeasurementFeedBack= customerMeasurementFeedbackRepositoryI.
               save(customerMeasurementFeedBack);
       CustomerMeasurement existingCustomerMeasurementObj = customerMeasurementRepository
               .findByCustomerIdAndName(feedbackJsonNode.findPath("customerId").textValue(),
                       feedbackJsonNode.findPath("sizeName").textValue());
       existingCustomerMeasurementObj.setIsFeedbackFormSubmit(
               savedcustomerMeasurementFeedBack.getIsFeedbackFormSubmit());
       existingCustomerMeasurementObj.setUpdatedDate(AppUtil.getTodayDate());
       existingCustomerMeasurementObj.setUpdatedBy(feedbackJsonNode.findPath("customerId").textValue());
        customerMeasurementRepository.save(existingCustomerMeasurementObj);
        adminService.updateCustomerOrderStatusDetails(savedcustomerMeasurementFeedBack.getOrderId(),
                ApplicationConstants.CustomerOrderStatusDetails.STRING_FIT_SAMPLE +
                        ApplicationConstants.CustomerOrderStatus.FEEDBACK_RECEIVED);
        return adminMapperUtil.convertFromCustomerMeasurementFeedBack(savedcustomerMeasurementFeedBack);
    }

    /**
     * Retrieve customer measurement feedback by order ID and return the DTO.
     *
     * @param orderId The order ID used to fetch feedback data.
     * @return CustomerMeasurementFeedbackDto containing the retrieved feedback data.
     */
    public CustomerMeasurementFeedbackDto getCustomerMeasurementFeedbackByOrderId(String orderId) {
        CustomerMeasurementFeedBack customerMeasurementFeedBack =  customerMeasurementFeedbackRepositoryI.
                getCustomerMeasurementFeedbackByOrderId(orderId);
        if(Optional.ofNullable(customerMeasurementFeedBack).isPresent()) {
            return modelMapper.map(customerMeasurementFeedBack, CustomerMeasurementFeedbackDto.class);
        } else {
            throw new RecordNotFoundException(messageSource.getMessage(
                    "api.customer.measurement.feedback.not.found", null,null));
        }
    }

    /**
     * Update customer measurement feedback by its ID and return the updated DTO.
     *
     * @param customerMeasurementFeedbackId The ID of the feedback to update.
     * @param loggedInUserId The ID of the logged-in user performing the update.
     * @return Updated CustomerMeasurementFeedbackDto after the update.
     * @throws ParseException If there is an issue with date parsing.
     * @throws JsonProcessingException If there is an issue with JSON processing.
     */

    public CustomerMeasurementFeedbackDto updateCustomerMeasurementFeedbackByCustomerMeasurementId(
            Integer customerMeasurementFeedbackId, CustomerMeasurementFeedbackDto customerMeasurementFeedbackDto,
            Integer loggedInUserId) throws ParseException, JsonProcessingException {
        Optional<CustomerMeasurementFeedBack> existingCustomerMeasurementFeedback =
                customerMeasurementFeedbackRepositoryI.findById(customerMeasurementFeedbackId);
        if (existingCustomerMeasurementFeedback.isPresent()) {
            existingCustomerMeasurementFeedback.get().setFeedback(customerMeasurementFeedbackDto.getFeedback());
            existingCustomerMeasurementFeedback.get().setIsApproved(customerMeasurementFeedbackDto.getIsApproved());
            existingCustomerMeasurementFeedback.get().setUpdatedBy(loggedInUserId);
            existingCustomerMeasurementFeedback.get().setUpdatedDate(AppUtil.getTodayDate());
            customerMeasurementFeedbackRepositoryI.save(existingCustomerMeasurementFeedback.get());
        } else {
            throw new RecordNotFoundException(messageSource.getMessage(
                    "api.customer.measurement.feedback.id.not.found",
                    new String[]{customerMeasurementFeedbackId.toString()}, Locale.ENGLISH)
            );
        }
        ObjectMapper objectMapper = new ObjectMapper();
        String feedbackString = existingCustomerMeasurementFeedback.get().getFeedback();
        JsonNode feedbackJsonNode = objectMapper.readTree(feedbackString);

        String customerId =feedbackJsonNode.findPath(DomainObject.CustomerMeasurementFeedback.CUSTOMER_ID).textValue();
        String sizeName = feedbackJsonNode.findPath(DomainObject.CustomerMeasurementFeedback.SIZE_NAME).textValue();
        CustomerMeasurement customerMeasurement = customerMeasurementRepository.
                findByCustomerIdAndName(customerId,sizeName);

        customerMeasurement.setShoulderWidth(feedbackJsonNode.
                findPath(DomainObject.CustomerMeasurementFeedback.SHOULDER_WIDTH).textValue());
        customerMeasurement.setHalfChestWidth(feedbackJsonNode.
                findPath(DomainObject.CustomerMeasurementFeedback.HALF_CHEST_WIDTH).textValue());
        customerMeasurement.setHalfBottomWidth(feedbackJsonNode.
                findPath(DomainObject.CustomerMeasurementFeedback.HALF_BOTTOM_WIDTH).textValue());
        customerMeasurement.setNeckWidth(feedbackJsonNode.
                findPath(DomainObject.CustomerMeasurementFeedback.NECK_WIDTH).textValue());
        customerMeasurement.setFrontNeckDrop(feedbackJsonNode.
                findPath(DomainObject.CustomerMeasurementFeedback.FRONT_NECK_DROP).textValue());
        customerMeasurement.setCbLength(feedbackJsonNode.
                findPath(DomainObject.CustomerMeasurementFeedback.CB_LENGTH).textValue());
        customerMeasurement.setSleeveLength(feedbackJsonNode.
                findPath(DomainObject.CustomerMeasurementFeedback.SLEEVE_LENGTH).textValue());
        customerMeasurement.setSleeveOpening(feedbackJsonNode.
                findPath(DomainObject.CustomerMeasurementFeedback.SLEEVE_OPENING).textValue());
        customerMeasurement.setArmholeStraight(feedbackJsonNode.
                findPath(DomainObject.CustomerMeasurementFeedback.ARM_HOLE_STRAIGTH).textValue());
        customerMeasurement.setUpdatedDate(AppUtil.getTodayDate());
        customerMeasurement.setUpdatedBy(loggedInUserId.toString());
        customerMeasurement.setIsNewSize(false);
        customerMeasurementRepository.save(customerMeasurement);

        CustomerOrderStatus existingCustomerOrderStatus = customerOrderStatusRepositoryI.
                findByOrderId(existingCustomerMeasurementFeedback.get().getOrderId()).get(0);
        existingCustomerOrderStatus.getCustomerMeasurement().setShoulderWidth(feedbackJsonNode.
                findPath(DomainObject.CustomerMeasurementFeedback.SHOULDER_WIDTH).textValue());
        existingCustomerOrderStatus.getCustomerMeasurement().setHalfChestWidth(feedbackJsonNode.
                findPath(DomainObject.CustomerMeasurementFeedback.HALF_CHEST_WIDTH).textValue());
        existingCustomerOrderStatus.getCustomerMeasurement().setHalfBottomWidth(feedbackJsonNode.
                findPath(DomainObject.CustomerMeasurementFeedback.HALF_BOTTOM_WIDTH).textValue());
        existingCustomerOrderStatus.getCustomerMeasurement().setNeckWidth(feedbackJsonNode.
                findPath(DomainObject.CustomerMeasurementFeedback.NECK_WIDTH).textValue());
        existingCustomerOrderStatus.getCustomerMeasurement().setFrontNeckDrop(feedbackJsonNode.
                findPath(DomainObject.CustomerMeasurementFeedback.FRONT_NECK_DROP).textValue());
        existingCustomerOrderStatus.getCustomerMeasurement().setCbLength(feedbackJsonNode.
                findPath(DomainObject.CustomerMeasurementFeedback.CB_LENGTH).textValue());
        existingCustomerOrderStatus.getCustomerMeasurement().setSleeveLength(feedbackJsonNode.
                findPath(DomainObject.CustomerMeasurementFeedback.SLEEVE_LENGTH).textValue());
        existingCustomerOrderStatus.getCustomerMeasurement().setSleeveOpening(feedbackJsonNode.
                findPath(DomainObject.CustomerMeasurementFeedback.SLEEVE_OPENING).textValue());
        existingCustomerOrderStatus.getCustomerMeasurement().setArmholeStraight(feedbackJsonNode.
                findPath(DomainObject.CustomerMeasurementFeedback.ARM_HOLE_STRAIGTH).textValue());
        JsonNode addressInformationJsonNode = objectMapper.readTree(existingCustomerOrderStatus.getAddressInformation());
        ((com.fasterxml.jackson.databind.node.ObjectNode) addressInformationJsonNode)
                .put("orderStatus", "Edit Measurements");
        String updatedAddressInformationJsonString = objectMapper
                .writeValueAsString(addressInformationJsonNode);
        existingCustomerOrderStatus.setAddressInformation(updatedAddressInformationJsonString);
        customerOrderStatusRepositoryI.save(existingCustomerOrderStatus);
        CustomerOrderStatusDto customerOrderStatusDto = adminService.updateCustomerOrderStatus(
                existingCustomerOrderStatus.getCustomerOrderStatusId(), "Measurement Updated",
                null, existingCustomerOrderStatus.getAddressInformation());
        return adminMapperUtil.convertFromCustomerMeasurementFeedBack(existingCustomerMeasurementFeedback.get());
    }
}