import {useEffect, useState} from "react";
import {DietService} from "../services/DietService";
import {toast} from "sonner";

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
                const dietInfoMap = await DietService.getDietsInfoForUsers(userIds);

                setDietInfo(dietInfoMap);
            } catch (error) {
                console.error('Error fetching diet info:', error);
                toast.error('Błąd podczas pobierania informacji o dietach');
            } finally {
                setLoading(false);
            }
        };

        fetchDietInfo().catch();
    }, [userIds]);

    return {dietInfo, loading};
};