package io.tasks_tracker.profile.exception;

public class EmailUsedException extends RuntimeException 
{
    public EmailUsedException()
    {
        super("Email is already registered.");
    }
}
