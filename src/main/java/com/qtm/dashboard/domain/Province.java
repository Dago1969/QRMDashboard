package com.qtm.dashboard.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Entity che rappresenta una provincia secondo il tracciato gi_db_comuni.
 */
@Entity
@Table(name = "province")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Province {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sigla_provincia", nullable = false, length = 4, unique = true)
    private String provinceCode;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "tipologia_provincia", nullable = false, length = 100)
    private String provinceType;

    @Column(name = "numero_comuni", nullable = false)
    private Integer cityCount;

    @Column(name = "superficie_kmq", nullable = false, precision = 10, scale = 4)
    private BigDecimal areaSquareKm;

    @Column(name = "codice_sovracomunale", nullable = false, length = 6, unique = true)
    private String supraMunicipalCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @OneToMany(mappedBy = "province", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<City> cities;
}
