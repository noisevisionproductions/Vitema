import { useState, useEffect } from 'react';
import { Diet } from '../types';
import { toast } from 'sonner';
import { User } from '../types/user';
import api from '../config/axios';

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
            setLoading(true);
            const response = await api.get('/diets', {
                params: {
                    page,
                    size
                }
            });
            setDiets(response.data);
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
            await api.delete(`/diets/${id}`);
            toast.success('Dieta została usunięta');
            await fetchDiets();
        } catch (error) {
            console.error('Error deleting diet:', error);
            toast.error('Błąd podczas usuwania diety');
        }
    };

    const updateDiet = async (id: string, dietData: Partial<Diet>) => {
        try {
            await api.put(`/diets/${id}`, dietData);
            toast.success('Dieta została zaktualizowana');
            await fetchDiets();
        } catch (error) {
            console.error('Error updating diet:', error);
            toast.error('Błąd podczas aktualizacji diety');
        }
    };

    const createDiet = async (dietData: Omit<Diet, 'id'>) => {
        try {
            const response = await api.post('/diets', dietData);
            toast.success('Dieta została utworzona');
            await fetchDiets();
            return response.data;
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