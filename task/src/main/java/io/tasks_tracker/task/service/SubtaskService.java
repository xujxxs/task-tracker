package io.tasks_tracker.task.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import io.tasks_tracker.task.dto.subtask.SubtaskCreateRequest;
import io.tasks_tracker.task.dto.subtask.SubtaskRequest;
import io.tasks_tracker.task.entity.Subtask;
import io.tasks_tracker.task.entity.Task;
import io.tasks_tracker.task.exception.NoAccessException;
import io.tasks_tracker.task.exception.NotFoundException;
import io.tasks_tracker.task.repository.SubtaskRepository;
import io.tasks_tracker.task.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SubtaskService 
{
    private final AuthenticationService authenticationService;
    private final CacheService cacheService;
    private final TaskService taskService;
    private final SubtaskRepository subtaskRepository;
    private final TaskRepository taskRepository;

    public SubtaskService(
        AuthenticationService authenticationService,
        CacheService cacheService,
        TaskService taskService,
        SubtaskRepository subtaskRepository,
        TaskRepository taskRepository
    ) {
        this.authenticationService = authenticationService;
        this.cacheService = cacheService;
        this.taskService = taskService;
        this.subtaskRepository = subtaskRepository;
        this.taskRepository = taskRepository;
    }

    @Cacheable(value = "subtasks", key = "#id")
    public Subtask getSubtask(
            Authentication authentication,
            Long id
    ) throws NotFoundException, NoAccessException
    {
        log.info("Starting fetch subtask: {}", id);
        Subtask subtask = cacheService.getSubtaskById(id);

        if(!taskService.hasAccess(subtask.getTask(), authentication)) {
            throw new NoAccessException("subtask", id);
        }

        log.info("Fetch subtask: {} successfully", id);
        return subtask;
    }

    public Subtask createSubtask(
            SubtaskCreateRequest subtask, 
            Authentication authentication
    ) throws NotFoundException, NoAccessException 
    {
        log.info("Starting create subtask");

        Subtask newSubtask = new Subtask();
        newSubtask.setTitle(subtask.getSubtask().getTitle());
        newSubtask.setCompleted(subtask.getSubtask().isCompleted());
        newSubtask.setCreatedBy(authenticationService.getUserId(authentication));

        log.debug("Link subtask with task: {}", subtask.getTaskId());
        Task task = taskService.getTask( 
            authentication,
            subtask.getTaskId()
        );
        newSubtask.setTask(task);

        Subtask savedSubtask = subtaskRepository.save(newSubtask);
        task.addSubtask(savedSubtask);
    
        cacheService.updateTaskCompletionStatus(task);

        log.info("Create subtask successfully, id: {}", savedSubtask.getId());
        return savedSubtask;
    }

    @CachePut(value = "subtasks", key = "#id")
    public Subtask markSubtask(
            Long id, 
            boolean isCompleted, 
            Authentication authentication
    ) throws NotFoundException, NoAccessException 
    {
        log.info("Starting mark subtask: {}", id);

        Subtask subtask = getSubtask(authentication, id);
        subtask.setCompleted(isCompleted);
        
        Subtask resSubtask = subtaskRepository.save(subtask);
        log.debug("Subtask {} updated successfully", id);

        cacheService.updateTaskCompletionStatus(cacheService.checkOnCacheInSubtask(resSubtask.getTask()));
        
        log.info("Mark subtask: {} successfully", id);
        return resSubtask;
    }

    @CachePut(value = "subtasks", key = "#id")
    public Subtask updateSubtask(
            Long id, 
            SubtaskRequest subtask, 
            Authentication authentication
    ) throws NotFoundException, NoAccessException 
    {
        log.info("Starting update subtask: {}", id);

        Subtask updateSubtask = getSubtask(authentication, id);
        updateSubtask.setTitle(subtask.getTitle());
        updateSubtask.setCompleted(subtask.isCompleted());

        Subtask resSubtask = subtaskRepository.save(updateSubtask);
        log.debug("Subtask {} updated successfully", id);

        cacheService.updateTaskCompletionStatus(cacheService.checkOnCacheInSubtask(resSubtask.getTask()));

        log.info("Update subtask: {} successfully", id);
        return resSubtask;
    }

    @CacheEvict(value = "subtasks", key = "#id")
    public void deleteSubtask(
            Long id, 
            Authentication authentication
    ) throws NotFoundException, NoAccessException 
    {
        log.info("Starting delete subtask: {}", id);

        Subtask subtask = getSubtask(authentication, id);
        Task task = cacheService.getTaskById(subtask.getTask().getId());

        log.debug("Remove subtask: {} from task: {}", id, task.getId());
        task.removeSubtask(subtask);
        taskRepository.save(task);
        log.debug("Subtask: {} was deleted and removed from task: {} successfully. ", id, task.getId());
        cacheService.updateTaskCompletionStatus(task);

        log.info("Delete subtask: {} successfully", id);
    }
}
