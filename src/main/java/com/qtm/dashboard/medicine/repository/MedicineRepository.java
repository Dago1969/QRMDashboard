package com.qtm.dashboard.medicine.repository;

import com.qtm.dashboard.medicine.entity.MedicineEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository JPA dei farmaci persistiti in QTMDB.
 */
public interface MedicineRepository extends JpaRepository<MedicineEntity, Long> {

    boolean existsByCodiceAicIgnoreCase(String codiceAic);

    boolean existsByCodiceAicIgnoreCaseAndIdNot(String codiceAic, Long id);

    Optional<MedicineEntity> findByCodiceAicIgnoreCase(String codiceAic);

    @Query("""
            select medicine
            from MedicineEntity medicine
            where (:query = ''
                or upper(medicine.codiceAic) like upper(concat('%', :query, '%'))
                or upper(medicine.codFarmaco) like upper(concat('%', :query, '%'))
                or upper(medicine.denominazione) like upper(concat('%', :query, '%'))
                or upper(medicine.descrizione) like upper(concat('%', :query, '%'))
                or upper(medicine.codiceAtc) like upper(concat('%', :query, '%')))
            order by medicine.denominazione asc, medicine.codiceAic asc
            """)
    List<MedicineEntity> lookup(@Param("query") String query, Pageable pageable);
}