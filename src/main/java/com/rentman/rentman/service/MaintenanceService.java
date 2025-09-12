package com.rentman.rentman.service;

import com.rentman.rentman.entity.Maintenance;
import com.rentman.rentman.entity.Vehicle;
import com.rentman.rentman.entity.Company;
import com.rentman.rentman.entity.User;
import com.rentman.rentman.repository.MaintenanceRepository;
import com.rentman.rentman.repository.VehicleRepository;
import com.rentman.rentman.repository.CompanyRepository;
import com.rentman.rentman.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MaintenanceService {

    @Autowired
    private MaintenanceRepository maintenanceRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    // ========== MAINTENANCE CRUD OPERATIONS ==========

    public Maintenance createMaintenance(Maintenance maintenance) {
        // Validate vehicle exists and belongs to company
        Vehicle vehicle = vehicleRepository.findById(maintenance.getVehicle().getId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found with ID: " + maintenance.getVehicle().getId()));

        Company company = companyRepository.findById(maintenance.getCompany().getId())
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + maintenance.getCompany().getId()));

        if (!vehicle.getCompany().getId().equals(company.getId())) {
            throw new RuntimeException("Vehicle does not belong to the specified company");
        }

        // Set default values
        if (maintenance.getStatus() == null) {
            maintenance.setStatus(Maintenance.MaintenanceStatus.SCHEDULED);
        }

        if (maintenance.getScheduledDate() == null && maintenance.getStatus() == Maintenance.MaintenanceStatus.SCHEDULED) {
            maintenance.setScheduledDate(LocalDate.now().plusDays(1));
        }

        // Set current mileage if not provided
        if (maintenance.getCurrentMileage() == null) {
            maintenance.setCurrentMileage(vehicle.getMileage());
        }

        Maintenance savedMaintenance = maintenanceRepository.save(maintenance);

        // Update vehicle status if maintenance is in progress
        if (maintenance.getStatus() == Maintenance.MaintenanceStatus.IN_PROGRESS) {
            vehicle.setStatus(Vehicle.VehicleStatus.MAINTENANCE);
            vehicleRepository.save(vehicle);
        }

        return savedMaintenance;
    }

    public Maintenance updateMaintenance(Long id, Maintenance maintenanceDetails) {
        Maintenance maintenance = getMaintenanceById(id);

        // Update fields
        maintenance.setType(maintenanceDetails.getType());
        maintenance.setTitle(maintenanceDetails.getTitle());
        maintenance.setDescription(maintenanceDetails.getDescription());
        maintenance.setScheduledDate(maintenanceDetails.getScheduledDate());
        maintenance.setEstimatedCost(maintenanceDetails.getEstimatedCost());
        maintenance.setServiceProvider(maintenanceDetails.getServiceProvider());
        maintenance.setServiceProviderContact(maintenanceDetails.getServiceProviderContact());
        maintenance.setNotes(maintenanceDetails.getNotes());
        maintenance.setIsRecurring(maintenanceDetails.getIsRecurring());
        maintenance.setRecurrenceIntervalMonths(maintenanceDetails.getRecurrenceIntervalMonths());
        maintenance.setRecurrenceIntervalMiles(maintenanceDetails.getRecurrenceIntervalMiles());

        return maintenanceRepository.save(maintenance);
    }

    public Maintenance getMaintenanceById(Long id) {
        return maintenanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Maintenance record not found with ID: " + id));
    }

    public List<Maintenance> getAllMaintenance() {
        return maintenanceRepository.findAll();
    }

    public void deleteMaintenance(Long id) {
        Maintenance maintenance = getMaintenanceById(id);
        
        // Only allow deletion of scheduled maintenance
        if (maintenance.getStatus() != Maintenance.MaintenanceStatus.SCHEDULED) {
            throw new RuntimeException("Cannot delete maintenance record that is not in scheduled status");
        }

        maintenanceRepository.deleteById(id);
    }

    // ========== MAINTENANCE STATUS MANAGEMENT ==========

    public Maintenance startMaintenance(Long id, Long employeeId) {
        Maintenance maintenance = getMaintenanceById(id);
        
        if (maintenance.getStatus() != Maintenance.MaintenanceStatus.SCHEDULED) {
            throw new RuntimeException("Only scheduled maintenance can be started");
        }

        maintenance.setStatus(Maintenance.MaintenanceStatus.IN_PROGRESS);
        maintenance.setStartDate(LocalDate.now());

        if (employeeId != null) {
            User employee = userRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            maintenance.setPerformedByEmployee(employee);
        }

        // Update vehicle status
        Vehicle vehicle = maintenance.getVehicle();
        vehicle.setStatus(Vehicle.VehicleStatus.MAINTENANCE);
        vehicleRepository.save(vehicle);

        return maintenanceRepository.save(maintenance);
    }

    public Maintenance completeMaintenance(Long id, BigDecimal actualCost, BigDecimal laborCost, 
                                         BigDecimal partsCost, String workPerformed, String notes, 
                                         Integer qualityRating, Long employeeId) {
        Maintenance maintenance = getMaintenanceById(id);
        
        if (maintenance.getStatus() != Maintenance.MaintenanceStatus.IN_PROGRESS) {
            throw new RuntimeException("Only in-progress maintenance can be completed");
        }

        maintenance.setStatus(Maintenance.MaintenanceStatus.COMPLETED);
        maintenance.setCompletionDate(LocalDate.now());
        maintenance.setActualCost(actualCost);
        maintenance.setLaborCost(laborCost);
        maintenance.setPartsCost(partsCost);
        maintenance.setWorkPerformed(workPerformed);
        maintenance.setNotes(notes);
        maintenance.setQualityRating(qualityRating);

        if (employeeId != null) {
            User employee = userRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            maintenance.setPerformedByEmployee(employee);
        }

        // Update vehicle status back to available
        Vehicle vehicle = maintenance.getVehicle();
        vehicle.setStatus(Vehicle.VehicleStatus.AVAILABLE);
        vehicle.setLastMaintenanceDate(LocalDate.now());
        
        // Set next maintenance date if recurring
        if (maintenance.getIsRecurring()) {
            if (maintenance.getRecurrenceIntervalMonths() != null) {
                vehicle.setNextMaintenanceDate(LocalDate.now().plusMonths(maintenance.getRecurrenceIntervalMonths()));
            } else if (maintenance.getRecurrenceIntervalMiles() != null && maintenance.getCurrentMileage() != null) {
                vehicle.setNextMaintenanceDate(null); // Will be calculated based on mileage
            }
        }
        
        vehicleRepository.save(vehicle);

        return maintenanceRepository.save(maintenance);
    }

    public Maintenance cancelMaintenance(Long id, String reason) {
        Maintenance maintenance = getMaintenanceById(id);
        
        if (maintenance.getStatus() == Maintenance.MaintenanceStatus.COMPLETED) {
            throw new RuntimeException("Cannot cancel completed maintenance");
        }

        maintenance.setStatus(Maintenance.MaintenanceStatus.CANCELLED);
        maintenance.setNotes(maintenance.getNotes() + "\nCancelled: " + reason);

        // Update vehicle status if it was in maintenance
        if (maintenance.getStatus() == Maintenance.MaintenanceStatus.IN_PROGRESS) {
            Vehicle vehicle = maintenance.getVehicle();
            vehicle.setStatus(Vehicle.VehicleStatus.AVAILABLE);
            vehicleRepository.save(vehicle);
        }

        return maintenanceRepository.save(maintenance);
    }

    // ========== MAINTENANCE SCHEDULING ==========

    public Maintenance scheduleMaintenance(Long vehicleId, Long companyId, Maintenance.MaintenanceType type, 
                                         String title, String description, LocalDate scheduledDate, 
                                         BigDecimal estimatedCost, String serviceProvider) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with ID: " + vehicleId));

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));

        Maintenance maintenance = new Maintenance();
        maintenance.setVehicle(vehicle);
        maintenance.setCompany(company);
        maintenance.setType(type);
        maintenance.setTitle(title);
        maintenance.setDescription(description);
        maintenance.setScheduledDate(scheduledDate);
        maintenance.setEstimatedCost(estimatedCost);
        maintenance.setServiceProvider(serviceProvider);
        maintenance.setStatus(Maintenance.MaintenanceStatus.SCHEDULED);
        maintenance.setCurrentMileage(vehicle.getMileage());

        return createMaintenance(maintenance);
    }

    public List<Maintenance> getScheduledMaintenance(Long companyId) {
        return maintenanceRepository.findByCompanyIdAndStatusOrderByCreatedAtDesc(companyId, Maintenance.MaintenanceStatus.SCHEDULED);
    }

    public List<Maintenance> getOverdueMaintenance(Long companyId) {
        return maintenanceRepository.findOverdueMaintenanceByCompany(companyId, LocalDate.now());
    }

    public List<Maintenance> getMaintenanceDueSoon(Long companyId, int daysAhead) {
        LocalDate futureDate = LocalDate.now().plusDays(daysAhead);
        return maintenanceRepository.findMaintenanceDueSoonByCompany(companyId, LocalDate.now(), futureDate);
    }

    // ========== MAINTENANCE BY VEHICLE ==========

    public List<Maintenance> getVehicleMaintenance(Long vehicleId) {
        return maintenanceRepository.findByVehicleIdOrderByCreatedAtDesc(vehicleId);
    }

    public List<Maintenance> getVehicleMaintenanceByType(Long vehicleId, Maintenance.MaintenanceType type) {
        return maintenanceRepository.findByVehicleIdAndTypeOrderByCreatedAtDesc(vehicleId, type);
    }

    public Maintenance getLastMaintenance(Long vehicleId) {
        return maintenanceRepository.findLastMaintenanceByVehicle(vehicleId, org.springframework.data.domain.PageRequest.of(0, 1))
                .getContent().stream().findFirst().orElse(null);
    }

    public Maintenance getNextScheduledMaintenance(Long vehicleId) {
        return maintenanceRepository.findNextScheduledMaintenanceByVehicle(vehicleId, org.springframework.data.domain.PageRequest.of(0, 1))
                .getContent().stream().findFirst().orElse(null);
    }

    // ========== MAINTENANCE BY COMPANY ==========

    public List<Maintenance> getCompanyMaintenance(Long companyId) {
        return maintenanceRepository.findByCompanyIdOrderByCreatedAtDesc(companyId);
    }

    public List<Maintenance> getCompanyMaintenanceByStatus(Long companyId, Maintenance.MaintenanceStatus status) {
        return maintenanceRepository.findByCompanyIdAndStatusOrderByCreatedAtDesc(companyId, status);
    }

    public List<Maintenance> getCompanyMaintenanceByType(Long companyId, Maintenance.MaintenanceType type) {
        return maintenanceRepository.findByCompanyIdAndTypeOrderByCreatedAtDesc(companyId, type);
    }

    // ========== MAINTENANCE ANALYTICS ==========

    public Object[] getMaintenanceStatistics(Long companyId) {
        return maintenanceRepository.getMaintenanceStatisticsByCompany(companyId);
    }

    public Object[] getVehicleMaintenanceStatistics(Long vehicleId) {
        return maintenanceRepository.getMaintenanceStatisticsByVehicle(vehicleId);
    }

    public BigDecimal calculateTotalMaintenanceCost(Long companyId) {
        return maintenanceRepository.calculateTotalCostByCompany(companyId);
    }

    public BigDecimal calculateMaintenanceCostByDateRange(Long companyId, LocalDate startDate, LocalDate endDate) {
        return maintenanceRepository.calculateTotalCostByCompanyAndDateRange(companyId, startDate, endDate);
    }

    public BigDecimal calculateVehicleMaintenanceCost(Long vehicleId) {
        return maintenanceRepository.calculateTotalCostByVehicle(vehicleId);
    }

    // ========== MAINTENANCE FILTERING AND SEARCH ==========

    public List<Maintenance> getMaintenanceByDateRange(Long companyId, LocalDate startDate, LocalDate endDate) {
        return maintenanceRepository.findMaintenanceByCompanyAndDateRange(companyId, startDate, endDate);
    }

    public List<Maintenance> getMaintenanceByCostRange(Long companyId, BigDecimal minCost, BigDecimal maxCost) {
        return maintenanceRepository.findByCompanyAndCostRange(companyId, minCost, maxCost);
    }

    public List<Maintenance> getMaintenanceByServiceProvider(Long companyId, String serviceProvider) {
        return maintenanceRepository.findByCompanyIdAndServiceProvider(companyId, serviceProvider);
    }

    public List<Maintenance> getMaintenanceByEmployee(Long employeeId) {
        return maintenanceRepository.findByPerformedByEmployeeId(employeeId);
    }

    public List<Maintenance> getMaintenanceUnderWarranty(Long companyId) {
        return maintenanceRepository.findMaintenanceUnderWarrantyByCompany(companyId, LocalDate.now());
    }

    public List<Maintenance> getRecurringMaintenance(Long companyId) {
        return maintenanceRepository.findByCompanyIdAndIsRecurringTrue(companyId);
    }

    // ========== MAINTENANCE VALIDATION ==========

    public boolean isMaintenanceOverdue(Long maintenanceId) {
        Maintenance maintenance = getMaintenanceById(maintenanceId);
        return maintenance.isOverdue();
    }

    public boolean isMaintenanceCompleted(Long maintenanceId) {
        Maintenance maintenance = getMaintenanceById(maintenanceId);
        return maintenance.isCompleted();
    }

    public boolean isMaintenanceInProgress(Long maintenanceId) {
        Maintenance maintenance = getMaintenanceById(maintenanceId);
        return maintenance.isInProgress();
    }

    public boolean isMaintenanceUnderWarranty(Long maintenanceId) {
        Maintenance maintenance = getMaintenanceById(maintenanceId);
        return maintenance.isUnderWarranty();
    }

    // ========== MAINTENANCE ALERTS ==========

    public List<Maintenance> getMaintenanceAlerts(Long companyId) {
        List<Maintenance> alerts = new java.util.ArrayList<>();
        
        // Add overdue maintenance
        alerts.addAll(getOverdueMaintenance(companyId));
        
        // Add maintenance due soon (next 7 days)
        alerts.addAll(getMaintenanceDueSoon(companyId, 7));
        
        return alerts;
    }

    public List<Vehicle> getVehiclesNeedingMaintenance(Long companyId) {
        return vehicleRepository.findVehiclesNeedingMaintenanceByCompany(companyId, LocalDate.now());
    }

    // ========== HELPER METHODS ==========

    public long countMaintenanceByStatus(Long companyId, Maintenance.MaintenanceStatus status) {
        return maintenanceRepository.countByCompanyIdAndStatus(companyId, status);
    }

    public long countMaintenanceByType(Long companyId, Maintenance.MaintenanceType type) {
        return maintenanceRepository.countByCompanyIdAndType(companyId, type);
    }

    public long countMaintenanceByVehicle(Long vehicleId) {
        return maintenanceRepository.countByVehicleId(vehicleId);
    }

    public Optional<Maintenance> findByMaintenanceNumber(String maintenanceNumber) {
        return maintenanceRepository.findByMaintenanceNumber(maintenanceNumber);
    }
}
