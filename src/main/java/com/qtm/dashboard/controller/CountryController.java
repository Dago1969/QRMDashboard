package com.qtm.dashboard.controller;

import com.qtm.dashboard.dto.CountryDto;
import com.qtm.dashboard.service.CountryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST Controller per Country.
 */
@RestController
@RequestMapping("/api/countries")
@RequiredArgsConstructor
public class CountryController {
    private final CountryService countryService;

    @GetMapping
    public List<CountryDto> getAll() {
        return countryService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CountryDto> getById(@PathVariable Long id) {
        CountryDto dto = countryService.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public CountryDto create(@RequestBody CountryDto dto) {
        return countryService.save(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CountryDto> update(@PathVariable Long id, @RequestBody CountryDto dto) {
        dto.setId(id);
        CountryDto updated = countryService.save(dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        countryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
