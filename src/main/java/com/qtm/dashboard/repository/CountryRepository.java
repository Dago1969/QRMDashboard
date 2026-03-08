package com.qtm.dashboard.repository;

import com.qtm.dashboard.domain.Country;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository per Country.
 */
public interface CountryRepository extends JpaRepository<Country, Long> {
}
