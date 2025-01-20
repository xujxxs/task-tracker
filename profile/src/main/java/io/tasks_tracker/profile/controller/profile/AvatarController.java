package io.tasks_tracker.profile.controller.profile;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
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
import io.tasks_tracker.profile.service.ProfileService;
import io.tasks_tracker.profile.service.S3StorageService;

@RestController
@RequestMapping("/api/profile")
public class AvatarController 
{
    @Autowired
    private ProfileService profileService;

    @Autowired
    private S3StorageService s3StorageService;

    @GetMapping("/avatar")
    public ResponseEntity<InputStreamResource> getAvatar(Authentication authentication) 
    {
        User user = profileService.getProfileWithOutCache(profileService.getUserId(authentication));

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + user.getAvatarLink())
                .contentType(MediaType.IMAGE_JPEG)
                .body(new InputStreamResource(s3StorageService.getAvatar(user)));
    }

    @PostMapping("/avatar")
    public ResponseEntity<?> postAvatar(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) throws IOException
    {
        s3StorageService.uploadAvatar(
            file,
            profileService.getProfileWithOutCache(profileService.getUserId(authentication))
        );
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @DeleteMapping("/avatar")
    public ResponseEntity<?> deleteAvatar(Authentication authentication)
    {
        s3StorageService.deleteAvatar(
            profileService.getProfileWithOutCache(profileService.getUserId(authentication))
        );

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @GetMapping("/{id}/avatar")
    public ResponseEntity<InputStreamResource> getAvatarById(
            @PathVariable Long userId,
            Authentication authentication
    ) {
        User user = profileService.getProfileWithOutCache(profileService.getUserId(authentication));
        if(!profileService.hasAccess(authentication, user)) {
            throw new NoAccessException("user", user.getId());
        }

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + user.getAvatarLink())
                .contentType(MediaType.IMAGE_JPEG)
                .body(new InputStreamResource(s3StorageService.getAvatar(user)));
    }

    @PostMapping("/{id}/avatar")
    public ResponseEntity<?> postAvatarById(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) throws IOException
    {
        User user = profileService.getProfileWithOutCache(profileService.getUserId(authentication));
        if(!profileService.hasAccess(authentication, user)) {
            throw new NoAccessException("user", user.getId());
        }

        s3StorageService.uploadAvatar(file, user);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @DeleteMapping("/{id}/avatar")
    public ResponseEntity<?> deleteAvatarById(
            @PathVariable Long userId,
            Authentication authentication
    ) {
        User user = profileService.getProfileWithOutCache(profileService.getUserId(authentication));
        if(!profileService.hasAccess(authentication, user)) {
            throw new NoAccessException("user", user.getId());
        }

        s3StorageService.deleteAvatar(user);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
