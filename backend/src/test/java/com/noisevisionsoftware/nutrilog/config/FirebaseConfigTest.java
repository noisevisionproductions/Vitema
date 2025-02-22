package com.noisevisionsoftware.nutrilog.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FirebaseConfigTest {

    @Mock
    private ClassPathResource classPathResource;

    @Mock
    private InputStream inputStream;

    @Mock
    private GoogleCredentials googleCredentials;

    @Mock
    private FirebaseOptions firebaseOptions;

    private FirebaseConfig firebaseConfig;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        firebaseConfig = new FirebaseConfig();
        ReflectionTestUtils.setField(firebaseConfig, "firebaseConfigPath", "firebase-service-account.json");
    }

    @Test
    void firebaseApp_WhenNoExistingApp_ShouldInitializeNewApp() throws IOException {
        try (MockedStatic<FirebaseApp> firebaseAppMockedStatic = mockStatic(FirebaseApp.class);
             MockedStatic<GoogleCredentials> googleCredentialsMockedStatic = mockStatic(GoogleCredentials.class)) {

            firebaseAppMockedStatic.when(FirebaseApp::getApps).thenReturn(java.util.Collections.emptyList());
            googleCredentialsMockedStatic.when(() -> GoogleCredentials.fromStream(any(InputStream.class)))
                    .thenReturn(googleCredentials);
            when(classPathResource.getInputStream()).thenReturn(inputStream);

            FirebaseApp mockApp = mock(FirebaseApp.class);
            firebaseAppMockedStatic.when(() -> FirebaseApp.initializeApp(any(FirebaseOptions.class)))
                    .thenReturn(mockApp);

            assertDoesNotThrow(() -> firebaseConfig.firebaseApp());
        }
    }

    @Test
    void firebaseAuth_ShouldReturnFirebaseAuthInstance() {
        try (MockedStatic<FirebaseAuth> firebaseAuthMockedStatic = mockStatic(FirebaseAuth.class)) {
            FirebaseApp mockApp = mock(FirebaseApp.class);
            FirebaseAuth mockAuth = mock(FirebaseAuth.class);

            firebaseAuthMockedStatic.when(() -> FirebaseAuth.getInstance(any(FirebaseApp.class)))
                    .thenReturn(mockAuth);

            FirebaseOptions mockOptions = mock(FirebaseOptions.class);
            when(mockApp.getOptions()).thenReturn(mockOptions);

            FirebaseAuth result = firebaseConfig.firebaseAuth(mockApp);

            assertNotNull(result);
            assertEquals(mockAuth, result);
        }
    }

    @Test
    void firestore_ShouldReturnFirestoreInstance() throws IOException {
        try (MockedStatic<FirebaseApp> firebaseAppMockedStatic = mockStatic(FirebaseApp.class);
             MockedStatic<GoogleCredentials> googleCredentialsMockedStatic = mockStatic(GoogleCredentials.class);
             MockedStatic<FirestoreClient> firestoreClientMockedStatic = mockStatic(FirestoreClient.class)) {

            FirebaseApp mockApp = mock(FirebaseApp.class);
            when(mockApp.getOptions()).thenReturn(firebaseOptions);
            firebaseAppMockedStatic.when(FirebaseApp::getInstance).thenReturn(mockApp);
            firebaseAppMockedStatic.when(() -> FirebaseApp.initializeApp(any(FirebaseOptions.class)))
                    .thenReturn(mockApp);

            googleCredentialsMockedStatic.when(() -> GoogleCredentials.fromStream(any(InputStream.class)))
                    .thenReturn(googleCredentials);

            when(classPathResource.getInputStream()).thenReturn(inputStream);

            Firestore mockFirestore = mock(Firestore.class);
            firestoreClientMockedStatic.when(() -> FirestoreClient.getFirestore(any(FirebaseApp.class)))
                    .thenReturn(mockFirestore);

            Firestore result = firebaseConfig.firestore();
            assertNotNull(result);
            assertEquals(mockFirestore, result);
        }
    }
}