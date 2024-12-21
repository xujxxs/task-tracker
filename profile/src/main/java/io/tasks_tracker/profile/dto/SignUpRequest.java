package io.tasks_tracker.profile.dto;

import java.time.LocalDate;

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
    private String email;
    
    @NotNull
    private String firstname;
    
    @NotNull
    private String lastname;
    
    @NotNull
    private LocalDate birth_date;
}
