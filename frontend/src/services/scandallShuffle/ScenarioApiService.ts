import {supabase} from '../../config/supabase';
import {Database, Scenario} from "../../types/scandallShuffle/database";
import {CardData, QuestionData} from "../../types/scandallShuffle/scenario-creation";

export class ScenarioApiService {
    // --- Core Scenario Management ---

    static async getAll(): Promise<Scenario[]> {
        const {data, error} = await supabase.from('scenarios').select('*').order('created_at', {ascending: false});
        if (error) throw new Error(error.message);
        return data || [];
    }

    static async getById(id: string): Promise<Scenario | null> {
        const {data, error} = await supabase.from('scenarios').select('*').eq('id', id).single();
        if (error) throw new Error(error.message);
        return data;
    }

    static async getCardsForScenario(scenarioId: string): Promise<CardData[]> {
        const {data, error} = await supabase
            .from('scenario_cards')
            .select('*')
            .eq('scenario_id', scenarioId)
            .order('card_order', {ascending: true});

        if (error) throw new Error(error.message);

        return (data || []).map(dbCard => ({
            id: dbCard.id,
            title: `Card ${dbCard.card_order}`,
            content: dbCard.card_text,
            isRelevant: dbCard.is_relevant,
            type: dbCard.is_relevant ? 'clue' : 'red_herring'
        }));
    }

    static async getQuestionsForScenario(scenarioId: string): Promise<QuestionData[]> {
        const {data, error} = await supabase
            .from('scenario_quiz')
            .select('*')
            .eq('scenario_id', scenarioId)
            .order('question_order', {ascending: true});

        if (error) throw new Error(error.message);

        return (data || []).map(dbQuiz => ({
            id: dbQuiz.id,
            question: dbQuiz.question,
            options: (dbQuiz.options as any) || [],
            correctAnswer: dbQuiz.correct_answer,
            explanation: dbQuiz.explanation || '',
            difficulty: dbQuiz.difficulty || 'medium'
        }));
    }

    static async create(scenario: Database['public']['Tables']['scenarios']['Insert']): Promise<Scenario> {
        const scenarioData = {
            ...scenario,
            language: scenario.language || 'en'
        };

        const {data, error} = await supabase.from('scenarios').insert(scenarioData).select().single();
        if (error) throw new Error(error.message);
        return data;
    }

    static async update(id: string, updates: Database['public']['Tables']['scenarios']['Update']): Promise<Scenario> {
        const {data, error} = await supabase.from('scenarios').update(updates).eq('id', id).select().single();
        if (error) throw new Error(error.message);
        return data;
    }

    static async delete(id: string): Promise<void> {
        const {error} = await supabase.from('scenarios').delete().eq('id', id);
        if (error) throw new Error(error.message);
    }

    // --- Scenario Details Management ---

    static async deleteCards(scenarioId: string): Promise<void> {
        const {error} = await supabase.from('scenario_cards').delete().eq('scenario_id', scenarioId);
        if (error) throw new Error(error.message);
    }

    static async deleteQuestions(scenarioId: string): Promise<void> {
        const {error} = await supabase.from('scenario_quiz').delete().eq('scenario_id', scenarioId);
        if (error) throw new Error(error.message);
    }

    static async createCards(scenarioId: string, cards: CardData[]): Promise<void> {
        if (cards.length === 0) return;
        const cardInserts = cards.map((card, index) => ({
            scenario_id: scenarioId,
            card_text: card.content,
            is_relevant: card.isRelevant,
            card_order: index + 1,
        }));
        const {error} = await supabase.from('scenario_cards').insert(cardInserts);
        if (error) throw new Error(error.message);
    }

    static async createQuestions(scenarioId: string, questions: QuestionData[]): Promise<void> {
        if (questions.length === 0) return;
        const questionInserts = questions.map((q, index) => ({
            scenario_id: scenarioId,
            question: q.question,
            options: q.options,
            correct_answer: q.correctAnswer,
            question_order: index + 1,
        }));
        const {error} = await supabase.from('scenario_quiz').insert(questionInserts);
        if (error) throw new Error(error.message);
    }

    static async uploadImage(imageBlob: Blob, scenarioId: string): Promise<string> {
        const fileName = `scenario-${scenarioId}-${Date.now()}.jpg`;
        const {data, error} = await supabase.storage
            .from('scenario-images')
            .upload(fileName, imageBlob, {contentType: 'image/jpeg'});

        if (error) throw new Error(error.message);

        const {data: {publicUrl}} = supabase.storage
            .from('scenario-images')
            .getPublicUrl(data.path);

        return publicUrl;
    }
}