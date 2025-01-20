package io.tasks_tracker.profile.service;

import org.springframework.beans.factory.annotation.Autowired;
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

@Service
public class ProfileService 
{
    @Autowired
    private UserRepository userRepository;

    public Long getUserId(Authentication authentication)
    {
        return (Long) authentication.getDetails();
    }

    public boolean hasAccess(
            Authentication authentication, 
            User user
    ) {
        return user.getUsername().equals(authentication.getName())
                || authentication.getAuthorities()
                    .stream()
                    .anyMatch(role -> role.getAuthority().equals("ADMIN"));
    }

    @Cacheable(value = "users", key = "#userId")
    public User getProfile(Long userId)
    {
        return userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }
    
    public User getProfileWithOutCache(Long userId)
    {
        return userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }

    @CachePut(value = "users", key = "#userId")
    public User updateProfile(
            UpdateProfileRequest updateProfileRequest,
            Long userId
    ) {
        User user = getProfile(userId);

        if(!user.getEmail().equals(updateProfileRequest.getEmail())
            && userRepository.findByEmail(updateProfileRequest.getEmail()).isPresent()) {
            throw new EmailUsedException();
        }

        user.setBirthDate(updateProfileRequest.getBirth_date());
        user.setEmail(updateProfileRequest.getEmail());
        user.setFirstname(updateProfileRequest.getFirstname());
        user.setLastname(updateProfileRequest.getLastname());

        return userRepository.save(user);
    }

    @CacheEvict(value = "users", key = "#userId")
    public void deleteProfile(Long userId)
    {
        userRepository.deleteById(userId);
    }

    @CachePut(value = "users", key = "#userId")
    public User getProfileById(
            Authentication authentication, 
            Long userId
    ) {
        User user = getProfile(userId);

        if(!hasAccess(authentication, user)) {
             throw new NoAccessException("user", userId);
        }
        return user;
    }

    @CachePut(value = "users", key = "#userId")
    public User updateProfileById(
            UpdateProfileRequest updateProfileRequest,
            Authentication authentication,
            Long userId
    ) {
        User user = getProfileById(authentication, userId);

        if(!user.getEmail().equals(updateProfileRequest.getEmail())
            && userRepository.findByEmail(updateProfileRequest.getEmail()).isPresent()) {
            throw new EmailUsedException();
        }

        user.setBirthDate(updateProfileRequest.getBirth_date());
        user.setEmail(updateProfileRequest.getEmail());
        user.setFirstname(updateProfileRequest.getFirstname());
        user.setLastname(updateProfileRequest.getLastname());

        return userRepository.save(user);
    }

    @CacheEvict(value = "users", key = "#userId")
    public void deleteProfileById(
            Authentication authentication,
            Long userId
    ) {
        User user = getProfileById(authentication, userId);
        userRepository.delete(user);
    }
}
