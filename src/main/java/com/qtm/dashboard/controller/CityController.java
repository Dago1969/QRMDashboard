package com.qtm.dashboard.controller;

import com.qtm.dashboard.dto.CityDto;
import com.qtm.dashboard.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST Controller per City.
 */
@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
public class CityController {
    private final CityService cityService;


    @GetMapping
    public List<CityDto> getAll() {
        return cityService.findAll();
    }

    /**
     * Restituisce tutte le città di una provincia specifica.
     * @param provinceId id della provincia
     * @return lista di città
     */
    @GetMapping("/by-province/{provinceId}")
    public List<CityDto> getByProvince(@PathVariable Long provinceId) {
        return cityService.findByProvinceId(provinceId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CityDto> getById(@PathVariable Long id) {
        CityDto dto = cityService.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public CityDto create(@RequestBody CityDto dto) {
        return cityService.save(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CityDto> update(@PathVariable Long id, @RequestBody CityDto dto) {
        dto.setId(id);
        CityDto updated = cityService.save(dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cityService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
