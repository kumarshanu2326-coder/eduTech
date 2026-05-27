package com.edtech.platform.repository;

import com.edtech.platform.entity.Teacher;
import com.edtech.platform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    Optional<Teacher> findByUser(User user);

    List<Teacher> findByApprovalStatus(Teacher.ApprovalStatus status);

    List<Teacher> findByIsSchoolRecommendedTrueAndApprovalStatus(Teacher.ApprovalStatus status);

    List<Teacher> findByIsFeaturedTrueAndApprovalStatus(Teacher.ApprovalStatus status);

    long countByApprovalStatus(Teacher.ApprovalStatus status);

    /**
     * Native PostgreSQL query.
     * Uses standard CAST(t.mode AS text) to prevent Hibernate from confusing
     * PostgreSQL's '::' cast syntax with named parameter binders.
     */
    @Query(value = """
        SELECT * FROM teachers t
        WHERE t.approval_status = 'APPROVED'
          AND (:subject    IS NULL OR LOWER(t.subjects)         LIKE LOWER(CONCAT('%', :subject, '%')))
          AND (:classLevel IS NULL OR LOWER(t.classes_handled)  LIKE LOWER(CONCAT('%', :classLevel, '%')))
          AND (:mode       IS NULL OR LOWER(CAST(t.mode AS text)) = LOWER(:mode)
                                   OR LOWER(CAST(t.mode AS text)) = 'both')
          AND (:city       IS NULL OR LOWER(t.city)             LIKE LOWER(CONCAT('%', :city, '%')))
          AND (:state      IS NULL OR LOWER(t.state)            LIKE LOWER(CONCAT('%', :state, '%')))
          AND (:district   IS NULL OR LOWER(t.district)         LIKE LOWER(CONCAT('%', :district, '%')))
          AND (:category   IS NULL OR LOWER(t.subject_category) LIKE LOWER(CONCAT('%', :category, '%')))
        ORDER BY t.is_featured DESC NULLS LAST,
                 t.is_school_recommended DESC NULLS LAST,
                 t.rating DESC NULLS LAST
        """, nativeQuery = true)
    List<Teacher> search(
            @Param("subject")    String subject,
            @Param("classLevel") String classLevel,
            @Param("mode")       String mode,
            @Param("city")       String city,
            @Param("state")      String state,
            @Param("district")   String district,
            @Param("category")   String category
    );
}