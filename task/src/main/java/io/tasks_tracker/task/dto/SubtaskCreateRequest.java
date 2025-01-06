package io.tasks_tracker.task.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubtaskCreateRequest 
{
    @NotNull
    private SubtaskRequest subtask;

    @NotNull
    private Long taskId;
}
