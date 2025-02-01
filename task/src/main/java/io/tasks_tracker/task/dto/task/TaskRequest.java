package io.tasks_tracker.task.dto.task;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskRequest 
{
    @NotNull
    private String title;

    private String description;

    private String category;
    
    private LocalDateTime dateEnd;

    @NotNull
    private Long importance;
}
