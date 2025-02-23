import { useState, useEffect } from 'react';
import { toast } from 'sonner';
import { Diet, Recipe } from "../types";
import { RecipeService } from "../services/RecipeService";

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

                const recipeIds = Array.from(new Set(
                    days.flatMap(day => day.meals?.map(meal => meal.recipeId) || [])
                )).filter(Boolean);

                if (recipeIds.length === 0) {
                    setIsLoadingRecipes(false);
                    return;
                }

                const recipesData = await RecipeService.getRecipesByIds(recipeIds);
                const recipesMap = recipesData.reduce((acc, recipe) => {
                    acc[recipe.id] = recipe;
                    return acc;
                }, {} as { [key: string]: Recipe });

                setRecipes(recipesMap);
            } catch (error) {
                console.error('Error fetching recipes:', error);
                toast.error('Błąd podczas pobierania przepisów');
            } finally {
                setIsLoadingRecipes(false);
            }
        };

        fetchRecipes().catch();
    }, [days]);

    const updateRecipe = async (id: string, data: {
        name: string;
        instructions: string;
        nutritionalValues?: {
            calories: number;
            protein: number;
            fat: number;
            carbs: number;
        };
    }) => {
        try {
            const updatedRecipe = await RecipeService.updateRecipe(id, data);
            setRecipes(prev => ({
                ...prev,
                [id]: updatedRecipe
            }));
            toast.success('Przepis został zaktualizowany');
            return updatedRecipe;
        } catch (error) {
            console.error('Error updating recipe:', error);
            toast.error('Błąd podczas aktualizacji przepisu');
            throw error;
        }
    };

    return {
        recipes,
        isLoadingRecipes,
        updateRecipe
    };
};