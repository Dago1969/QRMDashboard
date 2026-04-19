package com.qtm.dashboard.patient.service;

import com.qtm.dashboard.patient.entity.PatientEntity;

import java.time.LocalDate;

/**
 * Logica pura di matching dei filtri di ricerca pazienti.
 */
public final class PatientSearchMatcher {

    private PatientSearchMatcher() {
    }

    public static boolean matches(PatientEntity patient, PatientSearchCriteria criteria) {
        return contains(patient.getAssistedId(), criteria.assistedId())
                && contains(patient.getFirstName(), criteria.firstName())
                && contains(patient.getLastName(), criteria.lastName())
                && contains(patient.getEmail(), criteria.email())
                && contains(patient.getFiscalCode(), criteria.fiscalCode())
            && equalsDate(patient.getBirthDate(), criteria.birthDate())
            && equalsText(patient.getGender(), criteria.gender())
                && equalsLong(patient.getStructureId(), criteria.structureId());
    }

    private static boolean contains(String actual, String expected) {
        if (expected == null || expected.isBlank()) {
            return true;
        }
        if (actual == null) {
            return false;
        }
        return actual.toLowerCase().contains(expected.trim().toLowerCase());
    }

    private static boolean equalsLong(Long actual, Long expected) {
        return expected == null || expected.equals(actual);
    }

    private static boolean equalsDate(LocalDate actual, LocalDate expected) {
        return expected == null || expected.equals(actual);
    }

    private static boolean equalsText(String actual, String expected) {
        if (expected == null || expected.isBlank()) {
            return true;
        }
        if (actual == null) {
            return false;
        }
        return actual.equalsIgnoreCase(expected.trim());
    }
}