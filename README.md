# Nutrilog

Nutrilog to kompleksowe rozwiązanie dla specjalistów żywienia i ich pacjentów. Aplikacja umożliwia tworzenie spersonalizowanych planów żywieniowych, monitorowanie postępów i zarządzanie dietami.

## Funkcje

- ✅ Tworzenie i zarządzanie planami diet dla pacjentów
- ✅ Automatyczna kategoryzacja produktów spożywczych
- ✅ Generowanie list zakupów i wyświetlanie ich w aplikacji mobilnej
- ✅ Śledzenie wartości odżywczych posiłków

## Technologie

### Backend
- Java 21
- Spring Boot 3.2.3
- Spring Security
- Firebase Admin SDK
- Caffeine Cache

### Frontend
- React
- TypeScript
- Tailwind CSS
- React Router
- Sonner Toast

## Instalacja

### Wymagania
- Java 21 lub nowsza
- Node.js 18 lub nowszy
- Firebase Project

### Konfiguracja projektu

1. Klonowanie repozytorium:
```bash
git clone https://github.com/noisemonsterproductions/nutrilog
cd Nutrilog
```

2. Uruchomienie backendu:
```bash
./gradlew bootRun
```

3. Uruchomienie frontendu:
```bash
cd frontend
npm install
npm start
```