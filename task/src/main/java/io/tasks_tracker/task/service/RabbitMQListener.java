package io.tasks_tracker.task.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.tasks_tracker.task.config.RabbitMQConfig;
import io.tasks_tracker.task.repository.TaskRepository;

@Service
public class RabbitMQListener 
{
    @Autowired
    private TaskRepository taskRepository;

    @RabbitListener(queues = RabbitMQConfig.DELETE_QUEUE_NAME)
    public void handlerUserDelete(String username)
    {
        taskRepository.deleteAllByCreatedBy(username);
    }
}
