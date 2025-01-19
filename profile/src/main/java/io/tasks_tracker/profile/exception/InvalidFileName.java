package io.tasks_tracker.profile.exception;

public class InvalidFileName extends RuntimeException
{
    public InvalidFileName(String advanced)
    {
        super("Invalid file name: " + advanced);
    }
}
