package com.tcm.prescription.repository;

import com.tcm.prescription.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long>, JpaSpecificationExecutor<Prescription> {
    
    @Query("select p from Prescription p where p.id in :ids")
    List<Prescription> findAllByIds(@Param("ids") Collection<Long> ids);
}
