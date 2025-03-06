package io.tasks_tracker.task.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.tasks_tracker.task.entity.Subtask;
import io.tasks_tracker.task.entity.Task;
import io.tasks_tracker.task.exception.NotFoundException;
import io.tasks_tracker.task.repository.SubtaskRepository;
import io.tasks_tracker.task.repository.TaskRepository;

@ExtendWith(SpringExtension.class)
class CacheServiceTests 
{
    @Mock
    private TaskRepository taskRepository;

    @Mock
    private SubtaskRepository subtaskRepository;

    @InjectMocks
    private CacheService cacheService;

    private Task testTask;
    private Subtask testSubtask;
    private final Long taskId = 1L;
    private final Long subtaskId = 1L;
    private final Long userId = 1L;

    @BeforeEach
    void setUp()
    {
        testTask = Task.builder()
                .id(taskId)
                .createdBy(userId)
                .createdAt(LocalDateTime.now())
            .build();
        
        testSubtask = Subtask.builder()
                .id(subtaskId)
                .createdBy(userId)
                .createdAt(LocalDateTime.now())
                .task(testTask)
            .build();     
    }

    private Subtask createSubtask(boolean complete)
    {
        return Subtask.builder()
                .createdBy(userId)
                .createdAt(LocalDateTime.now())
                .isCompleted(complete)
            .build();
    }

    @Test
    void getTaskById_ReturnsTaskAndCacheIt()
    {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));

        Task result1 = cacheService.getTaskById(taskId);
        Task result2 = cacheService.getTaskById(taskId);

        assertEquals(result1, result2);
    }

    @Test
    void getTaskById_ThrowsNotFoundException()
    {
        when(taskRepository.findById(taskId + 1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> cacheService.getTaskById(taskId + 1L));
    }

    @Test
    void getSubtaskById_ReturnsSubaskAndCacheIt()
    {
        when(subtaskRepository.findById(subtaskId)).thenReturn(Optional.of(testSubtask));

        Subtask result1 = cacheService.getSubtaskById(subtaskId);
        Subtask result2 = cacheService.getSubtaskById(subtaskId);

        assertEquals(result1, result2);
    }

    @Test
    void getSubtaskById_ThrowsNotFoundException()
    {
        when(subtaskRepository.findById(subtaskId + 1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> cacheService.getSubtaskById(subtaskId + 1L));
    }

    @Test
    void checkOnCacheInSubtask_TaskFromCachedSubtask_ReturnsTask()
    {
        testTask.setCreatedAt(null);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));

        cacheService.checkOnCacheInSubtask(testTask);

        verify(taskRepository, times(1)).findById(taskId);
    }

    @Test
    void checkOnCacheInSubtask_TaskFromDatabase_ReturnsTask()
    {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));

        cacheService.checkOnCacheInSubtask(testTask);

        verify(taskRepository, times(0)).findById(taskId);
    }

    @Test
    void updateTaskCompletionStatus_NoSubtasks_ReturnsTask()
    {
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        Task result = cacheService.updateTaskCompletionStatus(testTask);

        assertNull(result.getEndedAt());
        verify(taskRepository).save(testTask);
    }

    @Test
    void updateTaskCompletionStatus_OneSubtaskNotCompleted_ReturnsTask()
    {
        testTask.setSubtasks(List.of(createSubtask(false), createSubtask(true)));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        Task result = cacheService.updateTaskCompletionStatus(testTask);

        assertNull(result.getEndedAt());
        verify(taskRepository).save(testTask);
    }

    @Test
    void updateTaskCompletionStatus_AllSubtaskCompleted_ReturnsTask()
    {
        testTask.setSubtasks(List.of(createSubtask(true), createSubtask(true)));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        Task result = cacheService.updateTaskCompletionStatus(testTask);

        assertNotNull(result.getEndedAt());
        verify(taskRepository).save(testTask);
    }
}
