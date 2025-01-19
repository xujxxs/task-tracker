package io.tasks_tracker.profile.exception;

import java.util.List;

public class InvalidFileExtension extends RuntimeException
{
    public InvalidFileExtension(List<String> supportedExtensions)
    {
        super("Invalid file extension. Now supported: " + supportedExtensions.toString() + ".");
    }
}
