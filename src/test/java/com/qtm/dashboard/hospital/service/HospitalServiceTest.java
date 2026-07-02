package com.qtm.dashboard.hospital.service;

import com.qtm.commonlib.dto.HospitalDto;
import com.qtm.dashboard.asl.entity.ASLEntity;
import com.qtm.dashboard.asl.repository.ASLRepository;
import com.qtm.dashboard.hospital.entity.HospitalEntity;
import com.qtm.dashboard.hospital.mapper.HospitalMapper;
import com.qtm.dashboard.hospital.repository.HospitalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HospitalServiceTest {

    @Mock
    private HospitalRepository hospitalRepository;

    @Mock
    private HospitalMapper hospitalMapper;

    @Mock
    private ASLRepository aslRepository;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Test
    void findAllWithImportStatusShouldOnlyExposeHospitalsForAssociatedAsl() {
        HospitalService hospitalService = new HospitalService(hospitalRepository, hospitalMapper, aslRepository, restClient, "http://ticket.test");

        ASLEntity associatedAsl = new ASLEntity();
        associatedAsl.setId(10L);
        when(aslRepository.findAll()).thenReturn(List.of(associatedAsl));

        HospitalDto visibleHospital = HospitalDto.builder()
                .id(100L)
                .aslId(10L)
                .struttura("Ospedale Test")
                .build();
        HospitalDto hiddenHospital = HospitalDto.builder()
                .id(200L)
                .aslId(99L)
                .struttura("Ospedale Altro")
                .build();

        when(restClient.get()).thenReturn((RestClient.RequestHeadersUriSpec<?>) requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/hospitals")).thenReturn((RestClient.RequestHeadersUriSpec<?>) requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(HospitalDto[].class)).thenReturn(new HospitalDto[]{visibleHospital, hiddenHospital});

        HospitalEntity localHospital = new HospitalEntity();
        localHospital.setId(100L);
        when(hospitalRepository.findAll()).thenReturn(List.of(localHospital));

        var overview = hospitalService.findAllWithImportStatus();

        assertThat(overview).hasSize(1);
        assertThat(overview.get(0).getId()).isEqualTo(100L);
        assertThat(overview.get(0).getImported()).isTrue();
        verify(hospitalRepository).findAll();
    }
}
