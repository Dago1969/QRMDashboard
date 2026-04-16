package com.qtm.dashboard.patient.service;

import com.qtm.commonlib.dto.PatientDto;
import com.qtm.dashboard.patient.entity.PatientEntity;
import com.qtm.dashboard.patient.mapper.PatientMapper;
import com.qtm.dashboard.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Service CRUD pazienti con persistenza nativa in QTMDB.
 */
@Service
@RequiredArgsConstructor
public class PatientService {

    private static final String ASSISTED_ID_PREFIX = "QTM-";

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    @Transactional
    public PatientDto create(PatientDto patientDto) {
        validateUniqueFiscalCodeForCreate(patientDto.getFiscalCode());
        PatientEntity entity = patientMapper.toEntity(patientDto);
        entity.setAssistedId(null);
        PatientEntity saved = patientRepository.save(entity);
        saved.setAssistedId(buildAssistedId(saved.getId()));
        return patientMapper.toDto(patientRepository.save(saved));
    }

    @Transactional(readOnly = true)
    public List<PatientDto> search(PatientSearchCriteria criteria) {
        return patientRepository.findAll().stream()
                .filter(patient -> PatientSearchMatcher.matches(patient, criteria))
                .map(patientMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public PatientDto findById(Long id) {
        return patientMapper.toDto(findEntityById(id));
    }

    @Transactional
    public PatientDto update(Long id, PatientDto patientDto) {
        PatientEntity current = findEntityById(id);
        validateUniqueFiscalCodeForUpdate(patientDto.getFiscalCode(), id);
        patientMapper.updateEntity(current, patientDto);
        return patientMapper.toDto(patientRepository.save(current));
    }

    @Transactional
    public void delete(Long id) {
        patientRepository.delete(findEntityById(id));
    }

    private PatientEntity findEntityById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Paziente non trovato"));
    }

    private void validateUniqueFiscalCodeForCreate(String fiscalCode) {
        if (patientRepository.existsByFiscalCodeIgnoreCase(fiscalCode)) {
            throw new ResponseStatusException(CONFLICT, "Esiste gia un paziente con lo stesso codice fiscale");
        }
    }

    private void validateUniqueFiscalCodeForUpdate(String fiscalCode, Long id) {
        if (patientRepository.existsByFiscalCodeIgnoreCaseAndIdNot(fiscalCode, id)) {
            throw new ResponseStatusException(CONFLICT, "Esiste gia un paziente con lo stesso codice fiscale");
        }
    }

    private String buildAssistedId(Long id) {
        return ASSISTED_ID_PREFIX + String.format("%06d", id);
    }
}