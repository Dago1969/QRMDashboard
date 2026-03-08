package com.qtm.dashboard.repository;

import com.qtm.dashboard.domain.City;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository per City.
 */
import java.util.List;

public interface CityRepository extends JpaRepository<City, Long> {
	/**
	 * Trova tutte le città di una provincia.
	 */
	List<City> findByProvinceId(Long provinceId);
}
