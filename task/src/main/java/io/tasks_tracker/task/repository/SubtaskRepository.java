package io.tasks_tracker.task.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.tasks_tracker.task.entity.Subtask;

public interface SubtaskRepository extends JpaRepository<Subtask, Long> {

}
