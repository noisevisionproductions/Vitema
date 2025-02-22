import React, {useState} from 'react';
import DietView from './view/DietView';
import DietEditModal from "./edit/DietEditModal";
import LoadingSpinner from "../common/LoadingSpinner";
import DietCard from "./DietCard";
import useUsers from "../../hooks/useUsers";
import DietFilter, {SortOption} from "./DietFilter";
import {Diet} from "../../types";
import {useDiets} from "../../hooks/useDiets";
import {toast} from "sonner";
import {getTimestamp} from "../../utils/dateUtils";

interface DietWithUser extends Diet {
    userEmail?: string;
}

const DietManagement: React.FC = () => {
    const {users, loading: usersLoading} = useUsers();
    const {
        diets,
        loading: dietsLoading,
        deleteDiet,
        updateDiet,
        refreshDiets
    } = useDiets(users, usersLoading);

    const [selectedDiet, setSelectedDiet] = useState<DietWithUser | null>(null);
    const [editingDiet, setEditingDiet] = useState<DietWithUser | null>(null);
    const [selectedUserId, setSelectedUserId] = useState<string | null>(null);
    const [searchQuery, setSearchQuery] = useState('');
    const [sortBy, setSortBy] = useState<SortOption>('newest');

    const filteredAndSortedDiets = React.useMemo(() => {
        let result = diets.filter(diet => {
            const matchesUser = selectedUserId ? diet.userId === selectedUserId : true;
            const matchesSearch = diet.userEmail?.toLowerCase().includes(searchQuery.toLowerCase()) ||
                diet.metadata?.fileName?.toLowerCase().includes(searchQuery.toLowerCase());
            return matchesUser && matchesSearch;
        });

        return result.sort((a, b) => {
            switch (sortBy) {
                case "newest":
                    return getTimestamp(b.createdAt) - getTimestamp(a.createdAt);
                case "oldest":
                    return getTimestamp(a.createdAt) - getTimestamp(b.createdAt);
                case "name":
                    return (a.metadata?.fileName || '').localeCompare(b.metadata?.fileName || '');
                default:
                    return 0;
            }
        });
    }, [diets, selectedUserId, searchQuery, sortBy]);

    const handleResetFilters = () => {
        setSelectedUserId(null);
        setSearchQuery('');
        setSortBy('newest');
    };

    const handleDietUpdate = async (diet: Diet) => {
        try {
            await updateDiet(diet.id, diet);
            setEditingDiet(null);
            await refreshDiets();
            toast.success('Dieta została zaktualizowana');
        } catch (error) {
            toast.error('Błąd podczas aktualizacji diety');
        }
    };

    const handleDietDelete = async (dietId: string) => {
        try {
            await deleteDiet(dietId);
            setSelectedDiet(null);
            setEditingDiet(null);
            await refreshDiets();
            toast.success('Dieta została usunięta');
        } catch (error) {
            toast.error('Błąd podczas usuwania diety');
        }
    };

    const activeUsers = React.useMemo(() => {
        const uniqueUserIds = new Set(diets.map(diet => diet.userId));
        return users.filter(user => uniqueUserIds.has(user.id));
    }, [diets, users]);

    if (dietsLoading || usersLoading) {
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
                sortBy={sortBy}
                onSortChange={setSortBy}
            />

            <div className="flex justify-between items-center">
                <span className="text-gray-500">
                    Liczba diet: {filteredAndSortedDiets.length}
                </span>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {filteredAndSortedDiets.map(diet => (
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
                    onDelete={handleDietDelete}
                />
            )}

            {editingDiet && (
                <DietEditModal
                    diet={editingDiet}
                    onClose={() => setEditingDiet(null)}
                    onUpdate={async (updatedDiet) => await handleDietUpdate(updatedDiet)}
                />
            )}
        </div>
    );
};

export default DietManagement;