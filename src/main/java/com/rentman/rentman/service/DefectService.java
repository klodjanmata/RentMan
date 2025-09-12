package com.rentman.rentman.service;

import com.rentman.rentman.entity.Defect;
import com.rentman.rentman.entity.Vehicle;
import com.rentman.rentman.entity.Company;
import com.rentman.rentman.entity.User;
import com.rentman.rentman.repository.DefectRepository;
import com.rentman.rentman.repository.VehicleRepository;
import com.rentman.rentman.repository.CompanyRepository;
import com.rentman.rentman.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DefectService {

    @Autowired
    private DefectRepository defectRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    // ========== DEFECT CRUD OPERATIONS ==========

    public Defect createDefect(Defect defect) {
        // Validate vehicle exists and belongs to company
        Vehicle vehicle = vehicleRepository.findById(defect.getVehicle().getId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found with ID: " + defect.getVehicle().getId()));

        Company company = companyRepository.findById(defect.getCompany().getId())
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + defect.getCompany().getId()));

        if (!vehicle.getCompany().getId().equals(company.getId())) {
            throw new RuntimeException("Vehicle does not belong to the specified company");
        }

        // Set default values
        if (defect.getStatus() == null) {
            defect.setStatus(Defect.DefectStatus.REPORTED);
        }

        if (defect.getReportedDate() == null) {
            defect.setReportedDate(LocalDate.now());
        }

        // Set severity based on safety impact
        if (defect.getSafetyImpact() != null && defect.getSafetyImpact() && defect.getSeverity() == null) {
            defect.setSeverity(Defect.DefectSeverity.CRITICAL);
        }

        Defect savedDefect = defectRepository.save(defect);

        // Update vehicle status if defect puts it out of service
        if (defect.getVehicleOutOfService() != null && defect.getVehicleOutOfService()) {
            vehicle.setStatus(Vehicle.VehicleStatus.OUT_OF_SERVICE);
            vehicleRepository.save(vehicle);
        }

        return savedDefect;
    }

    public Defect updateDefect(Long id, Defect defectDetails) {
        Defect defect = getDefectById(id);

        // Update fields
        defect.setType(defectDetails.getType());
        defect.setSeverity(defectDetails.getSeverity());
        defect.setTitle(defectDetails.getTitle());
        defect.setDescription(defectDetails.getDescription());
        defect.setLocation(defectDetails.getLocation());
        defect.setComponent(defectDetails.getComponent());
        defect.setInvestigationNotes(defectDetails.getInvestigationNotes());
        defect.setRootCause(defectDetails.getRootCause());
        defect.setResolutionNotes(defectDetails.getResolutionNotes());
        defect.setEstimatedResolutionDate(defectDetails.getEstimatedResolutionDate());
        defect.setSafetyImpact(defectDetails.getSafetyImpact());
        defect.setOperationalImpact(defectDetails.getOperationalImpact());
        defect.setCustomerImpact(defectDetails.getCustomerImpact());
        defect.setVehicleOutOfService(defectDetails.getVehicleOutOfService());
        defect.setEstimatedDowntimeDays(defectDetails.getEstimatedDowntimeDays());
        defect.setPhotoUrls(defectDetails.getPhotoUrls());
        defect.setDocumentUrls(defectDetails.getDocumentUrls());
        defect.setFollowUpRequired(defectDetails.getFollowUpRequired());
        defect.setFollowUpDate(defectDetails.getFollowUpDate());
        defect.setFollowUpNotes(defectDetails.getFollowUpNotes());

        return defectRepository.save(defect);
    }

    public Defect getDefectById(Long id) {
        return defectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Defect not found with ID: " + id));
    }

    public List<Defect> getAllDefects() {
        return defectRepository.findAll();
    }

    public void deleteDefect(Long id) {
        Defect defect = getDefectById(id);
        
        // Only allow deletion of reported defects
        if (defect.getStatus() != Defect.DefectStatus.REPORTED) {
            throw new RuntimeException("Cannot delete defect that is not in reported status");
        }

        defectRepository.deleteById(id);
    }

    // ========== DEFECT STATUS MANAGEMENT ==========

    public Defect assignDefect(Long id, Long employeeId) {
        Defect defect = getDefectById(id);
        
        if (defect.getStatus() != Defect.DefectStatus.REPORTED) {
            throw new RuntimeException("Only reported defects can be assigned");
        }

        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        defect.setAssignedToEmployee(employee);
        defect.setStatus(Defect.DefectStatus.INVESTIGATING);
        defect.setInvestigationStartDate(LocalDate.now());

        return defectRepository.save(defect);
    }

    public Defect startInvestigation(Long id, Long employeeId) {
        Defect defect = getDefectById(id);
        
        if (defect.getStatus() != Defect.DefectStatus.REPORTED && defect.getStatus() != Defect.DefectStatus.INVESTIGATING) {
            throw new RuntimeException("Only reported or assigned defects can start investigation");
        }

        defect.setStatus(Defect.DefectStatus.INVESTIGATING);
        defect.setInvestigationStartDate(LocalDate.now());

        if (employeeId != null) {
            User employee = userRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            defect.setAssignedToEmployee(employee);
        }

        return defectRepository.save(defect);
    }

    public Defect completeInvestigation(Long id, String rootCause, String investigationNotes, Long employeeId) {
        Defect defect = getDefectById(id);
        
        if (defect.getStatus() != Defect.DefectStatus.INVESTIGATING) {
            throw new RuntimeException("Only defects under investigation can complete investigation");
        }

        defect.setStatus(Defect.DefectStatus.IN_PROGRESS);
        defect.setInvestigationCompletedDate(LocalDate.now());
        defect.setRootCause(rootCause);
        defect.setInvestigationNotes(investigationNotes);

        if (employeeId != null) {
            User employee = userRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            defect.setAssignedToEmployee(employee);
        }

        return defectRepository.save(defect);
    }

    public Defect resolveDefect(Long id, String resolutionNotes, Integer actualDowntimeDays, Long employeeId) {
        Defect defect = getDefectById(id);
        
        if (defect.getStatus() != Defect.DefectStatus.IN_PROGRESS) {
            throw new RuntimeException("Only defects in progress can be resolved");
        }

        defect.setStatus(Defect.DefectStatus.RESOLVED);
        defect.setResolutionDate(LocalDate.now());
        defect.setResolutionNotes(resolutionNotes);
        defect.setActualDowntimeDays(actualDowntimeDays);

        if (employeeId != null) {
            User employee = userRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            defect.setAssignedToEmployee(employee);
        }

        // Update vehicle status if it was out of service
        if (defect.getVehicleOutOfService() != null && defect.getVehicleOutOfService()) {
            Vehicle vehicle = defect.getVehicle();
            vehicle.setStatus(Vehicle.VehicleStatus.AVAILABLE);
            vehicleRepository.save(vehicle);
        }

        return defectRepository.save(defect);
    }

    public Defect closeDefect(Long id, String notes) {
        Defect defect = getDefectById(id);
        
        if (defect.getStatus() != Defect.DefectStatus.RESOLVED) {
            throw new RuntimeException("Only resolved defects can be closed");
        }

        defect.setStatus(Defect.DefectStatus.CLOSED);
        defect.setInvestigationNotes(defect.getInvestigationNotes() + "\nClosed: " + notes);

        return defectRepository.save(defect);
    }

    public Defect cancelDefect(Long id, String reason) {
        Defect defect = getDefectById(id);
        
        if (defect.getStatus() == Defect.DefectStatus.CLOSED) {
            throw new RuntimeException("Cannot cancel closed defect");
        }

        defect.setStatus(Defect.DefectStatus.CANCELLED);
        defect.setInvestigationNotes(defect.getInvestigationNotes() + "\nCancelled: " + reason);

        // Update vehicle status if it was out of service
        if (defect.getVehicleOutOfService() != null && defect.getVehicleOutOfService()) {
            Vehicle vehicle = defect.getVehicle();
            vehicle.setStatus(Vehicle.VehicleStatus.AVAILABLE);
            vehicleRepository.save(vehicle);
        }

        return defectRepository.save(defect);
    }

    // ========== DEFECT REPORTING ==========

    public Defect reportDefect(Long vehicleId, Long companyId, Defect.DefectType type, Defect.DefectSeverity severity,
                              String title, String description, String location, String component,
                              Long reportedByUserId, Long reportedByEmployeeId, Long reservationId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with ID: " + vehicleId));

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));

        Defect defect = new Defect();
        defect.setVehicle(vehicle);
        defect.setCompany(company);
        defect.setType(type);
        defect.setSeverity(severity);
        defect.setTitle(title);
        defect.setDescription(description);
        defect.setLocation(location);
        defect.setComponent(component);
        defect.setReportedDate(LocalDate.now());
        defect.setReportedDuringReservationId(reservationId);

        if (reportedByUserId != null) {
            User user = userRepository.findById(reportedByUserId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            defect.setReportedByUser(user);
        }

        if (reportedByEmployeeId != null) {
            User employee = userRepository.findById(reportedByEmployeeId)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            defect.setReportedByEmployee(employee);
        }

        return createDefect(defect);
    }

    // ========== DEFECT BY VEHICLE ==========

    public List<Defect> getVehicleDefects(Long vehicleId) {
        return defectRepository.findByVehicleIdOrderByCreatedAtDesc(vehicleId);
    }

    public List<Defect> getVehicleDefectsByType(Long vehicleId, Defect.DefectType type) {
        return defectRepository.findByVehicleIdAndTypeOrderByCreatedAtDesc(vehicleId, type);
    }

    public List<Defect> getVehicleDefectsByStatus(Long vehicleId, Defect.DefectStatus status) {
        return defectRepository.findByVehicleIdAndStatusOrderByCreatedAtDesc(vehicleId, status);
    }

    public List<Defect> getVehicleDefectsBySeverity(Long vehicleId, Defect.DefectSeverity severity) {
        return defectRepository.findByVehicleIdAndSeverityOrderByCreatedAtDesc(vehicleId, severity);
    }

    // ========== DEFECT BY COMPANY ==========

    public List<Defect> getCompanyDefects(Long companyId) {
        return defectRepository.findByCompanyIdOrderByCreatedAtDesc(companyId);
    }

    public List<Defect> getCompanyDefectsByStatus(Long companyId, Defect.DefectStatus status) {
        return defectRepository.findByCompanyIdAndStatusOrderByCreatedAtDesc(companyId, status);
    }

    public List<Defect> getCompanyDefectsByType(Long companyId, Defect.DefectType type) {
        return defectRepository.findByCompanyIdAndTypeOrderByCreatedAtDesc(companyId, type);
    }

    public List<Defect> getCompanyDefectsBySeverity(Long companyId, Defect.DefectSeverity severity) {
        return defectRepository.findByCompanyIdAndSeverityOrderByCreatedAtDesc(companyId, severity);
    }

    // ========== CRITICAL AND HIGH PRIORITY DEFECTS ==========

    public List<Defect> getCriticalDefects(Long companyId) {
        return defectRepository.findCriticalDefectsByCompany(companyId);
    }

    public List<Defect> getHighPriorityDefects(Long companyId) {
        return defectRepository.findHighPriorityDefectsByCompany(companyId);
    }

    public List<Defect> getDefectsRequiringImmediateAttention(Long companyId) {
        return defectRepository.findDefectsRequiringImmediateAttentionByCompany(companyId);
    }

    public List<Defect> getDefectsWithSafetyImpact(Long companyId) {
        return defectRepository.findDefectsWithSafetyImpactByCompany(companyId);
    }

    public List<Defect> getDefectsWithOperationalImpact(Long companyId) {
        return defectRepository.findDefectsWithOperationalImpactByCompany(companyId);
    }

    public List<Defect> getDefectsWithCustomerImpact(Long companyId) {
        return defectRepository.findDefectsWithCustomerImpactByCompany(companyId);
    }

    public List<Defect> getDefectsWithVehicleOutOfService(Long companyId) {
        return defectRepository.findDefectsWithVehicleOutOfServiceByCompany(companyId);
    }

    // ========== DEFECT ANALYTICS ==========

    public Object[] getDefectStatistics(Long companyId) {
        return defectRepository.getDefectStatisticsByCompany(companyId);
    }

    public Object[] getVehicleDefectStatistics(Long vehicleId) {
        return defectRepository.getDefectStatisticsByVehicle(vehicleId);
    }

    // ========== DEFECT FILTERING AND SEARCH ==========

    public List<Defect> getOverdueDefects(Long companyId) {
        return defectRepository.findOverdueDefectsByCompany(companyId, LocalDate.now());
    }

    public List<Defect> getUnresolvedDefects(Long companyId) {
        return defectRepository.findUnresolvedDefectsByCompany(companyId);
    }

    public List<Defect> getDefectsByDateRange(Long companyId, LocalDate startDate, LocalDate endDate) {
        return defectRepository.findDefectsByCompanyAndDateRange(companyId, startDate, endDate);
    }

    public List<Defect> getDefectsByReporter(Long userId) {
        return defectRepository.findByReportedByUserId(userId);
    }

    public List<Defect> getDefectsByEmployee(Long employeeId) {
        return defectRepository.findByReportedByEmployeeId(employeeId);
    }

    public List<Defect> getDefectsByAssignedEmployee(Long employeeId) {
        return defectRepository.findByAssignedToEmployeeId(employeeId);
    }

    public List<Defect> getDefectsByLocation(Long companyId, String location) {
        return defectRepository.findByCompanyIdAndLocation(companyId, location);
    }

    public List<Defect> getDefectsByComponent(Long companyId, String component) {
        return defectRepository.findByCompanyIdAndComponent(companyId, component);
    }

    public List<Defect> getDefectsRequiringFollowUp(Long companyId) {
        return defectRepository.findDefectsRequiringFollowUpByCompany(companyId);
    }

    public List<Defect> getDefectsByReservation(Long reservationId) {
        return defectRepository.findByReportedDuringReservationId(reservationId);
    }

    // ========== DEFECT VALIDATION ==========

    public boolean isDefectCritical(Long defectId) {
        Defect defect = getDefectById(defectId);
        return defect.isCritical();
    }

    public boolean isDefectHighPriority(Long defectId) {
        Defect defect = getDefectById(defectId);
        return defect.isHighPriority();
    }

    public boolean isDefectResolved(Long defectId) {
        Defect defect = getDefectById(defectId);
        return defect.isResolved();
    }

    public boolean isDefectInProgress(Long defectId) {
        Defect defect = getDefectById(defectId);
        return defect.isInProgress();
    }

    public boolean isDefectOverdue(Long defectId) {
        Defect defect = getDefectById(defectId);
        return defect.isOverdue();
    }

    public boolean requiresImmediateAttention(Long defectId) {
        Defect defect = getDefectById(defectId);
        return defect.requiresImmediateAttention();
    }

    // ========== DEFECT ALERTS ==========

    public List<Defect> getDefectAlerts(Long companyId) {
        List<Defect> alerts = new java.util.ArrayList<>();
        
        // Add critical defects
        alerts.addAll(getCriticalDefects(companyId));
        
        // Add defects requiring immediate attention
        alerts.addAll(getDefectsRequiringImmediateAttention(companyId));
        
        // Add overdue defects
        alerts.addAll(getOverdueDefects(companyId));
        
        return alerts;
    }

    // ========== HELPER METHODS ==========

    public long countDefectsByStatus(Long companyId, Defect.DefectStatus status) {
        return defectRepository.countByCompanyIdAndStatus(companyId, status);
    }

    public long countDefectsByType(Long companyId, Defect.DefectType type) {
        return defectRepository.countByCompanyIdAndType(companyId, type);
    }

    public long countDefectsBySeverity(Long companyId, Defect.DefectSeverity severity) {
        return defectRepository.countByCompanyIdAndSeverity(companyId, severity);
    }

    public long countDefectsByVehicle(Long vehicleId) {
        return defectRepository.countByVehicleId(vehicleId);
    }

    public Optional<Defect> findByDefectNumber(String defectNumber) {
        return defectRepository.findByDefectNumber(defectNumber);
    }
}
