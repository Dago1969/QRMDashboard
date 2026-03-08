package com.qtm.dashboard.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * DTO per City.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CityDto {
    private Long id;
    private String istatCode;
    private String italianAlternativeName;
    private String name;
    private String cap;
    private String alternativeName;
    private String capitalFlag;
    private String belfioreCode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal areaSquareKm;
    private String supraMunicipalCode;
    private Long provinceId;
}
