package io.tasks_tracker.profile.entity;

import java.io.Serializable;

import io.tasks_tracker.profile.enumeration.RoleEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
public class Role implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(unique = false, nullable = false)
    private RoleEnum roleEnum;

    @ManyToOne
    @JoinColumn(
        name = "user_id",
        nullable = false,
        referencedColumnName = "user_id"
    )
    private User user;

    public Role(RoleEnum roleEnum)
    {
        this.roleEnum = roleEnum;
    }
}
