package com.hungover.util;

import com.hungover.core.domain.customer.CustomerMeasurement;
import com.hungover.core.domain.customer.CustomerMeasurementFeedBack;
import com.hungover.core.dto.customer.CustomerMeasurementDto;
import com.hungover.core.dto.customer.CustomerMeasurementFeedbackDto;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Utility class for mapping between different objects using ModelMapper.
 */
@Component
public class AdminMapperUtil {
    private final Logger adminMapperUtilLogger = LoggerFactory.getLogger(this.getClass());

    private ModelMapper modelMapper;

    public AdminMapperUtil(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    /**
     * Converts a CustomerMeasurementDto object to a CustomerMeasurement object.
     *
     * @param customerMeasurementDtoObj The CustomerMeasurementDto object to be converted.
     * @return The resulting CustomerMeasurement object.
     */
    public CustomerMeasurement convertFromCustomerMeasurementDto(CustomerMeasurementDto customerMeasurementDtoObj) {
        adminMapperUtilLogger.info("Convert CustomerMeasurementDto to CustomerMeasurement");
        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
        return modelMapper.map(customerMeasurementDtoObj, CustomerMeasurement.class);
    }

    /**
     * Converts a CustomerMeasurement object to a CustomerMeasurementDto object.
     *
     * @param customerMeasurementObj The CustomerMeasurement object to be converted.
     * @return The resulting CustomerMeasurementDto object.
     */
    public CustomerMeasurementDto convertToCustomerMeasurementDto(CustomerMeasurement customerMeasurementObj) {
        adminMapperUtilLogger.info("Convert CustomerMeasurement to CustomerMeasurementDto");
        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
        return modelMapper.map(customerMeasurementObj, CustomerMeasurementDto.class);
    }

    /**
     * Convert a CustomerMeasurementFeedbackDto to a CustomerMeasurementFeedBack entity.
     *
     * @param customerMeasurementFeedbackDto The DTO to be converted.
     * @return CustomerMeasurementFeedBack entity converted from the DTO.
     */
    public CustomerMeasurementFeedBack convertFromCustomerMeasurementFeedbackDto(CustomerMeasurementFeedbackDto
                                                                                         customerMeasurementFeedbackDto) {
        adminMapperUtilLogger.info("Convert CustomerMeasurementFeedbackDto to CustomerMeasurementFeedback");
        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
        return modelMapper.map(customerMeasurementFeedbackDto, CustomerMeasurementFeedBack.class);
    }

    /**
     * Convert a CustomerMeasurementFeedBack entity to a CustomerMeasurementFeedbackDto.
     *
     * @param savedcustomerMeasurementFeedBack The entity to be converted.
     * @return CustomerMeasurementFeedbackDto converted from the entity.
     */
    public CustomerMeasurementFeedbackDto convertFromCustomerMeasurementFeedBack(CustomerMeasurementFeedBack
                                                                                         savedcustomerMeasurementFeedBack) {
        adminMapperUtilLogger.info("Convert CustomerMeasurementFeedback to CustomerMeasurementFeedbackDto");
        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
        return modelMapper.map(savedcustomerMeasurementFeedBack, CustomerMeasurementFeedbackDto.class);
    }
}
