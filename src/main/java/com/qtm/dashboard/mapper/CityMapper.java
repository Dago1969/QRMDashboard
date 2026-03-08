package com.qtm.dashboard.mapper;

import com.qtm.dashboard.domain.City;
import com.qtm.dashboard.dto.CityDto;
import org.springframework.stereotype.Component;

/**
 * Mapper per City <-> CityDto.
 */
@Component
public class CityMapper {
    public CityDto toDto(City entity) {
        if (entity == null) return null;
        return CityDto.builder()
                .id(entity.getId())
                .istatCode(entity.getIstatCode())
                .italianAlternativeName(entity.getItalianAlternativeName())
                .name(entity.getName())
                .cap(entity.getCap())
                .alternativeName(entity.getAlternativeName())
                .capitalFlag(entity.getCapitalFlag())
                .belfioreCode(entity.getBelfioreCode())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .areaSquareKm(entity.getAreaSquareKm())
                .supraMunicipalCode(entity.getSupraMunicipalCode())
                .provinceId(entity.getProvince() != null ? entity.getProvince().getId() : null)
                .build();
    }
    public City toEntity(CityDto dto) {
        if (dto == null) return null;
        City city = new City();
        city.setId(dto.getId());
        city.setIstatCode(dto.getIstatCode());
        city.setItalianAlternativeName(dto.getItalianAlternativeName());
        city.setName(dto.getName());
        city.setCap(dto.getCap());
        city.setAlternativeName(dto.getAlternativeName());
        city.setCapitalFlag(dto.getCapitalFlag());
        city.setBelfioreCode(dto.getBelfioreCode());
        city.setLatitude(dto.getLatitude());
        city.setLongitude(dto.getLongitude());
        city.setAreaSquareKm(dto.getAreaSquareKm());
        city.setSupraMunicipalCode(dto.getSupraMunicipalCode());
        // province handled separately
        return city;
    }
}
