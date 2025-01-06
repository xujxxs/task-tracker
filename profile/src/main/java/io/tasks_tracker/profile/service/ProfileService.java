package io.tasks_tracker.profile.service;

import org.springframework.beans.factory.annotation.Autowired;
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

    public boolean hasAccess(
            Authentication authentication, 
            User user
    ) {
        return user.getUsername().equals(authentication.getName())
                || authentication.getAuthorities()
                    .stream()
                    .anyMatch(role -> role.getAuthority().equals("ADMIN"));
    }

    public User getProfile(Authentication authentication)
    {
        return userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }

    public User updateProfile(
            Authentication authentication,
            UpdateProfileRequest updateProfileRequest
    ) {
        User user = getProfile(authentication);

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

    public void deleteProfile(Authentication authentication)
    {
        userRepository.delete(getProfile(authentication));
    }

    public User getProfileById(
            Authentication authentication, 
            Long userId
    ) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("User not found by id: " + String.valueOf(userId)));

        if(!hasAccess(authentication, user)) {
             throw new NoAccessException("user", userId);
        }
        return user;
    }

    public User updateProfileById(
            Authentication authentication, 
            Long userId,
            UpdateProfileRequest updateProfileRequest
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

    public void deleteProfileById(
            Authentication authentication, 
            Long userId
    ) {
        User user = getProfileById(authentication, userId);
        userRepository.delete(user);
    }
}
