package io.tasks_tracker.profile.entity.userdetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import io.tasks_tracker.profile.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService
{
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
    throws UsernameNotFoundException
    {
        return userRepository.findByUsername(username)
                    .map(user -> new User(
                        user.getUsername(), 
                        user.getPassword(), 
                        user.getAuthorities()
                    ))
                .orElseThrow(() -> new UsernameNotFoundException(username + " not found."));
    }
}
