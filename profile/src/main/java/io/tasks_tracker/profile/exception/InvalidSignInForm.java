package io.tasks_tracker.profile.exception;

public class InvalidSignInForm extends RuntimeException
{
    public InvalidSignInForm() 
    {
        super("Invalid signin form.");
    }
}
