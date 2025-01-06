package io.tasks_tracker.task.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubtaskRequest 
{
    @NotNull
    private String title;

    private boolean isCompleted;
}
