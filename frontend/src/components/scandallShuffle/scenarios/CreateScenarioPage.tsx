import React, {useCallback, useEffect, useState} from 'react';
import {ArrowLeft, Save, Trash2} from 'lucide-react';
import {toast} from '../../../utils/toast';
import {ScenarioValidationService} from '../../../services/scandallShuffle/ScenarioValidationService';
import BasicInfoTab from "./tabs/BasicInfoTab";
import CardsTab from "./tabs/CardsTab";
import QuizTab from "./tabs/QuizTab";
import {
    CreateScenarioPageProps,
    FormErrors,
    CardData,
    QuestionData,
    ScenarioFormData
} from "../../../types/scandallShuffle/scenario-creation";
import {ScenarioApiService} from "../../../services/scandallShuffle/ScenarioApiService";
import SectionHeader from "../../shared/common/SectionHeader";
import {useScenarioNameValidation} from "../../../hooks/scandallShuffle/useScenarioNameValidation";
import {useScenarioDraft} from "../../../hooks/scandallShuffle/useScenarioDraft";

interface ExtendedCreateScenarioPageProps extends CreateScenarioPageProps {
    initialCards?: CardData[];
    initialQuestions?: QuestionData[];
}

const getInitialFormData = (): ScenarioFormData => ({
    name: '',
    description: '',
    firstClue: '',
    solution: '',
    difficulty: 'medium',
    duration: undefined,
    imageUri: undefined
});

const CreateScenarioPage: React.FC<ExtendedCreateScenarioPageProps> = ({
                                                                           scenario,
                                                                           onBack,
                                                                           onSuccess,
                                                                           initialCards = [],
                                                                           initialQuestions = []
                                                                       }) => {
    const isEditing = !!scenario;
    const {loadDraft, saveDraft, clearDraft} = useScenarioDraft(isEditing);

    const [editingCardId, setEditingCardId] = useState<string | null>(null);
    const [editingQuestionId, setEditingQuestionId] = useState<string | null>(null);

    const [formData, setFormData] = useState<ScenarioFormData>(() => {
        if (scenario) {
            return {
                name: scenario.name,
                description: scenario.description || '',
                firstClue: scenario.initial_clue || '',
                solution: scenario.solution || '',
                difficulty: (scenario.difficulty as 'easy' | 'medium' | 'hard') || 'medium',
                duration: scenario.duration_minutes || undefined,
                imageUri: scenario.image_url || undefined
            };
        }
        return getInitialFormData();
    });

    const nameValidation = useScenarioNameValidation(formData.name, scenario?.id);

    const [cards, setCards] = useState<CardData[]>(initialCards);
    const [questions, setQuestions] = useState<QuestionData[]>(initialQuestions);
    const [errors, setErrors] = useState<FormErrors>({});
    const [loading, setLoading] = useState(false);
    const [activeTab, setActiveTab] = useState<'basic' | 'cards' | 'quiz'>('basic');

    useEffect(() => {
        if (!isEditing) {
            const draft = loadDraft();
            if (draft) {
                setFormData(draft.formData);
                setCards(draft.cards);
                setQuestions(draft.questions);
                toast.success("Przywrócono zapisaną wersję roboczą.");
            }
        }
    }, [isEditing, loadDraft]);

    useEffect(() => {
        if (!isEditing) {
            const isDirty = formData.name || formData.description || formData.firstClue || formData.solution || cards.length > 0 || questions.length > 0;
            if (isDirty) {
                saveDraft({formData, cards, questions});
            }
        }
    }, [formData, cards, questions, saveDraft, isEditing]);

    useEffect(() => {
        if (formData.name !== nameValidation.name) {
            setFormData(prev => ({...prev, name: nameValidation.name}));
        }
    }, [nameValidation.name]);


    const difficultyOptions = [
        {value: 'easy', label: 'Easy'},
        {value: 'medium', label: 'Medium'},
        {value: 'hard', label: 'Hard'}
    ];

    const validateForm = async (): Promise<boolean> => {
        const validationErrors = await ScenarioValidationService.validate(
            formData,
            cards,
            questions,
            scenario?.id
        );
        setErrors(validationErrors);
        const isValid = Object.keys(validationErrors).length === 0;
        if (!isValid) {
            handleValidationErrors(validationErrors);
        }
        return isValid;
    };

    const handleValidationErrors = (validationErrors: FormErrors) => {
        if (validationErrors.general) {
            toast.error(`Validation Error: ${validationErrors.general}`);
        }
        const fieldErrorCount = Object.keys(validationErrors).filter(k => k !== 'general').length;
        if (fieldErrorCount > 0) {
            toast.error(`Please fix ${fieldErrorCount} field error(s) before submitting`);
        }
    };

    const handleSaveDetails = async (scenarioId: string) => {
        await ScenarioApiService.createCards(scenarioId, cards);
        await ScenarioApiService.createQuestions(scenarioId, questions);

        if (formData.imageUri && !formData.imageUri.startsWith('http')) {
            const imageFile = await fetch(formData.imageUri).then(res => res.blob());
            const publicUrl = await ScenarioApiService.uploadImage(imageFile, scenarioId);
            await ScenarioApiService.update(scenarioId, {image_url: publicUrl});
        }
    };

    const handleSubmit = async () => {
        const isValid = await validateForm();
        if (!isValid) return;

        setLoading(true);
        try {
            const maxPlayers = Math.floor((cards.length - 1) / 3);

            const scenarioCoreData = {
                name: formData.name.trim(),
                description: formData.description.trim(),
                initial_clue: formData.firstClue.trim(),
                solution: formData.solution?.trim(),
                difficulty: formData.difficulty,
                duration_minutes: formData.duration,
                max_players: maxPlayers > 0 ? maxPlayers : 1,
                suggested_players: maxPlayers > 0 ? maxPlayers : 1,
            };

            if (scenario) {
                const savedScenario = await ScenarioApiService.update(scenario.id, scenarioCoreData);
                await ScenarioApiService.deleteCards(savedScenario.id);
                await ScenarioApiService.deleteQuestions(savedScenario.id);
                await handleSaveDetails(savedScenario.id);
            } else {
                const savedScenario = await ScenarioApiService.create(scenarioCoreData);
                await handleSaveDetails(savedScenario.id);
                clearDraft();
            }

            toast.success(`Scenario ${scenario ? 'updated' : 'created'} successfully`);
            setTimeout(onSuccess, 1500);

        } catch (error) {
            console.error(`Error ${scenario ? 'updating' : 'creating'} scenario:`, error);
            toast.error(`Failed to ${scenario ? 'update' : 'create'} scenario`);
        } finally {
            setLoading(false);
        }
    };

    const handleClearDraft = useCallback(() => {
        if (window.confirm("Czy na pewno chcesz wyczyścić cały formularz? Niezapisane postępy zostaną utracone.")) {
            clearDraft();
            setFormData(getInitialFormData());
            setCards([]);
            setQuestions([]);
            setErrors({});
        }
    }, [clearDraft]);

    const updateField = (field: keyof ScenarioFormData, value: any) => {
        if (field === 'name') {
            nameValidation.setName(value);
        } else {
            setFormData(prev => ({...prev, [field]: value}));
        }

        if (value !== undefined && value !== null && value !== '') {
            clearFieldError(field as keyof FormErrors);
        }
    };

    const clearFieldError = (fieldName: keyof FormErrors) => {
        setErrors(prev => {
            const newErrors = {...prev};
            delete newErrors[fieldName];
            return newErrors;
        });
    };

    const addCard = () => {
        const newCard: CardData = {
            id: Date.now().toString(),
            title: `Card ${cards.length + 1}`,
            content: '',
            isRelevant: true,
            type: 'clue'
        };
        setCards(prev => [...prev, newCard]);
        setEditingCardId(newCard.id);
    };

    const updateCard = (id: string, updates: Partial<CardData>) => {
        setCards(prev => prev.map(card =>
            card.id === id ? {...card, ...updates} : card
        ));
    };

    const deleteCard = (id: string) => {
        setCards(prev => prev.filter(card => card.id !== id));
    };

    const addQuestion = () => {
        const newQuestion: QuestionData = {
            id: Date.now().toString(),
            question: '',
            options: ['', '', ''],
            correctAnswer: 0,
            explanation: '',
            difficulty: 'medium'
        };
        setQuestions(prev => [...prev, newQuestion]);
        setEditingQuestionId(newQuestion.id);
    };

    const updateQuestion = (id: string, updates: Partial<QuestionData>) => {
        setQuestions(prev => prev.map(question =>
            question.id === id ? {...question, ...updates} : question
        ));
    };

    const deleteQuestion = (id: string) => {
        setQuestions(prev => prev.filter(question => question.id !== id));
    };

    const getTabErrors = (tabId: string) => {
        switch (tabId) {
            case 'basic':
                return !!(errors.name || errors.description || errors.firstClue ||
                    errors.solution || errors.maxPlayers || errors.duration);
            case 'cards':
                return !!errors.cards;
            case 'quiz':
                return !!errors.quiz;
            default:
                return false;
        }
    };

    return (
        <div className="min-h-screen bg-gray-50">
            <div className=" mx-auto space-y-6">
                <SectionHeader
                    title={scenario ? 'Edit Scenario' : 'Create New Scenario'}
                    description={scenario ? 'Modify scenario details, cards and quiz questions' : 'Design a complete scenario with cards and quiz questions'}
                />

                <div className="flex justify-between items-center">
                    <div className="flex items-center gap-2">
                        <button
                            onClick={onBack}
                            className="flex items-center gap-2 px-3 py-2 rounded-md bg-white text-slate-700 border border-slate-200 shadow-sm hover:bg-slate-50 transition-colors text-sm font-medium"
                            title="Go back"
                        >
                            <ArrowLeft className="h-4 w-4"/>
                            Back
                        </button>
                        {!isEditing && (
                            <button
                                onClick={handleClearDraft}
                                className="flex items-center gap-2 px-3 py-2 rounded-md bg-red-600 text-white border border-red-700 shadow-sm hover:bg-red-700 transition-colors text-sm font-medium"
                                title="Clear draft and reset form"
                            >
                                <Trash2 className="h-4 w-4"/>
                                Clear Draft
                            </button>
                        )}
                    </div>
                    <button
                        onClick={handleSubmit}
                        disabled={loading}
                        className="bg-green-600 text-white px-6 py-2 rounded-lg hover:bg-green-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2 shadow-sm"
                    >
                        {loading ? (
                            <div
                                className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"/>
                        ) : (
                            <Save className="w-4 h-4"/>
                        )}
                        <span className="font-medium">
                            {loading ? (scenario ? 'Updating...' : 'Creating...') : (scenario ? 'Update Scenario' : 'Create Scenario')}
                        </span>
                    </button>
                </div>

                <div className="bg-white rounded-lg shadow-sm border border-slate-200 overflow-hidden">
                    <div className="border-b border-slate-200">
                        <nav className="flex space-x-8 px-4 sm:px-6 lg:px-8">
                            {[
                                {id: 'basic', label: 'Basic Info', count: null},
                                {id: 'cards', label: 'Game Cards', count: cards.length},
                                {id: 'quiz', label: 'Quiz Questions', count: questions.length}
                            ].map((tab) => (
                                <button
                                    key={tab.id}
                                    onClick={() => setActiveTab(tab.id as any)}
                                    className={`relative py-4 px-1 border-b-2 font-medium text-sm transition-colors ${activeTab === tab.id
                                        ? 'border-green-500 text-green-600'
                                        : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                                    }`}
                                >
                                    <div className="flex items-center space-x-2">
                                        <span>{tab.label}</span>
                                        {tab.count !== null && (
                                            <span
                                                className={`py-0.5 px-2 rounded-full text-xs font-medium ${activeTab === tab.id
                                                    ? 'bg-green-100 text-green-800'
                                                    : 'bg-gray-100 text-gray-600'
                                                }`}>
                                                {tab.count}
                                            </span>
                                        )}
                                        {getTabErrors(tab.id) && (
                                            <div className="w-2 h-2 bg-red-500 rounded-full"></div>
                                        )}
                                    </div>
                                </button>
                            ))}
                        </nav>
                    </div>

                    <div className="p-4 sm:p-6 lg:p-8">
                        <div className="transition-all duration-300 ease-in-out">
                            {activeTab === 'basic' && (
                                <BasicInfoTab
                                    formData={formData}
                                    errors={errors}
                                    difficultyOptions={difficultyOptions}
                                    updateField={updateField}
                                    nameValidationStatus={nameValidation.validationStatus}
                                    nameValidationMessage={nameValidation.validationMessage}
                                    cardCount={cards.length}
                                />
                            )}

                            {activeTab === 'cards' && (
                                <CardsTab
                                    cards={cards}
                                    allCards={cards}
                                    error={errors.cards}
                                    onAddCard={addCard}
                                    onUpdateCard={updateCard}
                                    onDeleteCard={deleteCard}
                                    editingCardId={editingCardId}
                                    onSetEditingCardId={setEditingCardId}
                                />
                            )}

                            {activeTab === 'quiz' && (
                                <QuizTab
                                    questions={questions}
                                    error={errors.quiz}
                                    onAddQuestion={addQuestion}
                                    onUpdateQuestion={updateQuestion}
                                    onDeleteQuestion={deleteQuestion}
                                    editingQuestionId={editingQuestionId}
                                    onSetEditingQuestionId={setEditingQuestionId}
                                />
                            )}
                        </div>
                    </div>
                </div>

                <div className="fixed bottom-6 right-6 sm:hidden">
                    <button
                        onClick={handleSubmit}
                        disabled={loading}
                        className="bg-green-600 text-white p-4 rounded-full shadow-lg hover:bg-green-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        {loading ? (
                            <div
                                className="w-6 h-6 border-2 border-white border-t-transparent rounded-full animate-spin"/>
                        ) : (
                            <Save className="w-6 h-6"/>
                        )}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default CreateScenarioPage;