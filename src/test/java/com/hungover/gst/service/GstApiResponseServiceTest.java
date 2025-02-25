package com.hungover.gst.service;


import com.hungover.common.apiresponse.SingleDataResponse;
import com.hungover.common.constant.ApplicationConstants;
import com.hungover.core.dto.gst.GstDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.mockito.Mockito.*;

class GstApiResponseServiceTest {

    @InjectMocks
    GstApiResponseService gstApiResponseService;
    @Mock
    private GstService gstService;
    @Mock
    private MessageSource messageSource;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should save GST and return DTO on success")
    void saveGst_Success() {
        // Arrange
        GstDto gstInputDto = new GstDto();
        gstInputDto.setCompanyName("CNW");
        gstInputDto.setGst("18.0");

        GstDto savedGstDto = new GstDto();
        savedGstDto.setCompanyName("CNW");
        savedGstDto.setGst("18.0");

        when(gstService.saveGst(any(GstDto.class))).thenReturn(savedGstDto);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("GST saved successfully");

        // Act
        SingleDataResponse actualSingleDataResponse = gstApiResponseService.saveGst(gstInputDto);

        // Assert
        verify(gstService).saveGst(gstInputDto);
        Assertions.assertEquals(ApplicationConstants.Status.SUCCESS, actualSingleDataResponse.getStatus().getSuccess());
        Assertions.assertEquals("GST saved successfully", actualSingleDataResponse.getStatus().getMessage());
        Assertions.assertEquals(savedGstDto, actualSingleDataResponse.getData());
    }

    @Test
    @DisplayName("Should return failure response when GST is not saved")
    void saveGst_Failure() {
        // Arrange
        GstDto gstInputDto = new GstDto();
        gstInputDto.setCompanyName("CNW");
        gstInputDto.setGst("18.0");

        when(gstService.saveGst(any(GstDto.class))).thenReturn(null);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Failed to save GST");

        // Act
        SingleDataResponse actualSingleDataResponse = gstApiResponseService.saveGst(gstInputDto);

        // Assert
        verify(gstService).saveGst(gstInputDto);
        Assertions.assertEquals(ApplicationConstants.Status.FAIL, actualSingleDataResponse.getStatus().getSuccess());
        Assertions.assertEquals("Failed to save GST", actualSingleDataResponse.getStatus().getMessage());
        Assertions.assertEquals(gstInputDto, actualSingleDataResponse.getData());
    }
}