package io.tasks_tracker.task.controller;

import org.springframework.web.bind.annotation.RestController;

import io.tasks_tracker.task.dto.filter.DateFilterParams;
import io.tasks_tracker.task.dto.filter.ImportanceFilterParams;
import io.tasks_tracker.task.dto.filter.PaginationParams;
import io.tasks_tracker.task.dto.filter.TaskFilterParams;
import io.tasks_tracker.task.dto.task.TaskCreateRequest;
import io.tasks_tracker.task.dto.task.TaskRequest;
import io.tasks_tracker.task.entity.Task;
import io.tasks_tracker.task.service.AuthenticationService;
import io.tasks_tracker.task.service.TaskService;

import java.net.URI;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/tasks")
public class TaskController 
{
    private AuthenticationService authenticationService;
    private TaskService taskService;

    public TaskController(
        AuthenticationService authenticationService,    
        TaskService taskService
    ) {
        this.authenticationService = authenticationService;
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<Page<Task>> getTaskPage(
        Authentication authentication,
        @RequestParam(defaultValue = "1") int pageNumber,
        @RequestParam(defaultValue = "10") int pageSize,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String sortOrder,
        @RequestParam(required = false) String title,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) LocalDateTime equalDateEnd,
        @RequestParam(required = false) LocalDateTime minDateEnd,
        @RequestParam(required = false) LocalDateTime maxDateEnd,
        @RequestParam(defaultValue = "false") boolean isNotHaveEnded,
        @RequestParam(required = false) LocalDateTime equalDateCreated,
        @RequestParam(required = false) LocalDateTime minDateCreated,
        @RequestParam(required = false) LocalDateTime maxDateCreated,
        @RequestParam(required = false) LocalDateTime equalDateUpdated,
        @RequestParam(required = false) LocalDateTime minDateUpdated,
        @RequestParam(required = false) LocalDateTime maxDateUpdated,
        @RequestParam(required = false) LocalDateTime equalDateEnded,
        @RequestParam(required = false) LocalDateTime minDateEnded,
        @RequestParam(required = false) LocalDateTime maxDateEnded,
        @RequestParam(defaultValue = "false") boolean isNotCompleted,
        @RequestParam(required = false) Long equalToImportance,
        @RequestParam(required = false) Long greaterThanOrEqualToImportance,
        @RequestParam(required = false) Long lessThanOrEqualToImportance
    ) {
        DateFilterParams dateEnd = new DateFilterParams(equalDateEnd, minDateEnd, maxDateEnd);
        DateFilterParams dateCreated = new DateFilterParams(equalDateCreated, minDateCreated, maxDateCreated);
        DateFilterParams dateUpdated = new DateFilterParams(equalDateUpdated, minDateUpdated, maxDateUpdated);
        DateFilterParams dateEnded = new DateFilterParams(equalDateEnded, minDateEnded, maxDateEnded);
        ImportanceFilterParams importance = new ImportanceFilterParams(equalToImportance, greaterThanOrEqualToImportance, lessThanOrEqualToImportance);

        return ResponseEntity
                .ok()
                .body(taskService.getTasks(
                    new PaginationParams(
                        pageNumber, pageSize, sortOrder, sortBy
                    ),
                    new TaskFilterParams(
                        title, category, dateEnd, isNotHaveEnded, 
                        dateCreated, dateUpdated, dateEnded, isNotCompleted, 
                        importance, authenticationService.getUserId(authentication)
                    )
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTask(
        Authentication authentication,
        @PathVariable Long id
    ) {
        return ResponseEntity
                .ok()
                .body(taskService.getTask(authentication, id));
    }
    
    @PostMapping
    public ResponseEntity<Task> createTask(
        Authentication authentication,
        @RequestBody TaskCreateRequest entity
    ) {
        Task newTask = taskService.createTask(entity, authenticationService.getUserId(authentication));
        return ResponseEntity
                .created(URI.create("/tasks/" + newTask.getId().toString()))
                .body(newTask);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(
        Authentication authentication,
        @PathVariable Long id, 
        @RequestBody TaskRequest entity
    ) {
        return ResponseEntity
                .ok()
                .body(taskService.updateTask(
                    id, 
                    entity, 
                    authentication
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
        Authentication authentication,
        @PathVariable Long id
    ) {
        taskService.deleteTask(authentication, id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
