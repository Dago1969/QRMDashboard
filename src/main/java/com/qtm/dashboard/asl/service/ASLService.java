package com.qtm.dashboard.asl.service;

import com.qtm.commonlib.dto.ASLDto;
import com.qtm.dashboard.asl.dto.ASLOverviewDto;
import com.qtm.dashboard.asl.entity.ASLEntity;
import com.qtm.dashboard.asl.mapper.ASLMapper;
import com.qtm.dashboard.asl.repository.ASLRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ASLService {

    private final ASLRepository aslRepository;
    private final ASLMapper aslMapper;
    private final RestClient restClient;

    public ASLService(
            ASLRepository aslRepository,
            ASLMapper aslMapper,
            @Value("${app.ticket.base-url:http://localhost:8084/api/ticket}") String ticketBaseUrl
    ) {
        this.aslRepository = aslRepository;
        this.aslMapper = aslMapper;
        // RestClient usato per chiamare le API QTMTicket; la base URL è configurabile in application.properties
        this.restClient = RestClient.builder().baseUrl(ticketBaseUrl).build();
        log.info("[ASLService] inizializzato con ticketBaseUrl={}", ticketBaseUrl);
    }

    @Transactional(readOnly = true)
    public List<ASLDto> findAll() {
        return aslRepository.findAll().stream()
                .map(aslMapper::entityToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ASLDto findById(Long id) {
        return aslRepository.findById(id)
                .map(aslMapper::entityToDto)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<ASLOverviewDto> findAllWithImportStatus() {
        log.info("[ASLService] richiesta overview ASL con stato import");
        List<ASLDto> sourceAsls = fetchAllAslsFromTicket();
        Map<Long, ASLEntity> localAslMap = aslRepository.findAll().stream()
                .collect(Collectors.toMap(ASLEntity::getId, entity -> entity));

        return sourceAsls.stream()
                .map(source -> {
                    ASLEntity localEntity = localAslMap.get(source.getId());
                    return ASLOverviewDto.builder()
                            .id(source.getId())
                            .codiceAzienda(source.getCodiceAzienda())
                            .denominazioneAzienda(source.getDenominazioneAzienda())
                            .indirizzo(source.getIndirizzo())
                            .email(source.getEmail())
                            .telefono(source.getTelefono())
                            .imported(localEntity != null)
                            .note(localEntity != null ? localEntity.getNote() : null)
                            .build();
                })
                .toList();
    }

    @Transactional
    public ASLDto update(Long id, ASLDto dto) {
        ASLEntity entity = aslRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ASL non trovata: " + id));
        entity.setNote(dto.getNote());
        return aslMapper.entityToDto(aslRepository.save(entity));
    }

    @Transactional
    public List<ASLDto> importFromSource(List<Long> sourceIds) {
        log.info("[ASLService] importazione ASL da sourceIds={}", sourceIds);
        return sourceIds.stream()
                .filter(Objects::nonNull)
                .map(this::importOneFromTicket)
                .toList();
    }

    /**
     * Importa una singola ASL da QTMTicket e la salva nella tabella locale.
     * Il note locale viene preservato se l'ASL esiste già.
     */
    private ASLDto importOneFromTicket(Long sourceId) {
        log.info("[ASLService] chiamata QTMTicket per import ASL id={}", sourceId);
        try {
            ASLDto dto = restClient.get()
                    .uri("/asl/{id}", sourceId)
                    .retrieve()
                    .body(ASLDto.class);

            if (dto == null) {
                throw new IllegalArgumentException("ASL non trovata in QTMTicket: " + sourceId);
            }

            dto.setId(sourceId);
            ASLEntity entity = aslMapper.dtoToEntity(dto);
            aslRepository.findById(sourceId).ifPresent(existing -> entity.setNote(existing.getNote()));
            ASLDto saved = aslMapper.entityToDto(aslRepository.save(entity));
            log.info("[ASLService] importOneFromTicket id={} salvata con note={}", sourceId, saved.getNote());
            return saved;
        } catch (Exception ex) {
            log.error("[ASLService] errore importOneFromTicket id={}", sourceId, ex);
            throw ex;
        }
    }

    /**
     * Recupera l'elenco completo delle ASL pubblicate da QTMTicket.
     */
    private List<ASLDto> fetchAllAslsFromTicket() {
        log.info("[ASLService] chiamata QTMTicket per fetch ASL lista /asl");
        try {
            ASLDto[] sourceAsls = restClient.get()
                    .uri("/asl")
                    .retrieve()
                    .body(ASLDto[].class);
            int size = sourceAsls == null ? 0 : sourceAsls.length;
            log.info("[ASLService] fetchAllAslsFromTicket restituisce {} record", size);
            return sourceAsls == null ? new ArrayList<>() : Arrays.asList(sourceAsls);
        } catch (Exception ex) {
            log.error("[ASLService] errore fetchAllAslsFromTicket", ex);
            throw ex;
        }
    }
}
