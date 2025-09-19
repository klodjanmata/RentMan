package com.rentman.rentman.dto;

import com.rentman.rentman.entity.Company;
import com.rentman.rentman.entity.User;
import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class CompanyRegistrationResult {
    private Company company;
    private User adminUser;
}
