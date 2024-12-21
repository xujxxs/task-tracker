package io.tasks_tracker.profile.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthEntryPoint implements AuthenticationEntryPoint 
{
    private final HandlerExceptionResolver resolver;

    public AuthEntryPoint(@Qualifier(value = "handlerExceptionResolver") HandlerExceptionResolver resolver) 
    {
        this.resolver = resolver;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authenticationException
    ) {
        resolver.resolveException(request, response, null, authenticationException);
    }
}
