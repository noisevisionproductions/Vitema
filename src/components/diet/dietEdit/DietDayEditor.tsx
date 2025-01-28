import React from 'react';
import { Day, MealType, Recipe } from '../../../types/diet';
import DietMealEditor from './DietMealEditor';

interface DietDayEditorProps {
    day: Day;
    dayIndex: number;
    recipes: { [key: string]: Recipe };
    onDateUpdate: (dayIndex: number, newDate: string) => void;
    onMealTimeUpdate: (dayIndex: number, mealIndex: number, newTime: string) => void;
}

const DietDayEditor: React.FC<DietDayEditorProps> = ({
                                                         day,
                                                         dayIndex,
                                                         recipes,
                                                         onDateUpdate,
                                                         onMealTimeUpdate
                                                     }) => {
    const formatDateForInput = (dateStr: string) => {
        const [day, month, year] = dateStr.split('.');
        return `${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}`;
    };

    const handleDateChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const date = new Date(e.target.value);
        const formattedDate = `${String(date.getDate()).padStart(2, '0')}.${String(date.getMonth() + 1).padStart(2, '0')}.${date.getFullYear()}`;
        onDateUpdate(dayIndex, formattedDate);
    };

    const sortedMeals = [...day.meals].sort((a, b) => {
        const timeA = a.time.split(':').map(Number);
        const timeB = b.time.split(':').map(Number);
        return (timeA[0] * 60 + timeA[1]) - (timeB[0] * 60 + timeB[1]);
    });

    const getMealTypeLabel = (mealType: MealType) => {
        const labels: { [key in MealType]: string } = {
            [MealType.BREAKFAST]: 'Śniadanie',
            [MealType.SECOND_BREAKFAST]: 'Drugie śniadanie',
            [MealType.LUNCH]: 'Obiad',
            [MealType.SNACK]: 'Przekąska',
            [MealType.DINNER]: 'Kolacja'
        };
        return labels[mealType];
    };
    return (
        <div className="border-b pb-6 last:border-b-0">
            <div className="flex items-center gap-4 mb-4">
                <h3 className="text-lg font-medium">Dzień {dayIndex + 1}</h3>
                <input
                    type="date"
                    value={formatDateForInput(day.date)}
                    onChange={handleDateChange}
                    className="border rounded-md px-2 py-1"
                />
            </div>

            <div className="space-y-4">
                {sortedMeals.map((meal, mealIndex) => (
                    <DietMealEditor
                        key={mealIndex}
                        meal={meal}
                        mealIndex={mealIndex}
                        dayIndex={dayIndex}
                        recipeName={recipes[meal.recipeId]?.name}
                        mealType={getMealTypeLabel(meal.mealType)}
                        onTimeUpdate={onMealTimeUpdate}
                    />
                ))}
            </div>
        </div>
    );
};

export default DietDayEditor;