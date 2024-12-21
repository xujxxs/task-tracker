package io.tasks_tracker.profile.exception;

public class InvalidSignUpForm extends RuntimeException 
{
    public InvalidSignUpForm(String advanced) 
    {
        super("Invalid sign in form: " + advanced + ".");
    }
}
