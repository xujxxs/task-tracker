package io.tasks_tracker.task.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.tasks_tracker.task.dto.filter.DateFilterParams;
import io.tasks_tracker.task.dto.filter.ImportanceFilterParams;
import io.tasks_tracker.task.dto.filter.PaginationParams;
import io.tasks_tracker.task.dto.filter.TaskFilterParams;
import io.tasks_tracker.task.dto.subtask.SubtaskRequest;
import io.tasks_tracker.task.dto.task.TaskCreateRequest;
import io.tasks_tracker.task.dto.task.TaskRequest;
import io.tasks_tracker.task.entity.Subtask;
import io.tasks_tracker.task.entity.Task;
import io.tasks_tracker.task.exception.NoAccessException;
import io.tasks_tracker.task.repository.SubtaskRepository;
import io.tasks_tracker.task.repository.TaskRepository;

@ExtendWith(SpringExtension.class)
@WithMockUser(authorities = {"USER"})
class TaskServiceTests 
{
    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private CacheService cacheService;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private SubtaskRepository subtaskRepository;

    @InjectMocks
    private TaskService taskService;

    private Authentication authentication;
    private Task testTask;

    private final Long userId = 1L;
    private final Long taskId = 1L;

    @BeforeEach
    void setUp()
    {
        testTask = Task.builder().id(taskId).createdBy(userId).build();

        authentication = SecurityContextHolder.getContext().getAuthentication();
        when(authenticationService.getUserId(authentication)).thenReturn(userId);
    }

    @Test
    void getHasAccess_UserIsCreater_ReturnsTrue()
    {
        assertTrue(taskService.hasAccess(testTask, authentication));
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void getHasAccess_UserIsAdmin_ReturnsTrue()
    {
        testTask.setCreatedBy(userId + 1L);

        assertTrue(taskService.hasAccess(testTask, authentication));
    }

    @Test
    void getHasAccess_NoAccess_ReturnsFalse()
    {     
        testTask.setCreatedBy(userId + 1L);

        assertFalse(taskService.hasAccess(testTask, authentication));
    }

    @Test
    void getTask_hasAccess_ReturnsTask()
    {
        when(cacheService.getTaskById(taskId)).thenReturn(testTask);

        assertEquals(testTask, taskService.getTask(authentication, taskId));
    }

    @Test
    void getTask_NoAccess_ThrowsNoAccessException()
    {
        testTask.setCreatedBy(userId + 1L);

        when(cacheService.getTaskById(taskId)).thenReturn(testTask);

        assertThrows(NoAccessException.class, () -> taskService.getTask(authentication, taskId));
    }

    @Test
    void getTasks_DefaultParams_ReturnsPage()
    {
        PaginationParams pagination = new PaginationParams(1, 10, "title", "asc");

        TaskFilterParams filters = new TaskFilterParams(
            null, null, new DateFilterParams(null, null, null), false,
            new DateFilterParams(null, null, null), 
            new DateFilterParams(null, null, null), 
            new DateFilterParams(null, null, null),  true, 
            new ImportanceFilterParams(null, null, null), userId
        );
        Page<Task> expectedPage = new PageImpl<>(List.of(testTask));

        when(taskRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(expectedPage);

        Page<Task> result = taskService.getTasks(pagination, filters);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testTask, result.getContent().get(0));

    }

    @Test
    void createTask_ReturnsTask()
    {
        String taskTitle = "Task title";
        String subtaskTitle = "Subtask title";
        testTask.setTitle(taskTitle);

        TaskCreateRequest request = TaskCreateRequest.builder()
                .task(TaskRequest.builder().title(taskTitle).build())
                .subtasks(List.of(
                    SubtaskRequest.builder().title(subtaskTitle + "1").build(),
                    SubtaskRequest.builder().title(subtaskTitle + "2").build()))
            .build();

        List.of(
            Subtask.builder().id(1L).title(subtaskTitle + "1").task(testTask).createdBy(userId).build(),
            Subtask.builder().id(2L).title(subtaskTitle + "2").task(testTask).createdBy(userId).build()
        ).forEach(subtask -> {
            testTask.addSubtask(subtask);
            when(subtaskRepository.save(
                Subtask.builder()
                    .title(subtask.getTitle())
                    .task(testTask)
                    .createdBy(userId)
                .build()
            )).thenReturn(subtask);
        });

        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(cacheService.updateTaskCompletionStatus(testTask)).thenReturn(testTask);

        Task result = taskService.createTask(request, userId);

        assertNotNull(result);
        assertEquals(testTask, result);
        verify(subtaskRepository, times(2)).save(any(Subtask.class));
    }

    @Test
    void updateTask_hasAccess_ReturnsTask()
    {
        String newTitle = "Updated title";
        TaskRequest taskRequest = TaskRequest.builder().title(newTitle).build();

        when(cacheService.getTaskById(taskId)).thenReturn(testTask);
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        Task result = taskService.updateTask(taskId, taskRequest, authentication);
        
        assertNotNull(result);
        assertEquals(taskRequest.getTitle(), result.getTitle());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void deleteTask_hasAccess_ReturnsVoid()
    {
        testTask.addSubtask(Subtask.builder().task(testTask).build());

        when(cacheService.getTaskById(taskId)).thenReturn(testTask);
        doNothing().when(taskRepository).delete(testTask);

        taskService.deleteTask(authentication, taskId);

        verify(cacheService, times(1)).evictSubtaskFromCache(any(Subtask.class));
        verify(taskRepository, times(1)).delete(testTask);
    }
}
