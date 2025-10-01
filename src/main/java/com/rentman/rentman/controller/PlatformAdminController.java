package com.rentman.rentman.controller;

import com.rentman.rentman.entity.Company;
import com.rentman.rentman.entity.User;
import com.rentman.rentman.repository.CompanyRepository;
import com.rentman.rentman.repository.UserRepository;
import com.rentman.rentman.service.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/platform-admin")
@CrossOrigin(origins = "*")
public class PlatformAdminController {

    private static final Logger logger = LoggerFactory.getLogger(PlatformAdminController.class);

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    // ========== AUTHORIZATION HELPER ==========

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetailsService.CustomUserPrincipal) {
            CustomUserDetailsService.CustomUserPrincipal principal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
            return principal.getUser();
        }
        return null;
    }

    private boolean isPlatformAdmin(User user) {
        return user != null && user.getRole() == User.UserRole.ADMIN;
    }

    private ResponseEntity<?> unauthorizedResponse() {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Unauthorized: Only platform administrators can perform this action");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    // ========== COMPANY APPROVAL ==========

    // Get all pending companies
    @GetMapping("/companies/pending")
    public ResponseEntity<?> getPendingCompanies() {
        try {
            User currentUser = getCurrentUser();
            if (!isPlatformAdmin(currentUser)) {
                return unauthorizedResponse();
            }

            List<Company> pendingCompanies = companyRepository.findByStatus(Company.CompanyStatus.PENDING_APPROVAL);
            logger.info("Platform admin {} retrieved {} pending companies", currentUser.getEmail(), pendingCompanies.size());

            return ResponseEntity.ok(pendingCompanies);
        } catch (Exception e) {
            logger.error("Failed to get pending companies: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get pending companies: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Get all companies (for platform admin)
    @GetMapping("/companies")
    public ResponseEntity<?> getAllCompanies(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        try {
            User currentUser = getCurrentUser();
            if (!isPlatformAdmin(currentUser)) {
                return unauthorizedResponse();
            }

            List<Company> companies;
            if (status != null && !status.isEmpty()) {
                Company.CompanyStatus companyStatus = Company.CompanyStatus.valueOf(status.toUpperCase());
                companies = companyRepository.findByStatus(companyStatus);
            } else {
                companies = companyRepository.findAll();
            }

            logger.info("Platform admin {} retrieved {} companies", currentUser.getEmail(), companies.size());

            return ResponseEntity.ok(companies);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid status: " + status);
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Failed to get companies: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get companies: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Approve company
    @PostMapping("/companies/{id}/approve")
    public ResponseEntity<?> approveCompany(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> requestBody) {
        
        try {
            User currentUser = getCurrentUser();
            if (!isPlatformAdmin(currentUser)) {
                return unauthorizedResponse();
            }

            Optional<Company> optionalCompany = companyRepository.findById(id);
            if (optionalCompany.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Company not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Company company = optionalCompany.get();

            // Check if company is already approved
            if (company.getStatus() == Company.CompanyStatus.ACTIVE) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Company is already approved");
                return ResponseEntity.badRequest().body(error);
            }

            // Approve the company
            company.setStatus(Company.CompanyStatus.ACTIVE);
            company.setApprovedAt(LocalDateTime.now());
            company.setApprovedBy(currentUser.getId());
            company.setIsVerified(true);

            Company approvedCompany = companyRepository.save(company);

            logger.info("Platform admin {} approved company {} (ID: {})", 
                currentUser.getEmail(), company.getCompanyName(), company.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Company approved successfully");
            response.put("company", approvedCompany);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to approve company: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to approve company: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Reject company
    @PostMapping("/companies/{id}/reject")
    public ResponseEntity<?> rejectCompany(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody) {
        
        try {
            User currentUser = getCurrentUser();
            if (!isPlatformAdmin(currentUser)) {
                return unauthorizedResponse();
            }

            Optional<Company> optionalCompany = companyRepository.findById(id);
            if (optionalCompany.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Company not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Company company = optionalCompany.get();
            String reason = requestBody.get("reason");

            // Reject the company
            company.setStatus(Company.CompanyStatus.REJECTED);
            company.setApprovedBy(currentUser.getId());
            // Note: You might want to add a 'rejectionReason' field to Company entity

            Company rejectedCompany = companyRepository.save(company);

            logger.info("Platform admin {} rejected company {} (ID: {}) - Reason: {}", 
                currentUser.getEmail(), company.getCompanyName(), company.getId(), reason);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Company rejected successfully");
            response.put("company", rejectedCompany);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to reject company: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to reject company: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Suspend company
    @PostMapping("/companies/{id}/suspend")
    public ResponseEntity<?> suspendCompany(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> requestBody) {
        
        try {
            User currentUser = getCurrentUser();
            if (!isPlatformAdmin(currentUser)) {
                return unauthorizedResponse();
            }

            Optional<Company> optionalCompany = companyRepository.findById(id);
            if (optionalCompany.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Company not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Company company = optionalCompany.get();
            String reason = requestBody != null ? requestBody.get("reason") : null;

            company.setStatus(Company.CompanyStatus.SUSPENDED);

            Company suspendedCompany = companyRepository.save(company);

            logger.info("Platform admin {} suspended company {} (ID: {}) - Reason: {}", 
                currentUser.getEmail(), company.getCompanyName(), company.getId(), reason);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Company suspended successfully");
            response.put("company", suspendedCompany);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to suspend company: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to suspend company: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Reactivate company
    @PostMapping("/companies/{id}/reactivate")
    public ResponseEntity<?> reactivateCompany(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            if (!isPlatformAdmin(currentUser)) {
                return unauthorizedResponse();
            }

            Optional<Company> optionalCompany = companyRepository.findById(id);
            if (optionalCompany.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Company not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Company company = optionalCompany.get();
            company.setStatus(Company.CompanyStatus.ACTIVE);

            Company reactivatedCompany = companyRepository.save(company);

            logger.info("Platform admin {} reactivated company {} (ID: {})", 
                currentUser.getEmail(), company.getCompanyName(), company.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Company reactivated successfully");
            response.put("company", reactivatedCompany);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to reactivate company: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to reactivate company: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ========== PLATFORM STATISTICS ==========

    // Get platform dashboard statistics
    @GetMapping("/dashboard")
    public ResponseEntity<?> getPlatformDashboard() {
        try {
            User currentUser = getCurrentUser();
            if (!isPlatformAdmin(currentUser)) {
                return unauthorizedResponse();
            }

            Map<String, Object> dashboard = new HashMap<>();

            // Company statistics by status
            Map<String, Long> companyStats = new HashMap<>();
            for (Company.CompanyStatus status : Company.CompanyStatus.values()) {
                long count = companyRepository.countByStatus(status);
                companyStats.put(status.name(), count);
            }
            dashboard.put("companyStatistics", companyStats);

            // Total companies
            long totalCompanies = companyRepository.count();
            dashboard.put("totalCompanies", totalCompanies);

            // Total users
            long totalUsers = userRepository.count();
            dashboard.put("totalUsers", totalUsers);

            // Recent companies (last 10)
            List<Company> recentCompanies = companyRepository.findTop10ByOrderByCreatedAtDesc();
            dashboard.put("recentCompanies", recentCompanies);

            // Pending approvals count
            long pendingCount = companyRepository.countByStatus(Company.CompanyStatus.PENDING_APPROVAL);
            dashboard.put("pendingApprovals", pendingCount);

            logger.info("Platform admin {} accessed dashboard", currentUser.getEmail());

            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            logger.error("Failed to get platform dashboard: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get platform dashboard: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ========== USER MANAGEMENT ==========

    // Get all platform admins
    @GetMapping("/admins")
    public ResponseEntity<?> getPlatformAdmins() {
        try {
            User currentUser = getCurrentUser();
            if (!isPlatformAdmin(currentUser)) {
                return unauthorizedResponse();
            }

            List<User> admins = userRepository.findByRole(User.UserRole.ADMIN);
            
            logger.info("Platform admin {} retrieved {} platform admins", currentUser.getEmail(), admins.size());

            return ResponseEntity.ok(admins);
        } catch (Exception e) {
            logger.error("Failed to get platform admins: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get platform admins: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}

