package com.qtm.dashboard.medicine.service;

/**
 * Criteri di ricerca farmaci derivati dai query param REST locali.
 */
public record MedicineSearchCriteria(
        String codiceAic,
        String codFarmaco,
        String codConfezione,
        String denominazione,
        String descrizione,
        String codiceAtc,
        String ragioneSociale,
        String statoAmministrativo
) {
}