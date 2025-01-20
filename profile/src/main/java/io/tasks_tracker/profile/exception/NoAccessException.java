package io.tasks_tracker.profile.exception;

public class NoAccessException extends RuntimeException 
{
    public NoAccessException(String type, Long userId) 
    {
        super("No access to " + type + " with id: " + userId);
    }
}
