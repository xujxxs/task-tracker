package io.tasks_tracker.task.dto.filter;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DateFilterParams 
{
    private LocalDateTime equal;
    private LocalDateTime min;
    private LocalDateTime max;
}
