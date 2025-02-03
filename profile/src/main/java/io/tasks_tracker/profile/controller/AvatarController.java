package io.tasks_tracker.profile.controller;

import java.io.IOException;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.tasks_tracker.profile.entity.User;
import io.tasks_tracker.profile.exception.NoAccessException;
import io.tasks_tracker.profile.service.AuthenticationService;
import io.tasks_tracker.profile.service.ProfileService;
import io.tasks_tracker.profile.service.S3StorageService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/profile")
public class AvatarController 
{
    private final AuthenticationService authenticationService;
    private final ProfileService profileService;
    private final S3StorageService s3StorageService;

    public AvatarController(
        AuthenticationService authenticationService,
        ProfileService profileService,
        S3StorageService s3StorageService
    ) {
        this.authenticationService = authenticationService;
        this.profileService = profileService;
        this.s3StorageService = s3StorageService;
    }

    @GetMapping("/avatar")
    public ResponseEntity<InputStreamResource> getAvatar(Authentication authentication) 
    {
        Long userId = authenticationService.getUserId(authentication);
        log.info("Initiating fetching self avatar for user: {}", userId);
        User user = profileService.getProfileWithOutCache(userId);
        
        log.debug("Self avatar fetched, file name: {}", user.getAvatarLink());
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + user.getAvatarLink())
                .contentType(MediaType.IMAGE_JPEG)
                .body(new InputStreamResource(s3StorageService.getAvatar(user)));
    }

    @PostMapping("/avatar")
    public ResponseEntity<Void> postAvatar(
        @RequestParam("file") MultipartFile file,
        Authentication authentication
    ) throws IOException
    {
        Long userId = authenticationService.getUserId(authentication);
        log.info("Initiating uploading self avatar for user: {}", userId);
        log.debug("File info: {} ({} bytes)", file.getOriginalFilename(), file.getSize());
        
        s3StorageService.uploadAvatar(
            file,
            profileService.getProfileWithOutCache(userId)
        );
        
        log.info("Self avatar uploaded successfully for user: {}", userId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @DeleteMapping("/avatar")
    public ResponseEntity<Void> deleteAvatar(Authentication authentication)
    {
        Long userId = authenticationService.getUserId(authentication);
        log.info("Initiating deleting self avatar for user: {}", userId);
        
        s3StorageService.deleteAvatar(
            profileService.getProfileWithOutCache(userId)
        );
        
        log.info("Self avatar deleted successfully for user: {}", userId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @GetMapping("/{id}/avatar")
    public ResponseEntity<InputStreamResource> getAvatarById(
        @PathVariable Long id,
        Authentication authentication
    ) {
        Long requesterId = authenticationService.getUserId(authentication);
        log.info("Initiating fetching avatar for user: {} by user: {}", id, requesterId);

        User user = profileService.getProfileWithOutCache(id);
        if(!profileService.hasAccess(authentication, user)) {
            throw new NoAccessException("user", user.getId());
        }

        log.debug("Avatar fetched, file name: {}", user.getAvatarLink());
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + user.getAvatarLink())
                .contentType(MediaType.IMAGE_JPEG)
                .body(new InputStreamResource(s3StorageService.getAvatar(user)));
    }

    @PostMapping("/{id}/avatar")
    public ResponseEntity<Void> postAvatarById(
        @PathVariable Long id,
        @RequestParam("file") MultipartFile file,
        Authentication authentication
    ) throws IOException
    {
        Long requesterId = authenticationService.getUserId(authentication);
        log.info("Initiating uploading avatar for user: {} by user: {}", id, requesterId);

        User user = profileService.getProfileWithOutCache(id);
        if(!profileService.hasAccess(authentication, user)) {
            throw new NoAccessException("user", user.getId());
        }

        log.debug("Processing file: {} ({} bytes)", file.getOriginalFilename(), file.getSize());
        s3StorageService.uploadAvatar(file, user);
        
        log.info("Avatar update completed for user: {} by user: {}", id, requesterId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @DeleteMapping("/{id}/avatar")
    public ResponseEntity<Void> deleteAvatarById(
        @PathVariable Long id,
        Authentication authentication
    ) {
        Long requesterId = authenticationService.getUserId(authentication);
        log.warn("Initiating avatar deletion for user: {} by user: {}", id, requesterId);

        User user = profileService.getProfileWithOutCache(id);
        if(!profileService.hasAccess(authentication, user)) {
            throw new NoAccessException("user", user.getId());
        }

        s3StorageService.deleteAvatar(user);

        log.info("Avatar deleted for user: {} by user: {}", id, requesterId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
