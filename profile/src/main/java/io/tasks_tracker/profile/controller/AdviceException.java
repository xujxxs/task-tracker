package io.tasks_tracker.profile.controller;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.tasks_tracker.profile.exception.EmailUsedException;
import io.tasks_tracker.profile.exception.InvalidFileExtension;
import io.tasks_tracker.profile.exception.InvalidFileName;
import io.tasks_tracker.profile.exception.InvalidPassword;
import io.tasks_tracker.profile.exception.InvalidSignInForm;
import io.tasks_tracker.profile.exception.InvalidSignUpForm;
import io.tasks_tracker.profile.exception.NoAccessException;
import io.tasks_tracker.profile.exception.NotFoundException;

@RestControllerAdvice
public class AdviceException
{
    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String ioException(IOException ex)
    {
        return ex.getMessage();
    }

    @ExceptionHandler(EmailUsedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String emailUsedException(EmailUsedException ex)
    {
        return ex.getMessage();
    }

    @ExceptionHandler(InvalidFileExtension.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String invalidFileExtension(InvalidFileExtension ex)
    {
        return ex.getMessage();
    }

    @ExceptionHandler(InvalidFileName.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String invalidFileName(InvalidFileName ex)
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
    @ResponseStatus(HttpStatus.BAD_REQUEST)
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

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFoundException(NotFoundException ex)
    {
        return ex.getMessage();
    }
}
