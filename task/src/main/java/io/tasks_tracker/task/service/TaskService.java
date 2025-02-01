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
        return task.getCreatedBy().equals(authenticationService.getUserId(authentication))
                || authentication.getAuthorities()
                    .stream()
                    .anyMatch(role -> role.getAuthority().equals("ADMIN"));
    }

    @Cacheable(value = "tasks", key = "#id")
    public Task getTask(
        Authentication authentication,
        Long id
    ) throws NotFoundException, NoAccessException 
    {
        Task task = cacheService.getTaskById(id);
        
        if(!hasAccess(task, authentication)) {
            throw new NoAccessException("task", id);
        }
        return task;
    }

    public Page<Task> getTasks(
        PaginationParams pagination, 
        TaskFilterParams filters
    ) {
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

        return taskRepository.findAll(spec, page);
    }

    public Task createTask(
        TaskCreateRequest taskRequest, 
        Long userId
    ) {
        Task task = new Task();
        task.setTitle(taskRequest.getTask().getTitle());
        task.setDescription(taskRequest.getTask().getDescription());
        task.setCategory(taskRequest.getTask().getCategory());
        task.setDateEnd(taskRequest.getTask().getDateEnd());
        task.setImportance(taskRequest.getTask().getImportance());
        task.setCreatedBy(userId);

        Task savedTask = taskRepository.save(task);
        taskRequest.getSubtasks().forEach(requestSubtask -> {
            Subtask subtask = new Subtask();
            subtask.setTitle(requestSubtask.getTitle());
            subtask.setCompleted(requestSubtask.isCompleted());
            subtask.setCreatedBy(userId);
            subtask.setTask(savedTask);
            savedTask.addSubtask(subtaskRepository.save(subtask));
        });

        return taskRepository.save(cacheService.updateTaskCompletionStatus(savedTask));
    }

    @CachePut(value = "tasks", key = "#id")
    public Task updateTask(
        Long id, 
        TaskRequest taskRequest, 
        Authentication authentication
    ) throws NotFoundException, NoAccessException 
    {
        Task taskUpdate = getTask(authentication, id);
        taskUpdate.setTitle(taskRequest.getTitle());
        taskUpdate.setDescription(taskRequest.getDescription());
        taskUpdate.setCategory(taskRequest.getCategory());
        taskUpdate.setDateEnd(taskRequest.getDateEnd());
        taskUpdate.setImportance(taskRequest.getImportance());
        
        return taskRepository.save(cacheService.updateTaskCompletionStatus(taskUpdate));
    }

    @CacheEvict(value = "tasks", key = "#id")
    public void deleteTask(
        Authentication authentication,
        Long id
    ) throws NotFoundException, NoAccessException 
    {
        Task task = getTask(authentication, id);
        task.getSubtasks().forEach(subtask -> cacheService.evictSubtaskFromCache(subtask));
        taskRepository.delete(task);
    }
}
