package com.qtm.dashboard.patient.service;

import com.qtm.commonlib.dto.PatientDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service CRUD pazienti che delega le operazioni al backend QTMPatients.
 */
@Service
@RequiredArgsConstructor
public class PatientService {

    private final QtmPatientsClient qtmPatientsClient;

    @Transactional
    public PatientDto create(PatientDto patientDto) {
        return qtmPatientsClient.create(patientDto);
    }

    @Transactional(readOnly = true)
    public List<PatientDto> findAll() {
        return qtmPatientsClient.findAll();
    }

    @Transactional(readOnly = true)
    public PatientDto findById(Long id) {
        return qtmPatientsClient.findById(id);
    }

    @Transactional
    public PatientDto update(Long id, PatientDto patientDto) {
        return qtmPatientsClient.update(id, patientDto);
    }

    @Transactional
    public void delete(Long id) {
        qtmPatientsClient.delete(id);
    }
}