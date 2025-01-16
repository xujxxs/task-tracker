package io.tasks_tracker.task.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import io.tasks_tracker.task.serializer.SubtaskProtoSerializer;
import io.tasks_tracker.task.serializer.TaskProtoSerializer;

@Configuration
@EnableCaching
public class CacheConfig 
{
    @Bean
    public SubtaskProtoSerializer subtaskProtoSerializer()
    {
        return new SubtaskProtoSerializer();
    }

    @Bean
    public TaskProtoSerializer taskProtoSerializer(SubtaskProtoSerializer subtaskProtoSerializer)
    {
        TaskProtoSerializer taskProtoSerializer = new TaskProtoSerializer();
        taskProtoSerializer.setSubtasksProtoSerializer(subtaskProtoSerializer);
        return taskProtoSerializer;
    }

    @Bean
    public RedisCacheManager cacheManager(
                RedisConnectionFactory redisConnectionFactory,
                TaskProtoSerializer taskProtoSerializer,
                SubtaskProtoSerializer subtaskProtoSerializer
    ) {
        RedisCacheConfiguration taskCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(taskProtoSerializer)
                );

        RedisCacheConfiguration subtaskCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(subtaskProtoSerializer)
                );

        return RedisCacheManager.builder(redisConnectionFactory)
                .withCacheConfiguration("tasks", taskCacheConfig)      
                .withCacheConfiguration("subtasks", subtaskCacheConfig)
                .build();
    }

}
