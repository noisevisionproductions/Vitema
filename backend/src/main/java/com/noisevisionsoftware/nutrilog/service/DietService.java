package com.noisevisionsoftware.nutrilog.service;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.protobuf.ServiceException;
import com.noisevisionsoftware.nutrilog.exception.NotFoundException;
import com.noisevisionsoftware.nutrilog.model.diet.Diet;
import com.noisevisionsoftware.nutrilog.repository.DietRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DietService {
    private final DietRepository dietRepository;

    @Cacheable(value = "dietsCache", key = "'allDiets'")
    public List<Diet> getAllDiets() {
        log.info("CACHE: Fetching all diets FROM DATABASE");
        return dietRepository.findAll();
    }

    @Cacheable(value = "dietsCache", key = "#id")
    public Diet getDietById(String id) {
        return dietRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Diet not found with id: " + id));
    }

    @Cacheable(value = "dietsListCache", key = "#userId + '_' + #page + '_' + #size")
    public List<Diet> getDietsByUserId(String userId) {
        return dietRepository.findByUserId(userId);
    }

    @CacheEvict(value = "dietsCache", allEntries = true)
    public Diet createDiet(Diet diet) {
        diet.setCreatedAt(Timestamp.now());
        diet.setUpdatedAt(Timestamp.now());
        return dietRepository.save(diet);
    }

    @CacheEvict(value = "dietsCache", key = "#diet.id")
    public Diet updateDiet(Diet diet) {
        Diet existingDiet = getDietById(diet.getId());
        diet.setCreatedAt(existingDiet.getCreatedAt());
        diet.setUpdatedAt(Timestamp.now());
        return dietRepository.save(diet);
    }

    @CacheEvict(value = "dietsCache", key = "#id")
    public void deleteDiet(String id) {
        dietRepository.delete(id);
    }
}