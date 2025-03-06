package io.tasks_tracker.task.dto.subtask;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubtaskCreateRequest 
{
    @NotNull
    private SubtaskRequest subtask;

    @NotNull
    private Long taskId;
}
