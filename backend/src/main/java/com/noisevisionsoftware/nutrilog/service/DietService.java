package com.noisevisionsoftware.nutrilog.service;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.protobuf.ServiceException;
import com.noisevisionsoftware.nutrilog.dto.response.diet.DietInfo;
import com.noisevisionsoftware.nutrilog.exception.NotFoundException;
import com.noisevisionsoftware.nutrilog.model.diet.Day;
import com.noisevisionsoftware.nutrilog.model.diet.Diet;
import com.noisevisionsoftware.nutrilog.repository.DietRepository;
import com.noisevisionsoftware.nutrilog.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DietService {
    private final DietRepository dietRepository;
    private final FirestoreService firestoreService;

    private static final String DIETS_CACHE = "dietsCache";
    private static final String DIETS_LIST_CACHE = "dietsListCache";

    @Cacheable(value = DIETS_CACHE, key = "'allDiets'")
    public List<Diet> getAllDiets() {
        log.info("CACHE: Fetching all diets FROM DATABASE");
        return dietRepository.findAll();
    }

    @Cacheable(value = DIETS_CACHE, key = "#id")
    public Diet getDietById(String id) {
        return dietRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Diet not found with id: " + id));
    }

    @Cacheable(value = DIETS_LIST_CACHE, key = "#userId")
    public List<Diet> getDietsByUserId(String userId) {
        return dietRepository.findByUserId(userId);
    }

    public Map<String, DietInfo> getDietsInfoForUsers(List<String> userIds) {
        Map<String, DietInfo> dietInfoMap = new HashMap<>();

        for (String userId : userIds) {
            List<Diet> userDiets = getDietsByUserId(userId);

            if (!userDiets.isEmpty()) {
                LocalDateTime earliestDate = null;
                LocalDateTime latestDate = null;

                for (Diet diet : userDiets) {
                    for (Day day : diet.getDays()) {
                        LocalDateTime dayDate = DateUtils.timestampToLocalDateTime(day.getDate());

                        if (earliestDate == null || dayDate.isBefore(earliestDate)) {
                            earliestDate = dayDate;
                        }
                        if (latestDate == null || dayDate.isAfter(latestDate)) {
                            latestDate = dayDate;
                        }
                    }
                }

                dietInfoMap.put(userId, DietInfo.builder()
                        .hasDiet(true)
                        .startDate(earliestDate)
                        .endDate(latestDate)
                        .build());
            } else {
                dietInfoMap.put(userId, DietInfo.builder()
                        .hasDiet(false)
                        .startDate(null)
                        .endDate(null)
                        .build());
            }
        }

        return dietInfoMap;
    }

    @Caching(evict = {
            @CacheEvict(value = DIETS_CACHE, allEntries = true),
            @CacheEvict(value = DIETS_LIST_CACHE, allEntries = true)
    })
    public Diet createDiet(Diet diet) {
        diet.setCreatedAt(Timestamp.now());
        diet.setUpdatedAt(Timestamp.now());
        return dietRepository.save(diet);
    }

    @Caching(evict = {
            @CacheEvict(value = DIETS_CACHE, allEntries = true),
            @CacheEvict(value = DIETS_LIST_CACHE, allEntries = true)
    })
    public Diet updateDiet(Diet diet) {
        if (diet.getId() == null) {
            throw new IllegalArgumentException("Diet ID cannot be null for update");
        }

        synchronized (this) {
            Diet existingDiet = getDietById(diet.getId());

            if (!existingDiet.getUserId().equals(diet.getUserId())) {
                throw new AccessDeniedException("You don't have permission to update this diet");
            }

            diet.setCreatedAt(existingDiet.getCreatedAt());
            diet.setUpdatedAt(Timestamp.now());

            diet.getDays().forEach(day -> {
                if (day.getDate() == null) {
                    day.setDate(Timestamp.now());
                }
            });

            log.info("Updating diet with ID: {}", diet.getId());
            Diet savedDiet = dietRepository.save(diet);
            log.info("Successfully updated diet with ID: {}", diet.getId());

            return savedDiet;
        }
    }

    @Caching(evict = {
            @CacheEvict(value = DIETS_CACHE, allEntries = true),
            @CacheEvict(value = DIETS_LIST_CACHE, allEntries = true)
    })
    public void deleteDiet(String id) {
        log.info("Starting diet deletion process for id: {}", id);
        try {
            firestoreService.deleteRelatedData(id);

            dietRepository.delete(id);

            log.info("Successfully deleted diet and related data for id: {}", id);
        } catch (Exception e) {
            log.error("Error deleting diet with id: {}", "Error deleting diet", e);
            throw e;
        }
    }
}