package com.edtech.platform.controller;

import com.edtech.platform.dto.ApiResponse;
import com.edtech.platform.dto.QuestionDto;
import com.edtech.platform.service.QuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    /** Admin — get all questions for a test (includes correct answers) */
    @GetMapping("/test/{testId}")
    public ResponseEntity<ApiResponse<List<QuestionDto.Response>>> getByTest(
            @PathVariable Long testId) {
        return ResponseEntity.ok(ApiResponse.ok(questionService.getByTest(testId)));
    }

    /** Student — get questions without correct answers or explanations */
    @GetMapping("/test/{testId}/student")
    public ResponseEntity<ApiResponse<List<QuestionDto.Response>>> getForStudent(
            @PathVariable Long testId) {
        List<QuestionDto.Response> questions = questionService.getByTest(testId);
        questions.forEach(q -> { q.setCorrectAnswer(null); q.setExplanation(null); });
        return ResponseEntity.ok(ApiResponse.ok(questions));
    }

    /** Admin — add a question to a test */
    @PostMapping("/test/{testId}")
    public ResponseEntity<ApiResponse<QuestionDto.Response>> create(
            @PathVariable Long testId,
            @RequestBody QuestionDto.CreateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Question added",
                questionService.create(testId, req)));
    }

    /** Admin — update a question */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<QuestionDto.Response>> update(
            @PathVariable Long id,
            @RequestBody QuestionDto.CreateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Question updated",
                questionService.update(id, req)));
    }

    /** Admin — delete a question */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        questionService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", null));
    }

    /** Admin — preview bulk upload (parse + validate, no save) */
    @PostMapping("/test/{testId}/bulk/preview")
    public ResponseEntity<ApiResponse<List<QuestionDto.BulkRow>>> previewBulk(
            @PathVariable Long testId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.ok(questionService.preview(file)));
    }

    /** Admin — import bulk upload (save all valid rows) */
    @PostMapping("/test/{testId}/bulk/import")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> importBulk(
            @PathVariable Long testId,
            @RequestParam("file") MultipartFile file) {
        int imported = questionService.importBulk(testId, file);
        return ResponseEntity.ok(ApiResponse.ok("Import complete",
                Map.of("imported", imported)));
    }
}