package io.tasks_tracker.task.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthenticationService {

    public Long getUserId(Authentication authentication)
    {
        log.trace("Getting user id for authentication: {}", authentication.getName());
        return (Long) authentication.getDetails();
    }
}
