package com.foodieblog.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AdminChangeRoleRequest {
    @NotBlank
    private String role; // "ADMIN" or "USER"
}
