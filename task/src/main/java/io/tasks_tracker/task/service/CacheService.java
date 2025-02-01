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
        return taskRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Task", id));
    }

    @Cacheable(value = "subtasks", key = "#id")
    public Subtask getSubtaskById(Long id)
    {
        return subtaskRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Subtask", id));
    }

    @CacheEvict(value = "tasks", key = "#task.id") 
    public void evictTaskFromCache(Task task) { 
        /*
         * This method is needed to remove task from cache.
         */
    }

    @CacheEvict(value = "subtasks", key = "#subtask.id")
    public void evictSubtaskFromCache(Subtask subtask) { 
        /*
         * This method is needed to remove subtask from cache.
         */
    }

    public Task checkOnCacheInSubtask(Task task)
    {
        return task.getCreatedAt() == null
                ? getTaskById(task.getId())
                : task;
    }

    @CachePut(value = "tasks", key = "#task.id")
    public Task updateTaskCompletionStatus(Task task) 
    {
        if(!task.getSubtasks().isEmpty()
                && task.getSubtasks().stream().allMatch(Subtask::isCompleted)) {
            task.setEndedAt(LocalDateTime.now());
            return taskRepository.save(task);
        }
        else {
            LocalDateTime timeEnd = task.getEndedAt();
            if(timeEnd != null) {
                task.setEndedAt(null);
                return taskRepository.save(task);
            }
        }
        return task;
    }
}
