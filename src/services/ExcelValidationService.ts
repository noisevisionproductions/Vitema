import {read, utils, WorkBook} from 'xlsx';
import {ExcelParserService, ParsedDay} from "./ExcelParserService";
import {collection, query, where, getDocs} from 'firebase/firestore';
import {db} from "../config/firebase";

interface ValidationError {
    row: number;
    column: string;
    message: string;
    type: 'warning' | 'error';
}

interface ValidationResult {
    isValid: boolean;
    errors: ValidationError[];
    warnings: ValidationError[];
    data?: ParsedDay[];
}

interface ColumnDefinition {
    key: string;
    aliases: string[];
    required: boolean;
    validate?: (value: any, row: number) => ValidationError | null;
}

interface NutritionalValues {
    calories: number;
    protein: number;
    fat: number;
    carbs: number;
}

export class ExcelValidationService {
    private static readonly columnDefinitions: ColumnDefinition[] = [
        {
            key: 'dateTime',
            aliases: ['data i godzina', 'data igodzina', 'data', 'datetime'],
            required: true,
            validate: (value: string, row: number) => {
                if (!value || !ExcelValidationService.isValidDateTime(value)) {
                    return {
                        row,
                        column: 'Data i godzina',
                        message: 'Nieprawidłowy format daty i godziny (wymagany format: DD.MM.RRRR, GG:MM)',
                        type: 'error'
                    };
                }
                return null;
            }
        },
        {
            key: 'mealName',
            aliases: ['nazwa posilku', 'nazwa posiłku', 'posilek', 'posiłek', 'meal name', 'meal'],
            required: true,
            validate: (value: string, row: number) => {
                if (!value) {
                    return {
                        row,
                        column: 'Nazwa posiłku',
                        message: 'Brak nazwy posiłku',
                        type: 'error'
                    };
                }
                if (value.length > 100) {
                    return {
                        row,
                        column: 'Nazwa posiłku',
                        message: 'Nazwa posiłku jest bardzo długa (ponad 100 znaków)',
                        type: 'warning'
                    };
                }
                return null;
            }
        },
        {
            key: 'instructions',
            aliases: ['sposob przygotowania', 'sposób przygotowania', 'przygotowanie', 'instructions', 'preparation'],
            required: true,
            validate: (value: string, row: number) => {
                if (!value) {
                    return {
                        row,
                        column: 'Sposób przygotowania',
                        message: 'Brak sposobu przygotowania',
                        type: 'warning'
                    };
                }
                return null;
            }
        },
        {
            key: 'nutritionalValues',
            aliases: ['wartosci odzywcze', 'wartości odżywcze', 'kalorie', 'calories', 'nutritional values'],
            required: true,
            validate: (value: string, row: number) => {
                // Reszta kodu walidacji pozostaje bez zmian
                if (!value) {
                    return {
                        row,
                        column: 'Wartości odżywcze',
                        message: 'Brak wartości odżywczych',
                        type: 'error'
                    };
                }

                if (!ExcelValidationService.isValidNutritionalValues(value)) {
                    return {
                        row,
                        column: 'Wartości odżywcze',
                        message: 'Nieprawidłowy format wartości odżywczych (wymagany format: kalorie,białko,tłuszcze,węglowodany)',
                        type: 'error'
                    };
                }

                const values = ExcelValidationService.parseNutritionalValues(value);
                if (values.calories < 100 || values.calories > 1500) {
                    return {
                        row,
                        column: 'Wartości odżywcze',
                        message: 'Nietypowa wartość kaloryczna posiłku (powinna być między 100 a 1500 kcal)',
                        type: 'warning'
                    };
                }

                return null;
            },
        },
        {
            key: 'shoppingList',
            aliases: ['lista zakupow', 'lista zakupów', 'zakupy', 'shopping list', 'ingredients'],
            required: true,
            validate: (value: string, row: number) => {
                if (!value || row === 1) {
                    return {
                        row,
                        column: 'Lista zakupów',
                        message: 'Brak listy zakupów',
                        type: 'warning'
                    };
                }
                return null;
            }
        }
    ];

    private static normalizeString(text: string): string {
        return text
            .toLowerCase()
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '')
            .replace(/[^a-z0-9]/g, '');
    }

    private static findColumnIndex(headers: string[], columnDef: ColumnDefinition): number {
        const normalizedHeaders = headers.map(header => this.normalizeString(header));
        return normalizedHeaders.findIndex(header =>
            columnDef.aliases.some(alias => header.includes(this.normalizeString(alias)))
        );
    }
    private static validateExcelStructure(workbook: WorkBook): {
        isValid: boolean;
        columnIndexes: { [key: string]: number };
        missingColumns: string[];
    } {
        const worksheet = workbook.Sheets[workbook.SheetNames[0]];
        const headers = utils.sheet_to_json(worksheet, {header: 1})[0] as string[];

        const columnIndexes: { [key: string]: number } = {};
        const missingColumns: string[] = [];

        this.columnDefinitions.forEach(columnDef => {
            const index = this.findColumnIndex(headers, columnDef);
            if (index !== -1) {
                columnIndexes[columnDef.key] = index;
            } else if (columnDef.required) {
                missingColumns.push(columnDef.aliases[0]);
            }
        });

        return {
            isValid: missingColumns.length === 0,
            columnIndexes,
            missingColumns
        };
    }

    private static validateRow(row: any[], rowIndex: number, columnIndexes: { [key: string]: number }): {
        errors: ValidationError[];
        warnings: ValidationError[];
    } {
        const errors: ValidationError[] = [];
        const warnings: ValidationError[] = [];

        this.columnDefinitions.forEach(columnDef => {
            const columnIndex = columnIndexes[columnDef.key];
            if (columnIndex !== undefined && columnDef.validate) {
                const value = row[columnIndex];
                const validationResult = columnDef.validate(value, rowIndex + 1);
                if (validationResult) {
                    if (validationResult.type === 'error') {
                        errors.push(validationResult);
                    } else {
                        warnings.push(validationResult);
                    }
                }
            }
        });

        return {errors, warnings};
    }

    private static async validateMealsPerDay(jsonData: any[][], columnIndexes: {
        [key: string]: number
    }): Promise<ValidationError[]> {
        const warnings: ValidationError[] = [];
        const datesInRows: { [key: string]: number[] } = {};

        jsonData.slice(1).forEach((row, index) => {
            const dateTime = row[columnIndexes.dateTime];
            if (dateTime) {
                const date = dateTime.split(' ')[0];
                if (!datesInRows[date]) {
                    datesInRows[date] = [];
                }
                datesInRows[date].push(index + 2); // +2 bo indeksujemy od 1 i pomijamy nagłówek
            }
        });

        Object.entries(datesInRows).forEach(([date, rows]) => {
            if (rows.length > 7) {
                warnings.push({
                    row: rows[0],
                    column: 'Data i godzina',
                    message: `Nietypowo dużo posiłków w dniu ${date} (${rows.length} posiłków)`,
                    type: 'warning'
                });
            }
        });

        return warnings;
    }

    private static isValidDateTime(value: string): boolean {
        const pattern = /^\d{2}\.\d{2}\.\d{4}, \d{1,2}:\d{2}$/;  // np. "26.01.2025, 6:30"
        if (!pattern.test(value)) return false;

        const [datePart, timePart] = value.split(', ');
        const [day, month, year] = datePart.split('.');
        const [hours, minutes] = timePart.split(':');

        // Konwersja na Date do sprawdzenia poprawności
        const date = new Date(
            parseInt(year),
            parseInt(month) - 1, // miesiące w JS są 0-based
            parseInt(day),
            parseInt(hours),
            parseInt(minutes)
        );

        return !isNaN(date.getTime());
    }

    private static isValidNutritionalValues(value: string): boolean {
        if (!value) return false;
        const values = value.split(',').map(v => parseFloat(v.trim()));
        return values.length === 4 && values.every(v => !isNaN(v) && v >= 0);
    }

    private static parseNutritionalValues(value: string): NutritionalValues {
        const values = value.split(',').map(v => parseFloat(v.trim()));
        return {
            calories: values[0] || 0,
            protein: values[1] || 0,
            fat: values[2] || 0,
            carbs: values[3] || 0
        };
    }

    private static async checkExistingDiets(userId: string, dates: string[]): Promise<string[]> {
        try {
            const dietsRef = collection(db, 'diets');
            const q = query(dietsRef, where('userId', '==', userId));
            const querySnapshot = await getDocs(q);

            const conflictingDates = new Set<string>();
            querySnapshot.forEach(doc => {
                const diet = doc.data();
                diet.days?.forEach((day: { date: string }) => {
                    if (dates.includes(day.date)) {
                        conflictingDates.add(day.date);
                    }
                });
            });

            const conflictingDatesArray = Array.from(conflictingDates);
            if (conflictingDatesArray.length > 0) {
                return conflictingDatesArray;
            }
        } catch (error) {
            console.error('Error checking existing diets:', error);
        }
        return [];
    }

    static async validateDietExcel(file: File, userId: string): Promise<ValidationResult> {
        try {
            const data = await file.arrayBuffer();
            const workbook: WorkBook = read(data);

            // 1. Walidacja struktury
            const structureValidation = this.validateExcelStructure(workbook);
            if (!structureValidation.isValid) {
                return {
                    isValid: false,
                    errors: [{
                        row: 0,
                        column: 'headers',
                        message: `Brak wymaganych kolumn: ${structureValidation.missingColumns.join(', ')}`,
                        type: 'error'
                    }],
                    warnings: []
                };
            }

            const worksheet = workbook.Sheets[workbook.SheetNames[0]];
            const jsonData = utils.sheet_to_json(worksheet, {header: 1}) as any[][];
            const {columnIndexes} = structureValidation;

            // 2. Walidacja poszczególnych wierszy
            const allErrors: ValidationError[] = [];
            const allWarnings: ValidationError[] = [];

            for (let i = 1; i < jsonData.length; i++) {
                const row = jsonData[i];
                if (!row.length) continue;

                const {errors, warnings} = this.validateRow(row, i, columnIndexes);
                allErrors.push(...errors);
                allWarnings.push(...warnings);
            }

            // 3. Walidacja liczby posiłków w dniu
            const mealsWarnings = await this.validateMealsPerDay(jsonData, columnIndexes);
            allWarnings.push(...mealsWarnings);

            // 4. Sprawdzenie konfliktów z istniejącymi dietami
            const dates = new Set(
                jsonData.slice(1)
                    .map(row => row[columnIndexes.dateTime]?.split(' ')[0])
                    .filter(Boolean)
            );

            const existingDiets = await this.checkExistingDiets(userId, Array.from(dates));
            if (existingDiets.length > 0) {
                allWarnings.push({
                    row: 0,
                    column: 'file',
                    message: `Użytkownik ma już diety w dniach: ${existingDiets.join(', ')}`,
                    type: 'warning'
                });
            }

            // 5. Zwrócenie wyniku walidacji
            if (allErrors.length > 0) {
                return {
                    isValid: false,
                    errors: allErrors,
                    warnings: allWarnings
                };
            }

            try {
                // Parsowanie danych po pomyślnej walidacji
                const parsedData = await ExcelParserService.parseDietExcel(file);

                if (!parsedData || !parsedData.days || parsedData.days.length === 0) {
                    return {
                        isValid: false,
                        errors: [{
                            row: 0,
                            column: 'file',
                            message: 'Nie udało się sparsować danych z pliku',
                            type: 'error'
                        }],
                        warnings: allWarnings
                    };
                }


                // Sprawdzenie, czy wszystkie dni mają posiłki
                const emptyDays = parsedData.days
                    .filter(day => !day.meals || day.meals.length === 0)
                    .map(day => day.date);

                if (emptyDays.length > 0) {
                    allWarnings.push({
                        row: 0,
                        column: 'file',
                        message: `Dni bez przypisanych posiłków: ${emptyDays.join(', ')}`,
                        type: 'warning'
                    });
                }

                if (!parsedData.shoppingList || parsedData.shoppingList.length === 0) {
                    allWarnings.push({
                        row: 0,
                        column: 'file',
                        message: 'Brak listy zakupów w pliku',
                        type: "warning"
                    });
                }

                return {
                    isValid: true,
                    errors: [],
                    warnings: allWarnings,
                    data: parsedData.days
                };
            } catch (error) {
                console.error('Error parsing Excel data:', error);
                return {
                    isValid: false,
                    errors: [{
                        row: 0,
                        column: 'file',
                        message: 'Błąd podczas parsowania danych z pliku',
                        type: 'error'
                    }],
                    warnings: allWarnings
                };
            }

        } catch (error) {
            console.error('Validation error:', error);
            return {
                isValid: false,
                errors: [{
                    row: 0,
                    column: 'file',
                    message: 'Błąd podczas przetwarzania pliku',
                    type: 'error'
                }],
                warnings: []
            };
        }
    }
}