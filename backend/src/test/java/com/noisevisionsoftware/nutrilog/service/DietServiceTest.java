package com.noisevisionsoftware.nutrilog.service;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.nutrilog.exception.NotFoundException;
import com.noisevisionsoftware.nutrilog.model.diet.Diet;
import com.noisevisionsoftware.nutrilog.model.diet.DietMetadata;
import com.noisevisionsoftware.nutrilog.repository.DietRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DietServiceTest {

    @Mock
    private DietRepository dietRepository;

    @Mock
    private FirestoreService firestoreService;

    @InjectMocks
    private DietService dietService;


    private Diet testDiet;
    private static final String TEST_ID = "test123";
    private static final String TEST_USER_ID = "user123";

    @BeforeEach
    void setUp() {
        testDiet = Diet.builder()
                .id(TEST_ID)
                .userId(TEST_USER_ID)
                .createdAt(Timestamp.now())
                .updatedAt(Timestamp.now())
                .days(new ArrayList<>())
                .metadata(DietMetadata.builder().build())
                .build();
    }

    @Test
    void getAllDiets_ShouldReturnAllDiets() {
        // Arrange
        List<Diet> expectedDiets = Collections.singletonList(testDiet);
        when(dietRepository.findAll()).thenReturn(expectedDiets);

        // Act
        List<Diet> actualDiets = dietService.getAllDiets();

        // Assert
        assertEquals(expectedDiets, actualDiets);
        verify(dietRepository).findAll();
    }

    @Test
    void getDietById_WhenDietExists_ShouldReturnDiet() {
        // Arrange
        when(dietRepository.findById(TEST_ID)).thenReturn(Optional.of(testDiet));

        // Act
        Diet actualDiet = dietService.getDietById(TEST_ID);

        // Assert
        assertEquals(testDiet, actualDiet);
        verify(dietRepository).findById(TEST_ID);
    }

    @Test
    void getDietById_WhenDietDoesNotExist_ShouldThrowNotFoundException() {
        // Arrange
        when(dietRepository.findById(TEST_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> dietService.getDietById(TEST_ID));
        verify(dietRepository).findById(TEST_ID);
    }

    @Test
    void getDietsByUserId_ShouldReturnUserDiets() {
        // Arrange
        List<Diet> expectedDiets = Collections.singletonList(testDiet);
        when(dietRepository.findByUserId(TEST_USER_ID)).thenReturn(expectedDiets);

        // Act
        List<Diet> actualDiets = dietService.getDietsByUserId(TEST_USER_ID);

        // Assert
        assertEquals(expectedDiets, actualDiets);
        verify(dietRepository).findByUserId(TEST_USER_ID);
    }

    @Test
    void createDiet_ShouldSaveDietWithTimestamps() {
        // Arrange
        Diet dietToCreate = Diet.builder()
                .userId(TEST_USER_ID)
                .build();

        when(dietRepository.save(any(Diet.class))).thenReturn(testDiet);

        // Act
        Diet createdDiet = dietService.createDiet(dietToCreate);

        // Assert
        assertNotNull(createdDiet);
        verify(dietRepository).save(dietToCreate);
        assertNotNull(dietToCreate.getCreatedAt());
        assertNotNull(dietToCreate.getUpdatedAt());
    }

    @Test
    void updateDiet_WhenDietExists_ShouldUpdateDiet() {
        // Arrange
        Diet dietToUpdate = Diet.builder()
                .id(TEST_ID)
                .userId(TEST_USER_ID)
                .days(new ArrayList<>())
                .build();

        when(dietRepository.findById(TEST_ID)).thenReturn(Optional.of(testDiet));
        when(dietRepository.save(any(Diet.class))).thenReturn(dietToUpdate);

        // Act
        Diet updatedDiet = dietService.updateDiet(dietToUpdate);

        // Assert
        assertNotNull(updatedDiet);
        verify(dietRepository).findById(TEST_ID);
        verify(dietRepository).save(dietToUpdate);
        assertEquals(testDiet.getCreatedAt(), dietToUpdate.getCreatedAt());
        assertNotNull(dietToUpdate.getUpdatedAt());
    }


    @Test
    void updateDiet_WhenDietDoesNotExist_ShouldThrowNotFoundException() {
        // Arrange
        Diet dietToUpdate = Diet.builder()
                .id(TEST_ID)
                .userId(TEST_USER_ID)
                .build();

        when(dietRepository.findById(TEST_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> dietService.updateDiet(dietToUpdate));
        verify(dietRepository).findById(TEST_ID);
        verify(dietRepository, never()).save(any(Diet.class));
    }

    @Test
    void deleteDiet_ShouldDeleteDiet() {
        // Arrange
        doNothing().when(dietRepository).delete(TEST_ID);
        doNothing().when(firestoreService).deleteRelatedData(TEST_ID);

        // Act
        dietService.deleteDiet(TEST_ID);

        // Assert
        verify(dietRepository).delete(TEST_ID);
        verify(firestoreService).deleteRelatedData(TEST_ID);
    }


    @Test
    void createDiet_ShouldSetCurrentTimestamps() {
        // Arrange
        Diet dietToCreate = Diet.builder()
                .userId(TEST_USER_ID)
                .build();

        when(dietRepository.save(any(Diet.class))).thenAnswer(invocation -> invocation.<Diet>getArgument(0));

        // Act
        Diet createdDiet = dietService.createDiet(dietToCreate);

        // Assert
        assertNotNull(createdDiet.getCreatedAt());
        assertNotNull(createdDiet.getUpdatedAt());
        assertTrue(createdDiet.getCreatedAt().getSeconds() > 0);
        assertTrue(createdDiet.getUpdatedAt().getSeconds() > 0);
    }

    @Test
    void updateDiet_ShouldPreserveCreatedAtAndUpdateUpdatedAt() {
        // Arrange
        Timestamp originalCreatedAt = Timestamp.now();
        testDiet.setCreatedAt(originalCreatedAt);

        Diet dietToUpdate = Diet.builder()
                .id(TEST_ID)
                .userId(TEST_USER_ID)
                .days(new ArrayList<>())
                .build();

        when(dietRepository.findById(TEST_ID)).thenReturn(Optional.of(testDiet));
        when(dietRepository.save(any(Diet.class))).thenAnswer(invocation ->
                invocation.<Diet>getArgument(0));

        // Act
        Diet updatedDiet = dietService.updateDiet(dietToUpdate);

        // Assert
        assertEquals(originalCreatedAt, updatedDiet.getCreatedAt());
        assertNotNull(updatedDiet.getUpdatedAt());
        assertTrue(updatedDiet.getUpdatedAt().getSeconds() >= originalCreatedAt.getSeconds());
    }
}