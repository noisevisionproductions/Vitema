import React, {useState, useEffect} from 'react';
import {collection, getDocs} from 'firebase/firestore';
import {db} from '../../config/firebase';
import {Diet} from '../../types/diet';
import {User} from '../../types/user';
import DietView from './DietView';
import {toast} from 'sonner';
import DietEditModal from "./dietEdit/DietEditModal";
import LoadingSpinner from "../common/LoadingSpinner";
import DietCard from "./DietCard";
import useUsers from "../../hooks/useUsers";
import DietFilter from "./DietFilter";

interface DietWithUser extends Diet {
    userEmail?: string;
}

interface UsersMap {
    [key: string]: User;
}

const DietManagement: React.FC = () => {
    const {users, loading: usersLoading} = useUsers();
    const [diets, setDiets] = useState<DietWithUser[]>([]);
    const [loading, setLoading] = useState(true);
    const [selectedDiet, setSelectedDiet] = useState<DietWithUser | null>(null);
    const [editingDiet, setEditingDiet] = useState<DietWithUser | null>(null);
    const [selectedUserId, setSelectedUserId] = useState<string | null>(null);
    const [searchQuery, setSearchQuery] = useState('');

    const fetchDiets = async () => {
        try {
            const dietsCollection = collection(db, 'diets');
            const dietsSnapshot = await getDocs(dietsCollection);
            const dietsData = dietsSnapshot.docs.map(doc => {
                const data = doc.data();
                return {
                    id: doc.id,
                    ...data,
                    metadata: data.metadata || {totalDays: data.days?.length || 0},
                    days: data.days || []
                } as Diet;
            });

            const usersMap = users.reduce<UsersMap>((acc, user) => ({
                ...acc,
                [user.id]: user
            }), {});

            const dietsWithUsers = dietsData.map(diet => ({
                ...diet,
                userEmail: usersMap[diet.userId]?.email || 'Nieznany użytkownik'
            }));

            setDiets(dietsWithUsers);
        } catch (error) {
            console.error('Error fetching diets:', error);
            toast.error('Błąd podczas pobierania diet');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (!usersLoading) {
            fetchDiets().catch();
        }
    }, [usersLoading, users]);

    const filteredDiets = diets.filter(diet => {
        const matchesUser = selectedUserId ? diet.userId === selectedUserId : true;
        const matchesSearch = diet.userEmail?.toLowerCase().includes(searchQuery.toLowerCase()) ||
            diet.metadata?.fileName?.toLowerCase().includes(searchQuery.toLowerCase());
        return matchesUser && matchesSearch;
    });

    const handleResetFilters = () => {
        setSelectedUserId(null);
        setSearchQuery('');
    };

    const activeUsers = React.useMemo(() => {
        const uniqueUserIds = new Set(diets.map(diet => diet.userId));
        return users.filter(user => uniqueUserIds.has(user.id));
    }, [diets, users]);

    if (loading || usersLoading) {
        return (
            <div className="flex justify-center items-center h-64">
                <LoadingSpinner/>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <DietFilter
                activeUsers={activeUsers}
                selectedUserId={selectedUserId}
                onUserSelect={setSelectedUserId}
                searchQuery={searchQuery}
                onSearchChange={setSearchQuery}
                onReset={handleResetFilters}
            />

            <div className="flex justify-between items-center">
                <span className="text-gray-500">
                    Liczba diet: {filteredDiets.length}
                </span>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {filteredDiets.map(diet => (
                    <DietCard
                        key={diet.id}
                        diet={diet}
                        onViewClick={() => setSelectedDiet(diet)}
                        onEditClick={() => setEditingDiet(diet)}
                    />
                ))}
            </div>

            {selectedDiet && (
                <DietView
                    diet={selectedDiet}
                    onClose={() => setSelectedDiet(null)}
                />
            )}

            {editingDiet && (
                <DietEditModal
                    diet={editingDiet}
                    onClose={() => setEditingDiet(null)}
                    onUpdate={fetchDiets}
                />
            )}
        </div>
    );
};

export default DietManagement;