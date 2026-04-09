package com.qtm.dashboard.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity utenti gestiti centralmente su QTMDashboard.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", referencedColumnName = "id")
    private RoleEntity role;

    @Column(name = "structure_id")
    private Long structureId;
    
    /**
     * Email dell'utente (univoca, può essere usata per login e comunicazioni).
     */
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    /**
     * Hash della password (campo tecnico, non esposto in output pubblico).
     */
    @Column(name = "password_hash")
    private String passwordHash;

    /**
     * Numero di telefono dell'utente.
     */
    @Column(name = "telefono")
    private String telefono;

    /**
     * Codice fiscale dell'utente.
     */
    @Column(name = "codice_fiscale")
    private String codiceFiscale;

    /**
     * Data fine validità password.
     */
    @Column(name = "data_fine_validita_password")
    private java.time.LocalDate dataFineValiditaPassword;

    /**
     * Canale OTP preferito.
     */
    @Column(name = "canale_otp")
    private String canaleOtp;

    /**
     * Token monouso per reset password via mail.
     */
    @Column(name = "password_reset_token", unique = true)
    private String passwordResetToken;

    /**
     * Scadenza del token di reset password.
     */
    @Column(name = "password_reset_token_expiry")
    private LocalDateTime passwordResetTokenExpiry;
}
