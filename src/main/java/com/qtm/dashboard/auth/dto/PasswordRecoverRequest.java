package com.qtm.dashboard.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO per la richiesta di recupero password.
 */
@Data
public class PasswordRecoverRequest {
    @NotBlank
    @Email
    private String email;
}
