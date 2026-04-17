package com.qtm.dashboard.medicine.service;

import com.qtm.dashboard.medicine.entity.MedicineEntity;

/**
 * Logica pura di matching dei filtri di ricerca farmaci.
 */
public final class MedicineSearchMatcher {

    private MedicineSearchMatcher() {
    }

    public static boolean matches(MedicineEntity medicine, MedicineSearchCriteria criteria) {
        return contains(medicine.getCodiceAic(), criteria.codiceAic())
                && contains(medicine.getCodFarmaco(), criteria.codFarmaco())
                && contains(medicine.getCodConfezione(), criteria.codConfezione())
                && contains(medicine.getDenominazione(), criteria.denominazione())
                && contains(medicine.getDescrizione(), criteria.descrizione())
                && contains(medicine.getCodiceAtc(), criteria.codiceAtc())
                && contains(medicine.getRagioneSociale(), criteria.ragioneSociale())
                && contains(medicine.getStatoAmministrativo(), criteria.statoAmministrativo());
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
}