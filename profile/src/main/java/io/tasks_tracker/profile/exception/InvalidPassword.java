package io.tasks_tracker.profile.exception;

public class InvalidPassword extends RuntimeException
{
    public InvalidPassword()
    {
        super("Invalid password.");
    }
}
