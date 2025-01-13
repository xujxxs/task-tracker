package io.tasks_tracker.profile.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.tasks_tracker.profile.exception.EmailUsedException;
import io.tasks_tracker.profile.exception.InvalidPassword;
import io.tasks_tracker.profile.exception.InvalidSignInForm;
import io.tasks_tracker.profile.exception.InvalidSignUpForm;
import io.tasks_tracker.profile.exception.NoAccessException;

@RestControllerAdvice
public class AdviceException
{
    @ExceptionHandler(EmailUsedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String emailUsedException(EmailUsedException ex)
    {
        return ex.getMessage();
    }

    @ExceptionHandler(InvalidPassword.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String invalidPassword(InvalidPassword ex)
    {
        return ex.getMessage();
    }

    @ExceptionHandler(InvalidSignInForm.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String invalidSignInForm(InvalidSignInForm ex)
    {
        return ex.getMessage();
    }

    @ExceptionHandler(InvalidSignUpForm.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String invalidSignUpForm(InvalidSignUpForm ex)
    {
        return ex.getMessage();
    }
    
    @ExceptionHandler(NoAccessException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String noAccessException(NoAccessException ex)
    {
        return ex.getMessage();
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String usernameNotFoundException(UsernameNotFoundException ex)
    {
        return ex.getMessage();
    }
}
