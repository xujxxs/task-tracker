package io.tasks_tracker.task.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.tasks_tracker.task.dto.subtask.SubtaskCreateRequest;
import io.tasks_tracker.task.dto.subtask.SubtaskRequest;
import io.tasks_tracker.task.entity.Subtask;
import io.tasks_tracker.task.entity.Task;
import io.tasks_tracker.task.exception.NoAccessException;
import io.tasks_tracker.task.repository.SubtaskRepository;
import io.tasks_tracker.task.repository.TaskRepository;

@ExtendWith(SpringExtension.class)
@WithMockUser(authorities = {"USER"})
class SubtaskServiceTests 
{
    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private CacheService cacheService;

    @Mock
    private TaskService taskService;

    @Mock
    private SubtaskRepository subtaskRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private SubtaskService subtaskService;

    private Authentication authentication;
    private Subtask testSubtask;
    private Task linkedTaskToTestSubtask;

    private final Long userId = 1L;
    private final Long taskId = 1L;
    private final Long subtaskId = 2L;

    @BeforeEach
    void setUp()
    {
        testSubtask = Subtask.builder()
                .id(subtaskId)
                .createdBy(userId)
                .createdAt(LocalDateTime.now())
            .build();
        
        linkedTaskToTestSubtask = Task.builder()
                .createdBy(userId)
            .build();

        linkedTaskToTestSubtask.addSubtask(testSubtask);

        authentication = SecurityContextHolder.getContext().getAuthentication();
        when(authenticationService.getUserId(authentication)).thenReturn(userId);
    }

    private void setUpGetSubtaskForTests()
    {
        when(cacheService.getSubtaskById(subtaskId)).thenReturn(testSubtask);
        when(taskService.hasAccess(linkedTaskToTestSubtask, authentication)).thenReturn(true);
    }

    private void setUpToUpdateTests()
    {
        setUpGetSubtaskForTests();
        when(subtaskRepository.save(any(Subtask.class))).thenReturn(testSubtask);
        when(cacheService.checkOnCacheInSubtask(linkedTaskToTestSubtask)).thenReturn(linkedTaskToTestSubtask);
        when(cacheService.updateTaskCompletionStatus(linkedTaskToTestSubtask)).thenReturn(linkedTaskToTestSubtask);
    }

    @Test
    void getSubtask_hasAccessToTask_ReturnsSubtask()
    {
        when(cacheService.getSubtaskById(subtaskId)).thenReturn(testSubtask);
        when(taskService.hasAccess(linkedTaskToTestSubtask, authentication)).thenReturn(true);

        Subtask result = subtaskService.getSubtask(authentication, subtaskId);

        assertNotNull(result);
    }

    @Test
    void getSubtask_noAccessToTask_ThrowsNoAccessException()
    {
        when(cacheService.getSubtaskById(subtaskId)).thenReturn(testSubtask);
        when(taskService.hasAccess(linkedTaskToTestSubtask, authentication)).thenReturn(false);

        assertThrows(NoAccessException.class, () -> subtaskService.getSubtask(authentication, subtaskId));
    }

    @Test
    void createSubtask_hasAccessToTask_ReturnsSubtask()
    {
        SubtaskCreateRequest request = SubtaskCreateRequest.builder()
                .taskId(taskId)
                .subtask(SubtaskRequest.builder().isCompleted(true).build())
            .build();

        Subtask expect = Subtask.builder()
                .isCompleted(true)
                .task(linkedTaskToTestSubtask)
            .build();

        when(taskService.getTask(authentication, taskId)).thenReturn(linkedTaskToTestSubtask);
        when(subtaskRepository.save(any(Subtask.class))).thenReturn(expect);
        when(cacheService.updateTaskCompletionStatus(linkedTaskToTestSubtask)).thenReturn(linkedTaskToTestSubtask);

        Subtask result = subtaskService.createSubtask(request, authentication);
        
        assertNotNull(result);
        assertTrue(result.isCompleted());

        verify(taskService).getTask(authentication, taskId);
        verify(subtaskRepository).save(any(Subtask.class));
        verify(cacheService).updateTaskCompletionStatus(linkedTaskToTestSubtask);
    }

    @Test
    void createSubtask_noAccessToTask_ThrowsNoAccessException()
    {
        SubtaskCreateRequest request = SubtaskCreateRequest.builder().taskId(taskId).build();
        
        when(taskService.getTask(authentication, taskId)).thenThrow(new NoAccessException("task", taskId));

        assertThrows(NoAccessException.class, () -> subtaskService.createSubtask(request, authentication));
    }

    @Test
    void markSubtask_hasAccessToTask_ReturnsSubtask()
    {
        setUpToUpdateTests();

        Subtask result = subtaskService.markSubtask(subtaskId, true, authentication);

        assertNotNull(result);
        assertTrue(result.isCompleted());
    }

    @Test
    void updateSubtask_hasAccessToTask_ReturnsSubtask()
    {
        String testTitle = "Test title to update";
        SubtaskRequest request = SubtaskRequest.builder()
                .title(testTitle)
                .isCompleted(true)
            .build();
        
        setUpToUpdateTests();

        Subtask result = subtaskService.updateSubtask(subtaskId, request, authentication);

        assertNotNull(result);
        assertEquals(request.getTitle(), result.getTitle());
        assertTrue(result.isCompleted());
    }

    @Test
    void deleteSubtask_hasAccessToTask_ReturnsSubtask()
    {
        setUpGetSubtaskForTests();
        when(cacheService.getTaskById(testSubtask.getTask().getId())).thenReturn(linkedTaskToTestSubtask);
        when(taskRepository.save(any(Task.class))).thenReturn(linkedTaskToTestSubtask);
        when(cacheService.updateTaskCompletionStatus(any(Task.class))).thenReturn(linkedTaskToTestSubtask);

        subtaskService.deleteSubtask(subtaskId, authentication);

        verify(taskRepository, times(1)).save(linkedTaskToTestSubtask);
        assertEquals(0, linkedTaskToTestSubtask.getSubtasks().size());
    }
}
