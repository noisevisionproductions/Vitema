import {MealType} from "../types/diet";
import {read, utils, WorkBook} from 'xlsx';

interface ParsedMeal {
    time: string,
    mealType: MealType,
    name: string,
    instructions: string,
    nutritionalValues: {
        calories: number;
        protein: number;
        fat: number;
        carbs: number;
    };
}

export interface ParsedDay {
    date: string;
    meals: ParsedMeal[];
}

export interface ParsedExcelResult {
    days: ParsedDay[];
    shoppingList: string[];
}

export class ExcelParserService {
    private static getMealType(time: string): MealType {
        const hour = parseInt(time.split(':')[0]);

        if (hour >= 3 && hour < 9) return MealType.BREAKFAST;
        if (hour >= 9 && hour < 12) return MealType.SECOND_BREAKFAST;
        if (hour >= 12 && hour < 16) return MealType.LUNCH;
        if (hour >= 16 && hour < 19) return MealType.SNACK;
        return MealType.DINNER
    }

    private static parseNutritionalValues(value: string): {
        calories: number;
        protein: number;
        fat: number;
        carbs: number;
    } {
        // Format: "450 kcal, 30 g białka, 12 g tłuszczu, 50 g węglowodanów"
        const values = value.split(',').map(v => parseFloat(v));
        return {
            calories: values[0] || 0,
            protein: values[1] || 0,
            fat: values[2] || 0,
            carbs: values[3] || 0
        };
    }

    static async parseDietExcel(file: File): Promise<ParsedExcelResult> {
        try {
            const data = await file.arrayBuffer();
            const workbook: WorkBook = read(data);
            const worksheet = workbook.Sheets[workbook.SheetNames[0]];
            const jsonData = utils.sheet_to_json<string[]>(worksheet, {
                header: 1,
                raw: false,
                defval: ''
            }) as string[][];

            const days: ParsedDay[] = [];
            let currentDay: ParsedDay | null = null;
            let shoppingList: string[] = [];

            const firstDataRow = jsonData[1];
            if (firstDataRow && firstDataRow.length >= 5) {
                const shoppingListText = firstDataRow[4];
                if (shoppingListText) {
                    shoppingList = shoppingListText
                        .split(',')
                        .map(item => item.trim())
                        .filter(item => item.length > 0);
                }
            }

            // Pomijamy wiersz nagłówkowy
            for (let i = 1; i < jsonData.length; i++) {
                const row = jsonData[i] as any[];
                if (!row[0]) continue;

                const time = row[0];
                const [datePart] = time.split(' ');
                const date = datePart.replace(',', '').trim();

                if (!currentDay || currentDay.date !== date) {
                    if (currentDay) days.push(currentDay);
                    currentDay = {
                        date,
                        meals: []
                    };
                }

                if (currentDay) {
                    currentDay.meals.push({
                        time: time.split(' ')[1],
                        mealType: this.getMealType(time.split(' ')[1]),
                        name: row[1],
                        instructions: row[2],
                        nutritionalValues: this.parseNutritionalValues(row[3])
                    });
                }
            }

            if (currentDay) days.push(currentDay);

            return {
                days,
                shoppingList
            };
        } catch (error) {
            console.error('Error parsing Excel file:', error);
            throw new Error('Błąd podczas parsowania pliku Excel');
        }
    }
}