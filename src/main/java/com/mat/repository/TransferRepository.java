package com.mat.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.mat.entity.Base;
import com.mat.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * TransferRepository
 *
 * Provides standard CRUD operations for the Transfer entity.
 * Spring Data JPA automatically implements this interface at runtime.
 */
@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {

    /** Transfers OUT from a base */
    List<Transfer> findByFromBase(Base base);

    /** Transfers IN to a base */
    List<Transfer> findByToBase(Base base);

    /** Transfers OUT from a base within a date range */
    List<Transfer> findByFromBaseAndTransferDateBetween(Base base, LocalDateTime start, LocalDateTime end);

    /** Transfers IN to a base within a date range */
    List<Transfer> findByToBaseAndTransferDateBetween(Base base, LocalDateTime start, LocalDateTime end);

    /** Transfers where fromBase or toBase matches a given base ID */
    List<Transfer> findByFromBase_IdOrToBase_Id(Long fromBaseId, Long toBaseId);
}
