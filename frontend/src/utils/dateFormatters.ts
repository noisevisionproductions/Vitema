import {Timestamp} from "firebase/firestore";

export const formatDate = (date: string | Timestamp) => {
    let dateObject: Date;

    if (date instanceof Timestamp) {
        dateObject = date.toDate();
    } else {
        const [day, month, year] = date.split('-').map(Number);
        dateObject = new Date(year, month - 1, day);
    }

    return dateObject.toLocaleDateString('pl-PL', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
};


export const formatTimestamp = (timestamp: any): string => {
    try {
        let date: Date;

        if (!timestamp) {
            return 'Brak daty';
        }

        // Obsługa obiektu timestamp z backendu
        if (timestamp.seconds !== undefined) {
            date = new Date(timestamp.seconds * 1000);
        }
        // Obsługa Firestore Timestamp
        else if (timestamp instanceof Timestamp) {
            date = timestamp.toDate();
        }
        // Obsługa zwykłej daty
        else if (timestamp instanceof Date) {
            date = timestamp;
        }
        // Obsługa timestampa jako liczby
        else if (typeof timestamp === 'number') {
            date = new Date(timestamp);
        }
        // Obsługa daty jako stringa
        else if (typeof timestamp === 'string') {
            date = new Date(timestamp);
        }
        else {
            return 'Nieprawidłowy format daty';
        }

        return date.toLocaleDateString('pl-PL', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    } catch (error) {
        console.error('Error formatting timestamp:', error);
        return 'Błąd formatu daty';
    }
};

export const convertTimestampToMillis = (timestamp: any): number | null => {
    if (!timestamp) return null;
    if (timestamp instanceof Timestamp) {
        return timestamp.toMillis();
    }
    if (typeof timestamp === 'number') {
        return timestamp;
    }
    if (timestamp?.toMillis && typeof timestamp.toMillis === 'function') {
        return timestamp.toMillis();
    }
    return null;
};

export const dateToString = (timestamp: Timestamp) => {
    const date = timestamp.toDate();
    return date.toISOString().split('T')[0];
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