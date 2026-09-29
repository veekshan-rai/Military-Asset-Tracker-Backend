package com.mat.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.mat.entity.Base;
import com.mat.entity.Expenditure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * ExpenditureRepository
 *
 * Provides standard CRUD operations for the Expenditure entity.
 * Spring Data JPA automatically implements this interface at runtime.
 */
@Repository
public interface ExpenditureRepository extends JpaRepository<Expenditure, Long> {

    /** Filter expenditures by base */
    List<Expenditure> findByBase(Base base);

    /** Filter expenditures by base and date range */
    List<Expenditure> findByBaseAndExpenditureDateBetween(Base base, LocalDateTime start, LocalDateTime end);

    /** Filter expenditures by base ID */
    List<Expenditure> findByBase_Id(Long baseId);
}
