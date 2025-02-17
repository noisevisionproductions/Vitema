import {useEffect, useState} from "react";
import {ChangelogEntry} from "../types/changeLog";
import {useAuth} from "../contexts/AuthContext";
import {addDoc, collection, doc, getDoc, getDocs, setDoc, orderBy, query, Timestamp} from "firebase/firestore";
import {db} from "../config/firebase";
import {toast} from "sonner";

export const useChangeLog = () => {
    const [entries, setEntries] = useState<ChangelogEntry[]>([]);
    const [loading, setLoading] = useState(true);
    const [hasUnread, setHasUnread] = useState(false);
    const {currentUser} = useAuth();

    const fetchChangelog = async () => {
        try {
            const q = query(
                collection(db, 'changelog'),
                orderBy('createdAt', 'desc')
            );
            const snapshot = await getDocs(q);
            const changelogData = snapshot.docs.map(doc => ({
                id: doc.id,
                ...doc.data()
            })) as ChangelogEntry[];

            setEntries(changelogData);

            if (currentUser) {
                const userSettingsRef = doc(db, 'userSettings', currentUser.uid);
                const userSettings = await getDoc(userSettingsRef);
                const lastRead = userSettings.data()?.lastChangelogRead?.toDate() || new Date(0);

                const hasNew = changelogData.some(entry =>
                    entry.createdAt.toDate() > lastRead
                );
                setHasUnread(hasNew);
            }
        } catch (error) {
            console.error('Error fetching changelog:', error);
            toast.error('Błąd podczas pobierania historii zmian');
        } finally {
            setLoading(false);
        }
    };

    const markAsRead = async () => {
        if (!currentUser) return;

        try {
            const userSettingsRef = doc(db, 'userSettings', currentUser.uid);
            await setDoc(userSettingsRef, {
                lastChangelogRead: Timestamp.now()
            }, { merge: true });
            setHasUnread(false);
        } catch (error) {
            console.error('Error marking changelog as read:', error);
        }
    };

    const addEntry = async (entry: Omit<ChangelogEntry, 'id' | 'createdAt' | 'author'>) => {
        try {
            if (!currentUser?.email) {
                toast.error('Musisz być zalogowany, aby dodać wpis');
                return;
            }

            await addDoc(collection(db, 'changelog'), {
                ...entry,
                createdAt: Timestamp.now(),
                author: currentUser.email
            });

            toast.success('Dodano nowy wpis do historii zmian');
            await fetchChangelog();
        } catch (error) {
            console.error('Error adding changelog entry:', error);
            toast.error('Błąd podczas dodawania wpisu');
        }
    };

    useEffect(() => {
        fetchChangelog().catch(console.error);
    }, []);

    return {
        entries,
        loading,
        addEntry,
        refresh: fetchChangelog(),
        hasUnread,
        markAsRead
    };
};