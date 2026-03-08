package com.qtm.dashboard.repository;

import com.qtm.dashboard.domain.Province;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository per Province.
 */
import java.util.List;

public interface ProvinceRepository extends JpaRepository<Province, Long> {
	/**
	 * Trova tutte le province di una regione.
	 */
	List<Province> findByRegionId(Long regionId);
}
