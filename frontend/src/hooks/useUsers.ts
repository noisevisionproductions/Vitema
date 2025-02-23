import {useCallback, useEffect, useState} from "react";
import {collection, doc, getDoc, getDocs} from "firebase/firestore";
import {db} from "../config/firebase";
import {toast} from "sonner";
import {User} from "../types/user";
import {formatTimestamp} from "../utils/dateFormatters";

export default function useUsers() {
    const [users, setUsers] = useState<User[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchUsers().catch();
    }, []);

    const fetchUsers = async () => {
        try {
            const usersCollection = collection(db, 'users');
            const usersSnapshot = await getDocs(usersCollection);
            const userData = usersSnapshot.docs.map(doc => ({
                id: doc.id,
                ...doc.data(),
                createdAt: doc.data().createdAt?.toMillis?.() || doc.data().createdAt,
                birthDate: doc.data().birthDate?.toMillis?.() || doc.data().birthDate,
                note: doc.data().note || ''
            })) as User[];
            setUsers(userData);
        } catch (error) {
            console.error('Error fetchin users:', error);
            toast.error('Błąd podczas pobierania użytkowników');
        } finally {
            setLoading(false);
        }
    };

    const getUserById = useCallback(async (userId: string): Promise<User | null> => {
        try {
            const userDoc = await getDoc(doc(db, 'users', userId));
            if (userDoc.exists()) {
                const userData = userDoc.data();
                return {
                    id: userDoc.id,
                    ...userData,
                    createdAt: formatTimestamp(userData.createdAt),
                    birthDate: formatTimestamp(userData.birthDate),
                    note: userData.note || ''
                } as unknown as User;
            }
            return null;
        } catch (error) {
            console.error('Error fetching user:', error);
            toast.error('Błąd podczas pobierania użytkownika');
            return null;
        }
    }, []);

    useEffect(() => {
        fetchUsers().catch();
    }, []);

    return {users, loading, fetchUsers, getUserById};
}