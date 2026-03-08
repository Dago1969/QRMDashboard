package com.qtm.dashboard.service;

import com.qtm.dashboard.domain.Country;
import com.qtm.dashboard.dto.CountryDto;
import com.qtm.dashboard.mapper.CountryMapper;
import com.qtm.dashboard.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service per Country. Orchestration tra repository e mapper.
 */
@Service
@RequiredArgsConstructor
public class CountryService {
    private final CountryRepository countryRepository;
    private final CountryMapper countryMapper;

    public List<CountryDto> findAll() {
        return countryRepository.findAll().stream().map(countryMapper::toDto).toList();
    }
    public CountryDto findById(Long id) {
        return countryRepository.findById(id).map(countryMapper::toDto).orElse(null);
    }
    public CountryDto save(CountryDto dto) {
        Country entity = countryMapper.toEntity(dto);
        return countryMapper.toDto(countryRepository.save(entity));
    }
    public void delete(Long id) {
        countryRepository.deleteById(id);
    }
}
