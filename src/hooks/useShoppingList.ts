import {useEffect, useState} from "react";
import {ShoppingList} from "../types/diet";
import {collection, getDocs, query, where} from "firebase/firestore";
import {db} from "../config/firebase";

export const useShoppingList = (dietId: string) => {
    const [shoppingList, setShoppingList] = useState<ShoppingList | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<Error | null>(null);

    useEffect(() => {
        const fetchShoppingList = async () => {
            try {
                const q = query(
                    collection(db, 'shopping_lists'),
                    where('dietId', '==', dietId)
                );

                const querySnapshot = await getDocs(q);
                if (!querySnapshot.empty) {
                    const doc = querySnapshot.docs[0];
                    setShoppingList({
                        id: doc.id,
                        ...doc.data()
                    } as ShoppingList);
                }
            } catch (err) {
                setError(err as Error);
            } finally {
                setLoading(false);
            }
        };

        if (dietId) {
            fetchShoppingList().catch();
        }
    }, [dietId]);

    return {shoppingList, loading, error};
};