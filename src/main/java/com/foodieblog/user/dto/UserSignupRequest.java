package com.foodieblog.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserSignupRequest {

    @Email @NotBlank
    private String email;

    @NotBlank @Size(min = 4, max = 50)
    private String password;

    @NotBlank @Size(max = 30)
    private String nickname;
}
