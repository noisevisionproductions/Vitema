# Szyta Dieta (Mobile App)

A mobile companion app for patients, part of the **Nutrilog** ecosystem. It provides users with instant access to personalized nutrition plans, generates automated shopping lists, and allows for real-time progress monitoring.

The app is available on [Google Play](https://play.google.com/store/apps/details?id=com.noisevisionsoftware.szytadieta).

## Key Features

* **⚡ Real-time Synchronization:** Instant updates of diet plans assigned by the nutritionist (powered by Firebase Firestore).
* **🛒 Smart Shopping List:** Automatic generation of shopping lists based on the active meal plan.
* **📊 Progress Tracking:** Monitoring of body weight, measurements, and water intake with visual charts (Vico/MPAndroidChart).
* **🔔 Notification System:** Reminders for meals and hydration (WorkManager + FCM).
* **📄 Document Handling:** Native support for reading and processing diet-related documents directly on the device (Apache POI).

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
    * Authentication
    * Firestore (NoSQL Database)
    * Storage (Media & Documents)
    * Crashlytics & Analytics
    * Cloud Functions
* **Networking:** Retrofit + OkHttp
* **Local Persistence:** DataStore Preferences
* **Visualization:** MPAndroidChart / Vico
* **Utilities:**
    * Apache POI (Excel/Office document support)
    * Coil (Image loading)
    * Konfetti (UI animations)

## Project Structure

The codebase is organized by layers, following Clean Architecture principles:

* `data/` - Repository implementations, Data Sources (API, Firebase, Cache), and Mappers.
* `domain/` - Pure business logic, Domain Models, Repository Interfaces, and Use Cases.
* `ui/` - Presentation layer built with Jetpack Compose:
    * `screens/` - Feature-based screen composables.
    * `navigation/` - Navigation graph and route definitions.
    * `theme/` - Styling, typography, and color palette.
* `di/` - Hilt modules for Dependency Injection.

## Prerequisites & Setup

### Requirements
* Android Studio Ladybug or newer
* JDK 17
* Device/Emulator with Android Min SDK 26 (Android 8.0)

### Getting Started

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/noisevisionproductions/szyta-dieta-android](https://github.com/noisevisionproductions/szyta-dieta-android)
    ```

2.  **Environment Configuration:**
    The project requires a `key.properties` file in the root directory and `google-services.json` in the `app/` folder.
    *(Please contact the repository owner to request access to the testing environment credentials).*

3.  **Build the project:**
    ```bash
    ./gradlew build
    ```

## License

This project is proprietary software. All rights reserved.