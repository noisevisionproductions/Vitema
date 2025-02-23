import React from "react";
import {
    Sheet,
    SheetContent,
    SheetHeader,
    SheetTitle,
} from "../../ui/sheet"
import { X } from "lucide-react";
import LoadingSpinner from "../../common/LoadingSpinner";
import { useShoppingList } from "../../../hooks/useShoppingList";
import { formatTimestamp } from "../../../utils/dateFormatters";
import {Diet, Recipe, ShoppingListV3} from "../../../types";
import CategoryShoppingList from "./CategoryShoppingList";
import { useRecipes } from "../../../hooks/useRecipes";
import { getMealTypeLabel } from "../../../utils/mealTypeUtils";

interface DietViewProps {
    diet: Diet;
    onClose: () => void;
    onDelete: (dietId: string) => void;
}

const DietView: React.FC<DietViewProps> = ({ diet, onClose }) => {
    const { recipes, isLoadingRecipes } = useRecipes(diet.days);
    const { shoppingList, loading: shoppingListLoading } = useShoppingList(diet.id);

    const renderShoppingList = () => {
        if (shoppingListLoading) {
            return (
                <div className="flex justify-center py-4">
                    <LoadingSpinner />
                </div>
            );
        }

        if (!shoppingList || shoppingList.version !== 3) {
            return null;
        }

        return (
            <CategoryShoppingList
                shoppingList={shoppingList as ShoppingListV3}
                loading={shoppingListLoading}
            />
        );
    };

    const renderMetadata = () => {
        if (!diet.metadata) return null;

        return (
            <div className="text-sm text-gray-600 space-y-2">
                <p>
                    <span className="font-medium">
                        Liczba dni:
                    </span>
                    {' '}
                    {diet.metadata.totalDays || diet.days?.length || 0}
                </p>
                {diet.metadata.fileName && (
                    <p>
                        <span className="font-medium">
                            Nazwa pliku:
                        </span>
                        {' '}
                        {diet.metadata.fileName}
                    </p>
                )}
                {diet.createdAt && (
                    <p>
                        <span className="font-medium">
                            Data utworzenia:
                        </span>
                        {' '}
                        {formatTimestamp(diet.createdAt)}
                    </p>
                )}
            </div>
        );
    };

    const renderRecipeDetails = (recipe: Recipe) => (
        <div className="space-y-2">
            <p className="font-medium">{recipe.name}</p>
            <p className="text-sm text-gray-600">
                {recipe.instructions}
            </p>
            <div className="text-sm">
                <p className="font-medium">Wartości odżywcze:</p>
                <p>
                    Kalorie: {recipe.nutritionalValues?.calories || 0} kcal,{' '}
                    Białko: {recipe.nutritionalValues?.protein || 0}g,{' '}
                    Tłuszcze: {recipe.nutritionalValues?.fat || 0}g,{' '}
                    Węglowodany: {recipe.nutritionalValues?.carbs || 0}g
                </p>
            </div>
        </div>
    );

    const renderMeal = (meal: Diet['days'][0]['meals'][0], mealIndex: number) => {
        const recipe = recipes[meal.recipeId];
        if (!recipe) return null;

        return (
            <div key={mealIndex} className="bg-gray-50 p-4 rounded-lg">
                <div className="flex justify-between mb-2">
                    <span className="font-medium">
                        {getMealTypeLabel(meal.mealType)} - {meal.time}
                    </span>
                </div>
                {renderRecipeDetails(recipe)}
            </div>
        );
    };

    const renderDay = (day: Diet['days'][0], index: number) => (
        <div key={index} className="border-b pb-6 last:border-b-0">
            <h3 className="text-lg font-medium mb-4">
                Dzień {index + 1} - {formatTimestamp(day.date)}
            </h3>
            <div className="space-y-4">
                {day.meals?.map((meal, mealIndex) => renderMeal(meal, mealIndex))}
            </div>
        </div>
    );

    const renderContent = () => {
        if (isLoadingRecipes) {
            return (
                <div className="flex justify-center py-8">
                    <LoadingSpinner />
                </div>
            );
        }

        if (!diet.days || diet.days.length === 0) {
            return (
                <div className="text-center py-8 text-gray-500">
                    Brak przypisanych posiłków do tej diety.
                </div>
            );
        }

        return diet.days.map((day, index) => renderDay(day, index));
    };

    return (
        <Sheet open={true} onOpenChange={onClose}>
            <SheetContent className="w-full sm:max-w-3xl overflow-y-auto">
                <SheetHeader>
                    <div className="flex justify-between items-center border-b pb-4">
                        <SheetTitle>Szczegóły Diety</SheetTitle>
                        <button
                            onClick={onClose}
                            className="text-gray-400 hover:text-gray-500"
                        >
                            <X className="h-6 w-6" />
                        </button>
                    </div>
                </SheetHeader>

                <div className="mt-6 space-y-6">
                    {renderMetadata()}
                    {renderShoppingList()}
                    {renderContent()}
                </div>
            </SheetContent>
        </Sheet>
    );
};

export default DietView;