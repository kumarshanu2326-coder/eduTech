package com.edtech.platform.service;

import com.edtech.platform.dto.QueryDto;
import com.edtech.platform.entity.Query;
import com.edtech.platform.repository.QueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QueryService {

    private final QueryRepository queryRepository;

    public QueryService(QueryRepository queryRepository) {
        this.queryRepository = queryRepository;
    }

    @Transactional
    public QueryDto.Response create(QueryDto.CreateRequest req) {
        Query q = Query.builder()
                .name(req.getName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .subject(req.getSubject())
                .message(req.getMessage())
                .build();
        return toResponse(queryRepository.save(q));
    }

    public List<QueryDto.Response> getAll() {
        return queryRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public QueryDto.Response resolve(Long id) {
        Query q = queryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Query not found: " + id));
        q.setStatus(Query.QueryStatus.RESOLVED);
        return toResponse(queryRepository.save(q));
    }

    private QueryDto.Response toResponse(Query q) {
        return QueryDto.Response.builder()
                .id(q.getId())
                .name(q.getName())
                .email(q.getEmail())
                .phone(q.getPhone())
                .subject(q.getSubject())
                .message(q.getMessage())
                .status(q.getStatus() != null ? q.getStatus().name() : "OPEN")
                .createdAt(q.getCreatedAt())
                .build();
    }
}
