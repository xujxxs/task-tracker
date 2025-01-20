package io.tasks_tracker.task.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
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

import io.tasks_tracker.task.dto.TaskCreateRequest;
import io.tasks_tracker.task.dto.TaskRequest;
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
    @Autowired
    private CacheService cacheService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private SubtaskRepository subtaskRepository;

    public Long getUserId(Authentication authentication)
    {
        return (Long) authentication.getDetails();
    }

    public boolean hasAccess(Task task, Authentication authentication)
    {
        return task.getCreatedBy().equals(getUserId(authentication))
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

    public Page<Task> getTaskByPage(
            int pageNum, 
            int pageSize, 
            String sortBy,
            String sortOrder,
            String title, 
            String category, 
            LocalDateTime equalDateEnd,
            LocalDateTime minDateEnd,
            LocalDateTime maxDateEnd,
            boolean isNotHaveEnded,
            LocalDateTime equalDateCreated,
            LocalDateTime minDateCreated,
            LocalDateTime maxDateCreated,
            LocalDateTime equalDateUpdated,
            LocalDateTime minDateUpdated,
            LocalDateTime maxDateUpdated,
            LocalDateTime equalDateEnded,
            LocalDateTime minDateEnded,
            LocalDateTime maxDateEnded,
            boolean isNotCompleted,
            Long equalToImportance,
            Long greaterThanOrEqualToImportance,
            Long lessThanOrEqualToImportance,
            Long userId
    ) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, 
            sortOrder.equalsIgnoreCase("asc") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending());

        Specification<Task> specification = Specification.where(TaskSpecification.filterByTitle(title))
                .and(TaskSpecification.filterByCategory(category))
                .and(TaskSpecification.filterByDateEnd(equalDateEnd, minDateEnd, maxDateEnd, isNotHaveEnded))
                .and(TaskSpecification.filterByCreated(equalDateCreated, minDateCreated, maxDateCreated))
                .and(TaskSpecification.filterByUpdated(equalDateUpdated, minDateUpdated, maxDateUpdated))
                .and(TaskSpecification.filterByEnded(equalDateEnded, minDateEnded, maxDateEnded, isNotCompleted))
                .and(TaskSpecification.filterByImportance(equalToImportance, greaterThanOrEqualToImportance, lessThanOrEqualToImportance))
                .and(TaskSpecification.filterByCreatedBy(userId));
        
        return taskRepository.findAll(specification, pageable);
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
