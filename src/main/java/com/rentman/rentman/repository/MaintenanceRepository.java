package com.rentman.rentman.repository;

import com.rentman.rentman.entity.Maintenance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenanceRepository extends JpaRepository<Maintenance, Long> {

    // Find by maintenance number
    Optional<Maintenance> findByMaintenanceNumber(String maintenanceNumber);

    // Find by vehicle
    List<Maintenance> findByVehicleIdOrderByCreatedAtDesc(Long vehicleId);

    // Find by company
    List<Maintenance> findByCompanyIdOrderByCreatedAtDesc(Long companyId);

    // Find by vehicle and status
    List<Maintenance> findByVehicleIdAndStatusOrderByCreatedAtDesc(Long vehicleId, Maintenance.MaintenanceStatus status);

    // Find by company and status
    List<Maintenance> findByCompanyIdAndStatusOrderByCreatedAtDesc(Long companyId, Maintenance.MaintenanceStatus status);

    // Find by status
    List<Maintenance> findByStatus(Maintenance.MaintenanceStatus status);

    // Find by type
    List<Maintenance> findByType(Maintenance.MaintenanceType type);

    // Find by company and type
    List<Maintenance> findByCompanyIdAndTypeOrderByCreatedAtDesc(Long companyId, Maintenance.MaintenanceType type);

    // Find overdue maintenance
    @Query("SELECT m FROM Maintenance m WHERE m.status = 'SCHEDULED' AND m.scheduledDate < :currentDate")
    List<Maintenance> findOverdueMaintenance(@Param("currentDate") LocalDate currentDate);

    // Find overdue maintenance by company
    @Query("SELECT m FROM Maintenance m WHERE m.company.id = :companyId AND m.status = 'SCHEDULED' AND m.scheduledDate < :currentDate")
    List<Maintenance> findOverdueMaintenanceByCompany(@Param("companyId") Long companyId, @Param("currentDate") LocalDate currentDate);

    // Find maintenance due soon (within next 7 days)
    @Query("SELECT m FROM Maintenance m WHERE m.status = 'SCHEDULED' AND m.scheduledDate BETWEEN :currentDate AND :futureDate")
    List<Maintenance> findMaintenanceDueSoon(@Param("currentDate") LocalDate currentDate, @Param("futureDate") LocalDate futureDate);

    // Find maintenance due soon by company
    @Query("SELECT m FROM Maintenance m WHERE m.company.id = :companyId AND m.status = 'SCHEDULED' AND m.scheduledDate BETWEEN :currentDate AND :futureDate")
    List<Maintenance> findMaintenanceDueSoonByCompany(@Param("companyId") Long companyId, 
                                                    @Param("currentDate") LocalDate currentDate, 
                                                    @Param("futureDate") LocalDate futureDate);

    // Find maintenance by date range
    @Query("SELECT m FROM Maintenance m WHERE m.scheduledDate BETWEEN :startDate AND :endDate")
    List<Maintenance> findMaintenanceByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Find maintenance by company and date range
    @Query("SELECT m FROM Maintenance m WHERE m.company.id = :companyId AND m.scheduledDate BETWEEN :startDate AND :endDate")
    List<Maintenance> findMaintenanceByCompanyAndDateRange(@Param("companyId") Long companyId, 
                                                         @Param("startDate") LocalDate startDate, 
                                                         @Param("endDate") LocalDate endDate);

    // Find maintenance by cost range
    @Query("SELECT m FROM Maintenance m WHERE m.actualCost BETWEEN :minCost AND :maxCost")
    List<Maintenance> findByCostRange(@Param("minCost") BigDecimal minCost, @Param("maxCost") BigDecimal maxCost);

    // Find maintenance by company and cost range
    @Query("SELECT m FROM Maintenance m WHERE m.company.id = :companyId AND m.actualCost BETWEEN :minCost AND :maxCost")
    List<Maintenance> findByCompanyAndCostRange(@Param("companyId") Long companyId, 
                                              @Param("minCost") BigDecimal minCost, 
                                              @Param("maxCost") BigDecimal maxCost);

    // Find maintenance by service provider
    List<Maintenance> findByServiceProvider(String serviceProvider);

    // Find maintenance by company and service provider
    List<Maintenance> findByCompanyIdAndServiceProvider(Long companyId, String serviceProvider);

    // Find maintenance by employee
    List<Maintenance> findByPerformedByEmployeeId(Long employeeId);

    List<Maintenance> findBySupervisedByEmployeeId(Long employeeId);

    // Find recurring maintenance
    List<Maintenance> findByIsRecurringTrue();

    // Find recurring maintenance by company
    List<Maintenance> findByCompanyIdAndIsRecurringTrue(Long companyId);

    // Find maintenance under warranty
    @Query("SELECT m FROM Maintenance m WHERE m.warrantyExpiryDate IS NOT NULL AND m.warrantyExpiryDate > :currentDate")
    List<Maintenance> findMaintenanceUnderWarranty(@Param("currentDate") LocalDate currentDate);

    // Find maintenance under warranty by company
    @Query("SELECT m FROM Maintenance m WHERE m.company.id = :companyId AND m.warrantyExpiryDate IS NOT NULL AND m.warrantyExpiryDate > :currentDate")
    List<Maintenance> findMaintenanceUnderWarrantyByCompany(@Param("companyId") Long companyId, @Param("currentDate") LocalDate currentDate);

    // Calculate total maintenance cost by vehicle
    @Query("SELECT SUM(m.actualCost) FROM Maintenance m WHERE m.vehicle.id = :vehicleId AND m.status = 'COMPLETED'")
    BigDecimal calculateTotalCostByVehicle(@Param("vehicleId") Long vehicleId);

    // Calculate total maintenance cost by company
    @Query("SELECT SUM(m.actualCost) FROM Maintenance m WHERE m.company.id = :companyId AND m.status = 'COMPLETED'")
    BigDecimal calculateTotalCostByCompany(@Param("companyId") Long companyId);

    // Calculate total maintenance cost by company and date range
    @Query("SELECT SUM(m.actualCost) FROM Maintenance m WHERE m.company.id = :companyId AND m.status = 'COMPLETED' AND m.completionDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalCostByCompanyAndDateRange(@Param("companyId") Long companyId, 
                                                     @Param("startDate") LocalDate startDate, 
                                                     @Param("endDate") LocalDate endDate);

    // Get maintenance statistics by company
    @Query("SELECT " +
           "COUNT(m) as totalMaintenance, " +
           "COUNT(CASE WHEN m.status = 'COMPLETED' THEN 1 END) as completedMaintenance, " +
           "COUNT(CASE WHEN m.status = 'SCHEDULED' THEN 1 END) as scheduledMaintenance, " +
           "COUNT(CASE WHEN m.status = 'IN_PROGRESS' THEN 1 END) as inProgressMaintenance, " +
           "SUM(m.actualCost) as totalCost, " +
           "AVG(m.actualCost) as averageCost, " +
           "AVG(m.qualityRating) as averageRating " +
           "FROM Maintenance m WHERE m.company.id = :companyId")
    Object[] getMaintenanceStatisticsByCompany(@Param("companyId") Long companyId);

    // Get maintenance statistics by vehicle
    @Query("SELECT " +
           "COUNT(m) as totalMaintenance, " +
           "COUNT(CASE WHEN m.status = 'COMPLETED' THEN 1 END) as completedMaintenance, " +
           "SUM(m.actualCost) as totalCost, " +
           "AVG(m.actualCost) as averageCost, " +
           "AVG(m.qualityRating) as averageRating " +
           "FROM Maintenance m WHERE m.vehicle.id = :vehicleId")
    Object[] getMaintenanceStatisticsByVehicle(@Param("vehicleId") Long vehicleId);

    // Find maintenance by quality rating
    @Query("SELECT m FROM Maintenance m WHERE m.qualityRating >= :minRating")
    List<Maintenance> findByMinimumQualityRating(@Param("minRating") Integer minRating);

    // Find maintenance by company and quality rating
    @Query("SELECT m FROM Maintenance m WHERE m.company.id = :companyId AND m.qualityRating >= :minRating")
    List<Maintenance> findByCompanyAndMinimumQualityRating(@Param("companyId") Long companyId, @Param("minRating") Integer minRating);

    // Find maintenance created in date range
    @Query("SELECT m FROM Maintenance m WHERE m.createdAt BETWEEN :startDate AND :endDate")
    List<Maintenance> findMaintenanceCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                                  @Param("endDate") LocalDateTime endDate);

    // Find maintenance by company and created date range
    @Query("SELECT m FROM Maintenance m WHERE m.company.id = :companyId AND m.createdAt BETWEEN :startDate AND :endDate")
    List<Maintenance> findMaintenanceByCompanyAndCreatedDateRange(@Param("companyId") Long companyId, 
                                                                @Param("startDate") LocalDateTime startDate, 
                                                                @Param("endDate") LocalDateTime endDate);

    // Count maintenance by status
    long countByStatus(Maintenance.MaintenanceStatus status);

    // Count maintenance by company and status
    long countByCompanyIdAndStatus(Long companyId, Maintenance.MaintenanceStatus status);

    // Count maintenance by type
    long countByType(Maintenance.MaintenanceType type);

    // Count maintenance by company and type
    long countByCompanyIdAndType(Long companyId, Maintenance.MaintenanceType type);

    // Count maintenance by vehicle
    long countByVehicleId(Long vehicleId);

    // Find maintenance by vehicle and type
    List<Maintenance> findByVehicleIdAndTypeOrderByCreatedAtDesc(Long vehicleId, Maintenance.MaintenanceType type);

    // Find last maintenance by vehicle
    @Query("SELECT m FROM Maintenance m WHERE m.vehicle.id = :vehicleId AND m.status = 'COMPLETED' ORDER BY m.completionDate DESC")
    Page<Maintenance> findLastMaintenanceByVehicle(@Param("vehicleId") Long vehicleId, Pageable pageable);

    // Find next scheduled maintenance by vehicle
    @Query("SELECT m FROM Maintenance m WHERE m.vehicle.id = :vehicleId AND m.status = 'SCHEDULED' ORDER BY m.scheduledDate ASC")
    Page<Maintenance> findNextScheduledMaintenanceByVehicle(@Param("vehicleId") Long vehicleId, Pageable pageable);
}
