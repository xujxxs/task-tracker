package io.tasks_tracker.profile.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

import static jakarta.persistence.CascadeType.*;

@Entity
@Table(name = "users")
@Data
public class User implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = false)
    private String firstname;

    @Column(nullable = false, unique = false)
    private String lastname;

    @Column(nullable = false, unique = false)
    private LocalDate birthDate;

    @OneToMany(
        cascade = {PERSIST, MERGE, REMOVE},
        fetch = FetchType.EAGER, 
        mappedBy = "user", 
        orphanRemoval = true
    )
    private Set<Role> roles = new HashSet<>();

    public void addRole(Role role)
    {
        this.roles.add(role);
        role.setUser(this);
    }

    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        return this
                .roles
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleEnum().toString()))
                .collect(Collectors.toSet());
    }
}
