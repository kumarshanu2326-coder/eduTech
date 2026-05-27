package com.edtech.platform.repository;

import com.edtech.platform.entity.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QueryRepository extends JpaRepository<Query, Long> {
    List<Query> findByStatusOrderByCreatedAtDesc(Query.QueryStatus status);
}
