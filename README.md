# Szyta Dieta (Mobile App)

A mobile companion app for patients, part of the **Nutrilog** ecosystem. It provides users with instant access to personalized nutrition plans, generates automated shopping lists, and allows for real-time progress monitoring.

The app is available on [Google Play](https://play.google.com/store/apps/details?id=com.noisevisionsoftware.szytadieta).

## Key Features

* **🛒 Smart Shopping Lists:** Automatically generates a comprehensive shopping list based on the ingredients required for the assigned diet plan, grouped for convenience.
* **⚡ Real-time Synchronization:** Instant updates of diet plans and recommendations assigned by the nutritionist (powered by Firebase Firestore).
* **📊 Progress Tracking:** Monitoring of body weight, measurements, and water intake with interactive visual charts (Vico/MPAndroidChart).
* **🔔 Smart Notifications:** Automated reminders for meals, hydration, and periodic surveys (WorkManager + FCM).
* **🛡️ Enhanced Security:** Implementation of Firebase App Check (Play Integrity) to ensure API request authenticity and data safety.

## Architecture & Tech Stack

The project adheres to **Modern Android Development (MAD)** standards, focusing on scalability, testability, and clean code principles.

### Core Stack

* **Language:** Kotlin (100%)
* **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material Design 3)
* **Architecture:** Clean Architecture (Presentation, Domain, Data layers) + MVVM pattern
* **Dependency Injection:** [Hilt](https://dagger.dev/hilt/)
* **Concurrency:** Kotlin Coroutines + Flow

### Libraries & Tools

* **Backend & Cloud (Firebase):**
    * Authentication (Email & Link verification)
    * Firestore (NoSQL Database)
    * Storage (Media & Documents)
    * App Check (Security & Anti-abuse)
    * Crashlytics & Analytics
    * Cloud Functions
* **Networking:** Retrofit + OkHttp
* **Local Persistence:** DataStore Preferences
* **Visualization:** MPAndroidChart / Vico
* **Utilities:**
    * Compose Markdown (Document rendering)
    * Coil (Image loading)
    * Konfetti (UI gamification & animations)

## Prerequisites & Setup

### Requirements
* Android Studio Ladybug or newer
* JDK 17
* Device/Emulator with Android Min SDK 26 (Android 8.0)

### Getting Started

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/noisevisionproductions/szytadieta](https://github.com/noisevisionproductions/szytadieta)
    ```

2.  **Environment Configuration:**
    The project requires a `key.properties` file in the root directory and `google-services.json` in the `app/` folder.
    * *Note:* To run on a physical device, ensure your SHA-1 fingerprint is added to the Firebase Console and Google Cloud Console.

3.  **Build the project:**
    ```bash
    ./gradlew build
    ```

## License

This project is proprietary software. All rights reserved.