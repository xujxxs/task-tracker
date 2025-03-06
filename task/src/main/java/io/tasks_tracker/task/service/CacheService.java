package io.tasks_tracker.task.service;

import java.time.LocalDateTime;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import io.tasks_tracker.task.entity.Subtask;
import io.tasks_tracker.task.entity.Task;
import io.tasks_tracker.task.exception.NotFoundException;
import io.tasks_tracker.task.repository.SubtaskRepository;
import io.tasks_tracker.task.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CacheService 
{
    private final TaskRepository taskRepository;
    private final SubtaskRepository subtaskRepository;

    public CacheService(
        TaskRepository taskRepository,
        SubtaskRepository subtaskRepository
    ) {
        this.taskRepository = taskRepository;
        this.subtaskRepository = subtaskRepository;
    }
   
    @Cacheable(value = "tasks", key = "#id")
    public Task getTaskById(Long id)
    {
        log.debug("Task: {} not found in cache. Fetching in database", id);
        return taskRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Task", id));
    }

    @Cacheable(value = "subtasks", key = "#id")
    public Subtask getSubtaskById(Long id)
    {
        log.debug("Subtask: {} not found in cache. Fetching in database", id);
        return subtaskRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Subtask", id));
    }

    @CacheEvict(value = "tasks", key = "#task.id") 
    public void evictTaskFromCache(Task task) 
    {
        log.debug("Evict task: {} from cache", task.getId()); 
    }

    @CacheEvict(value = "subtasks", key = "#subtask.id")
    public void evictSubtaskFromCache(Subtask subtask) 
    { 
        log.debug("Evict subtask: {} from cache", subtask.getId()); 
    }

    public Task checkOnCacheInSubtask(Task task)
    {
        log.debug("Checking if task: {} is entity from cache", task.getId());

        if (task.getCreatedAt() == null) {
            log.debug("Task: {} is from cache, fetching from database", task.getId());
            return getTaskById(task.getId());
        }
        log.debug("Task: {} is not from cache", task.getId());
        return task;
    }

    @CachePut(value = "tasks", key = "#task.id")
    public Task updateTaskCompletionStatus(Task task) 
    {
        log.debug("Starting update task: {} to change complete status", task.getId());
        if(!task.getSubtasks().isEmpty() && task.getSubtasks().stream().allMatch(Subtask::isCompleted)) 
        {
            log.debug("Marking task: {} as COMPLETED", task.getId());
            task.setEndedAt(LocalDateTime.now());
        }
        else if(task.getEndedAt() != null) {
            log.debug("Marking task: {} as IN_PROGRESS", task.getId());
            task.setEndedAt(null);
        }

        final Task updatedTask = taskRepository.save(task);
        log.debug("Persisted task {} status: {}", 
            task.getId(), updatedTask.getEndedAt() != null ? "COMPLETED" : "IN_PROGRESS");

        return task;
    }
}
