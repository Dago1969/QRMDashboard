package com.qtm.dashboard.mail.repository;

import com.qtm.dashboard.mail.entity.MailTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MailTemplateRepository extends JpaRepository<MailTemplateEntity, Long> {
    Optional<MailTemplateEntity> findByCodeAndLanguageAndEnabledTrue(String code, String language);
    Optional<MailTemplateEntity> findByCodeAndEnabledTrue(String code);
}
