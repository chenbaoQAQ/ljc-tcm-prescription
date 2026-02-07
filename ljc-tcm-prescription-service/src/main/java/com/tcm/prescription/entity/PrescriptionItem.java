package com.tcm.prescription.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "prescription_items")
@Getter
@Setter
@Where(clause = "deleted_at is null")
public class PrescriptionItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "herb_id", nullable = false)
    private Herb herb;

    @Column(name = "herb_name_snapshot", length = 64, nullable = false)
    private String herbNameSnapshot;

    @Column(name = "dose_g", nullable = false, precision = 10, scale = 2)
    private BigDecimal doseG;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
}
