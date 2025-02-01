package io.tasks_tracker.profile.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.tasks_tracker.profile.dto.SignInRequest;
import io.tasks_tracker.profile.dto.SignUpRequest;
import io.tasks_tracker.profile.dto.UpdatePasswordRequest;
import io.tasks_tracker.profile.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
    public ResponseEntity<Void> signUp(@RequestBody SignUpRequest request) 
    {
        authenticationService.signUp(request);
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
        authenticationService.signIn(request, response, form);
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
        authenticationService.changePassword(authentication, form);
        authenticationService.logoutAll(authentication, request);
        return ResponseEntity
                .ok()
                .build();
    }

    @PostMapping("/logout/all")
    public ResponseEntity<Void> logoutAll(
        Authentication authentication, 
        HttpServletRequest request
    ) {
        authenticationService.logoutAll(authentication, request);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
