package com.edtech.platform.controller;

import com.edtech.platform.dto.ApiResponse;
import com.edtech.platform.dto.QueryDto;
import com.edtech.platform.service.QueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/queries")
public class QueryController {

    private final QueryService queryService;
    public QueryController(QueryService queryService) { this.queryService = queryService; }

    /** Public — anyone can submit a query/contact form */
    @PostMapping
    public ResponseEntity<ApiResponse<QueryDto.Response>> create(@RequestBody QueryDto.CreateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Query submitted", queryService.create(req)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<QueryDto.Response>>> all() {
        return ResponseEntity.ok(ApiResponse.ok(queryService.getAll()));
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<QueryDto.Response>> resolve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Query resolved", queryService.resolve(id)));
    }
}
