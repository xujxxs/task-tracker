package io.tasks_tracker.task.exception;

public class NoAccessException extends RuntimeException 
{
    public NoAccessException(String type, Long id) 
    {
        super("No access to " + type + " with id: " + id.toString());
    }
}
