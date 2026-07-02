package com.qtm.dashboard.hospital.service;

import com.qtm.commonlib.dto.HospitalDto;
import com.qtm.dashboard.asl.entity.ASLEntity;
import com.qtm.dashboard.asl.repository.ASLRepository;
import com.qtm.dashboard.hospital.dto.HospitalImportRequest;
import com.qtm.dashboard.hospital.dto.HospitalOverviewDto;
import com.qtm.dashboard.hospital.entity.HospitalEntity;
import com.qtm.dashboard.hospital.mapper.HospitalMapper;
import com.qtm.dashboard.hospital.repository.HospitalRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Locale;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HospitalService {

    private final HospitalRepository hospitalRepository;
    private final HospitalMapper hospitalMapper;
    private final ASLRepository aslRepository;
    private final RestClient restClient;
    private final String ticketBaseUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public HospitalService(
            HospitalRepository hospitalRepository,
            HospitalMapper hospitalMapper,
            ASLRepository aslRepository,
            @Value("${app.ticket.base-url:http://localhost:8084/api/ticket}") String ticketBaseUrl
    ) {
        this(hospitalRepository, hospitalMapper, aslRepository, RestClient.builder().baseUrl(ticketBaseUrl).build(), ticketBaseUrl);
    }

    HospitalService(
            HospitalRepository hospitalRepository,
            HospitalMapper hospitalMapper,
            ASLRepository aslRepository,
            RestClient restClient,
            String ticketBaseUrl
    ) {
        this.hospitalRepository = hospitalRepository;
        this.hospitalMapper = hospitalMapper;
        this.aslRepository = aslRepository;
        this.restClient = restClient;
        this.ticketBaseUrl = Objects.requireNonNull(ticketBaseUrl, "app.ticket.base-url mancante");
    }

    @Transactional(readOnly = true)
    public List<HospitalOverviewDto> findAllWithImportStatus() {
        log.info("[HospitalService] richiesta overview ospedali con stato associazione");
        List<HospitalDto> sourceHospitals = fetchAllHospitalsFromTicket();
        Set<String> associatedAslKeys = aslRepository.findAll().stream()
            .map(a -> aslKey(a.getCodiceRegione(), a.getCodiceAzienda()))
            .collect(Collectors.toSet());
        Map<Long, HospitalEntity> localHospitalMap = hospitalRepository.findAll().stream()
                .collect(Collectors.toMap(HospitalEntity::getId, entity -> entity));

        long matchedHospitals = sourceHospitals.stream()
            .filter(hospital -> associatedAslKeys.contains(aslKey(hospital.getCodiceRegione(), hospital.getCodiceAsl())))
            .count();
        log.info("[HospitalService] associatedAslKeys={} sourceHospitals={} matchedHospitals={} localHospitalMap={}",
            associatedAslKeys.size(), sourceHospitals.size(), matchedHospitals, localHospitalMap.size());
        sourceHospitals.stream()
            .filter(hospital -> associatedAslKeys.contains(aslKey(hospital.getCodiceRegione(), hospital.getCodiceAsl())))
            .limit(5)
            .forEach(hospital -> log.info(
                "[HospitalService] matched hospital id={} key={} codiceStruttura={} aslId={}",
                hospital.getId(),
                aslKey(hospital.getCodiceRegione(), hospital.getCodiceAsl()),
                hospital.getCodiceStruttura(),
                hospital.getAslId()));

        return sourceHospitals.stream()
            .filter(hospital -> associatedAslKeys.contains(aslKey(hospital.getCodiceRegione(), hospital.getCodiceAsl())))
                .map(hospital -> {
                    HospitalEntity localEntity = localHospitalMap.get(hospital.getId());
                    return HospitalOverviewDto.builder()
                            .id(hospital.getId())
                            .codiceRegione(hospital.getCodiceRegione())
                            .codiceAsl(hospital.getCodiceAsl())
                            .codiceStruttura(hospital.getCodiceStruttura())
                            .struttura(hospital.getStruttura())
                            .indirizzo(hospital.getIndirizzo())
                            .hospitalTypeId(hospital.getHospitalTypeId())
                            .aslId(hospital.getAslId())
                            .imported(localEntity != null)
                            .note(localEntity != null ? localEntity.getNote() : null)
                            .build();
                })
                .toList();
    }

    private String aslKey(String codiceRegione, String codiceAzienda) {
        String reg = codiceRegione == null ? "" : codiceRegione.trim().toUpperCase(Locale.ROOT);
        String azienda = codiceAzienda == null ? "" : codiceAzienda.trim().toUpperCase(Locale.ROOT);
        if ("01|201".equals(reg + "|" + azienda)) {
			// Special case for Regione Piemonte and ASL Città di Torino
			log.info("Trovato: "+reg + "|" + azienda);
		}
        return reg + "|" + azienda;
    }

    @Transactional
    public void deleteAssociation(Long id) {
        log.info("[HospitalService] disassociazione ospedale id={}", id);
        hospitalRepository.deleteById(Objects.requireNonNull(id, "Hospital id mancante"));
    }

    @Transactional
    public List<HospitalDto> importFromSource(List<Long> sourceIds) {
        log.info("[HospitalService] importazione ospedali da sourceIds={}", sourceIds);
        return sourceIds.stream()
                .filter(Objects::nonNull)
                .map(this::importOneFromTicket)
                .toList();
    }

    private HospitalDto importOneFromTicket(Long sourceId) {
        log.info("[HospitalService] chiamata QTMTicket per import ospedale id={}", sourceId);
        try {
            HospitalDto dto = restClient.get()
                    .uri("/hospitals/{id}", sourceId)
                    .retrieve()
                    .body(HospitalDto.class);

            if (dto == null) {
                throw new IllegalArgumentException("Ospedale non trovato in QTMTicket: " + sourceId);
            }

            dto.setId(sourceId);
            HospitalEntity entity = Objects.requireNonNull(hospitalMapper.dtoToEntity(dto), "Entity ospedale non valorizzata");
            hospitalRepository.findById(Objects.requireNonNull(sourceId, "Source hospital id mancante"))
                    .ifPresent(existing -> entity.setNote(existing.getNote()));
            HospitalDto saved = hospitalMapper.entityToDto(hospitalRepository.save(entity));
            log.info("[HospitalService] importOneFromTicket id={} salvata", sourceId);
            return saved;
        } catch (Exception ex) {
            log.error("[HospitalService] errore importOneFromTicket id={}", sourceId, ex);
            throw mapTicketException(ex, "/hospitals/" + sourceId);
        }
    }

    private List<HospitalDto> fetchAllHospitalsFromTicket() {
        log.info("[HospitalService] chiamata QTMTicket per fetch ospedali lista /hospitals");
        try {
            String responseBody = restClient.get()
                    .uri("/hospitals")
                    .retrieve()
                    .body(String.class);
            if (responseBody == null || responseBody.isBlank()) {
                log.info("[HospitalService] fetchAllHospitalsFromTicket restituisce 0 record (body vuoto)");
                return new ArrayList<>();
            }
            JsonNode root = objectMapper.readTree(responseBody);
            List<HospitalDto> list = new ArrayList<>();
            if (root.isArray()) {
                for (JsonNode node : root) {
                    HospitalDto dto = HospitalDto.builder()
                            .id(node.has("id") ? (node.get("id").isNull() ? null : node.get("id").asLong()) : null)
                            .codiceRegione(node.has("codiceRegione") ? node.path("codiceRegione").asText("") : node.path("codice_regione").asText(""))
                            .codiceAsl(node.has("codiceAsl") ? node.path("codiceAsl").asText("") : node.path("codice_asl").asText(""))
                            .codiceStruttura(node.has("codiceStruttura") ? node.path("codiceStruttura").asText("") : node.path("codice_struttura").asText(""))
                            .struttura(node.has("struttura") ? node.path("struttura").asText("") : node.path("struttura").asText(""))
                            .indirizzo(node.has("indirizzo") ? node.path("indirizzo").asText("") : node.path("indirizzo").asText(""))
                            .hospitalTypeId(node.has("hospitalTypeId") ? (node.path("hospitalTypeId").isNull() ? null : node.path("hospitalTypeId").asLong()) : (node.has("hospital_type_id") ? (node.path("hospital_type_id").isNull() ? null : node.path("hospital_type_id").asLong()) : null))
                            .aslId(node.has("aslId") ? (node.path("aslId").isNull() ? null : node.path("aslId").asLong()) : (node.has("asl_id") ? (node.path("asl_id").isNull() ? null : node.path("asl_id").asLong()) : null))
                            .build();
                    list.add(dto);
                }
            }
            log.info("[HospitalService] fetchAllHospitalsFromTicket restituisce {} record", list.size());
            return list;
        } catch (Exception ex) {
            log.error("[HospitalService] errore fetchAllHospitalsFromTicket", ex);
            throw mapTicketException(ex, "/hospitals");
        }
    }

    private ResponseStatusException mapTicketException(Exception exception, String resourcePath) {
        String targetUrl = ticketBaseUrl + resourcePath;
        if (exception instanceof ResponseStatusException responseStatusException) {
            return responseStatusException;
        }
        if (exception instanceof RestClientResponseException restClientResponseException) {
            String detail = String.format(
                    "QTMTicket ha risposto con stato %s durante la chiamata %s.",
                    restClientResponseException.getStatusCode().value(),
                    targetUrl
            );
            return new ResponseStatusException(HttpStatus.BAD_GATEWAY, detail, exception);
        }
        if (exception instanceof ResourceAccessException) {
            String detail = String.format(
                    "QTMTicket non raggiungibile su %s. Verifica che il servizio sia avviato e che app.ticket.base-url sia corretto.",
                    targetUrl
            );
            return new ResponseStatusException(HttpStatus.BAD_GATEWAY, detail, exception);
        }
        String detail = String.format("Errore durante la chiamata a QTMTicket su %s.", targetUrl);
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, detail, exception);
    }
}
