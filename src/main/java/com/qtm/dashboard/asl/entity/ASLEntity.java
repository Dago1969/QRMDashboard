package com.qtm.dashboard.asl.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entità locale ASL importata da QTMTicket con ID preservato.
 */
@Entity
@Table(name = "asl")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ASLEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "note", length = 1000)
    private String note;

    @Column(name = "codice_azienda", length = 50)
    private String codiceAzienda;

    @Column(name = "denominazione_azienda", length = 500)
    private String denominazioneAzienda;

    @Column(name = "codice_regione", length = 10)
    private String codiceRegione;

    @Column(name = "referents_json", length = 8000)
    private String referentsJson;
}
