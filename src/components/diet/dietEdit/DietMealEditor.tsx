import React from 'react';
import { DayMeal } from '../../../types/diet';

interface DietMealEditorProps {
    meal: DayMeal;
    mealIndex: number;
    dayIndex: number;
    recipeName: string;
    mealType: string;
    onTimeUpdate: (dayIndex: number, mealIndex: number, newTime: string) => void;
}

const DietMealEditor: React.FC<DietMealEditorProps> = ({
                                                           meal,
                                                           mealIndex,
                                                           dayIndex,
                                                           recipeName,
                                                           mealType,
                                                           onTimeUpdate
                                                       }) => {
    const formatTimeForInput = (time: string) => {
        const [hours, minutes] = time.split(':');
        return `${hours.padStart(2, '0')}:${minutes.padStart(2, '0')}`;
    };

    const handleTimeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const [hours, minutes] = e.target.value.split(':');
        const formattedTime = `${parseInt(hours)}:${minutes}`;
        onTimeUpdate(dayIndex, mealIndex, formattedTime);
    };

    return (
        <div className="bg-gray-50 p-4 rounded-lg">
            <div className="flex flex-col gap-2">
                <div className="flex items-center gap-4">
                    <input
                        type="time"
                        value={formatTimeForInput(meal.time)}
                        onChange={handleTimeChange}
                        className="border rounded-md px-2 py-1 w-32"
                    />
                    <span className="font-medium text-gray-600">
                        {mealType}:
                    </span>
                </div>
                <div className="ml-36 text-gray-800">
                    {recipeName || 'Przepis niedostępny'}
                </div>
            </div>
        </div>
    );
};

export default DietMealEditor;