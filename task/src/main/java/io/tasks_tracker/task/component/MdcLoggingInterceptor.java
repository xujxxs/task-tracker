package io.tasks_tracker.task.component;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.tasks_tracker.task.service.AuthenticationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class MdcLoggingInterceptor extends OncePerRequestFilter 
{
    private final AuthenticationService authenticationService;

    public MdcLoggingInterceptor(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException
    {
        MDC.put("requestId", UUID.randomUUID().toString());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if(authentication != null && authentication.isAuthenticated()
            && authentication.getDetails() instanceof Long
        ) {
            MDC.put("userId", authenticationService.getUserId(authentication).toString());
        }

        filterChain.doFilter(request, response);

        MDC.clear();
    }
}
