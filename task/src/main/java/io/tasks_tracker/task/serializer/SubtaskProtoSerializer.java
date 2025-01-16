package io.tasks_tracker.task.serializer;

import io.tasks_tracker.task.proto.ProtoEntityTypes;
import io.tasks_tracker.task.entity.Subtask;
import io.tasks_tracker.task.entity.Task;

import org.springframework.data.redis.serializer.RedisSerializer;

import java.io.IOException;
import java.time.LocalDateTime;

public class SubtaskProtoSerializer implements RedisSerializer<Subtask> 
{
    public ProtoEntityTypes.Task toMinProtoTask(Task task)
    {
        return ProtoEntityTypes.Task.newBuilder()
                .setId(task.getId())
                .setCreatedBy(task.getCreatedBy())
                .build();
    }

    public ProtoEntityTypes.Subtask toSubtaskProto(Subtask subtask)
    {
        return ProtoEntityTypes.Subtask.newBuilder()
                .setId(subtask.getId())
                .setTitle(subtask.getTitle())
                .setCreatedBy(subtask.getCreatedBy())
                .setCreatedAt(subtask.getCreatedAt().toString())
                .setRedactedAt(subtask.getRedactedAt().toString())
                .setIsCompleted(subtask.isCompleted())
                .setTask(toMinProtoTask(subtask.getTask()))
                .build();
    }

    @Override
    public byte[] serialize(Subtask subtask) 
    {
        if (subtask == null) {
            return new byte[0];
        }
        return toSubtaskProto(subtask).toByteArray();
    }

    public Task toMinTask(ProtoEntityTypes.Task proto)
    {
        Task task = new Task();
        task.setId(proto.getId());
        task.setCreatedBy(proto.getCreatedBy());
        return task;
    }

    public Subtask toSubtask(ProtoEntityTypes.Subtask proto)
    {
        Subtask subtask = new Subtask();
        subtask.setId(proto.getId());
        subtask.setTitle(proto.getTitle());
        subtask.setCompleted(proto.getIsCompleted());
        subtask.setCreatedBy(proto.getCreatedBy());
        subtask.setCreatedAt(LocalDateTime.parse(proto.getCreatedAt()));
        subtask.setRedactedAt(proto.getRedactedAt() == "" ? null : LocalDateTime.parse(proto.getRedactedAt()));
        subtask.setTask(toMinTask(proto.getTask()));
        return subtask;
    }

    @Override
    public Subtask deserialize(byte[] bytes) 
    {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return toSubtask(ProtoEntityTypes.Subtask.parseFrom(bytes));
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize SubtaskProto", e);
        }
    }
}
