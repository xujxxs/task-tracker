package io.tasks_tracker.task.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.tasks_tracker.task.dto.task.TaskCreateRequest;
import io.tasks_tracker.task.dto.task.TaskRequest;
import io.tasks_tracker.task.entity.Task;
import io.tasks_tracker.task.exception.NoAccessException;
import io.tasks_tracker.task.exception.NotFoundException;
import io.tasks_tracker.task.service.AuthenticationService;
import io.tasks_tracker.task.service.TaskService;

@ExtendWith(MockitoExtension.class)
class TaskControllerTests 
{
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TaskService taskService;
    
    @Mock
    private Authentication authentication;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private TaskController taskController;

    @BeforeEach
    void setUp()
    {
        mockMvc = MockMvcBuilders
                .standaloneSetup(taskController)
                .setControllerAdvice(new AdviceExceptionHandler())
            .build();
    }

    @Test
    void getTask_hasAccess() throws Exception
    {
        Task testTask = Task.builder().id(1L).build();

        when(taskService.getTask(authentication, 1L)).thenReturn(testTask);

        mockMvc.perform(get("/api/tasks/1")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getTask_noAccess() throws Exception
    {
        when(taskService.getTask(authentication, 1L)).thenThrow(NoAccessException.class);

        mockMvc.perform(get("/api/tasks/1")
                .principal(authentication))
                .andExpect(status().isForbidden());
    }

    @Test
    void getTask_notFound() throws Exception
    {
        when(taskService.getTask(authentication, 1L)).thenThrow(NotFoundException.class);

        mockMvc.perform(get("/api/tasks/1")
                .principal(authentication))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetTasks() throws Exception
    {
        Task task = Task.builder().id(1L).title("Test Task").build();
        Page<Task> taskPage = new PageImpl<>(List.of(task), PageRequest.of(0, 10), 1);

        when(authenticationService.getUserId(authentication)).thenReturn(1L);
        when(taskService.getTasks(org.mockito.Mockito.any(), org.mockito.Mockito.any())).thenReturn(taskPage);

        mockMvc.perform(get("/api/tasks")
                .param("pageNumber", "1")
                .param("pageSize", "10")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Test Task"));
    }

    @Test
    void testCreateTask() throws Exception
    {
        TaskCreateRequest request = TaskCreateRequest.builder().build();
        Task testTask = Task.builder().id(1L).build();

        when(authenticationService.getUserId(authentication)).thenReturn(1L);
        when(taskService.createTask(request, 1L)).thenReturn(testTask);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(authentication))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/tasks/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testUpdateTask() throws Exception
    {
        TaskRequest request = TaskRequest.builder().build();
        Task testTask = Task.builder().id(1L).build();

        when(taskService.updateTask(1L, request, authentication)).thenReturn(testTask);

        mockMvc.perform(put("/api/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testDeleteTask() throws Exception
    {
        mockMvc.perform(delete("/api/tasks/1")
                .principal(authentication))
                .andExpect(status().isNoContent());
    }
}
