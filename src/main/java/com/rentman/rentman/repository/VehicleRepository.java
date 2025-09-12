package com.rentman.rentman.repository;

import com.rentman.rentman.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    // Find by license plate
    Optional<Vehicle> findByLicensePlate(String licensePlate);

    // Find all available vehicles
    List<Vehicle> findByStatus(Vehicle.VehicleStatus status);

    // Find vehicles by type
    List<Vehicle> findByType(Vehicle.VehicleType type);

    // Find vehicles by make and model
    List<Vehicle> findByMakeAndModel(String make, String model);

    // Find vehicles with daily rate less than or equal to specified amount
    List<Vehicle> findByDailyRateLessThanEqual(BigDecimal maxDailyRate);

    // Find available vehicles by type
    List<Vehicle> findByStatusAndType(Vehicle.VehicleStatus status, Vehicle.VehicleType type);

    // Custom query to find available vehicles with filters
    @Query("SELECT v FROM Vehicle v WHERE v.status = :status " +
            "AND (:type IS NULL OR v.type = :type) " +
            "AND (:maxRate IS NULL OR v.dailyRate <= :maxRate)")
    List<Vehicle> findAvailableVehiclesWithFilters(
            @Param("status") Vehicle.VehicleStatus status,
            @Param("type") Vehicle.VehicleType type,
            @Param("maxRate") BigDecimal maxRate
    );

    // Count vehicles by status
    long countByStatus(Vehicle.VehicleStatus status);

    // Find vehicles by year range
    List<Vehicle> findByYearBetween(Integer startYear, Integer endYear);

    // Find vehicles by company
    List<Vehicle> findByCompanyIdOrderByCreatedAtDesc(Long companyId);

    // Find vehicles by company and status
    List<Vehicle> findByCompanyIdAndStatusOrderByCreatedAtDesc(Long companyId, Vehicle.VehicleStatus status);

    // Find vehicles by company and type
    List<Vehicle> findByCompanyIdAndTypeOrderByCreatedAtDesc(Long companyId, Vehicle.VehicleType type);

    // Find available vehicles by company
    List<Vehicle> findByCompanyIdAndStatusAndIsAvailableForRentalTrueOrderByCreatedAtDesc(Long companyId, Vehicle.VehicleStatus status);

    // Find featured vehicles by company
    List<Vehicle> findByCompanyIdAndIsFeaturedTrueOrderByCreatedAtDesc(Long companyId);

    // Find vehicles by company and location
    List<Vehicle> findByCompanyIdAndCurrentLocation(Long companyId, String location);

    List<Vehicle> findByCompanyIdAndPickupLocation(Long companyId, String pickupLocation);

    // Find vehicles by company with daily rate range
    @Query("SELECT v FROM Vehicle v WHERE v.company.id = :companyId AND v.dailyRate BETWEEN :minRate AND :maxRate")
    List<Vehicle> findByCompanyIdAndDailyRateRange(@Param("companyId") Long companyId, 
                                                 @Param("minRate") BigDecimal minRate, 
                                                 @Param("maxRate") BigDecimal maxRate);

    // Find vehicles by company and year range
    List<Vehicle> findByCompanyIdAndYearBetween(Long companyId, Integer startYear, Integer endYear);

    // Find vehicles by company and make/model
    List<Vehicle> findByCompanyIdAndMakeAndModel(Long companyId, String make, String model);

    // Find vehicles by company and fuel type
    List<Vehicle> findByCompanyIdAndFuelType(Long companyId, String fuelType);

    // Find vehicles by company and transmission
    List<Vehicle> findByCompanyIdAndTransmission(Long companyId, String transmission);

    // Find vehicles by company and seating capacity
    List<Vehicle> findByCompanyIdAndSeatingCapacity(Long companyId, Integer seatingCapacity);

    // Find vehicles by company and color
    List<Vehicle> findByCompanyIdAndColor(Long companyId, String color);

    // Advanced search for customers (across all companies)
    @Query("SELECT v FROM Vehicle v WHERE " +
           "v.status = 'AVAILABLE' AND v.isAvailableForRental = true AND " +
           "(:companyId IS NULL OR v.company.id = :companyId) AND " +
           "(:type IS NULL OR v.type = :type) AND " +
           "(:make IS NULL OR LOWER(v.make) LIKE LOWER(CONCAT('%', :make, '%'))) AND " +
           "(:model IS NULL OR LOWER(v.model) LIKE LOWER(CONCAT('%', :model, '%'))) AND " +
           "(:minRate IS NULL OR v.dailyRate >= :minRate) AND " +
           "(:maxRate IS NULL OR v.dailyRate <= :maxRate) AND " +
           "(:minYear IS NULL OR v.year >= :minYear) AND " +
           "(:maxYear IS NULL OR v.year <= :maxYear) AND " +
           "(:fuelType IS NULL OR v.fuelType = :fuelType) AND " +
           "(:transmission IS NULL OR v.transmission = :transmission) AND " +
           "(:minSeating IS NULL OR v.seatingCapacity >= :minSeating) AND " +
           "(:maxSeating IS NULL OR v.seatingCapacity <= :maxSeating) AND " +
           "(:location IS NULL OR LOWER(v.currentLocation) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:airConditioning IS NULL OR v.airConditioning = :airConditioning) AND " +
           "(:gpsNavigation IS NULL OR v.gpsNavigation = :gpsNavigation) AND " +
           "(:bluetooth IS NULL OR v.bluetooth = :bluetooth) AND " +
           "(:backupCamera IS NULL OR v.backupCamera = :backupCamera) AND " +
           "(:sunroof IS NULL OR v.sunroof = :sunroof) AND " +
           "(:leatherSeats IS NULL OR v.leatherSeats = :leatherSeats)")
    List<Vehicle> searchVehicles(@Param("companyId") Long companyId,
                               @Param("type") Vehicle.VehicleType type,
                               @Param("make") String make,
                               @Param("model") String model,
                               @Param("minRate") BigDecimal minRate,
                               @Param("maxRate") BigDecimal maxRate,
                               @Param("minYear") Integer minYear,
                               @Param("maxYear") Integer maxYear,
                               @Param("fuelType") String fuelType,
                               @Param("transmission") String transmission,
                               @Param("minSeating") Integer minSeating,
                               @Param("maxSeating") Integer maxSeating,
                               @Param("location") String location,
                               @Param("airConditioning") Boolean airConditioning,
                               @Param("gpsNavigation") Boolean gpsNavigation,
                               @Param("bluetooth") Boolean bluetooth,
                               @Param("backupCamera") Boolean backupCamera,
                               @Param("sunroof") Boolean sunroof,
                               @Param("leatherSeats") Boolean leatherSeats);

    // Find vehicles by company with advanced filters
    @Query("SELECT v FROM Vehicle v WHERE v.company.id = :companyId AND " +
           "(:status IS NULL OR v.status = :status) AND " +
           "(:type IS NULL OR v.type = :type) AND " +
           "(:make IS NULL OR LOWER(v.make) LIKE LOWER(CONCAT('%', :make, '%'))) AND " +
           "(:model IS NULL OR LOWER(v.model) LIKE LOWER(CONCAT('%', :model, '%'))) AND " +
           "(:minRate IS NULL OR v.dailyRate >= :minRate) AND " +
           "(:maxRate IS NULL OR v.dailyRate <= :maxRate) AND " +
           "(:minYear IS NULL OR v.year >= :minYear) AND " +
           "(:maxYear IS NULL OR v.year <= :maxYear) AND " +
           "(:fuelType IS NULL OR v.fuelType = :fuelType) AND " +
           "(:transmission IS NULL OR v.transmission = :transmission) AND " +
           "(:minSeating IS NULL OR v.seatingCapacity >= :minSeating) AND " +
           "(:maxSeating IS NULL OR v.seatingCapacity <= :maxSeating) AND " +
           "(:location IS NULL OR LOWER(v.currentLocation) LIKE LOWER(CONCAT('%', :location, '%')))")
    List<Vehicle> searchVehiclesByCompany(@Param("companyId") Long companyId,
                                        @Param("status") Vehicle.VehicleStatus status,
                                        @Param("type") Vehicle.VehicleType type,
                                        @Param("make") String make,
                                        @Param("model") String model,
                                        @Param("minRate") BigDecimal minRate,
                                        @Param("maxRate") BigDecimal maxRate,
                                        @Param("minYear") Integer minYear,
                                        @Param("maxYear") Integer maxYear,
                                        @Param("fuelType") String fuelType,
                                        @Param("transmission") String transmission,
                                        @Param("minSeating") Integer minSeating,
                                        @Param("maxSeating") Integer maxSeating,
                                        @Param("location") String location);

    // Count vehicles by company
    long countByCompanyId(Long companyId);

    // Count vehicles by company and status
    long countByCompanyIdAndStatus(Long companyId, Vehicle.VehicleStatus status);

    // Count vehicles by company and type
    long countByCompanyIdAndType(Long companyId, Vehicle.VehicleType type);

    // Get vehicle statistics by company
    @Query("SELECT " +
           "COUNT(v) as totalVehicles, " +
           "COUNT(CASE WHEN v.status = 'AVAILABLE' THEN 1 END) as availableVehicles, " +
           "COUNT(CASE WHEN v.status = 'RENTED' THEN 1 END) as rentedVehicles, " +
           "COUNT(CASE WHEN v.status = 'MAINTENANCE' THEN 1 END) as maintenanceVehicles, " +
           "COUNT(CASE WHEN v.status = 'OUT_OF_SERVICE' THEN 1 END) as outOfServiceVehicles, " +
           "COUNT(CASE WHEN v.isFeatured = true THEN 1 END) as featuredVehicles, " +
           "SUM(v.totalRevenue) as totalRevenue, " +
           "AVG(v.dailyRate) as averageDailyRate, " +
           "AVG(v.averageRating) as averageRating " +
           "FROM Vehicle v WHERE v.company.id = :companyId")
    Object[] getVehicleStatisticsByCompany(@Param("companyId") Long companyId);

    // Find top performing vehicles by company
    @Query("SELECT v FROM Vehicle v WHERE v.company.id = :companyId ORDER BY v.totalRevenue DESC")
    List<Vehicle> findTopPerformingVehiclesByCompany(@Param("companyId") Long companyId);

    // Find vehicles with most rentals by company
    @Query("SELECT v FROM Vehicle v WHERE v.company.id = :companyId ORDER BY v.totalRentals DESC")
    List<Vehicle> findMostRentedVehiclesByCompany(@Param("companyId") Long companyId);

    // Find vehicles with highest ratings by company
    @Query("SELECT v FROM Vehicle v WHERE v.company.id = :companyId AND v.averageRating > 0 ORDER BY v.averageRating DESC")
    List<Vehicle> findHighestRatedVehiclesByCompany(@Param("companyId") Long companyId);

    // Find vehicles needing maintenance by company
    @Query("SELECT v FROM Vehicle v WHERE v.company.id = :companyId AND " +
           "(v.nextMaintenanceDate IS NOT NULL AND v.nextMaintenanceDate <= :currentDate OR " +
           "v.status = 'MAINTENANCE')")
    List<Vehicle> findVehiclesNeedingMaintenanceByCompany(@Param("companyId") Long companyId, 
                                                        @Param("currentDate") java.time.LocalDate currentDate);

    // Find vehicles with expiring insurance by company
    @Query("SELECT v FROM Vehicle v WHERE v.company.id = :companyId AND " +
           "v.insuranceExpiryDate IS NOT NULL AND v.insuranceExpiryDate <= :expiryDate")
    List<Vehicle> findVehiclesWithExpiringInsuranceByCompany(@Param("companyId") Long companyId, 
                                                           @Param("expiryDate") java.time.LocalDate expiryDate);

    // Find vehicles with expiring registration by company
    @Query("SELECT v FROM Vehicle v WHERE v.company.id = :companyId AND " +
           "v.registrationExpiryDate IS NOT NULL AND v.registrationExpiryDate <= :expiryDate")
    List<Vehicle> findVehiclesWithExpiringRegistrationByCompany(@Param("companyId") Long companyId, 
                                                              @Param("expiryDate") java.time.LocalDate expiryDate);

    // Find vehicles by company and created date range
    @Query("SELECT v FROM Vehicle v WHERE v.company.id = :companyId AND v.createdAt BETWEEN :startDate AND :endDate")
    List<Vehicle> findVehiclesByCompanyAndCreatedDateRange(@Param("companyId") Long companyId, 
                                                         @Param("startDate") java.time.LocalDateTime startDate, 
                                                         @Param("endDate") java.time.LocalDateTime endDate);
}