package io.tasks_tracker.task.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.tasks_tracker.task.entity.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> 
{
    @Query("SELECT t FROM Task t WHERE t.createdBy = :username")
    public List<Task> findByCreatedBy(String username, Pageable pageable);
}
