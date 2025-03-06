package io.tasks_tracker.task.dto.subtask;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubtaskRequest 
{
    @NotNull
    private String title;

    private boolean isCompleted;
}
