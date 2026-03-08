package com.qtm.dashboard.mapper;

import com.qtm.dashboard.domain.Region;
import com.qtm.dashboard.dto.RegionDto;
import org.springframework.stereotype.Component;

/**
 * Mapper per Region <-> RegionDto.
 */
@Component
public class RegionMapper {
    public RegionDto toDto(Region entity) {
        if (entity == null) return null;
        return RegionDto.builder()
                .id(entity.getId())
                .geographicArea(entity.getGeographicArea())
                .regionCode(entity.getRegionCode())
                .name(entity.getName())
                .regionType(entity.getRegionType())
                .provinceCount(entity.getProvinceCount())
                .cityCount(entity.getCityCount())
                .areaSquareKm(entity.getAreaSquareKm())
                .countryId(entity.getCountry() != null ? entity.getCountry().getId() : null)
                .provinces(null)
                .build();
    }
    public Region toEntity(RegionDto dto) {
        if (dto == null) return null;
        Region region = new Region();
        region.setId(dto.getId());
        region.setGeographicArea(dto.getGeographicArea());
        region.setRegionCode(dto.getRegionCode());
        region.setName(dto.getName());
        region.setRegionType(dto.getRegionType());
        region.setProvinceCount(dto.getProvinceCount());
        region.setCityCount(dto.getCityCount());
        region.setAreaSquareKm(dto.getAreaSquareKm());
        // country and provinces handled separately
        return region;
    }
}
