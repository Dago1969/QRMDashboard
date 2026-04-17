package com.qtm.dashboard.medicine.service;

import com.qtm.commonlib.dto.MedicineDto;
import com.qtm.dashboard.medicine.entity.MedicineEntity;
import com.qtm.dashboard.medicine.mapper.MedicineMapper;
import com.qtm.dashboard.medicine.repository.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Service CRUD farmaci con persistenza nativa in QTMDB e validazione sul codice AIC.
 */
@Service
@RequiredArgsConstructor
public class MedicineService {

    private static final int LOOKUP_LIMIT = 30;

    private final MedicineRepository medicineRepository;
    private final MedicineMapper medicineMapper;

    @Transactional
    public MedicineDto create(MedicineDto medicineDto) {
        validateUniqueCodiceAicForCreate(medicineDto.getCodiceAic());
        MedicineEntity saved = medicineRepository.save(medicineMapper.toEntity(medicineDto));
        return medicineMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<MedicineDto> search(MedicineSearchCriteria criteria) {
        return medicineRepository.findAll().stream()
                .filter(medicine -> MedicineSearchMatcher.matches(medicine, criteria))
                .map(medicineMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public MedicineDto findById(Long id) {
        return medicineMapper.toDto(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public MedicineDto findByCodiceAic(String codiceAic) {
        return medicineRepository.findByCodiceAicIgnoreCase(normalizeRequiredLookupValue(codiceAic))
                .map(medicineMapper::toDto)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Farmaco non trovato"));
    }

    @Transactional(readOnly = true)
    public List<MedicineDto> lookup(String query) {
        return medicineRepository.lookup(normalizeLookupQuery(query), PageRequest.of(0, LOOKUP_LIMIT)).stream()
                .map(medicineMapper::toDto)
                .toList();
    }

    @Transactional
    public MedicineDto update(Long id, MedicineDto medicineDto) {
        MedicineEntity current = findEntityById(id);
        validateUniqueCodiceAicForUpdate(medicineDto.getCodiceAic(), id);
        medicineMapper.updateEntity(current, medicineDto);
        return medicineMapper.toDto(medicineRepository.save(current));
    }

    @Transactional
    public void delete(Long id) {
        medicineRepository.delete(findEntityById(id));
    }

    private MedicineEntity findEntityById(Long id) {
        return medicineRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Farmaco non trovato"));
    }

    private void validateUniqueCodiceAicForCreate(String codiceAic) {
        if (medicineRepository.existsByCodiceAicIgnoreCase(codiceAic)) {
            throw new ResponseStatusException(CONFLICT, "Esiste gia un farmaco con lo stesso codice AIC");
        }
    }

    private void validateUniqueCodiceAicForUpdate(String codiceAic, Long id) {
        if (medicineRepository.existsByCodiceAicIgnoreCaseAndIdNot(codiceAic, id)) {
            throw new ResponseStatusException(CONFLICT, "Esiste gia un farmaco con lo stesso codice AIC");
        }
    }

    private String normalizeLookupQuery(String query) {
        return query == null ? "" : query.trim();
    }

    private String normalizeRequiredLookupValue(String value) {
        String normalizedValue = normalizeLookupQuery(value);
        if (normalizedValue.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "Farmaco non trovato");
        }
        return normalizedValue;
    }
}