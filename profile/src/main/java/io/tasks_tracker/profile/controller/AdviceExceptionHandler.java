package io.tasks_tracker.profile.controller;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

import io.tasks_tracker.profile.exception.EmailUsedException;
import io.tasks_tracker.profile.exception.InvalidFileExtension;
import io.tasks_tracker.profile.exception.InvalidFileName;
import io.tasks_tracker.profile.exception.InvalidPassword;
import io.tasks_tracker.profile.exception.InvalidSignUpForm;
import io.tasks_tracker.profile.exception.NoAccessException;
import io.tasks_tracker.profile.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class AdviceExceptionHandler
{
    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String ioException(IOException ex)
    {
        log.error("IO error: {}", ex.getMessage());
        return ex.getMessage();
    }

    @ExceptionHandler(EmailUsedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String emailUsedException(EmailUsedException ex)
    {
        log.warn("Email conflict: {}", ex.getMessage());
        return ex.getMessage();
    }

    @ExceptionHandler(InvalidFileExtension.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String invalidFileExtension(InvalidFileExtension ex)
    {
        log.warn("Invalid file extension: {}", ex.getMessage());
        return ex.getMessage();
    }

    @ExceptionHandler(InvalidFileName.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String invalidFileName(InvalidFileName ex)
    {
        log.warn("Invalid file name: {}", ex.getMessage());
        return ex.getMessage();
    }

    @ExceptionHandler(InvalidPassword.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String invalidPassword(InvalidPassword ex)
    {
        log.warn("Invalid password attempt: {}", ex.getMessage());
        return ex.getMessage();
    }

    @ExceptionHandler(InvalidSignUpForm.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String invalidSignUpForm(InvalidSignUpForm ex)
    {
        log.warn("Invalid registration: {}", ex.getMessage());
        return ex.getMessage();
    }
    
    @ExceptionHandler(NoAccessException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String noAccessException(NoAccessException ex)
    {
        log.warn("Access denied: {}", ex.getMessage());
        return ex.getMessage();
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String usernameNotFoundException(UsernameNotFoundException ex)
    {
        log.warn("User not found: {}", ex.getMessage());
        return ex.getMessage();
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFoundException(NotFoundException ex)
    {
        log.warn("Resource not found: {}", ex.getMessage());
        return ex.getMessage();
    }

    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String multipartException(MultipartException ex)
    {
        log.warn("Multipart error: {}", ex.getMessage());
        return "Current request is not a multipart request";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleAllExceptions(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return "Internal server error";
    }
}
