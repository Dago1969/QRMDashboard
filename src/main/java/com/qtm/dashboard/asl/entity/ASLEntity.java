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

    @Column(name = "referents_json", length = 8000)
    private String referentsJson;
}
