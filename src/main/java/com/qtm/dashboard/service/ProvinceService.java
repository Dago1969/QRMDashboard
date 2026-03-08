package com.qtm.dashboard.service;

import com.qtm.dashboard.domain.Province;
import com.qtm.dashboard.dto.ProvinceDto;
import com.qtm.dashboard.mapper.ProvinceMapper;
import com.qtm.dashboard.repository.ProvinceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Service per Province. Orchestration tra repository e mapper.
 */
@Service
@RequiredArgsConstructor
public class ProvinceService {
    private final ProvinceRepository provinceRepository;
    private final ProvinceMapper provinceMapper;

    public List<ProvinceDto> findAll() {
        return provinceRepository.findAll().stream().map(provinceMapper::toDto).toList();
    }
    public ProvinceDto findById(Long id) {
        return provinceRepository.findById(id).map(provinceMapper::toDto).orElse(null);
    }
    public ProvinceDto save(ProvinceDto dto) {
        Province entity = provinceMapper.toEntity(dto);
        return provinceMapper.toDto(provinceRepository.save(entity));
    }
    public void delete(Long id) {
        provinceRepository.deleteById(id);
    }

    /**
     * Restituisce tutte le province di una regione specifica.
     * @param regionId id della regione
     * @return lista di province DTO
     */
    public List<ProvinceDto> findByRegionId(Long regionId) {
        return provinceRepository.findByRegionId(regionId).stream()
                .map(provinceMapper::toDto)
                .toList();
    }
}
