package io.tasks_tracker.profile.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.tasks_tracker.profile.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>
{
    public Optional<User> findByUsername(String username); 

    public Optional<User> findByEmail(String email); 
}
