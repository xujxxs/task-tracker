package io.tasks_tracker.profile.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SignUpRequest
{
    @NotNull
    private String username;
    
    @NotNull
    private String password;

    @NotNull
    private UpdateProfileRequest user_details;
}
