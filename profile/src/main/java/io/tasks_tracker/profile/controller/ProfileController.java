package io.tasks_tracker.profile.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.tasks_tracker.profile.config.RabbitMQConfig;
import io.tasks_tracker.profile.dto.UpdateProfileRequest;
import io.tasks_tracker.profile.entity.User;
import io.tasks_tracker.profile.service.AuntificationService;
import io.tasks_tracker.profile.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/profile")
public class ProfileController 
{
    @Autowired
    private ProfileService profileService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AuntificationService auntificationService;

    @GetMapping
    public ResponseEntity<User> getProfile(Authentication authentication) 
    {
        return ResponseEntity
                .ok()
                .body(profileService.getProfile(authentication.getName()));
    }
    
    @PutMapping
    public ResponseEntity<User> updateProfile(
            @RequestBody UpdateProfileRequest entity,
            Authentication authentication
    ) {
        return ResponseEntity
                .ok()
                .body(profileService.updateProfile(authentication, entity));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteProfile(
                Authentication authentication,
                HttpServletRequest request
    ) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "user.delete", authentication.getName());
        auntificationService.logoutAll(authentication, request);
        profileService.deleteProfile(authentication);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @GetMapping("/{username}")
    public ResponseEntity<User> getProfileByUsername(
            @PathVariable String username,
            Authentication authentication
    ) {
        return ResponseEntity
                .ok()
                .body(profileService.getProfileByUsername(authentication, username));
    }
    
    @PutMapping("/{username}")
    public ResponseEntity<User> updateProfileByUsername(
            @PathVariable String username,
            @RequestBody UpdateProfileRequest entity,
            Authentication authentication
    ) {
        return ResponseEntity
                .ok()
                .body(profileService.updateProfileByUsername(authentication, username, entity));
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<?> deleteProfileByUsername(
            @PathVariable String username,
            Authentication authentication,
            HttpServletRequest request
    ) {
        User userToDelete = profileService.getProfileByUsername(authentication, username);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "user.delete", userToDelete.getUsername());
        
        if(userToDelete.getUsername().equals(authentication.getName())) {
            auntificationService.logoutAll(authentication, request);
        }
        else {
            auntificationService.deleteAllSessions(userToDelete.getUsername());
        }

        profileService.deleteProfileByUsername(authentication, username);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
