package io.tasks_tracker.task.controller;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.tasks_tracker.task.dto.subtask.SubtaskCreateRequest;
import io.tasks_tracker.task.dto.subtask.SubtaskRequest;
import io.tasks_tracker.task.entity.Subtask;
import io.tasks_tracker.task.service.SubtaskService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Slf4j
@RestController
@RequestMapping("/api/subtasks")
public class SubtaskController 
{
    private final SubtaskService subtaskService;

    public SubtaskController(SubtaskService subtaskService)
    {
        this.subtaskService = subtaskService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Subtask> getSubtask(
        @PathVariable Long id,
        Authentication authentication
    ) {
        log.info("Initialing fetching subtask by id: {}", id);
        Subtask subtask = subtaskService.getSubtask(authentication, id);

        log.debug("Subtask fetchind successfully by id: {}", id);
        return ResponseEntity
                .ok()
                .body(subtask);
    }

    @PostMapping
    public ResponseEntity<Subtask> createSubtask(
        @RequestBody SubtaskCreateRequest subtask,
        Authentication authentication
    ) {
        log.info("Initialing create subtask");
        Subtask newSubtask = subtaskService.createSubtask(subtask, authentication);

        log.debug("Create subtask successfully, subtask id: {}", newSubtask.getId());
        return ResponseEntity
                .created(URI.create("/api/subtasks/" + newSubtask.getId().toString()))
                .body(newSubtask);
    }

    @PutMapping("/{id}/mark")
    public ResponseEntity<Subtask> markSubtask(
        @PathVariable Long id, 
        @RequestBody boolean isCompleted,
        Authentication authentication
    ) {
        log.info("Initialing mark subtask: {}", id);
        Subtask subtask = subtaskService.markSubtask(id, isCompleted, authentication);

        log.debug("Marking successfully for subtask: {}", id);
        return ResponseEntity
                .ok()
                .body(subtask);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Subtask> updateSubtask(
        @PathVariable Long id, 
        @RequestBody SubtaskRequest request,
        Authentication authentication
    ) {
        log.info("Initialing update subtask: {}", id);
        Subtask subtask = subtaskService.updateSubtask(id, request, authentication);

        log.debug("Update successfully for subtask: {}", id);
        return ResponseEntity
                .ok()
                .body(subtask);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubtask(
        Authentication authentication,
        @PathVariable Long id
    ) {
        log.info("Initialing delete subtask: {}", id);
        subtaskService.deleteSubtask(id, authentication);

        log.debug("Delete successfully for subtask: {}", id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
