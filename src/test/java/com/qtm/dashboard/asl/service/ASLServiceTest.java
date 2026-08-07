package com.qtm.dashboard.asl.service;

import com.qtm.commonlib.dto.ASLDto;
import com.qtm.dashboard.asl.entity.ASLEntity;
import com.qtm.dashboard.asl.mapper.ASLMapper;
import com.qtm.dashboard.asl.repository.ASLRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ASLServiceTest {

    @Mock
    private ASLRepository aslRepository;

    @Mock
    private ASLMapper aslMapper;

    @InjectMocks
    private ASLService aslService;

    @Test
    void importFromSourceShouldPersistEntitiesUsingProvidedIds() {
        ASLDto dto = ASLDto.builder()
                .id(321L)
                .codiceAzienda("001")
                .denominazioneAzienda("ASL Test")
                .build();


        ASLEntity entity = new ASLEntity();
        entity.setId(321L);
        //FIXME FRANCESCO COMMENTATI Che NOn FACEVA la build..
//        entity.setCodiceAzienda("001");
//        entity.setDenominazioneAzienda("ASL Test");


        when(aslMapper.dtoToEntity(dto)).thenReturn(entity);
        when(aslRepository.save(any(ASLEntity.class))).thenReturn(entity);
        when(aslMapper.entityToDto(entity)).thenReturn(dto);

        List<ASLDto> imported = aslService.importFromSource(List.of(321L));

        assertThat(imported).hasSize(1);
        assertThat(imported.get(0).getId()).isEqualTo(321L);
        verify(aslRepository).save(any(ASLEntity.class));
    }
}
