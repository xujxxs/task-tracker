package io.tasks_tracker.task.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import io.tasks_tracker.task.dto.SubtaskCreateRequest;
import io.tasks_tracker.task.dto.SubtaskRequest;
import io.tasks_tracker.task.entity.Subtask;
import io.tasks_tracker.task.entity.Task;
import io.tasks_tracker.task.exception.NoAccessException;
import io.tasks_tracker.task.exception.NotFoundException;
import io.tasks_tracker.task.repository.SubtaskRepository;
import io.tasks_tracker.task.repository.TaskRepository;

@Service
public class SubtaskService 
{
    @Autowired
    private CacheService cacheService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private SubtaskRepository subtaskRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Cacheable(value = "subtasks", key = "#id")
    public Subtask getSubtask(
            Authentication authentication,
            Long id
    ) throws NotFoundException, NoAccessException
    {
        Subtask subtask = cacheService.getSubtaskById(id);

        if(!taskService.hasAccess(subtask.getTask(), authentication)) {
            throw new NoAccessException("subtask", id);
        }
        return subtask;
    }

    public Subtask createSubtask(
            SubtaskCreateRequest subtask, 
            Authentication authentication
    ) throws NotFoundException, NoAccessException 
    {
        Subtask newSubtask = new Subtask();
        newSubtask.setTitle(subtask.getSubtask().getTitle());
        newSubtask.setCompleted(subtask.getSubtask().isCompleted());
        newSubtask.setCreatedBy(taskService.getUserId(authentication));

        Task task = taskService.getTask( 
            authentication,
            subtask.getTaskId()
        );
        newSubtask.setTask(task);

        Subtask savedSubtask = subtaskRepository.save(newSubtask);
        task.addSubtask(savedSubtask);
    
        cacheService.updateTaskCompletionStatus(task);
        return savedSubtask;
    }

    @CachePut(value = "subtasks", key = "#id")
    public Subtask markSubtask(
            Long id, 
            boolean isCompleted, 
            Authentication authentication
    ) throws NotFoundException, NoAccessException 
    {
        Subtask subtask = getSubtask(authentication, id);
        subtask.setCompleted(isCompleted);
        
        Subtask resSubtask = subtaskRepository.save(subtask);
        cacheService.updateTaskCompletionStatus(cacheService.checkOnCacheInSubtask(resSubtask.getTask()));
        return resSubtask;
    }

    @CachePut(value = "subtasks", key = "#id")
    public Subtask updateSubtask(
            Long id, 
            SubtaskRequest subtask, 
            Authentication authentication
    ) throws NotFoundException, NoAccessException 
    {
        Subtask updateSubtask = getSubtask(authentication, id);
        updateSubtask.setTitle(subtask.getTitle());
        updateSubtask.setCompleted(subtask.isCompleted());

        Subtask resSubtask = subtaskRepository.save(updateSubtask);
        cacheService.updateTaskCompletionStatus(cacheService.checkOnCacheInSubtask(resSubtask.getTask()));
        return resSubtask;
    }

    @CacheEvict(value = "subtasks", key = "#id")
    public void deleteSubtask(
            Long id, 
            Authentication authentication
    ) throws NotFoundException, NoAccessException 
    {
        Subtask subtask = getSubtask(authentication, id);
        Task task = cacheService.getTaskById(subtask.getTask().getId());
        task.removeSubtask(subtask);
        taskRepository.save(task);
        cacheService.updateTaskCompletionStatus(task);
    }
}
