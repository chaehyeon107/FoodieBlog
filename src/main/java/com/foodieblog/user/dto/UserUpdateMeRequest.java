package com.foodieblog.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UserUpdateMeRequest {
    @NotBlank @Size(max = 30)
    private String nickname;
}
