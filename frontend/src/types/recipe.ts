import { Timestamp } from 'firebase/firestore';
import { MealType } from './meal';

export interface Recipe {
    id: string;
    name: string;
    instructions: string;
    createdAt: Timestamp;
    photos: string[];
    nutritionalValues: NutritionalValues;
    parentRecipeId: string | null;
}

export interface NutritionalValues {
    calories: number;
    protein: number;
    fat: number;
    carbs: number;
}

export interface RecipeReference {
    recipeId: string;
    dietId: string;
    userId: string;
    mealType: MealType;
    addedAt: Timestamp;
}