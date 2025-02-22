export const getTimestamp = (timestamp: any): number => {
    if (!timestamp) return 0;

    // Jeśli to obiekt Timestamp z Firestore
    if (timestamp.toMillis) {
        return timestamp.toMillis();
    }

    // Jeśli to obiekt z backend
    if (timestamp.seconds) {
        return timestamp.seconds * 1000;
    }

    // Jeśli to string ISO
    if (typeof timestamp === 'string') {
        return new Date(timestamp).getTime();
    }

    return 0;
};