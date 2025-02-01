package io.tasks_tracker.task.dto.filter;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImportanceFilterParams 
{
    private Long equal;
    private Long greaterOrEqual;
    private Long lessOrEqual;
}
