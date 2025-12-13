package com.foodieblog.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UserChangePasswordRequest {

    @NotBlank
    private String currentPassword;

    @NotBlank @Size(min = 4, max = 50)
    private String newPassword;
}
