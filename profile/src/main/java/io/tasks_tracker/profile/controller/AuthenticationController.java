package io.tasks_tracker.profile.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.tasks_tracker.profile.dto.SignInRequest;
import io.tasks_tracker.profile.dto.SignUpRequest;
import io.tasks_tracker.profile.dto.UpdatePasswordRequest;
import io.tasks_tracker.profile.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController 
{
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) 
    {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/registration")
    public ResponseEntity<Void> signUp(@RequestBody SignUpRequest form) 
    {
        log.info("Initiating registration for username: {}", form.getUsername());
        authenticationService.signUp(form);
        log.info("Registration completed successfully");
        return ResponseEntity
                .ok()
                .build();
    }
    
    @PostMapping("/login")
    public ResponseEntity<Void> signIn(
        @RequestBody SignInRequest form,
        HttpServletRequest request, 
        HttpServletResponse response
    ) {
        log.info("Initiating login for username: {}", form.getUsername());
        authenticationService.signIn(request, response, form);
        log.info("Login completed successfully");
        return ResponseEntity
                .ok()
                .build();
    }

    @PostMapping("/change/password")
    public ResponseEntity<Void> changePassword(
        @RequestBody UpdatePasswordRequest form,
        Authentication authentication,
        HttpServletRequest request
    ) {
        Long userId = authenticationService.getUserId(authentication);
        log.info("Initiating change password for user: {}", userId);
        authenticationService.changePassword(authentication, form);

        log.info("Logout all sessions for user: {}", userId);
        authenticationService.logoutAll(authentication, request);

        log.info("Change password successfully completed for user: {}", userId);
        return ResponseEntity
                .ok()
                .build();
    }

    @PostMapping("/logout/all")
    public ResponseEntity<Void> logoutAll(
        Authentication authentication, 
        HttpServletRequest request
    ) {
        Long userId = authenticationService.getUserId(authentication);
        log.info("Initiating logout all sessions for user: {}", userId);
        authenticationService.logoutAll(authentication, request);
        
        log.info("Logout all sessions successfully for user: {}", userId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
