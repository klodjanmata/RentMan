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
}