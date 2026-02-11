package com.tcm.prescription.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "medical_records")
// @Where(clause = "deleted_at is null") // Changed to physical delete
public class MedicalRecord extends BaseEntity {

    @Column(name = "patient_name", nullable = false, length = 64)
    private String patientName;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Column(name = "prescription_ids_json", nullable = false, columnDefinition = "TEXT")
    private String prescriptionIdsJson;

    @Column(name = "prescription_names_snapshot", nullable = false, length = 512)
    private String prescriptionNamesSnapshot;

    @Column(name = "merged_herbs_json", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String mergedHerbsJson;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
