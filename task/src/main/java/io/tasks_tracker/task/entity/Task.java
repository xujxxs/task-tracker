package io.tasks_tracker.task.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table
@Data
public class Task implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = true)
    private String description;

    @Column(nullable = true)
    private String category;

    @OneToMany(
        mappedBy = "task", 
        fetch = FetchType.EAGER,
        cascade = CascadeType.ALL, 
        orphanRemoval = true
    )
    @JsonManagedReference
    private List<Subtask> subtasks = new ArrayList<>();
    
    @Column(nullable = true)
    private LocalDateTime dateEnd;

    @Column(nullable = false)
    private Long importance;

    @Column(nullable = false)
    private String createdBy;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime redactedAt;
    
    @Column(nullable = true)
    private LocalDateTime endedAt;

    public void addSubtask(Subtask subtask)
    {
        subtasks.add(subtask);
        subtask.setTask(this);
    }

    public void removeSubtask(Subtask subtask)
    {
        subtasks.removeIf(s -> s.getId().equals(subtask.getId()));
        subtask.setTask(null);
    }
}
