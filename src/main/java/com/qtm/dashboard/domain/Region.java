package com.qtm.dashboard.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Entity che rappresenta una regione secondo il tracciato gi_db_comuni.
 */
@Entity
@Table(name = "region")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Region {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ripartizione_geografica", nullable = false, length = 20)
    private String geographicArea;

    @Column(name = "codice_regione", nullable = false, length = 4, unique = true)
    private String regionCode;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "tipologia_regione", nullable = false, length = 30)
    private String regionType;

    @Column(name = "numero_province", nullable = false)
    private Integer provinceCount;

    @Column(name = "numero_comuni", nullable = false)
    private Integer cityCount;

    @Column(name = "superficie_kmq", nullable = false, precision = 10, scale = 4)
    private BigDecimal areaSquareKm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Province> provinces;
}
