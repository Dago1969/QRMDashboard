package com.qtm.dashboard.patient.service;

import java.time.LocalDate;

/**
 * Criteri di ricerca pazienti derivati dai query param REST locali.
 */
public record PatientSearchCriteria(
        String assistedId,
        String firstName,
        String lastName,
        String email,
        String fiscalCode,
        LocalDate birthDate,
        String gender,
        Long structureId
) {
}