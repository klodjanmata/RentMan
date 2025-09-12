package com.rentman.rentman.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "defect")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Defect {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    @NotNull(message = "Vehicle is required")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    @NotNull(message = "Company is required")
    private Company company;

    @Column(name = "defect_number", unique = true, nullable = false)
    private String defectNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DefectType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DefectSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DefectStatus status = DefectStatus.REPORTED;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    // Location and details
    @Column(name = "location")
    private String location; // Front, Rear, Left, Right, Interior, etc.

    @Column(name = "component")
    private String component; // Engine, Brakes, Tires, etc.

    // Reporting information
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by_user_id")
    private User reportedByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by_employee_id")
    private User reportedByEmployee;

    @Column(name = "reported_date", nullable = false)
    private LocalDate reportedDate;

    @Column(name = "reported_during_reservation_id")
    private Long reportedDuringReservationId;

    // Investigation and resolution
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_employee_id")
    private User assignedToEmployee;

    @Column(name = "investigation_notes", columnDefinition = "TEXT")
    private String investigationNotes;

    @Column(name = "root_cause", columnDefinition = "TEXT")
    private String rootCause;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    // Dates
    @Column(name = "investigation_start_date")
    private LocalDate investigationStartDate;

    @Column(name = "investigation_completed_date")
    private LocalDate investigationCompletedDate;

    @Column(name = "resolution_date")
    private LocalDate resolutionDate;

    @Column(name = "estimated_resolution_date")
    private LocalDate estimatedResolutionDate;

    // Impact assessment
    @Column(name = "safety_impact")
    private Boolean safetyImpact = false;

    @Column(name = "operational_impact")
    private Boolean operationalImpact = false;

    @Column(name = "customer_impact")
    private Boolean customerImpact = false;

    @Column(name = "vehicle_out_of_service")
    private Boolean vehicleOutOfService = false;

    @Column(name = "estimated_downtime_days")
    private Integer estimatedDowntimeDays;

    @Column(name = "actual_downtime_days")
    private Integer actualDowntimeDays;

    // Related maintenance
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_maintenance_id")
    private Maintenance relatedMaintenance;

    // Photos and documentation
    @Column(name = "photo_urls", columnDefinition = "TEXT")
    private String photoUrls; // JSON array of photo URLs

    @Column(name = "document_urls", columnDefinition = "TEXT")
    private String documentUrls; // JSON array of document URLs

    // Follow-up
    @Column(name = "follow_up_required")
    private Boolean followUpRequired = false;

    @Column(name = "follow_up_date")
    private LocalDate followUpDate;

    @Column(name = "follow_up_notes", columnDefinition = "TEXT")
    private String followUpNotes;

    // Audit fields
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (defectNumber == null) {
            defectNumber = generateDefectNumber();
        }
        if (reportedDate == null) {
            reportedDate = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business logic methods
    private String generateDefectNumber() {
        return "DEF" + System.currentTimeMillis();
    }

    public boolean isCritical() {
        return severity == DefectSeverity.CRITICAL;
    }

    public boolean isHighPriority() {
        return severity == DefectSeverity.HIGH || severity == DefectSeverity.CRITICAL;
    }

    public boolean isResolved() {
        return status == DefectStatus.RESOLVED;
    }

    public boolean isInProgress() {
        return status == DefectStatus.INVESTIGATING || status == DefectStatus.IN_PROGRESS;
    }

    public boolean isOverdue() {
        return estimatedResolutionDate != null && 
               estimatedResolutionDate.isBefore(LocalDate.now()) && 
               !isResolved();
    }

    public boolean requiresImmediateAttention() {
        return safetyImpact || isCritical() || vehicleOutOfService;
    }

    // Enums
    public enum DefectType {
        MECHANICAL("Mechanical"),
        ELECTRICAL("Electrical"),
        BODY_DAMAGE("Body Damage"),
        INTERIOR_DAMAGE("Interior Damage"),
        TIRE_ISSUE("Tire Issue"),
        BRAKE_ISSUE("Brake Issue"),
        ENGINE_ISSUE("Engine Issue"),
        TRANSMISSION_ISSUE("Transmission Issue"),
        AIR_CONDITIONING("Air Conditioning"),
        SAFETY_EQUIPMENT("Safety Equipment"),
        CLEANLINESS("Cleanliness"),
        FUEL_SYSTEM("Fuel System"),
        EXHAUST_SYSTEM("Exhaust System"),
        SUSPENSION("Suspension"),
        OTHER("Other");

        private final String displayName;

        DefectType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum DefectSeverity {
        LOW("Low"),
        MEDIUM("Medium"),
        HIGH("High"),
        CRITICAL("Critical");

        private final String displayName;

        DefectSeverity(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum DefectStatus {
        REPORTED("Reported"),
        INVESTIGATING("Investigating"),
        IN_PROGRESS("In Progress"),
        RESOLVED("Resolved"),
        CLOSED("Closed"),
        CANCELLED("Cancelled"),
        DUPLICATE("Duplicate");

        private final String displayName;

        DefectStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
