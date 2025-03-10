package io.tasks_tracker.task.dto.task;

import java.util.List;

import io.tasks_tracker.task.dto.subtask.SubtaskRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaskCreateRequest 
{
    @NotNull
    private TaskRequest task;

    @NotNull
    private List<SubtaskRequest> subtasks;
}
