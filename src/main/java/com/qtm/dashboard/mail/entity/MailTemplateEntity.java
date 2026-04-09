package com.qtm.dashboard.mail.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity per la gestione centralizzata dei template mail personalizzati.
 */
@Entity
@Table(name = "mail_template")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MailTemplateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String code; // es: PASSWORD_RECOVER, ONBOARDING

    @Column(nullable = false, length = 8)
    private String language; // es: it, en

    @Column(nullable = false, length = 256)
    private String subject;

    @Lob
    @Column(nullable = false)
    private String body;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private LocalDateTime lastUpdate;
}
