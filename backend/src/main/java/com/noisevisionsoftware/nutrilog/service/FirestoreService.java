package com.noisevisionsoftware.nutrilog.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FirestoreService {

    private final Firestore firestore;

    public void deleteRelatedData(String dietId) {
        log.info("Deleting related Firestore data for diet: {}", dietId);
        try {
            deleteCollection("shopping_lists", dietId);

            deleteCollection("recipe_references", dietId);

            firestore.document("diets/" + dietId).delete().get();

            log.info("Successfully deleted all related data for diet: {}", dietId);
        } catch (Exception e) {
            log.error("Error deleting Firestore data for diet: {}", dietId, e);
            throw new RuntimeException("Failed to delete Firestore data", e);
        }
    }

    private void deleteCollection(String collectionName, String value) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(collectionName)
                    .whereEqualTo("dietId", value)
                    .get();

            QuerySnapshot snapshot = future.get();
            for (DocumentSnapshot document : snapshot.getDocuments()) {
                document.getReference().delete().get();
            }
        } catch (Exception e) {
            log.error("Error deleting collection {} for {}: {}", collectionName, "dietId", value, e);
            throw new RuntimeException("Failed to delete collection", e);
        }
    }
}
