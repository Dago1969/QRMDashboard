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
import static org.mockito.Mockito.doReturn;
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
        associatedAsl.setCodiceRegione("01");
        associatedAsl.setCodiceAzienda("201");
        when(aslRepository.findAll()).thenReturn(List.of(associatedAsl));

        HospitalDto visibleHospital = HospitalDto.builder()
                .id(100L)
            .codiceRegione("01")
            .codiceAsl("201")
                .aslId(10L)
                .struttura("Ospedale Test")
                .build();
        HospitalDto hiddenHospital = HospitalDto.builder()
                .id(200L)
            .codiceRegione("01")
            .codiceAsl("999")
                .aslId(99L)
                .struttura("Ospedale Altro")
                .build();

        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).uri("/hospitals");
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn("""
            [
              {
                \"id\": 100,
                \"codiceRegione\": \"01\",
                \"codiceAsl\": \"201\",
                \"aslId\": 10,
                \"struttura\": \"Ospedale Test\"
              },
              {
                \"id\": 200,
                \"codiceRegione\": \"01\",
                \"codiceAsl\": \"999\",
                \"aslId\": 99,
                \"struttura\": \"Ospedale Altro\"
              }
            ]
            """);

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
