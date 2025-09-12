package com.rentman.rentman.repository;

import com.rentman.rentman.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find by email (for login)
    Optional<User> findByEmail(String email);

    // Check if email exists
    boolean existsByEmail(String email);

    // Check if phone number exists
    boolean existsByPhoneNumber(String phoneNumber);

    // Find by driver license number
    Optional<User> findByDriverLicenseNumber(String driverLicenseNumber);

    // Find users by role
    List<User> findByRole(User.UserRole role);

    // Find users by status
    List<User> findByStatus(User.UserStatus status);

    // Find users by role and status
    List<User> findByRoleAndStatus(User.UserRole role, User.UserStatus status);

    // Find all customers
    List<User> findByRoleOrderByCreatedAtDesc(User.UserRole role);

    // Find users created between dates
    List<User> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find users by name (case insensitive)
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> findByFullNameContaining(@Param("name") String name);

    // Find users by first or last name
    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName);

    // Find employees by department
    List<User> findByRoleAndDepartment(User.UserRole role, String department);

    // Find users by city
    List<User> findByCity(String city);

    // Find users with unverified email
    List<User> findByEmailVerifiedFalse();

    // Find users who haven't logged in recently
    @Query("SELECT u FROM User u WHERE u.lastLogin < :cutoffDate OR u.lastLogin IS NULL")
    List<User> findUsersWithOldLastLogin(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Count users by role
    long countByRole(User.UserRole role);

    // Count active users
    long countByStatus(User.UserStatus status);

    // Find users by employee ID
    Optional<User> findByEmployeeId(String employeeId);

    // Custom query for user statistics
    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> getUserCountByRole();

    // Find recent customers (last 30 days)
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.createdAt >= :thirtyDaysAgo ORDER BY u.createdAt DESC")
    List<User> findRecentCustomers(@Param("role") User.UserRole role, @Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);

    // Find users by company
    List<User> findByCompanyIdOrderByCreatedAtDesc(Long companyId);

    // Find users by company and role
    List<User> findByCompanyIdAndRoleOrderByCreatedAtDesc(Long companyId, User.UserRole role);

    // Find users by company and status
    List<User> findByCompanyIdAndStatusOrderByCreatedAtDesc(Long companyId, User.UserStatus status);

    // Find employees by company and department
    List<User> findByCompanyIdAndRoleAndDepartment(Long companyId, User.UserRole role, String department);

    // Find company admins
    List<User> findByRoleAndCompanyIdIsNotNull(User.UserRole role);

    // Find company admins by company
    List<User> findByRoleAndCompanyId(User.UserRole role, Long companyId);

    // Find users with specific permissions
    List<User> findByCanManageFleetTrue();

    List<User> findByCanManageReservationsTrue();

    List<User> findByCanManageEmployeesTrue();

    List<User> findByCanViewReportsTrue();

    List<User> findByCanManageFinancesTrue();

    // Find users by company with specific permissions
    List<User> findByCompanyIdAndCanManageFleetTrue(Long companyId);

    List<User> findByCompanyIdAndCanManageReservationsTrue(Long companyId);

    List<User> findByCompanyIdAndCanManageEmployeesTrue(Long companyId);

    List<User> findByCompanyIdAndCanViewReportsTrue(Long companyId);

    List<User> findByCompanyIdAndCanManageFinancesTrue(Long companyId);

    // Count users by company
    long countByCompanyId(Long companyId);

    // Count users by company and role
    long countByCompanyIdAndRole(Long companyId, User.UserRole role);

    // Count users by company and status
    long countByCompanyIdAndStatus(Long companyId, User.UserStatus status);

    // Get user statistics by company
    @Query("SELECT " +
           "COUNT(u) as totalUsers, " +
           "COUNT(CASE WHEN u.role = 'EMPLOYEE' THEN 1 END) as employees, " +
           "COUNT(CASE WHEN u.role = 'COMPANY_ADMIN' THEN 1 END) as companyAdmins, " +
           "COUNT(CASE WHEN u.status = 'ACTIVE' THEN 1 END) as activeUsers " +
           "FROM User u WHERE u.company.id = :companyId")
    Object[] getUserStatisticsByCompany(@Param("companyId") Long companyId);

    // Find users created in date range by company
    @Query("SELECT u FROM User u WHERE u.company.id = :companyId AND u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findUsersByCompanyAndCreatedDateRange(@Param("companyId") Long companyId, 
                                                   @Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);
}