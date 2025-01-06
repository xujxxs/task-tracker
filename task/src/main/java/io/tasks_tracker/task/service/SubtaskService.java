package io.tasks_tracker.task.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import io.tasks_tracker.task.dto.SubtaskCreateRequest;
import io.tasks_tracker.task.dto.SubtaskRequest;
import io.tasks_tracker.task.entity.Subtask;
import io.tasks_tracker.task.entity.Task;
import io.tasks_tracker.task.exception.NoAccessException;
import io.tasks_tracker.task.exception.NotFoundException;
import io.tasks_tracker.task.repository.SubtaskRepository;

@Service
public class SubtaskService 
{
    @Autowired
    private SubtaskRepository subtaskRepository;

    @Autowired
    private TaskService taskService;

    public Subtask getSubtaskById(
            Authentication authentication,
            Long id
    ) throws NotFoundException, NoAccessException
    {
        Subtask subtask = subtaskRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Subtask", id));

        if(!taskService.hasAccess(subtask.getTask(), authentication)) {
            throw new NoAccessException("subtask", id);
        }
        return subtask;
    }

    public Subtask createSubtask(
            SubtaskCreateRequest subtask, 
            Authentication authentication
    ) throws NotFoundException, NoAccessException 
    {
        Subtask newSubtask = new Subtask();
        newSubtask.setTitle(subtask.getSubtask().getTitle());
        newSubtask.setCompleted(subtask.getSubtask().isCompleted());
        newSubtask.setCreatedBy(authentication.getName());

        Task task = taskService.getTaskById( 
            authentication,
            subtask.getTaskId()
        );
        newSubtask.setTask(task);

        Subtask savedSubtask = subtaskRepository.save(newSubtask);
        taskService.checkAndSetCompleted(savedSubtask.getTask());
        return savedSubtask;
    }

    public Subtask markSubtask(
            Long id, 
            boolean isCompleted, 
            Authentication authentication
    ) throws NotFoundException, NoAccessException 
    {
        Subtask subtask = getSubtaskById(authentication, id);
        subtask.setCompleted(isCompleted);
        
        taskService.checkAndSetCompleted(subtask.getTask());
        return subtaskRepository.save(subtask);
    }

    public Subtask updateSubtask(
            Long id, 
            SubtaskRequest subtask, 
            Authentication authentication
    ) throws NotFoundException, NoAccessException 
    {
        Subtask updateSubtask = getSubtaskById(authentication, id);
        updateSubtask.setTitle(subtask.getTitle());
        updateSubtask.setCompleted(subtask.isCompleted());

        taskService.checkAndSetCompleted(updateSubtask.getTask());
        return subtaskRepository.save(updateSubtask);
    }

    public void deleteSubtask(
            Long id, 
            Authentication authentication
    ) throws NotFoundException, NoAccessException 
    {
        Subtask subtask = getSubtaskById(authentication, id);
        Task task = subtask.getTask();

        subtaskRepository.deleteById(id);
        taskService.checkAndSetCompletedWithOutSubtaskId(task, id);
    }
}
