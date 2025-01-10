package io.tasks_tracker.task.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import io.tasks_tracker.task.entity.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> 
{
    @Transactional
    @Modifying
    @Query("DELETE FROM Task t WHERE t.createdBy = :username")
    public void deleteAllByCreatedBy(String username);
}
