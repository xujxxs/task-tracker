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

    public User getProfile(String username)
    {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }

    public User updateProfile(
            Authentication authentication,
            UpdateProfileRequest updateProfileRequest
    ) {
        User user = getProfile(authentication.getName());

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
        userRepository.delete(getProfile(authentication.getName()));
    }

    public User getProfileByUsername(
            Authentication authentication, 
            String username
    ) {
        User user = getProfile(username);

        if(!hasAccess(authentication, user)) {
             throw new NoAccessException("user", username);
        }
        return user;
    }

    public User updateProfileByUsername(
            Authentication authentication, 
            String username,
            UpdateProfileRequest updateProfileRequest
    ) {
        User user = getProfileByUsername(authentication, username);

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

    public void deleteProfileByUsername(
            Authentication authentication, 
            String username
    ) {
        User user = getProfileByUsername(authentication, username);
        userRepository.delete(user);
    }
}
