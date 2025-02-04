package io.tasks_tracker.task.service;

import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import io.tasks_tracker.task.config.RabbitMQConfig;
import io.tasks_tracker.task.entity.Task;
import io.tasks_tracker.task.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        log.info("Starting deletion of all tasks created by user: {}", userId);

        for(Long iteration = Long.valueOf(0); true; ++iteration)
        {
            log.debug("Iteration {}: Fetching tasks page with size: {}", iteration, PAGE_SIZE);
            List<Task> tasks = taskRepository.findByCreatedBy(userId, PageRequest.of(0, PAGE_SIZE));
            log.debug("Found {} tasks in iteration {}", tasks.size(), iteration);

            if(tasks.isEmpty()) {
                log.debug("Empty list at iteration {}", iteration);
                break;
            }

            log.debug("Evicting {} tasks and linked subtasks", tasks.size());
            tasks.forEach(task -> {
                cacheService.evictTaskFromCache(task);
                task.getSubtasks().forEach(cacheService::evictSubtaskFromCache);
            });

            log.info("Deleting tasks from database in iteration {}", iteration);
            taskRepository.deleteAll(tasks);
            log.debug("Iteration {} completed; tasks deleted.", iteration);
        }
        log.info("Deletion of all tasks created by user {} completed successfully.", userId);
    }
}
