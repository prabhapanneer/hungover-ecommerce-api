package com.hungover.gst.service;

import com.hungover.core.domain.gst.Gst;
import com.hungover.core.dto.gst.GstDto;
import com.hungover.gst.repository.GstRepositoryI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GstServiceTest {

    @InjectMocks
    private GstService gstService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private GstRepositoryI gstRepositoryI;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveGst() {
        GstDto gstInputDto = new GstDto();
        gstInputDto.setCompanyName("CNW");
        gstInputDto.setGst("18.0");

        Gst gstObj = new Gst();
        gstObj.setCompanyName("CNW");
        gstObj.setGst("18.0");

        when(modelMapper.map(gstInputDto, Gst.class)).thenReturn(gstObj);
        when(gstRepositoryI.save(gstObj)).thenReturn(gstObj);
        when(modelMapper.map(gstObj, GstDto.class)).thenReturn(gstInputDto);

        GstDto savedGstDto = gstService.saveGst(gstInputDto);

        verify(gstRepositoryI).save(gstObj);
        Assertions.assertEquals(gstObj.getGst(), savedGstDto.getGst());
    }
}