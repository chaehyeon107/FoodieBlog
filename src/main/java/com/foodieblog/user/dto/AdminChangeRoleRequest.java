package com.foodieblog.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminChangeRoleRequest {
    @NotBlank
    private String role; // "ADMIN" or "USER"
}
