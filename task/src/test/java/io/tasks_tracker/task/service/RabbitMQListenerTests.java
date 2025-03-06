package io.tasks_tracker.task.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import io.tasks_tracker.task.entity.Task;
import io.tasks_tracker.task.repository.TaskRepository;

@ExtendWith(MockitoExtension.class)
class RabbitMQListenerTests 
{
    private static final int PAGE_SIZE = 2;

    @Mock
    private CacheService cacheService;

    @Mock
    private TaskRepository taskRepository;

    private RabbitMQListener rabbitMQListener;

    private final Long userId = 1L;

    @BeforeEach
    void setUp()
    {
        rabbitMQListener = new RabbitMQListener(PAGE_SIZE, cacheService, taskRepository);
    }

    @Test
    void handlerUserDelete_NoTasks_ShouldNotDeleteOrEvict()
    {
        when(taskRepository.findByCreatedBy(userId, PageRequest.of(0, PAGE_SIZE)))
            .thenReturn(Collections.emptyList());
        
        rabbitMQListener.handlerUserDelete(userId);

        verify(taskRepository, times(1)).findByCreatedBy(userId, PageRequest.of(0, PAGE_SIZE));
        verifyNoInteractions(cacheService);
    }

    @Test
    void handlerUserDelete_WithTasks_ShouldNotDeleteOrEvict()
    {
        Task task1 = mock(Task.class);
        Task task2 = mock(Task.class);
        List<Task> tasks = List.of(task1, task2);

        when(taskRepository.findByCreatedBy(userId, PageRequest.of(0, PAGE_SIZE)))
            .thenReturn(tasks)
            .thenReturn(Collections.emptyList());

        rabbitMQListener.handlerUserDelete(userId);

        verify(taskRepository, times(2)).findByCreatedBy(userId, PageRequest.of(0, PAGE_SIZE));
        verify(taskRepository, times(1)).deleteAll(tasks);
        verify(cacheService, times(1)).evictTaskFromCache(task1);
        verify(cacheService, times(1)).evictTaskFromCache(task2);
    }
}
