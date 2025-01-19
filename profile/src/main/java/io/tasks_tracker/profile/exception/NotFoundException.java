package io.tasks_tracker.profile.exception;

public class NotFoundException extends RuntimeException 
{
    public NotFoundException(String type) 
    {
        super(type + " not found.");
    }
}
