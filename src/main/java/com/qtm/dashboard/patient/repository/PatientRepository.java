package com.qtm.dashboard.patient.repository;

import com.qtm.dashboard.patient.entity.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository JPA dei pazienti persistiti in QTMDB.
 */
public interface PatientRepository extends JpaRepository<PatientEntity, Long> {

    boolean existsByFiscalCodeIgnoreCase(String fiscalCode);

    boolean existsByFiscalCodeIgnoreCaseAndIdNot(String fiscalCode, Long id);
}