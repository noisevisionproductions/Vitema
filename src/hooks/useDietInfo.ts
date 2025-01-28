import {useEffect, useState} from "react";
import {collection, getDocs, query, where} from "firebase/firestore";
import {db} from "../config/firebase";
import {Diet} from "../types/diet";

interface DietInfo {
    hasDiet: boolean;
    startDate: string | null;
    endDate: string | null;
}

export interface UserDietInfo {
    [userId: string]: DietInfo;
}

export const useDietInfo = (userIds: string[]) => {
    const [dietInfo, setDietInfo] = useState<UserDietInfo>({});
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchDietInfo = async () => {
            if (!userIds.length) {
                setDietInfo({});
                setLoading(false);
                return;
            }

            try {
                const dietsRef = collection(db, 'diets');
                const dietInfoMap: UserDietInfo = {};

                for (const userId of userIds) {
                    const q = query(dietsRef, where('userId', '==', userId));
                    const querySnapshot = await getDocs(q);

                    if (!querySnapshot.empty) {
                        let earliestDate: string | null = null;
                        let latestDate: string | null = null;

                        querySnapshot.docs.forEach((doc) => {
                            const diet = doc.data() as Diet;
                            if (diet.days && diet.days.length > 0) {
                                const dates = diet.days.map(day => day.date);
                                const minDate = dates.reduce((a, b) => a < b ? a : b);
                                const maxDate = dates.reduce((a, b) => a > b ? a : b);

                                if (!earliestDate || minDate < earliestDate) earliestDate = minDate;
                                if (!latestDate || maxDate > latestDate) latestDate = maxDate;
                            }
                        });

                        dietInfoMap[userId] = {
                            hasDiet: true,
                            startDate: earliestDate,
                            endDate: latestDate
                        };
                    } else {
                        dietInfoMap[userId] = {
                            hasDiet: false,
                            startDate: null,
                            endDate: null
                        };
                    }
                }

                setDietInfo(dietInfoMap);
            } catch (error) {
                console.error('Error fetching diet info:', error);
            } finally {
                setLoading(false);
            }
        };

        fetchDietInfo().catch();
    }, [userIds]);

    return {dietInfo, loading};
};