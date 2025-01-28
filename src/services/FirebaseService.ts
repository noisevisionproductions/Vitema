import {
    doc,
    deleteDoc,
    addDoc,
    query,
    collection,
    where,
    getDocs,
    writeBatch,
    Timestamp,
    updateDoc
} from 'firebase/firestore';
import {ref, uploadBytes, getDownloadURL} from 'firebase/storage';
import {db, storage} from '../config/firebase';
import {Recipe, Diet, ShoppingList, MealType, ParsedDietData} from '../types/diet';

export class FirebaseService {
    static async uploadExcelFile(file: File, userId: string) {
        const storageRef = ref(storage, `diets/${userId}/${file.name}`);
        await uploadBytes(storageRef, file);
        return await getDownloadURL(storageRef);
    }

    static async saveShoppingList(shoppingList: Omit<ShoppingList, 'id'>) {
        const shoppingListRef = collection(db, 'shopping_lists');
        const docRef = await addDoc(shoppingListRef, shoppingList);
        return docRef.id;
    }

    static async deleteDietWithRelatedData(dietId: string) {
        const shoppingListQuery = query(
            collection(db, 'shopping_lists'),
            where('dietId', '==', dietId)
        );
        const shoppingListSnapshot = await getDocs(shoppingListQuery);
        await Promise.all(
            shoppingListSnapshot.docs.map(doc => deleteDoc(doc.ref))
        );

        await deleteDoc(doc(db, 'diets', dietId));
    }

    static async saveRecipe(recipe: Omit<Recipe, 'id'>, dietId: string, userId: string, mealType: MealType) {
        const batch = writeBatch(db);

        const recipesRef = collection(db, 'recipes');
        const recipeDoc = doc(recipesRef);

        batch.set(recipeDoc, {
            ...recipe,
            createdAt: Timestamp.fromDate(new Date()),
            photos: [],
            parentRecipeId: null
        });

        const referencesRef = collection(db, 'recipe_references');
        const referenceDoc = doc(referencesRef); // Stwórz referencję dokumentu

        batch.set(referenceDoc, {
            recipeId: recipeDoc.id,
            dietId,
            userId,
            mealType,
            addedAt: Timestamp.fromDate(new Date())
        });

        await batch.commit();

        return recipeDoc.id;
    }

    static async saveDiet(
        parsedData: ParsedDietData,
        userId: string,
        fileInfo: {
            fileName: string,
            fileUrl: string
        }
    ) {
        const dietsRef = collection(db, 'diets');

        const diet: Omit<Diet, 'id'> = {
            userId,
            createdAt: Timestamp.fromDate(new Date()),
            updatedAt: Timestamp.fromDate(new Date()),
            days: [],
            metadata: {
                totalDays: parsedData.days.length,
                fileName: fileInfo.fileName,
                fileUrl: fileInfo.fileUrl
            }
        };

        const dietDoc = await addDoc(dietsRef, diet);

        const recipePromises = parsedData.days.flatMap(day =>
            day.meals.map(async (parsedMeal) => {
                const recipeId = await this.saveRecipe(
                    {
                        name: parsedMeal.name,
                        instructions: parsedMeal.instructions,
                        nutritionalValues: parsedMeal.nutritionalValues,
                        createdAt: Timestamp.fromDate(new Date()),
                        photos: [],
                        parentRecipeId: null
                    },
                    dietDoc.id,
                    userId,
                    parsedMeal.mealType
                );

                return {
                    recipeId,
                    mealType: parsedMeal.mealType,
                    time: parsedMeal.time
                };
            })
        );

        const mealPromises = Promise.all(recipePromises);
        let mealIndex = 0;
        const savedMeals = await mealPromises;

        const updatedDays = parsedData.days.map(day => ({
            date: day.date,
            meals: day.meals.map(() => savedMeals[mealIndex++])
        }));

        await updateDoc(doc(db, 'diets', dietDoc.id), {days: updatedDays});

        return dietDoc.id;
    }
}