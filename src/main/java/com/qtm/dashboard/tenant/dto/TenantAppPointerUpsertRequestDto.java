package com.qtm.dashboard.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO di input per create/update dei puntamenti verso TENANTS-APP.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantAppPointerUpsertRequestDto {

    @NotBlank
    @Size(max = 100)
    private String clientCode;

    @NotBlank
    @Size(max = 255)
    private String clientName;

    @NotBlank
    @Size(max = 512)
    private String tenantAppUrl;

    @NotNull
    private Boolean enabled;
}
