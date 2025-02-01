package io.tasks_tracker.task.dto.filter;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaginationParams 
{
    private int pageNumber;
    private int pageSize;
    private String sortOrder;
    private String sortBy;
}
