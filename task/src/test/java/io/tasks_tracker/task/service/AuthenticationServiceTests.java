package io.tasks_tracker.task.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTests 
{
    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private Authentication authentication;

    @Test
    void getUserId_ReturnsCorrectLong()
    {
        Long excpect = 5L;
        String testUsername = "testUsername";

        when(authentication.getDetails()).thenReturn(excpect);
        when(authentication.getName()).thenReturn(testUsername);

        Long result = authenticationService.getUserId(authentication);
        
        assertEquals(excpect, result);
    }
}
