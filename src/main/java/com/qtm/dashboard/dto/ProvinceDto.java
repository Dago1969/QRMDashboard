package com.qtm.dashboard.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO per Province.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProvinceDto {
    private Long id;
    private String provinceCode;
    private String name;
    private String provinceType;
    private Integer cityCount;
    private BigDecimal areaSquareKm;
    private String supraMunicipalCode;
    private Long regionId;
    private List<CityDto> cities;
}
