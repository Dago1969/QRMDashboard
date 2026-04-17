package com.qtm.dashboard.medicine.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity farmaco persistita direttamente in QTMDB con i campi del catalogo AIC.
 */
@Entity
@Table(name = "medicines")
@Getter
@Setter
@NoArgsConstructor
public class MedicineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codice_aic", nullable = false, unique = true)
    private String codiceAic;

    @Column(name = "cod_farmaco", nullable = false)
    private String codFarmaco;

    @Column(name = "cod_confezione", nullable = false)
    private String codConfezione;

    @Column(name = "denominazione", nullable = false)
    private String denominazione;

    @Column(name = "descrizione", length = 1000)
    private String descrizione;

    @Column(name = "codice_ditta")
    private String codiceDitta;

    @Column(name = "ragione_sociale")
    private String ragioneSociale;

    @Column(name = "stato_amministrativo")
    private String statoAmministrativo;

    @Column(name = "tipo_procedura")
    private String tipoProcedura;

    @Column(name = "forma")
    private String forma;

    @Column(name = "codice_atc")
    private String codiceAtc;

    @Column(name = "pa_associati", length = 4000)
    private String paAssociati;

    @Column(name = "fornitura", length = 1000)
    private String fornitura;

    @Column(name = "link_fi", length = 2048)
    private String linkFi;

    @Column(name = "link_rcp", length = 2048)
    private String linkRcp;
}