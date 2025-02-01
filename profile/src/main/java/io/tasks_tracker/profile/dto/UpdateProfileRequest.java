package io.tasks_tracker.profile.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateProfileRequest 
{
    @NotNull
    @Email
    private String email;
    
    @NotNull
    private String firstname;
    
    @NotNull
    private String lastname;
    
    @NotNull
    private LocalDate birth_date;
}
