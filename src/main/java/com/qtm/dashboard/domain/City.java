package com.qtm.dashboard.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entity che rappresenta un comune secondo il tracciato gi_db_comuni.
 */
@Entity
@Table(name = "city")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codice_istat", nullable = false, length = 12, unique = true)
    private String istatCode;

    @Column(name = "denominazione_ita_altra", nullable = false, length = 191)
    private String italianAlternativeName;

    @Column(name = "name", nullable = false, length = 191)
    private String name;

    @Column(name = "cap", nullable = false, length = 16)
    private String cap;

    @Column(name = "denominazione_altra", nullable = false, length = 191)
    private String alternativeName;

    @Column(name = "flag_capoluogo", nullable = false, length = 4)
    private String capitalFlag;

    @Column(name = "codice_belfiore", nullable = false, length = 8)
    private String belfioreCode;

    @Column(name = "lat", nullable = false, precision = 13, scale = 7)
    private BigDecimal latitude;

    @Column(name = "lon", nullable = false, precision = 13, scale = 7)
    private BigDecimal longitude;

    @Column(name = "superficie_kmq", nullable = false, precision = 10, scale = 4)
    private BigDecimal areaSquareKm;

    @Column(name = "codice_sovracomunale", nullable = false, length = 6)
    private String supraMunicipalCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_id", nullable = false)
    private Province province;
}
