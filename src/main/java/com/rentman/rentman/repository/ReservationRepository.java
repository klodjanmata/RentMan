package com.rentman.rentman.repository;

import com.rentman.rentman.entity.Reservation;
import com.rentman.rentman.entity.User;
import com.rentman.rentman.entity.Vehicle;
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
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // Find by reservation number
    Optional<Reservation> findByReservationNumber(String reservationNumber);

    // Find reservations by customer
    List<Reservation> findByCustomerOrderByCreatedAtDesc(User customer);

    // Find reservations by customer ID
    List<Reservation> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    // Find reservations by vehicle
    List<Reservation> findByVehicleOrderByStartDateDesc(Vehicle vehicle);

    // Find reservations by vehicle ID
    List<Reservation> findByVehicleIdOrderByStartDateDesc(Long vehicleId);

    // Find reservations by status
    List<Reservation> findByStatusOrderByCreatedAtDesc(Reservation.ReservationStatus status);

    // Find reservations by date range
    List<Reservation> findByStartDateBetween(LocalDate startDate, LocalDate endDate);

    // Find current active reservations
    @Query("SELECT r FROM Reservation r WHERE r.status IN ('CONFIRMED', 'IN_PROGRESS') " +
            "AND r.startDate <= :today AND r.endDate >= :today")
    List<Reservation> findCurrentActiveReservations(@Param("today") LocalDate today);

    // Find upcoming reservations (next 7 days)
    @Query("SELECT r FROM Reservation r WHERE r.status = 'CONFIRMED' " +
            "AND r.startDate BETWEEN :today AND :nextWeek ORDER BY r.startDate ASC")
    List<Reservation> findUpcomingReservations(@Param("today") LocalDate today,
                                               @Param("nextWeek") LocalDate nextWeek);

    // Check vehicle availability for date range
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.vehicle.id = :vehicleId " +
            "AND r.status IN ('CONFIRMED', 'IN_PROGRESS') " +
            "AND NOT (r.endDate < :startDate OR r.startDate > :endDate)")
    long countConflictingReservations(@Param("vehicleId") Long vehicleId,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);

    // Find overdue reservations
    @Query("SELECT r FROM Reservation r WHERE r.endDate < :today " +
            "AND r.status IN ('CONFIRMED', 'IN_PROGRESS')")
    List<Reservation> findOverdueReservations(@Param("today") LocalDate today);

    // Find reservations pending pickup (start date is today)
    @Query("SELECT r FROM Reservation r WHERE r.status = 'CONFIRMED' " +
            "AND r.startDate = :today")
    List<Reservation> findReservationsPendingPickup(@Param("today") LocalDate today);

    // Find reservations pending return (end date is today)
    @Query("SELECT r FROM Reservation r WHERE r.status = 'IN_PROGRESS' " +
            "AND r.endDate = :today")
    List<Reservation> findReservationsPendingReturn(@Param("today") LocalDate today);

    // Find reservations by customer and status
    List<Reservation> findByCustomerAndStatusOrderByCreatedAtDesc(User customer,
                                                                  Reservation.ReservationStatus status);

    // Find recent reservations (last 30 days)
    @Query("SELECT r FROM Reservation r WHERE r.createdAt >= :thirtyDaysAgo " +
            "ORDER BY r.createdAt DESC")
    List<Reservation> findRecentReservations(@Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);

    // Find reservations by total amount range
    List<Reservation> findByTotalAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);

    // Count reservations by status
    long countByStatus(Reservation.ReservationStatus status);

    // Count customer's total reservations
    long countByCustomer(User customer);

    // Find customer's current reservation
    @Query("SELECT r FROM Reservation r WHERE r.customer.id = :customerId " +
            "AND r.status IN ('CONFIRMED', 'IN_PROGRESS') " +
            "AND r.startDate <= :today AND r.endDate >= :today")
    Optional<Reservation> findCustomerCurrentReservation(@Param("customerId") Long customerId,
                                                         @Param("today") LocalDate today);

    // Revenue calculations
    @Query("SELECT SUM(r.totalAmount) FROM Reservation r WHERE r.status = 'COMPLETED' " +
            "AND r.completedAt BETWEEN :startDate AND :endDate")
    BigDecimal calculateRevenueByDateRange(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    // Monthly revenue
    @Query("SELECT SUM(r.totalAmount) FROM Reservation r WHERE r.status = 'COMPLETED' " +
            "AND YEAR(r.completedAt) = :year AND MONTH(r.completedAt) = :month")
    BigDecimal calculateMonthlyRevenue(@Param("year") int year, @Param("month") int month);

    // Vehicle utilization
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.vehicle.id = :vehicleId " +
            "AND r.status = 'COMPLETED' " +
            "AND r.startDate BETWEEN :startDate AND :endDate")
    long calculateVehicleUtilization(@Param("vehicleId") Long vehicleId,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);

    // Top customers by reservation count
    @Query("SELECT r.customer, COUNT(r) as reservationCount FROM Reservation r " +
            "WHERE r.status = 'COMPLETED' " +
            "GROUP BY r.customer ORDER BY reservationCount DESC")
    List<Object[]> findTopCustomersByReservationCount();

    // Popular vehicles
    @Query("SELECT r.vehicle, COUNT(r) as reservationCount FROM Reservation r " +
            "WHERE r.status = 'COMPLETED' " +
            "GROUP BY r.vehicle ORDER BY reservationCount DESC")
    List<Object[]> findPopularVehicles();

    // Find reservations by location
    List<Reservation> findByPickupLocationContainingIgnoreCase(String location);
    List<Reservation> findByReturnLocationContainingIgnoreCase(String location);

    // Find reservations handled by employee
    List<Reservation> findByHandledByEmployeeOrderByCreatedAtDesc(User employee);

    // Find reservations by company
    List<Reservation> findByCompanyIdOrderByCreatedAtDesc(Long companyId);

    // Find reservations by company and status
    List<Reservation> findByCompanyIdAndStatusOrderByCreatedAtDesc(Long companyId, Reservation.ReservationStatus status);

    // Find reservations by company and date range
    @Query("SELECT r FROM Reservation r WHERE r.company.id = :companyId AND r.startDate BETWEEN :startDate AND :endDate")
    List<Reservation> findReservationsByCompanyAndDateRange(@Param("companyId") Long companyId, 
                                                          @Param("startDate") LocalDate startDate, 
                                                          @Param("endDate") LocalDate endDate);

    // Find current active reservations by company
    @Query("SELECT r FROM Reservation r WHERE r.company.id = :companyId AND r.status IN ('CONFIRMED', 'IN_PROGRESS') " +
           "AND r.startDate <= :today AND r.endDate >= :today")
    List<Reservation> findCurrentActiveReservationsByCompany(@Param("companyId") Long companyId, @Param("today") LocalDate today);

    // Find upcoming reservations by company
    @Query("SELECT r FROM Reservation r WHERE r.company.id = :companyId AND r.status = 'CONFIRMED' " +
           "AND r.startDate BETWEEN :today AND :nextWeek ORDER BY r.startDate ASC")
    List<Reservation> findUpcomingReservationsByCompany(@Param("companyId") Long companyId, 
                                                      @Param("today") LocalDate today,
                                                      @Param("nextWeek") LocalDate nextWeek);

    // Find overdue reservations by company
    @Query("SELECT r FROM Reservation r WHERE r.company.id = :companyId AND r.endDate < :today " +
           "AND r.status IN ('CONFIRMED', 'IN_PROGRESS')")
    List<Reservation> findOverdueReservationsByCompany(@Param("companyId") Long companyId, @Param("today") LocalDate today);

    // Find reservations pending pickup by company
    @Query("SELECT r FROM Reservation r WHERE r.company.id = :companyId AND r.status = 'CONFIRMED' " +
           "AND r.startDate = :today")
    List<Reservation> findReservationsPendingPickupByCompany(@Param("companyId") Long companyId, @Param("today") LocalDate today);

    // Find reservations pending return by company
    @Query("SELECT r FROM Reservation r WHERE r.company.id = :companyId AND r.status = 'IN_PROGRESS' " +
           "AND r.endDate = :today")
    List<Reservation> findReservationsPendingReturnByCompany(@Param("companyId") Long companyId, @Param("today") LocalDate today);

    // Calculate revenue by company
    @Query("SELECT SUM(r.totalAmount) FROM Reservation r WHERE r.company.id = :companyId AND r.status = 'COMPLETED' " +
           "AND r.completedAt BETWEEN :startDate AND :endDate")
    BigDecimal calculateRevenueByCompanyAndDateRange(@Param("companyId") Long companyId,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    // Calculate monthly revenue by company
    @Query("SELECT SUM(r.totalAmount) FROM Reservation r WHERE r.company.id = :companyId AND r.status = 'COMPLETED' " +
           "AND YEAR(r.completedAt) = :year AND MONTH(r.completedAt) = :month")
    BigDecimal calculateMonthlyRevenueByCompany(@Param("companyId") Long companyId, @Param("year") int year, @Param("month") int month);

    // Get reservation statistics by company
    @Query("SELECT " +
           "COUNT(r) as totalReservations, " +
           "COUNT(CASE WHEN r.status = 'PENDING' THEN 1 END) as pendingReservations, " +
           "COUNT(CASE WHEN r.status = 'CONFIRMED' THEN 1 END) as confirmedReservations, " +
           "COUNT(CASE WHEN r.status = 'IN_PROGRESS' THEN 1 END) as inProgressReservations, " +
           "COUNT(CASE WHEN r.status = 'COMPLETED' THEN 1 END) as completedReservations, " +
           "COUNT(CASE WHEN r.status = 'CANCELLED' THEN 1 END) as cancelledReservations, " +
           "SUM(r.totalAmount) as totalRevenue, " +
           "AVG(r.totalAmount) as averageReservationValue " +
           "FROM Reservation r WHERE r.company.id = :companyId")
    Object[] getReservationStatisticsByCompany(@Param("companyId") Long companyId);

    // Find reservations by company and customer
    List<Reservation> findByCompanyIdAndCustomerIdOrderByCreatedAtDesc(Long companyId, Long customerId);

    // Find reservations by company and vehicle
    List<Reservation> findByCompanyIdAndVehicleIdOrderByStartDateDesc(Long companyId, Long vehicleId);

    // Find reservations by company and employee
    List<Reservation> findByCompanyIdAndHandledByEmployeeIdOrderByCreatedAtDesc(Long companyId, Long employeeId);

    // Count reservations by company and status
    long countByCompanyIdAndStatus(Long companyId, Reservation.ReservationStatus status);

    // Count reservations by company
    long countByCompanyId(Long companyId);

    // Find recent reservations by company
    @Query("SELECT r FROM Reservation r WHERE r.company.id = :companyId AND r.createdAt >= :thirtyDaysAgo " +
           "ORDER BY r.createdAt DESC")
    List<Reservation> findRecentReservationsByCompany(@Param("companyId") Long companyId, 
                                                    @Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);

    // Find reservations by company and total amount range
    @Query("SELECT r FROM Reservation r WHERE r.company.id = :companyId AND r.totalAmount BETWEEN :minAmount AND :maxAmount")
    List<Reservation> findByCompanyIdAndTotalAmountBetween(@Param("companyId") Long companyId, 
                                                         @Param("minAmount") BigDecimal minAmount, 
                                                         @Param("maxAmount") BigDecimal maxAmount);

    // Find top customers by company
    @Query("SELECT r.customer, COUNT(r) as reservationCount, SUM(r.totalAmount) as totalSpent " +
           "FROM Reservation r WHERE r.company.id = :companyId AND r.status = 'COMPLETED' " +
           "GROUP BY r.customer ORDER BY totalSpent DESC")
    List<Object[]> findTopCustomersByCompany(@Param("companyId") Long companyId);

    // Find popular vehicles by company
    @Query("SELECT r.vehicle, COUNT(r) as reservationCount, SUM(r.totalAmount) as totalRevenue " +
           "FROM Reservation r WHERE r.company.id = :companyId AND r.status = 'COMPLETED' " +
           "GROUP BY r.vehicle ORDER BY totalRevenue DESC")
    List<Object[]> findPopularVehiclesByCompany(@Param("companyId") Long companyId);

    // Find reservations by company and location
    @Query("SELECT r FROM Reservation r WHERE r.company.id = :companyId AND " +
           "(LOWER(r.pickupLocation) LIKE LOWER(CONCAT('%', :location, '%')) OR " +
           "LOWER(r.returnLocation) LIKE LOWER(CONCAT('%', :location, '%')))")
    List<Reservation> findByCompanyIdAndLocation(@Param("companyId") Long companyId, @Param("location") String location);

    // Find reservations created in date range by company
    @Query("SELECT r FROM Reservation r WHERE r.company.id = :companyId AND r.createdAt BETWEEN :startDate AND :endDate")
    List<Reservation> findReservationsByCompanyAndCreatedDateRange(@Param("companyId") Long companyId, 
                                                                 @Param("startDate") LocalDateTime startDate, 
                                                                 @Param("endDate") LocalDateTime endDate);
}