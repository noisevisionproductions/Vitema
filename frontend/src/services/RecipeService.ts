import api from "../config/axios";
import {Recipe, NutritionalValues} from "../types";

interface RecipeUpdateData {
    name: string;
    instructions: string;
    nutritionalValues?: NutritionalValues;
}

export class RecipeService {
    private static readonly BASE_URL = '/recipes';

    static async getRecipeById(id: string): Promise<Recipe> {
        const response = await api.get(`${this.BASE_URL}/${id}`);
        return response.data;
    }

    static async getRecipesByIds(ids: string[]): Promise<Recipe[]> {
        const response = await api.get(`${this.BASE_URL}/batch`, {
            params: {ids: ids.join(',')}
        });
        return response.data;
    }

    static async updateRecipe(id: string, data: Partial<Recipe>): Promise<Recipe> {
        const updateData: RecipeUpdateData = {
            name: data.name || '',
            instructions: data.instructions || '',
            nutritionalValues: data.nutritionalValues
        };

        const response = await api.put(`${this.BASE_URL}/${id}`, updateData);
        return response.data;
    }
}