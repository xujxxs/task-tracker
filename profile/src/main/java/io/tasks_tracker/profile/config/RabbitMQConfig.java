package io.tasks_tracker.profile.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig 
{
    public static final String EXCHANGE_NAME = "user-exchange";
    public static final String DELETE_QUEUE_NAME = "user-delete-queue";

    @Bean
    public TopicExchange exchange()
    {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue deleteQueue()
    {
        return new Queue(DELETE_QUEUE_NAME, true);
    }

    @Bean
    public Binding deleteBinding(Queue deleteQueue, TopicExchange exchange)
    {
        return BindingBuilder.bind(deleteQueue).to(exchange).with("user.delete");
    }
}
