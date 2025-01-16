package io.tasks_tracker.task.controller;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.tasks_tracker.task.dto.SubtaskCreateRequest;
import io.tasks_tracker.task.dto.SubtaskRequest;
import io.tasks_tracker.task.entity.Subtask;
import io.tasks_tracker.task.service.SubtaskService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/subtasks")
public class SubtaskController 
{
    @Autowired
    private SubtaskService subtaskService;

    @GetMapping("/{id}")
    public ResponseEntity<Subtask> getSubtask(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity
                .ok()
                .body(subtaskService.getSubtask(authentication, id));
    }

    @PostMapping
    public ResponseEntity<Subtask> createSubtask(
            @RequestBody SubtaskCreateRequest subtask,
            Authentication authentication
    ) {
        Subtask newSubtask = subtaskService.createSubtask(subtask, authentication);
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
        return ResponseEntity
                .ok()
                .body(subtaskService.markSubtask(
                    id, 
                    isCompleted, 
                    authentication
                ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Subtask> updateSubtask(
            @PathVariable Long id, 
            @RequestBody SubtaskRequest subtask,
            Authentication authentication
    ) {
        return ResponseEntity
                .ok()
                .body(subtaskService.updateSubtask(
                    id, 
                    subtask, 
                    authentication
                ));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSubtask(
            Authentication authentication,
            @PathVariable Long id
    ) {
        subtaskService.deleteSubtask(id, authentication);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
