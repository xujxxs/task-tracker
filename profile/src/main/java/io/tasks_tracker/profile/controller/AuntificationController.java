package io.tasks_tracker.profile.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.tasks_tracker.profile.dto.SignInRequest;
import io.tasks_tracker.profile.dto.SignUpRequest;
import io.tasks_tracker.profile.dto.UpdatePasswordRequest;
import io.tasks_tracker.profile.exception.InvalidSignInForm;
import io.tasks_tracker.profile.exception.InvalidSignUpForm;
import io.tasks_tracker.profile.service.AuntificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/auth")
public class AuntificationController 
{
    @Autowired
    private AuntificationService auntificationService;

    @PostMapping("/registration")
    public ResponseEntity<?> signUp(@RequestBody SignUpRequest request) 
    {
        try {
            auntificationService.signUp(request);
            return ResponseEntity
                    .ok()
                    .build();
        } catch (InvalidSignUpForm e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> signIn(
            HttpServletRequest request, 
            HttpServletResponse response, 
            @RequestBody SignInRequest form
    ) {
        try {
            auntificationService.signIn(request, response, form);
            return ResponseEntity
                    .ok()
                    .build();
        } catch (InvalidSignInForm e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }
    }

    @PostMapping("/change/password")
    public ResponseEntity<?> changePassword(
            Authentication authentication,
            HttpServletRequest request,
            @RequestBody UpdatePasswordRequest form
    ) {
        try {
            auntificationService.changePassword(authentication, form);
            auntificationService.logoutAll(authentication, request);
            return ResponseEntity
                    .ok()
                    .build();
        } catch (UsernameNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }
    }

    @PostMapping("/logout/all")
    public ResponseEntity<?> logoutAll(
            Authentication authentication, 
            HttpServletRequest request
    ) {
        auntificationService.logoutAll(authentication, request);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
    
}
