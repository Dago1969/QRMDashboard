package com.qtm.dashboard.mapper;

import com.qtm.dashboard.domain.Country;
import com.qtm.dashboard.dto.CountryDto;
import org.springframework.stereotype.Component;

/**
 * Mapper per Country <-> CountryDto.
 */
@Component
public class CountryMapper {
    public CountryDto toDto(Country entity) {
        if (entity == null) return null;
        return CountryDto.builder()
                .id(entity.getId())
                .countryCode(entity.getCountryCode())
                .belfioreCode(entity.getBelfioreCode())
                .name(entity.getName())
                .nationalityName(entity.getNationalityName())
            .regions(null)
                .build();
    }
    public Country toEntity(CountryDto dto) {
        if (dto == null) return null;
        Country country = new Country();
        country.setId(dto.getId());
        country.setCountryCode(dto.getCountryCode());
        country.setBelfioreCode(dto.getBelfioreCode());
        country.setName(dto.getName());
        country.setNationalityName(dto.getNationalityName());
        // regions handled separately
        return country;
    }
}
