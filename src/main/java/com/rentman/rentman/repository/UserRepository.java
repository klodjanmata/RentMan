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
}