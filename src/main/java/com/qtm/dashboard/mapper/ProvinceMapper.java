package com.qtm.dashboard.mapper;

import com.qtm.dashboard.domain.Province;
import com.qtm.dashboard.dto.ProvinceDto;
import org.springframework.stereotype.Component;

/**
 * Mapper per Province <-> ProvinceDto.
 */
@Component
public class ProvinceMapper {
    public ProvinceDto toDto(Province entity) {
        if (entity == null) return null;
        return ProvinceDto.builder()
                .id(entity.getId())
                .provinceCode(entity.getProvinceCode())
                .name(entity.getName())
                .provinceType(entity.getProvinceType())
                .cityCount(entity.getCityCount())
                .areaSquareKm(entity.getAreaSquareKm())
                .supraMunicipalCode(entity.getSupraMunicipalCode())
                .regionId(entity.getRegion() != null ? entity.getRegion().getId() : null)
                .cities(null)
                .build();
    }
    public Province toEntity(ProvinceDto dto) {
        if (dto == null) return null;
        Province province = new Province();
        province.setId(dto.getId());
        province.setProvinceCode(dto.getProvinceCode());
        province.setName(dto.getName());
        province.setProvinceType(dto.getProvinceType());
        province.setCityCount(dto.getCityCount());
        province.setAreaSquareKm(dto.getAreaSquareKm());
        province.setSupraMunicipalCode(dto.getSupraMunicipalCode());
        // region and cities handled separately
        return province;
    }
}
