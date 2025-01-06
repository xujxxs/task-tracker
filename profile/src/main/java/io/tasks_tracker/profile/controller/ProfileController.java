package io.tasks_tracker.profile.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.tasks_tracker.profile.dto.UpdateProfileRequest;
import io.tasks_tracker.profile.entity.User;
import io.tasks_tracker.profile.service.ProfileService;

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

    @GetMapping
    public ResponseEntity<User> getProfile(Authentication authentication) 
    {
        return ResponseEntity
                .ok()
                .body(profileService.getProfile(authentication));
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
    public ResponseEntity<?> deleteProfile(Authentication authentication) 
    {
        //TODO: add delete tasks
        profileService.deleteProfile(authentication);
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
            @RequestBody UpdateProfileRequest entity,
            Authentication authentication
    ) {
        return ResponseEntity
                .ok()
                .body(profileService.updateProfileById(authentication, id, entity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProfileById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        //TODO: add delete tasks
        profileService.deleteProfileById(authentication, id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
