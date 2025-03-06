package io.tasks_tracker.task.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.tasks_tracker.task.dto.subtask.SubtaskCreateRequest;
import io.tasks_tracker.task.dto.subtask.SubtaskRequest;
import io.tasks_tracker.task.entity.Subtask;
import io.tasks_tracker.task.exception.NoAccessException;
import io.tasks_tracker.task.exception.NotFoundException;
import io.tasks_tracker.task.service.SubtaskService;

@ExtendWith(MockitoExtension.class)
class SubtaskControllerTests 
{
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SubtaskService subtaskService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private SubtaskController subtaskController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(subtaskController)
                .setControllerAdvice(new AdviceExceptionHandler())
            .build();
    }

    @Test
    void getSubask_hasAccess() throws Exception
    {
        Subtask testSubtask = Subtask.builder().id(1L).build();

        when(subtaskService.getSubtask(authentication, 1L)).thenReturn(testSubtask);

        mockMvc.perform(get("/api/subtasks/1")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getSubask_noAccess() throws Exception
    {
        when(subtaskService.getSubtask(authentication, 1L)).thenThrow(NoAccessException.class);

        mockMvc.perform(get("/api/subtasks/1")
                .principal(authentication))
                .andExpect(status().isForbidden());
    }

    @Test
    void getSubask_notFound() throws Exception
    {
        when(subtaskService.getSubtask(authentication, 1L)).thenThrow(NotFoundException.class);

        mockMvc.perform(get("/api/subtasks/1")
                .principal(authentication))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateSubtask() throws Exception
    {
        SubtaskCreateRequest request = SubtaskCreateRequest.builder().build();
        Subtask testSubtask = Subtask.builder().id(1L).build();

        when(subtaskService.createSubtask(request, authentication)).thenReturn(testSubtask);

        mockMvc.perform(post("/api/subtasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(authentication))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/subtasks/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testMarkSubtask() throws Exception
    {
        Subtask testSubtask = Subtask.builder().id(1L).build();

        when(subtaskService.markSubtask(1L, true, authentication)).thenReturn(testSubtask);

        mockMvc.perform(put("/api/subtasks/1/mark")
                .contentType(MediaType.APPLICATION_JSON)
                .content("true")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testUpdateSubtask() throws Exception
    {
        SubtaskRequest request = SubtaskRequest.builder().build();
        Subtask testSubtask = Subtask.builder().id(1L).build();

        when(subtaskService.updateSubtask(1L, request, authentication)).thenReturn(testSubtask);

        mockMvc.perform(put("/api/subtasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testDeleteSubtask() throws Exception
    {
        doNothing().when(subtaskService).deleteSubtask(1L, authentication);

        mockMvc.perform(delete("/api/subtasks/1")
                .principal(authentication))
                .andExpect(status().isNoContent());
    }
}
