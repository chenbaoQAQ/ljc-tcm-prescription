package com.tcm.prescription.repository;

import com.tcm.prescription.entity.Herb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface HerbRepository extends JpaRepository<Herb, Long>, JpaSpecificationExecutor<Herb> {
    boolean existsByNameCn(String nameCn);
}
