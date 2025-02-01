package io.tasks_tracker.task.service;

import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import io.tasks_tracker.task.config.RabbitMQConfig;
import io.tasks_tracker.task.entity.Task;
import io.tasks_tracker.task.repository.TaskRepository;

@Service
public class RabbitMQListener 
{
    private final int PAGE_SIZE;
    private final CacheService cacheService;
    private final TaskRepository taskRepository;

    public RabbitMQListener(
        @Value("${request.delete.add.createdby:1000}") int PAGE_SIZE,
        CacheService cacheService,
        TaskRepository taskRepository
    ) {
        this.PAGE_SIZE = PAGE_SIZE;
        this.cacheService = cacheService;
        this.taskRepository = taskRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.DELETE_QUEUE_NAME)
    public void handlerUserDelete(Long userId)
    {
        while(true)
        {
            List<Task> tasks = taskRepository.findByCreatedBy(userId, PageRequest.of(0, PAGE_SIZE));

            if(tasks.isEmpty()) {
                break;
            }

            tasks.forEach(task -> cacheService.evictTaskFromCache(task));
            tasks.forEach(task -> task.getSubtasks().forEach(subtasks -> cacheService.evictSubtaskFromCache(subtasks)));

            taskRepository.deleteAll(tasks);
        }
    }
}
