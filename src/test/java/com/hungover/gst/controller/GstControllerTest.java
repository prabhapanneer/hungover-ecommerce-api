package com.hungover.gst.controller;

import com.hungover.common.apiresponse.SingleDataResponse;
import com.hungover.core.dto.gst.GstDto;
import com.hungover.gst.service.GstApiResponseService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class GstControllerTest {

    @InjectMocks
    GstController gstController;
    @Mock
    private GstApiResponseService gstApiResponseService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveGst() {
        GstDto gstDto = new GstDto();
        gstDto.setCompanyName("CNW");
        gstDto.setGst("18.0");

        SingleDataResponse singleDataMockResponse = new SingleDataResponse();
        when(gstApiResponseService.saveGst(gstDto)).thenReturn(singleDataMockResponse);

        ResponseEntity<SingleDataResponse> singleDataResponseResponseEntity = gstController.saveGst(gstDto);

        verify(gstApiResponseService).saveGst(gstDto);

        Assertions.assertEquals(HttpStatus.OK, singleDataResponseResponseEntity.getStatusCode());
        Assertions.assertSame(singleDataMockResponse, singleDataResponseResponseEntity.getBody());
    }
}