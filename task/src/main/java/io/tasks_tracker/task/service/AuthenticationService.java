package io.tasks_tracker.task.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    public Long getUserId(Authentication authentication)
    {
        return (Long) authentication.getDetails();
    }
}
