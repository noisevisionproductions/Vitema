import {useState, useEffect} from 'react';
import {Diet} from '../types';
import {toast} from 'sonner';
import {User} from '../types/user';
import {DietService} from "../services/DietService";

export interface DietWithUser extends Diet {
    userEmail?: string;
}

export const useDiets = (_users: User[], usersLoading: boolean) => {
    const [diets, setDiets] = useState<DietWithUser[]>([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [size] = useState(10);

    const fetchDiets = async () => {
        try {
            console.log('Fetching diets...');
            setLoading(true);
            const response = await DietService.getDiets();
            console.log('Fetched diets:', response);
            setDiets(response);
        } catch (error) {
            console.error('Error fetching diets:', error);
            toast.error('Błąd podczas pobierania diet');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (usersLoading) return;
        fetchDiets().catch(console.error);
    }, [usersLoading, page, size]);


    const deleteDiet = async (id: string) => {
        try {
            console.log('Starting diet deletion, ID:', id);

            // Natychmiastowa aktualizacja UI
            setDiets(prevDiets => prevDiets.filter(diet => diet.id !== id));

            // Wywołanie API
            await DietService.deleteDiet(id);
            console.log('Diet deleted successfully, ID:', id);

            // Odśwież dane z serwera
            await fetchDiets();

            toast.success('Dieta została usunięta');
        } catch (error) {
            console.error('Diet deletion failed:', error);
            // Przywróć poprzedni stan w przypadku błędu
            await fetchDiets();
            throw error;
        }
    };
    const updateDiet = async (id: string, dietData: Partial<Diet>) => {
        try {
            await DietService.updateDiet(id, dietData);
            toast.success('Dieta została zaktualizowana');
            await fetchDiets();
        } catch (error) {
            console.error('Error updating diet:', error);
            toast.error('Błąd podczas aktualizacji diety');
        }
    };

    const createDiet = async (dietData: Omit<Diet, 'id'>) => {
        try {
            const response = await DietService.createDiet(dietData);
            toast.success('Dieta została utworzona');
            await fetchDiets();
            return response;
        } catch (error) {
            console.error('Error creating diet:', error);
            toast.error('Błąd podczas tworzenia diety');
            throw error;
        }
    };

    return {
        diets,
        loading,
        page,
        setPage,
        deleteDiet,
        updateDiet,
        createDiet,
        refreshDiets: fetchDiets
    };
};