package io.tasks_tracker.task.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.tasks_tracker.task.entity.Subtask;

@Repository
public interface SubtaskRepository extends JpaRepository<Subtask, Long> {

}
