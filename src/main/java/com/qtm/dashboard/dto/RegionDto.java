package com.qtm.dashboard.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO per Region.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegionDto {
    private Long id;
    private String geographicArea;
    private String regionCode;
    private String name;
    private String regionType;
    private Integer provinceCount;
    private Integer cityCount;
    private BigDecimal areaSquareKm;
    private Long countryId;
    private List<ProvinceDto> provinces;
}
