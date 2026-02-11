package com.tcm.prescription.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "herbs")
// @Where(clause = "deleted_at is null") // Changed to physical delete
@Getter
@Setter
public class Herb extends BaseEntity {

    @Column(name = "name_cn", nullable = false, unique = true, length = 64)
    private String nameCn;

    @Column(name = "unit", nullable = false, length = 8)
    private String unit = "g";

    @Column(name = "default_dose_g", precision = 10, scale = 2)
    private BigDecimal defaultDoseG;

    @Column(name = "notes")
    private String notes;

    @Column(name = "status", nullable = false)
    private Integer status = 1; // 1: Enable, 0: Disable
}
