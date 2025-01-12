package io.tasks_tracker.profile.exception;

public class NoAccessException extends RuntimeException 
{
    public NoAccessException(String type, String username) 
    {
        super("No access to " + type + " with username: " + username);
    }
}
