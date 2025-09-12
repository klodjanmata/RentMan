package com.rentman.rentman.service;

import com.rentman.rentman.dto.ReservationCreateDto;
import com.rentman.rentman.entity.Reservation;
import com.rentman.rentman.entity.User;
import com.rentman.rentman.entity.Vehicle;
import com.rentman.rentman.repository.ReservationRepository;
import com.rentman.rentman.repository.UserRepository;
import com.rentman.rentman.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    // Create new reservation
    public Reservation createReservation(ReservationCreateDto createDto) {
        // Validate customer exists
        User customer = userRepository.findById(createDto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + createDto.getCustomerId()));

        // Validate customer is actually a customer
        if (customer.getRole() != User.UserRole.CUSTOMER) {
            throw new RuntimeException("User is not a customer");
        }

        // Validate vehicle exists
        Vehicle vehicle = vehicleRepository.findById(createDto.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found with ID: " + createDto.getVehicleId()));

        // Validate vehicle is available
        if (vehicle.getStatus() != Vehicle.VehicleStatus.AVAILABLE) {
            throw new RuntimeException("Vehicle is not available for rental");
        }

        // Validate dates
        validateReservationDates(createDto.getStartDate(), createDto.getEndDate());

        // Check vehicle availability for the requested dates
        if (!isVehicleAvailable(createDto.getVehicleId(), createDto.getStartDate(), createDto.getEndDate())) {
            throw new RuntimeException("Vehicle is not available for the selected dates");
        }

        // Create reservation
        Reservation reservation = new Reservation();
        reservation.setCustomer(customer);
        reservation.setVehicle(vehicle);
        reservation.setStartDate(createDto.getStartDate());
        reservation.setEndDate(createDto.getEndDate());
        reservation.setPickupLocation(createDto.getPickupLocation());
        reservation.setReturnLocation(createDto.getReturnLocation());
        reservation.setPickupTime(createDto.getPickupTime());
        reservation.setReturnTime(createDto.getReturnTime());
        reservation.setSpecialRequests(createDto.getSpecialRequests());

        // Set additional services
        reservation.setInsuranceIncluded(createDto.getInsuranceIncluded());
        reservation.setAdditionalDriver(createDto.getAdditionalDriver());
        reservation.setGpsIncluded(createDto.getGpsIncluded());
        reservation.setChildSeatIncluded(createDto.getChildSeatIncluded());

        // Set pricing (using vehicle's current daily rate)
        reservation.setDailyRate(vehicle.getDailyRate());

        // Calculate additional costs
        calculateAdditionalCosts(reservation);

        // Save reservation
        Reservation savedReservation = reservationRepository.save(reservation);

        return savedReservation;
    }

    // Check vehicle availability
    public boolean isVehicleAvailable(Long vehicleId, LocalDate startDate, LocalDate endDate) {
        long conflictingReservations = reservationRepository.countConflictingReservations(
                vehicleId, startDate, endDate);
        return conflictingReservations == 0;
    }

    // Confirm reservation
    public Reservation confirmReservation(Long reservationId, Long employeeId) {
        Reservation reservation = getReservationById(reservationId);

        if (reservation.getStatus() != Reservation.ReservationStatus.PENDING) {
            throw new RuntimeException("Only pending reservations can be confirmed");
        }

        // Set employee who confirmed
        if (employeeId != null) {
            User employee = userRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            if (!employee.isAdminOrEmployee()) {
                throw new RuntimeException("User is not an employee or admin");
            }
            reservation.setHandledByEmployee(employee);
        }

        reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
        reservation.setConfirmedAt(LocalDateTime.now());

        return reservationRepository.save(reservation);
    }

    // Start reservation (vehicle pickup)
    public Reservation startReservation(Long reservationId, Integer pickupMileage,
                                        String fuelLevel, String vehicleCondition, Long employeeId) {
        Reservation reservation = getReservationById(reservationId);

        if (reservation.getStatus() != Reservation.ReservationStatus.CONFIRMED) {
            throw new RuntimeException("Only confirmed reservations can be started");
        }

        reservation.setStatus(Reservation.ReservationStatus.IN_PROGRESS);
        reservation.setActualStartDate(LocalDate.now());
        reservation.setPickupMileage(pickupMileage);
        reservation.setFuelLevelPickup(fuelLevel);
        reservation.setVehicleConditionPickup(vehicleCondition);

        if (employeeId != null) {
            User employee = userRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            reservation.setHandledByEmployee(employee);
        }

        // Update vehicle status
        Vehicle vehicle = reservation.getVehicle();
        vehicle.setStatus(Vehicle.VehicleStatus.RENTED);
        vehicleRepository.save(vehicle);

        return reservationRepository.save(reservation);
    }

    // Complete reservation (vehicle return)
    public Reservation completeReservation(Long reservationId, Integer returnMileage,
                                           String fuelLevel, String vehicleCondition,
                                           BigDecimal additionalFees, String notes, Long employeeId) {
        Reservation reservation = getReservationById(reservationId);

        if (reservation.getStatus() != Reservation.ReservationStatus.IN_PROGRESS) {
            throw new RuntimeException("Only in-progress reservations can be completed");
        }

        reservation.setStatus(Reservation.ReservationStatus.COMPLETED);
        reservation.setActualEndDate(LocalDate.now());
        reservation.setCompletedAt(LocalDateTime.now());
        reservation.setReturnMileage(returnMileage);
        reservation.setFuelLevelReturn(fuelLevel);
        reservation.setVehicleConditionReturn(vehicleCondition);
        reservation.setNotes(notes);

        // Add additional fees if any
        if (additionalFees != null && additionalFees.compareTo(BigDecimal.ZERO) > 0) {
            reservation.setAdditionalFees(additionalFees);
        }

        if (employeeId != null) {
            User employee = userRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            reservation.setHandledByEmployee(employee);
        }

        // Update vehicle status back to available
        Vehicle vehicle = reservation.getVehicle();
        vehicle.setStatus(Vehicle.VehicleStatus.AVAILABLE);

        // Update vehicle mileage
        if (returnMileage != null) {
            vehicle.setMileage(returnMileage);
        }

        vehicleRepository.save(vehicle);

        return reservationRepository.save(reservation);
    }

    // Cancel reservation
    public Reservation cancelReservation(Long reservationId, String cancellationReason) {
        Reservation reservation = getReservationById(reservationId);

        if (!reservation.canBeCancelled()) {
            throw new RuntimeException("Reservation cannot be cancelled in current status: " + reservation.getStatus());
        }

        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        reservation.setCancellationReason(cancellationReason);
        reservation.setCancelledAt(LocalDateTime.now());

        // If vehicle was reserved, make it available again
        if (reservation.getVehicle().getStatus() == Vehicle.VehicleStatus.RENTED) {
            Vehicle vehicle = reservation.getVehicle();
            vehicle.setStatus(Vehicle.VehicleStatus.AVAILABLE);
            vehicleRepository.save(vehicle);
        }

        return reservationRepository.save(reservation);
    }

    // Get all reservations
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    // Get reservation by ID
    public Reservation getReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found with ID: " + id));
    }

    // Get reservation by reservation number
    public Optional<Reservation> getReservationByNumber(String reservationNumber) {
        return reservationRepository.findByReservationNumber(reservationNumber);
    }

    // Get customer's reservations
    public List<Reservation> getCustomerReservations(Long customerId) {
        return reservationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    // Get vehicle's reservations
    public List<Reservation> getVehicleReservations(Long vehicleId) {
        return reservationRepository.findByVehicleIdOrderByStartDateDesc(vehicleId);
    }

    // Get reservations by status
    public List<Reservation> getReservationsByStatus(Reservation.ReservationStatus status) {
        return reservationRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    // Get current active reservations
    public List<Reservation> getCurrentActiveReservations() {
        return reservationRepository.findCurrentActiveReservations(LocalDate.now());
    }

    // Get upcoming reservations
    public List<Reservation> getUpcomingReservations() {
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);
        return reservationRepository.findUpcomingReservations(today, nextWeek);
    }

    // Get overdue reservations
    public List<Reservation> getOverdueReservations() {
        return reservationRepository.findOverdueReservations(LocalDate.now());
    }

    // Get reservations pending pickup today
    public List<Reservation> getTodayPickups() {
        return reservationRepository.findReservationsPendingPickup(LocalDate.now());
    }

    // Get reservations pending return today
    public List<Reservation> getTodayReturns() {
        return reservationRepository.findReservationsPendingReturn(LocalDate.now());
    }

    // Update reservation
    public Reservation updateReservation(Long id, Reservation updatedReservation) {
        Reservation reservation = getReservationById(id);

        // Only allow updates if reservation is pending or confirmed
        if (reservation.getStatus() != Reservation.ReservationStatus.PENDING &&
                reservation.getStatus() != Reservation.ReservationStatus.CONFIRMED) {
            throw new RuntimeException("Cannot update reservation in status: " + reservation.getStatus());
        }

        // Validate new dates if changed
        if (!reservation.getStartDate().equals(updatedReservation.getStartDate()) ||
                !reservation.getEndDate().equals(updatedReservation.getEndDate())) {

            validateReservationDates(updatedReservation.getStartDate(), updatedReservation.getEndDate());

            if (!isVehicleAvailable(reservation.getVehicle().getId(),
                    updatedReservation.getStartDate(), updatedReservation.getEndDate())) {
                throw new RuntimeException("Vehicle is not available for the new dates");
            }
        }

        // Update fields
        reservation.setStartDate(updatedReservation.getStartDate());
        reservation.setEndDate(updatedReservation.getEndDate());
        reservation.setPickupLocation(updatedReservation.getPickupLocation());
        reservation.setReturnLocation(updatedReservation.getReturnLocation());
        reservation.setPickupTime(updatedReservation.getPickupTime());
        reservation.setReturnTime(updatedReservation.getReturnTime());
        reservation.setSpecialRequests(updatedReservation.getSpecialRequests());
        reservation.setInsuranceIncluded(updatedReservation.getInsuranceIncluded());
        reservation.setAdditionalDriver(updatedReservation.getAdditionalDriver());
        reservation.setGpsIncluded(updatedReservation.getGpsIncluded());
        reservation.setChildSeatIncluded(updatedReservation.getChildSeatIncluded());

        // Recalculate costs
        calculateAdditionalCosts(reservation);

        return reservationRepository.save(reservation);
    }

    // Delete reservation (only if pending)
    public void deleteReservation(Long id) {
        Reservation reservation = getReservationById(id);

        if (reservation.getStatus() != Reservation.ReservationStatus.PENDING) {
            throw new RuntimeException("Only pending reservations can be deleted");
        }

        reservationRepository.delete(reservation);
    }

    // Calculate revenue for date range
    public BigDecimal calculateRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal revenue = reservationRepository.calculateRevenueByDateRange(startDate, endDate);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    // Calculate monthly revenue
    public BigDecimal calculateMonthlyRevenue(int year, int month) {
        BigDecimal revenue = reservationRepository.calculateMonthlyRevenue(year, month);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    // Get reservation statistics
    public Map<String, Object> getReservationStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalReservations", reservationRepository.count());
        stats.put("pendingReservations", reservationRepository.countByStatus(Reservation.ReservationStatus.PENDING));
        stats.put("confirmedReservations", reservationRepository.countByStatus(Reservation.ReservationStatus.CONFIRMED));
        stats.put("inProgressReservations", reservationRepository.countByStatus(Reservation.ReservationStatus.IN_PROGRESS));
        stats.put("completedReservations", reservationRepository.countByStatus(Reservation.ReservationStatus.COMPLETED));
        stats.put("cancelledReservations", reservationRepository.countByStatus(Reservation.ReservationStatus.CANCELLED));

        stats.put("currentActiveReservations", getCurrentActiveReservations().size());
        stats.put("upcomingReservations", getUpcomingReservations().size());
        stats.put("overdueReservations", getOverdueReservations().size());
        stats.put("todayPickups", getTodayPickups().size());
        stats.put("todayReturns", getTodayReturns().size());

        // Revenue statistics
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime monthEnd = monthStart.plusMonths(1).minusSeconds(1);

        stats.put("monthlyRevenue", calculateRevenue(monthStart, monthEnd));

        return stats;
    }

    // Private helper methods
    private void validateReservationDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new RuntimeException("Start date and end date are required");
        }

        if (startDate.isBefore(LocalDate.now())) {
            throw new RuntimeException("Start date cannot be in the past");
        }

        if (endDate.isBefore(startDate)) {
            throw new RuntimeException("End date must be after start date");
        }

        if (endDate.equals(startDate)) {
            throw new RuntimeException("End date must be at least one day after start date");
        }
    }

    private void calculateAdditionalCosts(Reservation reservation) {
        BigDecimal additionalCosts = BigDecimal.ZERO;

        // Insurance cost (example: $15/day)
        if (reservation.getInsuranceIncluded() != null && reservation.getInsuranceIncluded()) {
            BigDecimal insuranceCost = BigDecimal.valueOf(15).multiply(BigDecimal.valueOf(reservation.getTotalDays()));
            reservation.setInsuranceAmount(insuranceCost);
            additionalCosts = additionalCosts.add(insuranceCost);
        }

        // GPS cost (example: $5/day)
        if (reservation.getGpsIncluded() != null && reservation.getGpsIncluded()) {
            BigDecimal gpsCost = BigDecimal.valueOf(5).multiply(BigDecimal.valueOf(reservation.getTotalDays()));
            additionalCosts = additionalCosts.add(gpsCost);
        }

        // Child seat cost (example: $8/day)
        if (reservation.getChildSeatIncluded() != null && reservation.getChildSeatIncluded()) {
            BigDecimal childSeatCost = BigDecimal.valueOf(8).multiply(BigDecimal.valueOf(reservation.getTotalDays()));
            additionalCosts = additionalCosts.add(childSeatCost);
        }

        // Additional driver cost (example: $10/day)
        if (reservation.getAdditionalDriver() != null && reservation.getAdditionalDriver()) {
            BigDecimal additionalDriverCost = BigDecimal.valueOf(10).multiply(BigDecimal.valueOf(reservation.getTotalDays()));
            additionalCosts = additionalCosts.add(additionalDriverCost);
        }

        if (additionalCosts.compareTo(BigDecimal.ZERO) > 0) {
            reservation.setAdditionalFees(additionalCosts);
        }

        // Calculate tax (example: 8.5% of subtotal + additional fees)
        if (reservation.getSubtotal() != null) {
            BigDecimal taxableAmount = reservation.getSubtotal().add(additionalCosts);
            BigDecimal taxRate = BigDecimal.valueOf(0.085); // 8.5%
            BigDecimal taxAmount = taxableAmount.multiply(taxRate);
            reservation.setTaxAmount(taxAmount);
        }
    }
}