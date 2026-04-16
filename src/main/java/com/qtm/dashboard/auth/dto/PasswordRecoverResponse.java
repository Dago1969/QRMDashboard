package com.qtm.dashboard.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO per la risposta generica delle operazioni di recupero password.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordRecoverResponse {
    private String message;
}
