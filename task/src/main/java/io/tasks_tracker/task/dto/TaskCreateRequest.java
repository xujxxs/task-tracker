package io.tasks_tracker.task.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskCreateRequest 
{
    @NotNull
    private TaskRequest task;

    @NotNull
    private List<SubtaskRequest> subtasks;
}
