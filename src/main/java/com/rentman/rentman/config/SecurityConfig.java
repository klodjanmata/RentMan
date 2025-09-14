package com.rentman.rentman.config;

import com.rentman.rentman.security.JwtAuthenticationFilter;
import com.rentman.rentman.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/search/**").permitAll()
                .requestMatchers("/api/companies/active").permitAll()
                .requestMatchers("/api/companies/featured").permitAll()
                .requestMatchers("/api/companies/location").permitAll()
                .requestMatchers("/api/vehicles/available").permitAll()
                .requestMatchers("/api/vehicles/type/**").permitAll()
                .requestMatchers("/api/vehicles/{id}").permitAll()
                .requestMatchers("/api/companies/{id}").permitAll()
                .requestMatchers("/api/companies/statistics/platform").permitAll()
                
                // Admin only endpoints
                .requestMatchers("/api/companies/{id}/status").hasRole("ADMIN")
                .requestMatchers("/api/companies/{id}/subscription").hasRole("ADMIN")
                .requestMatchers("/api/companies/{id}").hasAnyRole("ADMIN", "COMPANY_ADMIN")
                .requestMatchers("/api/companies").hasAnyRole("ADMIN", "COMPANY_ADMIN")
                .requestMatchers("/api/users/**").hasAnyRole("ADMIN", "COMPANY_ADMIN")
                .requestMatchers("/api/companies/statistics/**").hasAnyRole("ADMIN", "COMPANY_ADMIN")
                
                // Company admin and employee endpoints
                .requestMatchers("/api/companies/{id}/dashboard").hasAnyRole("ADMIN", "COMPANY_ADMIN", "EMPLOYEE")
                .requestMatchers("/api/companies/{id}/employees/**").hasAnyRole("ADMIN", "COMPANY_ADMIN", "EMPLOYEE")
                .requestMatchers("/api/companies/{id}/vehicles/**").hasAnyRole("ADMIN", "COMPANY_ADMIN", "EMPLOYEE")
                .requestMatchers("/api/companies/{id}/reservations/**").hasAnyRole("ADMIN", "COMPANY_ADMIN", "EMPLOYEE")
                .requestMatchers("/api/companies/{id}/maintenance/**").hasAnyRole("ADMIN", "COMPANY_ADMIN", "EMPLOYEE")
                .requestMatchers("/api/companies/{id}/defects/**").hasAnyRole("ADMIN", "COMPANY_ADMIN", "EMPLOYEE")
                .requestMatchers("/api/companies/{id}/invoices/**").hasAnyRole("ADMIN", "COMPANY_ADMIN", "EMPLOYEE")
                .requestMatchers("/api/companies/{id}/statistics").hasAnyRole("ADMIN", "COMPANY_ADMIN", "EMPLOYEE")
                .requestMatchers("/api/companies/{id}/revenue").hasAnyRole("ADMIN", "COMPANY_ADMIN", "EMPLOYEE")
                
                // Vehicle management
                .requestMatchers("/api/vehicles").hasAnyRole("ADMIN", "COMPANY_ADMIN", "EMPLOYEE")
                .requestMatchers("/api/vehicles/**").hasAnyRole("ADMIN", "COMPANY_ADMIN", "EMPLOYEE")
                
                // Reservation management
                .requestMatchers("/api/reservations").hasAnyRole("ADMIN", "COMPANY_ADMIN", "EMPLOYEE", "CUSTOMER")
                .requestMatchers("/api/reservations/**").hasAnyRole("ADMIN", "COMPANY_ADMIN", "EMPLOYEE", "CUSTOMER")
                
                // All other requests need authentication
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
