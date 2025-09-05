// src/types/scandallShuffle/scenario-creation.ts

import {Scenario} from "./database";

export interface ScenarioFormData {
    name: string;
    description: string;
    firstClue: string;
    solution: string;
    difficulty: 'easy' | 'medium' | 'hard';
    maxPlayers?: number;
    suggestedPlayers?: number;
    duration?: number;
    imageUri?: string;
}

export interface FormErrors {
    name?: string;
    description?: string;
    firstClue?: string;
    solution?: string;
    maxPlayers?: string;
    duration?: string;
    general?: string;
    cards?: string;
    quiz?: string;
}

export interface CardData {
    id: string;
    title: string;
    content: string;
    isRelevant: boolean;
    type: 'clue' | 'evidence' | 'red_herring';
}

export interface QuestionData {
    id: string;
    question: string;
    options: string[];
    correctAnswer: number;
    explanation?: string;
    difficulty: 'easy' | 'medium' | 'hard';
}

export interface CreateScenarioPageProps {
    scenario?: Scenario;
    onBack: () => void;
    onSuccess: () => void;
}

export type ValidationStatus = 'idle' | 'checking' | 'available' | 'unavailable' | 'too-short' | 'error';

export interface BasicInfoTabProps {
    formData: ScenarioFormData;
    errors: FormErrors;
    difficultyOptions: { value: string; label: string }[];
    updateField: (field: keyof ScenarioFormData, value: any) => void;
    nameValidationStatus: ValidationStatus;
    nameValidationMessage: string | null;
    cardCount: number;
}

export interface CardItemProps {
    card: CardData;
    index: number;
    allCards: CardData[];
    isEditing: boolean;
    onEdit: () => void;
    onCancel: () => void;
    onDelete: () => void;
    onUpdate: (id: string, updates: Partial<CardData>) => void;
}

export interface QuestionItemProps {
    question: QuestionData;
    index: number;
    isEditing: boolean;
    onEdit: () => void;
    onCancel: () => void;
    onDelete: () => void;
    onUpdate: (id: string, updates: Partial<QuestionData>) => void;
}

export interface CardsTabProps {
    cards: CardData[];
    allCards: CardData[];
    error?: string;
    onAddCard: () => void;
    onUpdateCard: (id: string, updates: Partial<CardData>) => void;
    onDeleteCard: (id: string) => void;
    editingCardId: string | null;
    onSetEditingCardId: (id: string | null) => void;
}

export interface QuizTabProps {
    questions: QuestionData[];
    error?: string;
    onAddQuestion: () => void;
    onUpdateQuestion: (id: string, updates: Partial<QuestionData>) => void;
    onDeleteQuestion: (id: string) => void;
    editingQuestionId: string | null;
    onSetEditingQuestionId: (id: string | null) => void;
}