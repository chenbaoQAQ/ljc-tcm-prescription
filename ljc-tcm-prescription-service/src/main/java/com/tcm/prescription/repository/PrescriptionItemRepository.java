package com.tcm.prescription.repository;

import com.tcm.prescription.entity.PrescriptionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface PrescriptionItemRepository extends JpaRepository<PrescriptionItem, Long> {

    @Query("select pi from PrescriptionItem pi join fetch pi.herb join fetch pi.prescription where pi.prescription.id in :prescriptionIds")
    List<PrescriptionItem> findByPrescriptionIds(@Param("prescriptionIds") Collection<Long> prescriptionIds);
}
