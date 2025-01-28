import {Timestamp} from 'firebase/firestore';

export interface Recipe {
    id: string;
    name: string;
    instructions: string;
    createdAt: Timestamp;
    photos: string[];
    nutritionalValues: {
        calories: number;
        protein: number;
        fat: number;
        carbs: number;
    };
    parentRecipeId: string | null;
}

export interface RecipeReference {
    recipeId: string;
    dietId: string;
    userId: string;
    mealType: MealType;
    addedAt: Timestamp;
}

export interface Diet {
    id: string;
    userId: string;
    createdAt: Timestamp;
    updatedAt: Timestamp;
    days: Day[];
    metadata: {
        totalDays: number;
        fileName: string;
        fileUrl: string;
    }
}

export interface Day {
    date: string;
    meals: DayMeal[];
}

export interface DayMeal {
    recipeId: string;
    mealType: MealType;
    time: string;
}

export interface ParsedMeal {
    name: string;
    instructions: string;
    nutritionalValues: {
        calories: number;
        protein: number;
        fat: number;
        carbs: number;
    };
    mealType: MealType;
    time: string;
}

export interface ParsedDay {
    date: string;
    meals: ParsedMeal[];
}

export interface ParsedDietData {
    days: ParsedDay[];
    shoppingList: string[];
}

export interface ShoppingList {
    id: string;
    userId: string;
    dietId: string;
    items: string[];
    createdAt: Timestamp;
    startDate: string;
    endDate: string;
}

export enum MealType {
    BREAKFAST = 'BREAKFAST',
    SECOND_BREAKFAST = 'SECOND_BREAKFAST',
    LUNCH = 'LUNCH',
    SNACK = 'SNACK',
    DINNER = 'DINNER'
}