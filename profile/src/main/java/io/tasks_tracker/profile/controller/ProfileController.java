package io.tasks_tracker.profile.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.tasks_tracker.profile.config.RabbitMQConfig;
import io.tasks_tracker.profile.dto.UpdateProfileRequest;
import io.tasks_tracker.profile.entity.User;
import io.tasks_tracker.profile.service.AuthenticationService;
import io.tasks_tracker.profile.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@RestController
@RequestMapping("/api/profile")
public class ProfileController 
{
    private final AuthenticationService authenticationService;
    private final ProfileService profileService;
    private final RabbitTemplate rabbitTemplate;

    public ProfileController(
        ProfileService profileService,
        RabbitTemplate rabbitTemplate,
        AuthenticationService authenticationService
    ) {
        this.profileService = profileService;
        this.rabbitTemplate = rabbitTemplate;
        this.authenticationService = authenticationService;
    }

    @GetMapping
    public ResponseEntity<User> getProfile(Authentication authentication) 
    {
        Long userId = authenticationService.getUserId(authentication);
        log.info("Initiating fetching self profile");
        User user = profileService.getProfile(userId);

        log.debug("Fetching self profile successfully for user: {}", userId);
        return ResponseEntity
                .ok()
                .body(user);
    }
    
    @PutMapping
    public ResponseEntity<User> updateProfile(
        @RequestBody UpdateProfileRequest request,
        Authentication authentication
    ) {
        Long userId = authenticationService.getUserId(authentication);
        log.info("Initiating profile update by user: {}", userId);
        User user = profileService.updateProfileById(request, authentication, userId);

        log.debug("Profile updated successfully for user: {}", userId);
        return ResponseEntity
                .ok()
                .body(user);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteProfile(
        Authentication authentication,
        HttpServletRequest request
    ) {
        Long userId = authenticationService.getUserId(authentication);
        log.info("Initiating account deletion by user: {}", userId);

        log.info("Sending user deletion event for user: {}", userId);
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_NAME, 
            "user.delete", 
            authenticationService.getUserId(authentication)
        );
        
        log.debug("Terminating all sessions");
        authenticationService.logoutAll(authentication, request);
        profileService.deleteProfileById(authentication, userId);

        log.info("User account: {} permanently deleted", userId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getProfileById(
        @PathVariable Long id,
        Authentication authentication
    ) {
        Long requesterId = authenticationService.getUserId(authentication);
        log.info("Fetching profile user: {} initiated by user: {}", id, requesterId);
        User user = profileService.getProfileById(authentication, id);

        log.debug("Profile data retrieved for user: {}", id);
        return ResponseEntity
                .ok()
                .body(user);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<User> updateProfileById(
        @PathVariable Long id,
        @RequestBody UpdateProfileRequest request,
        Authentication authentication
    ) {
        Long requesterId = authenticationService.getUserId(authentication);
        log.info("Profile update initiated by: {} for user: {}", requesterId, id);
        User updatedUser = profileService.updateProfileById(request, authentication, id);

        log.info("Profile user: {} updated by user: {}", id, requesterId);
        return ResponseEntity
                .ok()
                .body(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfileById(
        @PathVariable Long id,
        Authentication authentication,
        HttpServletRequest request
    ) {
        Long requesterId = authenticationService.getUserId(authentication);
        log.info("User deletion initiated by: {} for user: {}", requesterId, id);

        User userToDelete = profileService.getProfileById(authentication, id);
        log.info("Sending deletion event for user: {}", id);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "user.delete", id);
        
        if(userToDelete.getUsername().equals(authentication.getName())) {
            log.info("Self-deletion detected, terminating sessions for user: {}", id);
            authenticationService.logoutAll(authentication, request);
        }
        else {
            log.debug("Terminating all sessions for user: {}", id);
            authenticationService.deleteAllSessions(userToDelete.getUsername());
        }

        profileService.deleteProfileById(authentication, id);

        log.info("User account: {} permanently deleted by user: {}", id, requesterId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
