package io.tasks_tracker.profile.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import io.tasks_tracker.profile.dto.UpdateProfileRequest;
import io.tasks_tracker.profile.entity.User;
import io.tasks_tracker.profile.exception.EmailUsedException;
import io.tasks_tracker.profile.exception.NoAccessException;
import io.tasks_tracker.profile.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProfileService 
{
    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;
    private final S3StorageService s3StorageService;

    public ProfileService(
            AuthenticationService authenticationService,
            UserRepository userRepository,
            S3StorageService s3StorageService
    ) {
        this.authenticationService = authenticationService;
        this.userRepository = userRepository;
        this.s3StorageService = s3StorageService;
    }

    public boolean hasAccess(
            Authentication authentication, 
            User user
    ) {
        Long userIdWantAccess = authenticationService.getUserId(authentication);
        boolean hasAccess = user.getId().equals(userIdWantAccess)
                || authentication.getAuthorities()
                    .stream()
                    .anyMatch(role -> role.getAuthority().equals("ADMIN"));

        if(hasAccess) {
            log.debug("Access granted: User: {} have access to user: {}", userIdWantAccess, user.getId());
            return true;
        }
        log.warn("Access denied: User: {} not have access to user: {}", userIdWantAccess, user.getId());
        return false;
    }

    @Cacheable(value = "users", key = "#userId")
    public User getProfile(Long userId)
    {
        log.debug("Fetching profile for user: {}", userId);
        return userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }
    
    public User getProfileWithOutCache(Long userId)
    {
        log.debug("Fetching profile for user: {} bypassing cache", userId);
        return userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }

    @CachePut(value = "users", key = "#userId")
    public User getProfileById(
            Authentication authentication, 
            Long userId
    ) {
        log.debug("Request getting user: {} from user: {}", 
            userId, authenticationService.getUserId(authentication));

        User user = getProfile(userId);

        if(!hasAccess(authentication, user)) {
            log.warn("Unauthorized profile access attempt: {} trying to access {}", 
                authenticationService.getUserId(authentication), userId);
            throw new NoAccessException("user", userId);
        }

        log.info("Authorized profile access: {} accessed user {}", 
            authenticationService.getUserId(authentication), userId);
        
        return user;
    }

    @CachePut(value = "users", key = "#userId")
    public User updateProfileById(
            UpdateProfileRequest updateProfileRequest,
            Authentication authentication,
            Long userId
    ) {
        log.info("Starting update profile user: {}", userId);
        User user = getProfileById(authentication, userId);

        if(!user.getEmail().equals(updateProfileRequest.getEmail())
            && userRepository.findByEmail(updateProfileRequest.getEmail()).isPresent()) 
        {
            log.warn("Email: '{}' already registered", user.getEmail());
            throw new EmailUsedException();
        }

        log.info("Updating fields for user: ", user.getId());
        user.setBirthDate(updateProfileRequest.getBirth_date());
        user.setEmail(updateProfileRequest.getEmail());
        user.setFirstname(updateProfileRequest.getFirstname());
        user.setLastname(updateProfileRequest.getLastname());

        User savedUser = userRepository.save(user);
        log.info("Successfully updated user: {}", userId);
        return savedUser;
    }

    @CacheEvict(value = "users", key = "#userId")
    public void deleteProfileById(
            Authentication authentication,
            Long userId
    ) {
        log.info("Initiating profile deletion for user: {}", userId);
        User user = getProfileById(authentication, userId);
        
        s3StorageService.deleteAvatar(user);
        userRepository.delete(user);

        log.info("User: {} successfully deleted", userId);
    }
}
