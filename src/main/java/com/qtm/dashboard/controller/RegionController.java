package com.qtm.dashboard.controller;

import com.qtm.dashboard.dto.RegionDto;
import com.qtm.dashboard.service.RegionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST Controller per Region.
 */
@RestController
@RequestMapping("/api/regions")
@RequiredArgsConstructor
@Slf4j
public class RegionController {
    private final RegionService regionService;


    @GetMapping
    public List<RegionDto> getAll() {
        log.info("Fetching all regions");
        return regionService.findAll();
    }

    /**
     * Restituisce tutte le regioni di un country specifico.
     * @param countryId id del country
     * @return lista di regioni
     */
    @GetMapping("/by-country/{countryId}")
    public List<RegionDto> getByCountry(@PathVariable Long countryId) {
        return regionService.findByCountryId(countryId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegionDto> getById(@PathVariable Long id) {
        RegionDto dto = regionService.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public RegionDto create(@RequestBody RegionDto dto) {
        return regionService.save(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegionDto> update(@PathVariable Long id, @RequestBody RegionDto dto) {
        dto.setId(id);
        RegionDto updated = regionService.save(dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        regionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
