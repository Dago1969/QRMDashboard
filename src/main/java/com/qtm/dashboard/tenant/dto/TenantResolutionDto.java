package com.qtm.dashboard.tenant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO di risoluzione rapida per instradamento client_code -> tenant_app_url.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantResolutionDto {

    private String clientCode;
    private String tenantAppUrl;
}
