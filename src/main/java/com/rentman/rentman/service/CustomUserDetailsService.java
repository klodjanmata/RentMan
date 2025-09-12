package com.rentman.rentman.service;

import com.rentman.rentman.entity.User;
import com.rentman.rentman.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new CustomUserPrincipal(user);
    }

    public static class CustomUserPrincipal implements UserDetails {
        private final User user;

        public CustomUserPrincipal(User user) {
            this.user = user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            // Return authorities based on user role
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        }

        @Override
        public String getPassword() {
            return user.getPassword();
        }

        @Override
        public String getUsername() {
            return user.getEmail();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return user.getStatus() != User.UserStatus.SUSPENDED && user.getStatus() != User.UserStatus.BANNED;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return user.getStatus() == User.UserStatus.ACTIVE;
        }

        // Custom methods to access user details
        public User getUser() {
            return user;
        }

        public Long getUserId() {
            return user.getId();
        }

        public User.UserRole getUserRole() {
            return user.getRole();
        }

        public Long getCompanyId() {
            return user.getCompany() != null ? user.getCompany().getId() : null;
        }

        public boolean isAdmin() {
            return user.getRole() == User.UserRole.ADMIN;
        }

        public boolean isCompanyAdmin() {
            return user.getRole() == User.UserRole.COMPANY_ADMIN;
        }

        public boolean isEmployee() {
            return user.getRole() == User.UserRole.EMPLOYEE;
        }

        public boolean isCustomer() {
            return user.getRole() == User.UserRole.CUSTOMER;
        }

        public boolean belongsToCompany(Long companyId) {
            return user.getCompany() != null && user.getCompany().getId().equals(companyId);
        }

        public boolean canManageFleet() {
            return user.getCanManageFleet() != null && user.getCanManageFleet();
        }

        public boolean canManageReservations() {
            return user.getCanManageReservations() != null && user.getCanManageReservations();
        }

        public boolean canManageEmployees() {
            return user.getCanManageEmployees() != null && user.getCanManageEmployees();
        }

        public boolean canViewReports() {
            return user.getCanViewReports() != null && user.getCanViewReports();
        }

        public boolean canManageFinances() {
            return user.getCanManageFinances() != null && user.getCanManageFinances();
        }
    }
}
