package io.tasks_tracker.profile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.tasks_tracker.profile.entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

}
