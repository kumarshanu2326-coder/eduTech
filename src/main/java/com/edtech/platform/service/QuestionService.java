package com.edtech.platform.service;

import com.edtech.platform.dto.QuestionDto;
import com.edtech.platform.entity.*;
import com.edtech.platform.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    private final QuestionRepository questionRepo;
    private final TestRepository      testRepo;
    private final ObjectMapper        mapper = new ObjectMapper();

    public QuestionService(QuestionRepository questionRepo, TestRepository testRepo) {
        this.questionRepo = questionRepo;
        this.testRepo     = testRepo;
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<QuestionDto.Response> getByTest(Long testId) {
        Test test = findTest(testId);
        return questionRepo.findByTestOrderByDisplayOrderAsc(test)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public QuestionDto.Response create(Long testId, QuestionDto.CreateRequest req) {
        Test test = findTest(testId);
        Question q = buildFromRequest(test, req);
        if (q.getDisplayOrder() == 0) {
            q.setDisplayOrder((int) questionRepo.countByTest(test) + 1);
        }
        return toResponse(questionRepo.save(q));
    }

    @Transactional
    public QuestionDto.Response update(Long questionId, QuestionDto.CreateRequest req) {
        Question q = questionRepo.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        if (req.getType()          != null) q.setType(req.getType());
        if (req.getQuestionText()  != null) q.setQuestionText(req.getQuestionText());
        if (req.getImageUrl()      != null) q.setImageUrl(req.getImageUrl());
        if (req.getOptions()       != null) q.setOptionsJson(toJson(req.getOptions()));
        if (req.getCorrectAnswer() != null) q.setCorrectAnswer(req.getCorrectAnswer());
        if (req.getExplanation()   != null) q.setExplanation(req.getExplanation());
        if (req.getMarks()         != null) q.setMarks(req.getMarks());
        if (req.getNegativeMarks() != null) q.setNegativeMarks(req.getNegativeMarks());
        if (req.getDisplayOrder()  != null) q.setDisplayOrder(req.getDisplayOrder());
        return toResponse(questionRepo.save(q));
    }

    @Transactional
    public void delete(Long questionId) {
        questionRepo.deleteById(questionId);
    }

    // ── Bulk ──────────────────────────────────────────────────────────────────

    public List<QuestionDto.BulkRow> preview(MultipartFile file) {
        String name = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase();
        if (name.endsWith(".json")) return parseJson(file);
        if (name.endsWith(".csv"))  return parseCsv(file);
        throw new IllegalArgumentException("Unsupported file type. Use .csv or .json");
    }

    @Transactional
    public int importBulk(Long testId, MultipartFile file) {
        Test test = findTest(testId);
        List<QuestionDto.BulkRow> rows = preview(file);
        int order = (int) questionRepo.countByTest(test);
        int count = 0;
        for (QuestionDto.BulkRow row : rows) {
            if (Boolean.FALSE.equals(row.getValid())) continue;
            questionRepo.save(rowToQuestion(test, row, ++order));
            count++;
        }
        if (count > 0) {
            test.setTotalQuestions((int) questionRepo.countByTest(test));
            testRepo.save(test);
        }
        return count;
    }

    // ── Parsers ───────────────────────────────────────────────────────────────

    private List<QuestionDto.BulkRow> parseJson(MultipartFile file) {
        try {
            List<QuestionDto.BulkRow> rows = mapper.readValue(
                    file.getInputStream(), new TypeReference<>() {});
            rows.forEach(this::validate);
            return rows;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON: " + e.getMessage());
        }
    }

    private List<QuestionDto.BulkRow> parseCsv(MultipartFile file) {
        List<QuestionDto.BulkRow> rows = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            r.readLine(); // skip header
            String line; int lineNo = 1;
            while ((line = r.readLine()) != null) {
                lineNo++;
                if (line.trim().isEmpty()) continue;
                String[] c = splitCsv(line);
                QuestionDto.BulkRow row = new QuestionDto.BulkRow();
                try {
                    row.setType(          safe(c, 0, "MCQ"));
                    row.setQuestionText(  safe(c, 1, ""));
                    row.setOptionA(       safe(c, 2, ""));
                    row.setOptionB(       safe(c, 3, ""));
                    row.setOptionC(       safe(c, 4, ""));
                    row.setOptionD(       safe(c, 5, ""));
                    row.setCorrectAnswer( safe(c, 6, ""));
                    row.setExplanation(   safe(c, 7, ""));
                    row.setMarks(         safe(c,8,"").isEmpty() ? 1 : Integer.parseInt(safe(c,8,"1").trim()));
                    row.setNegativeMarks( safe(c,9,"").isEmpty() ? 0 : Integer.parseInt(safe(c,9,"0").trim()));
                } catch (Exception e) {
                    row.setValid(false); row.setErrorMessage("Line " + lineNo + ": " + e.getMessage());
                    rows.add(row); continue;
                }
                validate(row); rows.add(row);
            }
        } catch (IllegalArgumentException e) { throw e; }
        catch (Exception e) { throw new IllegalArgumentException("Error reading CSV: " + e.getMessage()); }
        return rows;
    }

    private String safe(String[] arr, int i, String def) {
        return (arr.length > i && arr[i] != null) ? arr[i].trim() : def;
    }

    private String[] splitCsv(String line) {
        List<String> r = new ArrayList<>();
        boolean q = false; StringBuilder sb = new StringBuilder();
        for (char c : line.toCharArray()) {
            if (c == '"') { q = !q; }
            else if (c == ',' && !q) { r.add(sb.toString()); sb = new StringBuilder(); }
            else sb.append(c);
        }
        r.add(sb.toString());
        return r.toArray(new String[0]);
    }

    private void validate(QuestionDto.BulkRow row) {
        if (row.getQuestionText() == null || row.getQuestionText().isBlank()) {
            row.setValid(false); row.setErrorMessage("Question text required"); return;
        }
        if (row.getCorrectAnswer() == null || row.getCorrectAnswer().isBlank()) {
            row.setValid(false); row.setErrorMessage("Correct answer required"); return;
        }
        String t = row.getType() != null ? row.getType().toUpperCase() : "MCQ";
        try { Question.QuestionType.valueOf(t); }
        catch (Exception e) { row.setValid(false); row.setErrorMessage("Unknown type: " + t); return; }
        row.setValid(true);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Question buildFromRequest(Test test, QuestionDto.CreateRequest req) {
        return Question.builder()
                .test(test)
                .type(req.getType() != null ? req.getType() : Question.QuestionType.MCQ)
                .questionText(req.getQuestionText())
                .imageUrl(req.getImageUrl())
                .optionsJson(req.getOptions() != null ? toJson(req.getOptions()) : null)
                .correctAnswer(req.getCorrectAnswer())
                .explanation(req.getExplanation())
                .marks(req.getMarks() != null ? req.getMarks() : 1)
                .negativeMarks(req.getNegativeMarks() != null ? req.getNegativeMarks() : 0)
                .displayOrder(req.getDisplayOrder() != null ? req.getDisplayOrder() : 0)
                .isMandatory(req.getIsMandatory() != null ? req.getIsMandatory() : false)
                .build();
    }

    private Question rowToQuestion(Test test, QuestionDto.BulkRow row, int order) {
        List<String> opts = new ArrayList<>();
        if (notBlank(row.getOptionA())) opts.add(row.getOptionA());
        if (notBlank(row.getOptionB())) opts.add(row.getOptionB());
        if (notBlank(row.getOptionC())) opts.add(row.getOptionC());
        if (notBlank(row.getOptionD())) opts.add(row.getOptionD());
        String ans = convertLetterAnswer(row.getCorrectAnswer());
        return Question.builder()
                .test(test)
                .type(Question.QuestionType.valueOf(row.getType().toUpperCase()))
                .questionText(row.getQuestionText())
                .optionsJson(opts.isEmpty() ? null : toJson(opts))
                .correctAnswer(ans)
                .explanation(row.getExplanation())
                .marks(row.getMarks() != null ? row.getMarks() : 1)
                .negativeMarks(row.getNegativeMarks() != null ? row.getNegativeMarks() : 0)
                .displayOrder(order)
                .isMandatory(false)
                .build();
    }

    /** "A"→"0", "B"→"1", "A,C"→"0,2", numeric pass-through */
    private String convertLetterAnswer(String raw) {
        if (raw == null) return "";
        Map<String,String> m = Map.of("A","0","B","1","C","2","D","3","E","4");
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .map(s -> m.getOrDefault(s.toUpperCase(), s))
                .collect(Collectors.joining(","));
    }

    private String toJson(List<String> list) {
        try { return mapper.writeValueAsString(list); } catch (Exception e) { return "[]"; }
    }

    public List<String> fromJson(String json) {
        if (json == null || json.isBlank()) return new ArrayList<>();
        try { return mapper.readValue(json, new TypeReference<>() {}); } catch (Exception e) { return new ArrayList<>(); }
    }

    public QuestionDto.Response toResponse(Question q) {
        return QuestionDto.Response.builder()
                .id(q.getId())
                .testId(q.getTest() != null ? q.getTest().getId() : null)
                .type(q.getType())
                .questionText(q.getQuestionText())
                .imageUrl(q.getImageUrl())
                .options(fromJson(q.getOptionsJson()))
                .correctAnswer(q.getCorrectAnswer())
                .explanation(q.getExplanation())
                .marks(q.getMarks())
                .negativeMarks(q.getNegativeMarks())
                .displayOrder(q.getDisplayOrder())
                .isMandatory(q.getIsMandatory())
                .build();
    }

    private Test findTest(Long id) {
        return testRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Test not found: " + id));
    }

    private boolean notBlank(String s) { return s != null && !s.isBlank(); }
}