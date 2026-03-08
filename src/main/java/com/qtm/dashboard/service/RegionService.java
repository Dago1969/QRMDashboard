package com.qtm.dashboard.service;

import com.qtm.dashboard.domain.Region;
import com.qtm.dashboard.dto.RegionDto;
import com.qtm.dashboard.mapper.RegionMapper;
import com.qtm.dashboard.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Service per Region. Orchestration tra repository e mapper.
 */
@Service
@RequiredArgsConstructor
public class RegionService {
    private final RegionRepository regionRepository;
    private final RegionMapper regionMapper;

    public List<RegionDto> findAll() {
        return regionRepository.findAllByOrderByName().stream().map(regionMapper::toDto).toList();
    }
    public RegionDto findById(Long id) {
        return regionRepository.findById(id).map(regionMapper::toDto).orElse(null);
    }
    public RegionDto save(RegionDto dto) {
        Region entity = regionMapper.toEntity(dto);
        return regionMapper.toDto(regionRepository.save(entity));
    }
    public void delete(Long id) {
        regionRepository.deleteById(id);
    }

    /**
     * Restituisce tutte le regioni di un country specifico.
     * @param countryId id del country
     * @return lista di regioni DTO
     */
    public List<RegionDto> findByCountryId(Long countryId) {
        return regionRepository.findByCountryId(countryId).stream()
                .map(regionMapper::toDto)
                .toList();
    }
}
