package io.tasks_tracker.profile.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePasswordRequest 
{
    @NotNull
    private String old_password;

    @NotNull
    private String new_password;
}
