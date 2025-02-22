package com.noisevisionsoftware.nutrilog.controller;

import com.noisevisionsoftware.nutrilog.dto.request.diet.DietRequest;
import com.noisevisionsoftware.nutrilog.dto.response.diet.DietResponse;
import com.noisevisionsoftware.nutrilog.exception.NotFoundException;
import com.noisevisionsoftware.nutrilog.mapper.diet.DietMapper;
import com.noisevisionsoftware.nutrilog.model.diet.Diet;
import com.noisevisionsoftware.nutrilog.service.DietService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/diets")
@RequiredArgsConstructor
@Validated
public class DietController {
    private final DietService dietService;
    private final DietMapper dietMapper;

    @GetMapping
    public ResponseEntity<List<DietResponse>> getAllDiets(
            @RequestParam(required = false) String userId) {
        List<Diet> diets = userId != null ?
                dietService.getDietsByUserId(userId) :
                dietService.getAllDiets();
        return ResponseEntity.ok(diets.stream()
                .map(dietMapper::toResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DietResponse> getDietById(@PathVariable String id) {
        Diet diet = dietService.getDietById(id);
        return ResponseEntity.ok(dietMapper.toResponse(diet));
    }

    @PostMapping
    public ResponseEntity<DietResponse> createDiet(
            @Valid @RequestBody DietRequest request) {
        Diet diet = dietMapper.toDomain(request);
        Diet savedDiet = dietService.createDiet(diet);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(dietMapper.toResponse(savedDiet));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DietResponse> updateDiet(
            @PathVariable String id,
            @Valid @RequestBody DietRequest request) {
        Diet diet = dietMapper.toDomain(request);
        diet.setId(id);
        Diet updatedDiet = dietService.updateDiet(diet);
        return ResponseEntity.ok(dietMapper.toResponse(updatedDiet));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiet(@PathVariable String id) {
        dietService.deleteDiet(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFoundException(NotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(problem);
    }
}