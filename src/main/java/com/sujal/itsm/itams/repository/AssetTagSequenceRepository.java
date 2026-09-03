package com.sujal.itsm.itams.repository;

import com.sujal.itsm.itams.model.AssetTagSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssetTagSequenceRepository extends JpaRepository<AssetTagSequence, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM AssetTagSequence s WHERE s.seqName = :name")
    Optional<AssetTagSequence> lockByName(@Param("name") String name);
}