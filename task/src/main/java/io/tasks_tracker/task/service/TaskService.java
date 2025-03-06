package io.tasks_tracker.task.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import io.tasks_tracker.task.dto.filter.PaginationParams;
import io.tasks_tracker.task.dto.filter.TaskFilterParams;
import io.tasks_tracker.task.dto.task.TaskCreateRequest;
import io.tasks_tracker.task.dto.task.TaskRequest;
import io.tasks_tracker.task.entity.Subtask;
import io.tasks_tracker.task.entity.Task;
import io.tasks_tracker.task.exception.NoAccessException;
import io.tasks_tracker.task.exception.NotFoundException;
import io.tasks_tracker.task.repository.SubtaskRepository;
import io.tasks_tracker.task.repository.TaskRepository;
import io.tasks_tracker.task.specification.TaskSpecification;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TaskService 
{
    private final AuthenticationService authenticationService;
    private final CacheService cacheService;
    private final TaskRepository taskRepository;
    private final SubtaskRepository subtaskRepository;

    public TaskService(
        AuthenticationService authenticationService,
        CacheService cacheService,
        TaskRepository taskRepository,
        SubtaskRepository subtaskRepository
    ) {
        this.authenticationService = authenticationService;
        this.cacheService = cacheService;
        this.taskRepository = taskRepository;
        this.subtaskRepository = subtaskRepository;
    }

    public boolean hasAccess(Task task, Authentication authentication)
    {
        Long userIdWantAccess = authenticationService.getUserId(authentication);
        boolean hasAccess = task.getCreatedBy().equals(userIdWantAccess)
                || authentication.getAuthorities()
                    .stream()
                    .anyMatch(role -> role.getAuthority().equals("ADMIN"));
        
        if(hasAccess) {
            log.debug("Access granted: User: {} have access to task: {}", userIdWantAccess, task.getId());
            return true;
        }
        log.warn("Access denied: User: {} not have access to task: {}", userIdWantAccess, task.getId());
        return false;
    }

    @Cacheable(value = "tasks", key = "#id")
    public Task getTask(
        Authentication authentication,
        Long id
    ) {
        log.info("Starting fetch task: {}", id);
        Task task = cacheService.getTaskById(id);
        
        if(!hasAccess(task, authentication)) {
            throw new NoAccessException("task", id);
        }

        log.info("Fetch task: {} successfully", id);
        return task;
    }

    public Page<Task> getTasks(
        PaginationParams pagination, 
        TaskFilterParams filters
    ) {
        log.info("Starting fetch page tasks by pagination filter: {} and param filter: {}", pagination, filters);
        Pageable page = PageRequest.of(
            pagination.getPageNumber() - 1, 
            pagination.getPageSize(), 
            pagination.getSortOrder().equalsIgnoreCase("asc") 
                ? Sort.by(pagination.getSortBy()).ascending() 
                : Sort.by(pagination.getSortBy()).descending());

        Specification<Task> spec = Specification.where(TaskSpecification.filterByTitle(filters.getTitle()))
            .and(TaskSpecification.filterByCategory(filters.getCategory()))
            .and(TaskSpecification.filterByDateEnd(
                filters.getDateEnd().getEqual(), 
                filters.getDateEnd().getMin(),
                filters.getDateEnd().getMax(),
                filters.isNotHaveEnded()))
            .and(TaskSpecification.filterByCreated(
                filters.getDateCreated().getEqual(), 
                filters.getDateCreated().getMin(),
                filters.getDateCreated().getMax()))
            .and(TaskSpecification.filterByUpdated(
                filters.getDateUpdated().getEqual(), 
                filters.getDateUpdated().getMin(),
                filters.getDateUpdated().getMax()))
            .and(TaskSpecification.filterByEnded(
                filters.getDateEnded().getEqual(), 
                filters.getDateEnded().getMin(),
                filters.getDateEnded().getMax(),
                filters.isNotCompleted()))
            .and(TaskSpecification.filterByImportance(
                filters.getImportance().getEqual(), 
                filters.getImportance().getGreaterOrEqual(),
                filters.getImportance().getLessOrEqual()))
            .and(TaskSpecification.filterByCreatedBy(filters.getUserId()));

        Page<Task> pageTask = taskRepository.findAll(spec, page);
        log.info("Fetch page tasks by filters successfully");
        return pageTask;
    }

    public Task createTask(
        TaskCreateRequest taskRequest, 
        Long userId
    ) {
        long countSubtasks = taskRequest.getSubtasks() != null ? taskRequest.getSubtasks().size() : 0;
        log.info("Starting create task with {} subtasks", countSubtasks);

        Task savedTask = taskRepository.save(
            Task.builder()
                .title(taskRequest.getTask().getTitle())
                .description(taskRequest.getTask().getDescription())
                .category(taskRequest.getTask().getCategory())
                .dateEnd(taskRequest.getTask().getDateEnd())
                .importance(taskRequest.getTask().getImportance())
                .createdBy(userId)
            .build()
        );

        log.debug("Starting linking a task: {} with {} subtasks", 
            savedTask.getId(), countSubtasks);

        if(countSubtasks != 0L) {
            taskRequest.getSubtasks().forEach(requestSubtask -> 
                savedTask.addSubtask(subtaskRepository.save(
                    Subtask.builder()
                        .title(requestSubtask.getTitle())
                        .isCompleted(requestSubtask.isCompleted())
                        .createdBy(userId)
                        .task(savedTask)
                    .build()
            )));
        }
        Task createdTask = taskRepository.save(cacheService.updateTaskCompletionStatus(savedTask));

        log.info("Task: {} created successfully", createdTask.getId());
        return createdTask;
    }

    @CachePut(value = "tasks", key = "#id")
    public Task updateTask(
        Long id, 
        TaskRequest taskRequest, 
        Authentication authentication
    ) throws NotFoundException, NoAccessException 
    {
        log.info("Starting update task: {}", id);

        Task taskUpdate = getTask(authentication, id);
        taskUpdate.setTitle(taskRequest.getTitle());
        taskUpdate.setDescription(taskRequest.getDescription());
        taskUpdate.setCategory(taskRequest.getCategory());
        taskUpdate.setDateEnd(taskRequest.getDateEnd());
        taskUpdate.setImportance(taskRequest.getImportance());
        
        Task task = taskRepository.save(taskUpdate);

        log.info("Update task: {} successfully", id);
        return task;
    }

    @CacheEvict(value = "tasks", key = "#id")
    public void deleteTask(
        Authentication authentication,
        Long id
    ) throws NotFoundException, NoAccessException 
    {
        log.info("Starting delete task: {}", id);

        Task task = getTask(authentication, id);

        log.debug("Delete all subtask({}) linked with task: {}", task.getSubtasks().size(), id);
        task.getSubtasks().forEach(subtask -> cacheService.evictSubtaskFromCache(subtask));
        taskRepository.delete(task);

        log.info("Delete task: {} successfully", id);
    }
}
