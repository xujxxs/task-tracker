package io.tasks_tracker.profile.controller.profile;

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
                .body(profileService.getProfile(profileService.getUserId(authentication)));
    }
    
    @PutMapping
    public ResponseEntity<User> updateProfile(
            @RequestBody UpdateProfileRequest request,
            Authentication authentication
    ) {
        return ResponseEntity
                .ok()
                .body(profileService.updateProfile(request, profileService.getUserId(authentication)));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteProfile(
                Authentication authentication,
                HttpServletRequest request
    ) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_NAME, 
            "user.delete", 
            profileService.getUserId(authentication)
        );
        
        auntificationService.logoutAll(authentication, request);
        profileService.deleteProfile(profileService.getUserId(authentication));
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
                    profileService.getUserId(authentication)
                ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProfileById(
            @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest request
    ) {
        User userToDelete = profileService.getProfileById(authentication, id);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "user.delete", id);
        
        if(userToDelete.getUsername().equals(authentication.getName())) {
            auntificationService.logoutAll(authentication, request);
        }
        else {
            auntificationService.deleteAllSessions(userToDelete.getUsername());
        }

        profileService.deleteProfileById(authentication, id);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
