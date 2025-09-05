import {useCallback} from 'react';
import {CardData, QuestionData, ScenarioFormData} from "../../types/scandallShuffle/scenario-creation";
import {toast} from "../../utils/toast";

interface ScenarioDraft {
    formData: ScenarioFormData;
    cards: CardData[];
    questions: QuestionData[];
}

const DRAFT_STORAGE_KEY = 'scenarioCreationDraft';

/**
 * Hook to manage saving and loading scenario creation drafts from localStorage.
 */
export const useScenarioDraft = (isEditing: boolean) => {
    const loadDraft = useCallback((): ScenarioDraft | null => {
        if (isEditing) {
            return null;
        }
        try {
            const savedDraft = localStorage.getItem(DRAFT_STORAGE_KEY);
            if (savedDraft) {
                console.log('[DEBUG] Draft found in localStorage. Loading data.');
                return JSON.parse(savedDraft);
            }
        } catch (error) {
            console.error('Failed to load draft from localStorage:', error);
        }
        return null;
    }, [isEditing]);

    const saveDraft = useCallback((data: ScenarioDraft) => {
        if (isEditing) {
            return;
        }
        try {
            const draftString = JSON.stringify(data);
            localStorage.setItem(DRAFT_STORAGE_KEY, draftString);
        } catch (error) {
            console.error('Failed to save draft to localStorage:', error);
        }
    }, [isEditing]);

    const clearDraft = useCallback(() => {
        try {
            localStorage.removeItem(DRAFT_STORAGE_KEY);
            toast.info('Formularz został wyczyszczony.');
        } catch (error) {
            console.error('Failed to clear draft from localStorage:', error);
        }
    }, []);

    return {loadDraft, saveDraft, clearDraft};
};
