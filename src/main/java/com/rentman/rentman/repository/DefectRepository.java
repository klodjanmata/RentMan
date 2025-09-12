package com.rentman.rentman.repository;

import com.rentman.rentman.entity.Defect;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DefectRepository extends JpaRepository<Defect, Long> {

    // Find by defect number
    Optional<Defect> findByDefectNumber(String defectNumber);

    // Find by vehicle
    List<Defect> findByVehicleIdOrderByCreatedAtDesc(Long vehicleId);

    // Find by company
    List<Defect> findByCompanyIdOrderByCreatedAtDesc(Long companyId);

    // Find by vehicle and status
    List<Defect> findByVehicleIdAndStatusOrderByCreatedAtDesc(Long vehicleId, Defect.DefectStatus status);

    // Find by company and status
    List<Defect> findByCompanyIdAndStatusOrderByCreatedAtDesc(Long companyId, Defect.DefectStatus status);

    // Find by status
    List<Defect> findByStatus(Defect.DefectStatus status);

    // Find by type
    List<Defect> findByType(Defect.DefectType type);

    // Find by severity
    List<Defect> findBySeverity(Defect.DefectSeverity severity);

    // Find by company and type
    List<Defect> findByCompanyIdAndTypeOrderByCreatedAtDesc(Long companyId, Defect.DefectType type);

    // Find by company and severity
    List<Defect> findByCompanyIdAndSeverityOrderByCreatedAtDesc(Long companyId, Defect.DefectSeverity severity);

    // Find critical defects
    @Query("SELECT d FROM Defect d WHERE d.severity = 'CRITICAL'")
    List<Defect> findCriticalDefects();

    // Find critical defects by company
    @Query("SELECT d FROM Defect d WHERE d.company.id = :companyId AND d.severity = 'CRITICAL'")
    List<Defect> findCriticalDefectsByCompany(@Param("companyId") Long companyId);

    // Find high priority defects
    @Query("SELECT d FROM Defect d WHERE d.severity IN ('HIGH', 'CRITICAL')")
    List<Defect> findHighPriorityDefects();

    // Find high priority defects by company
    @Query("SELECT d FROM Defect d WHERE d.company.id = :companyId AND d.severity IN ('HIGH', 'CRITICAL')")
    List<Defect> findHighPriorityDefectsByCompany(@Param("companyId") Long companyId);

    // Find defects requiring immediate attention
    @Query("SELECT d FROM Defect d WHERE d.safetyImpact = true OR d.severity = 'CRITICAL' OR d.vehicleOutOfService = true")
    List<Defect> findDefectsRequiringImmediateAttention();

    // Find defects requiring immediate attention by company
    @Query("SELECT d FROM Defect d WHERE d.company.id = :companyId AND (d.safetyImpact = true OR d.severity = 'CRITICAL' OR d.vehicleOutOfService = true)")
    List<Defect> findDefectsRequiringImmediateAttentionByCompany(@Param("companyId") Long companyId);

    // Find overdue defects
    @Query("SELECT d FROM Defect d WHERE d.estimatedResolutionDate IS NOT NULL AND d.estimatedResolutionDate < :currentDate AND d.status NOT IN ('RESOLVED', 'CLOSED')")
    List<Defect> findOverdueDefects(@Param("currentDate") LocalDate currentDate);

    // Find overdue defects by company
    @Query("SELECT d FROM Defect d WHERE d.company.id = :companyId AND d.estimatedResolutionDate IS NOT NULL AND d.estimatedResolutionDate < :currentDate AND d.status NOT IN ('RESOLVED', 'CLOSED')")
    List<Defect> findOverdueDefectsByCompany(@Param("companyId") Long companyId, @Param("currentDate") LocalDate currentDate);

    // Find unresolved defects
    @Query("SELECT d FROM Defect d WHERE d.status NOT IN ('RESOLVED', 'CLOSED', 'CANCELLED')")
    List<Defect> findUnresolvedDefects();

    // Find unresolved defects by company
    @Query("SELECT d FROM Defect d WHERE d.company.id = :companyId AND d.status NOT IN ('RESOLVED', 'CLOSED', 'CANCELLED')")
    List<Defect> findUnresolvedDefectsByCompany(@Param("companyId") Long companyId);

    // Find defects by date range
    @Query("SELECT d FROM Defect d WHERE d.reportedDate BETWEEN :startDate AND :endDate")
    List<Defect> findDefectsByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Find defects by company and date range
    @Query("SELECT d FROM Defect d WHERE d.company.id = :companyId AND d.reportedDate BETWEEN :startDate AND :endDate")
    List<Defect> findDefectsByCompanyAndDateRange(@Param("companyId") Long companyId, 
                                                @Param("startDate") LocalDate startDate, 
                                                @Param("endDate") LocalDate endDate);

    // Find defects by reporter
    List<Defect> findByReportedByUserId(Long userId);

    List<Defect> findByReportedByEmployeeId(Long employeeId);

    // Find defects by assigned employee
    List<Defect> findByAssignedToEmployeeId(Long employeeId);

    // Find defects by location
    List<Defect> findByLocation(String location);

    // Find defects by component
    List<Defect> findByComponent(String component);

    // Find defects by company and location
    List<Defect> findByCompanyIdAndLocation(Long companyId, String location);

    // Find defects by company and component
    List<Defect> findByCompanyIdAndComponent(Long companyId, String component);

    // Find defects with safety impact
    @Query("SELECT d FROM Defect d WHERE d.safetyImpact = true")
    List<Defect> findDefectsWithSafetyImpact();

    // Find defects with safety impact by company
    @Query("SELECT d FROM Defect d WHERE d.company.id = :companyId AND d.safetyImpact = true")
    List<Defect> findDefectsWithSafetyImpactByCompany(@Param("companyId") Long companyId);

    // Find defects with operational impact
    @Query("SELECT d FROM Defect d WHERE d.operationalImpact = true")
    List<Defect> findDefectsWithOperationalImpact();

    // Find defects with operational impact by company
    @Query("SELECT d FROM Defect d WHERE d.company.id = :companyId AND d.operationalImpact = true")
    List<Defect> findDefectsWithOperationalImpactByCompany(@Param("companyId") Long companyId);

    // Find defects with customer impact
    @Query("SELECT d FROM Defect d WHERE d.customerImpact = true")
    List<Defect> findDefectsWithCustomerImpact();

    // Find defects with customer impact by company
    @Query("SELECT d FROM Defect d WHERE d.company.id = :companyId AND d.customerImpact = true")
    List<Defect> findDefectsWithCustomerImpactByCompany(@Param("companyId") Long companyId);

    // Find defects that put vehicle out of service
    @Query("SELECT d FROM Defect d WHERE d.vehicleOutOfService = true")
    List<Defect> findDefectsWithVehicleOutOfService();

    // Find defects that put vehicle out of service by company
    @Query("SELECT d FROM Defect d WHERE d.company.id = :companyId AND d.vehicleOutOfService = true")
    List<Defect> findDefectsWithVehicleOutOfServiceByCompany(@Param("companyId") Long companyId);

    // Find defects requiring follow-up
    @Query("SELECT d FROM Defect d WHERE d.followUpRequired = true")
    List<Defect> findDefectsRequiringFollowUp();

    // Find defects requiring follow-up by company
    @Query("SELECT d FROM Defect d WHERE d.company.id = :companyId AND d.followUpRequired = true")
    List<Defect> findDefectsRequiringFollowUpByCompany(@Param("companyId") Long companyId);

    // Find defects by reservation
    List<Defect> findByReportedDuringReservationId(Long reservationId);

    // Get defect statistics by company
    @Query("SELECT " +
           "COUNT(d) as totalDefects, " +
           "COUNT(CASE WHEN d.status = 'REPORTED' THEN 1 END) as reportedDefects, " +
           "COUNT(CASE WHEN d.status = 'INVESTIGATING' THEN 1 END) as investigatingDefects, " +
           "COUNT(CASE WHEN d.status = 'IN_PROGRESS' THEN 1 END) as inProgressDefects, " +
           "COUNT(CASE WHEN d.status = 'RESOLVED' THEN 1 END) as resolvedDefects, " +
           "COUNT(CASE WHEN d.severity = 'CRITICAL' THEN 1 END) as criticalDefects, " +
           "COUNT(CASE WHEN d.severity = 'HIGH' THEN 1 END) as highDefects, " +
           "COUNT(CASE WHEN d.safetyImpact = true THEN 1 END) as safetyImpactDefects, " +
           "COUNT(CASE WHEN d.vehicleOutOfService = true THEN 1 END) as vehicleOutOfServiceDefects " +
           "FROM Defect d WHERE d.company.id = :companyId")
    Object[] getDefectStatisticsByCompany(@Param("companyId") Long companyId);

    // Get defect statistics by vehicle
    @Query("SELECT " +
           "COUNT(d) as totalDefects, " +
           "COUNT(CASE WHEN d.status = 'RESOLVED' THEN 1 END) as resolvedDefects, " +
           "COUNT(CASE WHEN d.severity = 'CRITICAL' THEN 1 END) as criticalDefects, " +
           "COUNT(CASE WHEN d.safetyImpact = true THEN 1 END) as safetyImpactDefects " +
           "FROM Defect d WHERE d.vehicle.id = :vehicleId")
    Object[] getDefectStatisticsByVehicle(@Param("vehicleId") Long vehicleId);

    // Find defects created in date range
    @Query("SELECT d FROM Defect d WHERE d.createdAt BETWEEN :startDate AND :endDate")
    List<Defect> findDefectsCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);

    // Find defects by company and created date range
    @Query("SELECT d FROM Defect d WHERE d.company.id = :companyId AND d.createdAt BETWEEN :startDate AND :endDate")
    List<Defect> findDefectsByCompanyAndCreatedDateRange(@Param("companyId") Long companyId, 
                                                       @Param("startDate") LocalDateTime startDate, 
                                                       @Param("endDate") LocalDateTime endDate);

    // Count defects by status
    long countByStatus(Defect.DefectStatus status);

    // Count defects by company and status
    long countByCompanyIdAndStatus(Long companyId, Defect.DefectStatus status);

    // Count defects by type
    long countByType(Defect.DefectType type);

    // Count defects by company and type
    long countByCompanyIdAndType(Long companyId, Defect.DefectType type);

    // Count defects by severity
    long countBySeverity(Defect.DefectSeverity severity);

    // Count defects by company and severity
    long countByCompanyIdAndSeverity(Long companyId, Defect.DefectSeverity severity);

    // Count defects by vehicle
    long countByVehicleId(Long vehicleId);

    // Find defects by vehicle and type
    List<Defect> findByVehicleIdAndTypeOrderByCreatedAtDesc(Long vehicleId, Defect.DefectType type);

    // Find defects by vehicle and severity
    List<Defect> findByVehicleIdAndSeverityOrderByCreatedAtDesc(Long vehicleId, Defect.DefectSeverity severity);

    // Find recent defects by vehicle
    @Query("SELECT d FROM Defect d WHERE d.vehicle.id = :vehicleId ORDER BY d.createdAt DESC")
    Page<Defect> findRecentDefectsByVehicle(@Param("vehicleId") Long vehicleId, Pageable pageable);

    // Find recent defects by company
    @Query("SELECT d FROM Defect d WHERE d.company.id = :companyId ORDER BY d.createdAt DESC")
    Page<Defect> findRecentDefectsByCompany(@Param("companyId") Long companyId, Pageable pageable);
}
