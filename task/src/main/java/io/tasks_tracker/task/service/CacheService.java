package io.tasks_tracker.task.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private SubtaskRepository subtaskRepository;
   
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
    public void evictTaskFromCache(Task task) { }

    @CacheEvict(value = "subtasks", key = "#subtask.id")
    public void evictSubtaskFromCache(Subtask subtask) { }

    public Task checkOnCacheInSubtask(Task task)
    {
        return task.getCreatedAt() == null
                ? getTaskById(task.getId())
                : task;
    }

    @CachePut(value = "tasks", key = "#task.id")
    public Task updateTaskCompletionStatus(Task task) 
    {
        System.out.println(task.getSubtasks().size());
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
