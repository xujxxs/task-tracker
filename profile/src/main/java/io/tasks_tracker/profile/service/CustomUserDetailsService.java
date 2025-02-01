package io.tasks_tracker.profile.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import io.tasks_tracker.profile.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService
{
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository)
    {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
        throws UsernameNotFoundException
    {
        log.debug("Attempting to load user: {}", username);

        return userRepository.findByUsername(username)
                .map(user -> {
                    log.debug("User found by username: {}, id: {}", username, user.getId());

                    return new User(
                        user.getUsername(), 
                        user.getPassword(), 
                        user.getAuthorities()
                    );
                })
                .orElseThrow(() -> {
                    log.warn("User not found by username: {}", username);
                    return new UsernameNotFoundException(username + " not found.");
                });
    }
}
