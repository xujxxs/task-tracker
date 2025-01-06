package io.tasks_tracker.task.specification;

import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.Specification;

import io.tasks_tracker.task.entity.Task;
import jakarta.persistence.criteria.Predicate;

public class TaskSpecification 
{
    public static Specification<Task> filterByTitle(String title) 
    {
        return (root, query, criteriaBuilder) -> {
            if (title == null || title.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(root.get("title"), "%" + title + "%");
        };
    }

    public static Specification<Task> filterByCategory(String category) 
    {
        return (root, query, criteriaBuilder) -> {
            if (category != null && !category.isEmpty()) {
                return criteriaBuilder.equal(root.get("category"), category);
            }
            return criteriaBuilder.conjunction();
        };
    }

    public static Specification<Task> filterByDateEnd(
            LocalDateTime equalDate, 
            LocalDateTime minDate, 
            LocalDateTime maxDate,
            boolean isNotHaveEnded
    ) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            if (isNotHaveEnded) {
                return criteriaBuilder.and(predicate, 
                    criteriaBuilder.isNull(root.get("dateEnd")));
            }

            if (equalDate != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("dateEnd"), equalDate));
            }

            if (minDate != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("dateEnd"), minDate));
            }

            if (maxDate != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("dateEnd"), maxDate));
            }

            return predicate;
        };
    }

    public static Specification<Task> filterByCreated(
            LocalDateTime equalDate, 
            LocalDateTime minDate, 
            LocalDateTime maxDate
    ) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            if (equalDate != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("createdAt"), equalDate));
            }

            if (minDate != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), minDate));
            }

            if (maxDate != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), maxDate));
            }

            return predicate;
        };
    }

    public static Specification<Task> filterByUpdated(
            LocalDateTime equalDate, 
            LocalDateTime minDate, 
            LocalDateTime maxDate
    ) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            if (equalDate != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("redactedAt"), equalDate));
            }

            if (minDate != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("redactedAt"), minDate));
            }

            if (maxDate != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("redactedAt"), maxDate));
            }

            return predicate;
        };
    }

    public static Specification<Task> filterByEnded(
        LocalDateTime equalDate, 
        LocalDateTime minDate, 
        LocalDateTime maxDate,
        boolean isNotCompleted
    ) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            if (isNotCompleted) {
                return criteriaBuilder.and(predicate, 
                    criteriaBuilder.isNull(root.get("endedAt")));
            }

            if (equalDate != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("endedAt"), equalDate));
            }

            if (minDate != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("endedAt"), minDate));
            }

            if (maxDate != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("endedAt"), maxDate));
            }

            return predicate;
        };
    }

    public static Specification<Task> filterByImportance(
            Long equalTo, 
            Long greaterThanOrEqualTo, 
            Long lessThanOrEqualTo
    ) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            if (equalTo != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("importance"), equalTo));
            }

            if (greaterThanOrEqualTo != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("importance"), greaterThanOrEqualTo));
            }

            if (lessThanOrEqualTo != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("importance"), lessThanOrEqualTo));
            }

            return predicate;
        };
    }

    public static Specification<Task> filterByCreatedBy(String username) 
    {
        return (root, query, criteriaBuilder) -> {
            if (username != null) {
                return criteriaBuilder.equal(root.get("createdBy"), username);
            }
            return criteriaBuilder.conjunction();
        };
    }
}
