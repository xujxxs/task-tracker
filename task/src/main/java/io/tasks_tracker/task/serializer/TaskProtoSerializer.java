package io.tasks_tracker.task.serializer;

import io.tasks_tracker.task.proto.ProtoEntityTypes;
import io.tasks_tracker.task.entity.Task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.io.IOException;
import java.time.LocalDateTime;

public class TaskProtoSerializer implements RedisSerializer<Task> 
{
    @Autowired
    private SubtaskProtoSerializer subtaskProtoSerializer;

    public void setSubtasksProtoSerializer(SubtaskProtoSerializer subtaskProtoSerializer)
    {
        this.subtaskProtoSerializer = subtaskProtoSerializer;
    }

    @Override
    public byte[] serialize(Task task) 
    {
        if (task == null) {
            return new byte[0];
        }
        ProtoEntityTypes.Task proto = ProtoEntityTypes.Task.newBuilder()
                .setId(task.getId())
                .setTitle(task.getTitle())
                .setDescription(task.getDescription())
                .setCategory(task.getCategory())
                .setDateEnd(task.getDateEnd() != null ? task.getDateEnd().toString() : "")
                .setImportance(task.getImportance())
                .setCreatedBy(task.getCreatedBy())
                .setCreatedAt(task.getCreatedAt().toString())
                .setRedactedAt(task.getRedactedAt() != null ? task.getRedactedAt().toString() : "")
                .setEndedAt(task.getEndedAt() != null ? task.getEndedAt().toString() : "")
                .addAllSubtasks(
                    task.getSubtasks().stream()
                        .map(subtask -> subtaskProtoSerializer.toSubtaskProto(subtask))
                        .toList()
                )
                .build();

        return proto.toByteArray();
    }

    @Override
    public Task deserialize(byte[] bytes) 
    {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            ProtoEntityTypes.Task proto = ProtoEntityTypes.Task.parseFrom(bytes);
            Task task = new Task();
            task.setId(proto.getId());
            task.setTitle(proto.getTitle());
            task.setDescription(proto.getDescription());
            task.setCategory(proto.getCategory());
            task.setDateEnd(proto.getDateEnd() != "" ? LocalDateTime.parse(proto.getDateEnd()) : null);
            task.setImportance(proto.getImportance());
            task.setCreatedBy(proto.getCreatedBy());
            task.setCreatedAt(LocalDateTime.parse(proto.getCreatedAt()));
            task.setRedactedAt(proto.getRedactedAt() != "" ? LocalDateTime.parse(proto.getRedactedAt()) : null);
            task.setEndedAt(proto.getEndedAt() != "" ? LocalDateTime.parse(proto.getEndedAt()) : null);

            proto.getSubtasksList().forEach(protoSubtask -> {
                task.addSubtask(subtaskProtoSerializer.toSubtask(protoSubtask));
            });

            return task;
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize TaskProto", e);
        }
    }
}
