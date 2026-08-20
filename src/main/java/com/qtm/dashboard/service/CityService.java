package com.qtm.dashboard.service;

import com.qtm.dashboard.domain.City;
import com.qtm.dashboard.dto.CityDto;
import com.qtm.dashboard.dto.GeographicOptionDto;
import com.qtm.dashboard.mapper.CityMapper;
import com.qtm.dashboard.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Service per City. Orchestration tra repository e mapper.
 */
@Service
@RequiredArgsConstructor
public class CityService {
    private final CityRepository cityRepository;
    private final CityMapper cityMapper;

    public List<CityDto> findAll() {
        return cityRepository.findAll().stream().map(cityMapper::toDto).toList();
    }
    public CityDto findById(Long id) {
        return cityRepository.findById(id).map(cityMapper::toDto).orElse(null);
    }
    public CityDto save(CityDto dto) {
        City entity = cityMapper.toEntity(dto);
        return cityMapper.toDto(cityRepository.save(entity));
    }
    public void delete(Long id) {
        cityRepository.deleteById(id);
    }

    /**
     * Restituisce tutte le città di una provincia specifica.
     * @param provinceId id della provincia
     * @return lista di città DTO
     */
    public List<CityDto> findByProvinceId(Long provinceId) {
        return cityRepository.findByProvinceId(provinceId).stream()
                .map(cityMapper::toDto)
                .toList();
    }

    public List<GeographicOptionDto> findOptionsByProvinceId(Long provinceId) {
        return cityRepository.findOptionsByProvinceId(provinceId);
    }
}
