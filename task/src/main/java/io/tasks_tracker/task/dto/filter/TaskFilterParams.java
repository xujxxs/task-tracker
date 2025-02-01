package io.tasks_tracker.task.dto.filter;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TaskFilterParams 
{
    private String title;
    private String category;
    private DateFilterParams dateEnd;
    private boolean isNotHaveEnded;
    private DateFilterParams dateCreated;
    private DateFilterParams dateUpdated;
    private DateFilterParams dateEnded;
    private boolean isNotCompleted;
    private ImportanceFilterParams importance;
    private Long userId;
}
