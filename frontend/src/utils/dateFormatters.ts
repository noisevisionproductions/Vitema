import {Timestamp} from 'firebase/firestore';

/**
 * Uniwersalna funkcja do formatowania dat w aplikacji
 */
export const formatTimestamp = (timestamp: number | Date | Timestamp | {
    seconds: number,
    nanoseconds: number
} | string) => {
    let date: Date;

    if (timestamp instanceof Date) {
        date = timestamp;
    } else if (typeof timestamp === 'number') {
        date = new Date(timestamp);
    } else if (timestamp instanceof Timestamp) {
        date = timestamp.toDate();
    } else if (typeof timestamp === 'string') {
        // Obsługa string ISO oraz formatu dd-mm-yyyy
        if (timestamp.includes('-')) {
            const parts = timestamp.split('-');
            if (parts.length === 3 && parts[0].length === 2) {
                // Format dd-mm-yyyy
                const [day, month, year] = parts.map(Number);
                date = new Date(year, month - 1, day);
            } else {
                // Format ISO
                date = new Date(timestamp);
            }
        } else {
            date = new Date(timestamp);
        }
    } else if (timestamp && typeof timestamp === 'object' && 'seconds' in timestamp) {
        date = new Date(timestamp.seconds * 1000);
    } else {
        console.warn('Invalid timestamp format:', timestamp);
        date = new Date();
    }

    if (isNaN(date.getTime())) {
        console.warn('Invalid date created from timestamp:', timestamp);
        date = new Date();
    }

    return date.toLocaleDateString('pl-PL', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
};

/**
 * Konwersja dowolnego formatu daty na Timestamp (do zapisu w Firestore)
 */
export const toFirestoreTimestamp = (date: Date | string | number | Timestamp): Timestamp => {
    if (date instanceof Timestamp) {
        return date;
    }
    if (date instanceof Date) {
        return Timestamp.fromDate(date);
    }
    if (typeof date === 'string') {
        return Timestamp.fromDate(new Date(date));
    }
    return Timestamp.fromMillis(date);
};

/**
 * Konwersja Timestamp na format ISO (YYYY-MM-DD)
 */
export const toISODate = (timestamp: Timestamp | Date | string | number): string => {
    let date: Date;

    if (timestamp instanceof Timestamp) {
        date = timestamp.toDate();
    } else if (timestamp instanceof Date) {
        date = timestamp;
    } else if (typeof timestamp === 'string') {
        date = new Date(timestamp);
    } else {
        date = new Date(timestamp);
    }

    return date.toISOString().split('T')[0];
};

/**
 * Pomocnicza funkcja do porównywania dat
 */
export const getTimestamp = (date: any): number => {
    if (date instanceof Date) {
        return date.getTime();
    }
    if (date instanceof Timestamp) {
        return date.toMillis();
    }
    if (typeof date === 'number') {
        return date;
    }
    if (typeof date === 'string') {
        return new Date(date).getTime();
    }
    if (date && typeof date === 'object' && 'seconds' in date) {
        return date.seconds * 1000;
    }
    return 0;
};

export const formatMonthYear = (date: Date): string => {
    return `${date.getMonth() + 1}/${date.getFullYear()}`;
};

export const stringToTimestamp = (dateString: string) => {
    const date = new Date(dateString);
    return Timestamp.fromDate(date);
};

export const calculateAge = (birthDate: number | null): number => {
    if (!birthDate) return 0;

    const today = new Date();
    const birthDateObj = new Date(birthDate);

    let age = today.getFullYear() - birthDateObj.getFullYear();
    const monthDiff = today.getMonth() - birthDateObj.getMonth();

    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDateObj.getDate())) {
        age--;
    }

    return age;
};