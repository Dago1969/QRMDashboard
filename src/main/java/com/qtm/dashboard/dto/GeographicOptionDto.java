package com.qtm.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO minimale per select geografiche con sola coppia id/name.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeographicOptionDto {
    private Long id;
    private String name;
}