import {useState, useEffect} from 'react';
import {doc, getDoc} from 'firebase/firestore';
import {db} from '../config/firebase';
import {toast} from 'sonner';
import {Diet, Recipe} from "../types";

export const useRecipes = (days: Diet['days']) => {
    const [recipes, setRecipes] = useState<{ [key: string]: Recipe }>({});
    const [isLoadingRecipes, setIsLoadingRecipes] = useState(true);

    useEffect(() => {
        const fetchRecipes = async () => {
            try {
                if (!days || days.length === 0) {
                    setIsLoadingRecipes(false);
                    return;
                }

                const recipeIds = new Set(
                    days.flatMap((day: { meals: any[]; }) => day.meals?.map(meal => meal.recipeId) || [])
                );

                const recipesData: { [key: string]: Recipe } = {};
                for (const id of recipeIds) {
                    if (!id) continue;

                    const docs = await getDoc(doc(db, 'recipes', id));
                    if (docs.exists()) {
                        recipesData[id] = {id: docs.id, ...docs.data()} as Recipe;
                    }
                }
                setRecipes(recipesData);
            } catch (error) {
                console.error('Error fetching recipes:', error);
                toast.error('Błąd podczas pobierania przepisów');
            } finally {
                setIsLoadingRecipes(false);
            }
        };

        fetchRecipes().catch();
    }, [days]);

    return {recipes, isLoadingRecipes};
};