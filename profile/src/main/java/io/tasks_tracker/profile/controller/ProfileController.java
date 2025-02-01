package io.tasks_tracker.profile.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.tasks_tracker.profile.config.RabbitMQConfig;
import io.tasks_tracker.profile.dto.UpdateProfileRequest;
import io.tasks_tracker.profile.entity.User;
import io.tasks_tracker.profile.service.AuthenticationService;
import io.tasks_tracker.profile.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

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
        return ResponseEntity
                .ok()
                .body(profileService.getProfile(authenticationService.getUserId(authentication)));
    }
    
    @PutMapping
    public ResponseEntity<User> updateProfile(
        @RequestBody UpdateProfileRequest request,
        Authentication authentication
    ) {
        return ResponseEntity
                .ok()
                .body(profileService.updateProfileById(
                    request, authentication, authenticationService.getUserId(authentication)
                ));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteProfile(
        Authentication authentication,
        HttpServletRequest request
    ) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_NAME, 
            "user.delete", 
            authenticationService.getUserId(authentication)
        );
        
        authenticationService.logoutAll(authentication, request);
        profileService.deleteProfileById(authentication, authenticationService.getUserId(authentication));
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getProfileById(
        @PathVariable Long id,
        Authentication authentication
    ) {
        return ResponseEntity
                .ok()
                .body(profileService.getProfileById(authentication, id));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<User> updateProfileById(
        @PathVariable Long id,
        @RequestBody UpdateProfileRequest request,
        Authentication authentication
    ) {
        return ResponseEntity
                .ok()
                .body(profileService.updateProfileById(
                    request, 
                    authentication, 
                    authenticationService.getUserId(authentication)
                ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfileById(
        @PathVariable Long id,
        Authentication authentication,
        HttpServletRequest request
    ) {
        User userToDelete = profileService.getProfileById(authentication, id);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "user.delete", id);
        
        if(userToDelete.getUsername().equals(authentication.getName())) {
            authenticationService.logoutAll(authentication, request);
        }
        else {
            authenticationService.deleteAllSessions(userToDelete.getUsername());
        }

        profileService.deleteProfileById(authentication, id);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
