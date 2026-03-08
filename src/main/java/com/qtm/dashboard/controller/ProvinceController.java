package com.qtm.dashboard.controller;

import com.qtm.dashboard.dto.ProvinceDto;
import com.qtm.dashboard.service.ProvinceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST Controller per Province.
 */
@RestController
@RequestMapping("/api/provinces")
@RequiredArgsConstructor
public class ProvinceController {
    private final ProvinceService provinceService;


    @GetMapping
    public List<ProvinceDto> getAll() {
        return provinceService.findAll();
    }

    /**
     * Restituisce tutte le province di una regione specifica.
     * @param regionId id della regione
     * @return lista di province
     */
    @GetMapping("/by-region/{regionId}")
    public List<ProvinceDto> getByRegion(@PathVariable Long regionId) {
        return provinceService.findByRegionId(regionId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProvinceDto> getById(@PathVariable Long id) {
        ProvinceDto dto = provinceService.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ProvinceDto create(@RequestBody ProvinceDto dto) {
        return provinceService.save(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProvinceDto> update(@PathVariable Long id, @RequestBody ProvinceDto dto) {
        dto.setId(id);
        ProvinceDto updated = provinceService.save(dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        provinceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
