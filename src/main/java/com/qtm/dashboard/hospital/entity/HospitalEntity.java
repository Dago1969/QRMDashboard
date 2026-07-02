package com.qtm.dashboard.hospital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entità locale per gli ospedali associati da QTMTicket.
 */
@Entity
@Table(name = "hospital")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HospitalEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "note", length = 1000)
    private String note;
}
