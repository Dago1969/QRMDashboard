package com.qtm.dashboard.repository;

import com.qtm.dashboard.domain.City;
import com.qtm.dashboard.dto.GeographicOptionDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository per City.
 */
import java.util.List;

public interface CityRepository extends JpaRepository<City, Long> {
	/**
	 * Trova tutte le città di una provincia.
	 */
	List<City> findByProvinceId(Long provinceId);

	@Query("select new com.qtm.dashboard.dto.GeographicOptionDto(city.id, city.name) " +
			"from City city where city.province.id = :provinceId order by city.name asc")
	List<GeographicOptionDto> findOptionsByProvinceId(@Param("provinceId") Long provinceId);
}
