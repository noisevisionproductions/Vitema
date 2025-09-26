import React, {useCallback, useEffect, useMemo, useState} from 'react';
import {Plus, Wand2} from 'lucide-react';
import {Scenario} from '../../../types/scandallShuffle/database';
import LoadingSpinner from '../../shared/common/LoadingSpinner';
import {toast} from '../../../utils/toast';
import SectionHeader from '../../shared/common/SectionHeader';
import CreateScenarioPage from './CreateScenarioPage';
import {ScenarioApiService} from "../../../services/scandallShuffle/ScenarioApiService";
import {CardData, QuestionData} from "../../../types/scandallShuffle/scenario-creation";
import {FilterState, ScenarioStatus} from "./types";
import ScenarioFilters from "./ui/ScenarioFilters";
import ScenarioCard from "./ui/ScenarioCard";
import ScenarioDetailModal from "./ui/ScenarioDetailModal";

type ViewMode = 'list' | 'create' | 'edit';

const ScenariosManagement: React.FC = () => {
    const [scenarios, setScenarios] = useState<Scenario[]>([]);
    const [loading, setLoading] = useState(true);
    const [viewMode, setViewMode] = useState<ViewMode>('list');
    const [viewingScenario, setViewingScenario] = useState<Scenario | null>(null);
    const [fullEditingScenario, setFullEditingScenario] = useState<{
        scenario: Scenario,
        cards: CardData[],
        questions: QuestionData[]
    } | null>(null);

    const [filters, setFilters] = useState<FilterState>({
        status: 'all',
        search: '',
        language: 'all',
    });

    const fetchScenarios = useCallback(async () => {
        setLoading(true);
        try {
            const scenariosData = await ScenarioApiService.getAll();
            setScenarios(scenariosData);
        } catch (error) {
            console.error('Error fetching scenarios:', error);
            toast.error('Błąd podczas pobierania scenariuszy');
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        if (viewMode === 'list') {
            fetchScenarios().catch(console.error);
        }
    }, [viewMode, fetchScenarios]);

    const availableLanguages = useMemo(() => {
        const languages = new Set(scenarios.map(s => s.language));
        return Array.from(languages);
    }, [scenarios]);

    const filteredScenarios = useMemo(() => {
        return scenarios.filter(scenario => {
            const searchLower = filters.search.toLowerCase();
            // Status filter
            if (filters.status !== 'all' && scenario.status !== filters.status) {
                return false;
            }
            // Language filter
            if (filters.language !== 'all' && scenario.language !== filters.language) {
                return false;
            }
            // Search filter
            return !(filters.search &&
                !scenario.name.toLowerCase().includes(searchLower) &&
                !scenario.description?.toLowerCase().includes(searchLower));
        });
    }, [scenarios, filters]);

    const handleFilterChange = (filterName: string, value: string) => {
        setFilters(prev => ({...prev, [filterName]: value}));
    };

    const handleStatusChange = async (scenarioToUpdate: Scenario, newStatus: ScenarioStatus) => {
        const originalStatus = scenarioToUpdate.status;

        setScenarios(prev => prev.map(s => s.id === scenarioToUpdate.id ? {...s, status: newStatus} : s));

        try {
            await ScenarioApiService.updateStatus(scenarioToUpdate.id, newStatus);
            toast.success(`Scenario "${scenarioToUpdate.name}" status updated to ${newStatus}.`);
        } catch (error) {
            console.error('Failed to update status:', error);
            toast.error('Failed to update status. Reverting changes.');
            // Revert on failure
            setScenarios(prev => prev.map(s => s.id === scenarioToUpdate.id ? {...s, status: originalStatus} : s));
        }
    };

    const handleCreateSuccess = () => {
        setViewMode('list');
    };

    const handleEdit = async (scenario: Scenario) => {
        setLoading(true);
        try {
            const [cardsData, questionsData] = await Promise.all([
                ScenarioApiService.getCardsForScenario(scenario.id),
                ScenarioApiService.getQuestionsForScenario(scenario.id)
            ]);
            setFullEditingScenario({
                scenario: scenario,
                cards: cardsData,
                questions: questionsData
            });
            setViewMode('edit');
        } catch (error) {
            console.error("Failed to load scenario for editing:", error);
            toast.error("Could not load scenario details.");
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (scenario: Scenario) => {
        if (!confirm(`Are you sure you want to delete "${scenario.name}"?`)) {
            return;
        }
        try {
            await ScenarioApiService.delete(scenario.id);
            toast.success('Scenario deleted successfully');
            await fetchScenarios();
        } catch (error) {
            console.error('Error deleting scenario:', error);
            toast.error('Failed to delete scenario');
        }
    };

    const handleCreateSampleScenario = async () => {
        if (!confirm('Are you sure you want to create a sample test scenario?')) {
            return;
        }

        setLoading(true);
        try {
            const uniqueName = `[TEST] Sample Scenario ${Math.floor(Date.now() / 1000)}`;

            const sampleCards: CardData[] = [...Array(15)].map((_, i) => ({
                id: `temp_card_${i + 1}`,
                title: `Sample Card ${i + 1}`,
                content: `Sample content for card ${i + 1}. This clue may or may not be relevant to the case.`,
                isRelevant: i % 4 !== 0,
                type: 'clue'
            }));

            const sampleQuestions: QuestionData[] = [
                {
                    id: 'temp_q_1',
                    question: 'What is the answer to the primary test question?',
                    options: ['Option A (Correct)', 'Option B', 'Option C', 'Option D'],
                    correctAnswer: 0,
                    explanation: 'This is correct because it is the first option in the test data.',
                    difficulty: 'easy'
                }
            ];

            const maxPlayers = 4;

            const scenarioCoreData = {
                name: uniqueName,
                description: 'This is an auto-generated scenario used for testing UI functionality and API integration.',
                initial_clue: 'This is the starting clue for the auto-generated test scenario. Your investigation begins here.',
                solution: 'The solution is that this scenario is a test. The butler did it. The correct answer to the quiz was "Option A".',
                difficulty: 'easy',
                duration_minutes: 30,
                max_players: maxPlayers,
                suggested_players: maxPlayers,
                status: 'pending',
            } as const;

            const newScenario = await ScenarioApiService.create(scenarioCoreData);

            await ScenarioApiService.createCards(newScenario.id, sampleCards);
            await ScenarioApiService.createQuestions(newScenario.id, sampleQuestions);

            toast.success(`Successfully created sample scenario: "${uniqueName}"`);
            await fetchScenarios();

        } catch (error) {
            console.error('Error creating sample scenario:', error);
            if (error instanceof Error) {
                toast.error(`Failed to create sample scenario: ${error.message}`);
            } else {
                toast.error('An unknown error occurred while creating the sample scenario.');
            }
        } finally {
            setLoading(false);
        }
    };

    if (viewMode === 'create') {
        return (
            <CreateScenarioPage
                onBack={() => setViewMode('list')}
                onSuccess={handleCreateSuccess}
            />
        );
    }

    if (viewMode === 'edit' && fullEditingScenario) {
        return (
            <CreateScenarioPage
                scenario={fullEditingScenario.scenario}
                initialCards={fullEditingScenario.cards}
                initialQuestions={fullEditingScenario.questions}
                onBack={() => {
                    setViewMode('list');
                    setFullEditingScenario(null);
                }}
                onSuccess={handleCreateSuccess}
            />
        );
    }

    if (loading) {
        return (
            <div className="flex justify-center items-center h-64">
                <LoadingSpinner size="lg"/>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <SectionHeader
                    title="Scenario Management"
                    description="Create and manage scenarios for Scandal Shuffle game"
                />
                <div className="flex items-center gap-3">
                    <button
                        onClick={handleCreateSampleScenario}
                        className="bg-indigo-600 text-white px-4 py-2 rounded-lg hover:bg-indigo-700 transition-colors flex items-center gap-2"
                        title="Create a sample scenario with dummy data for testing"
                    >
                        <Wand2 className="h-4 w-4"/>
                        Add Sample
                    </button>
                    <button
                        onClick={() => setViewMode('create')}
                        className="bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 transition-colors flex items-center gap-2"
                    >
                        <Plus className="h-4 w-4"/>
                        Add Scenario
                    </button>
                </div>
            </div>

            <ScenarioFilters
                filters={filters}
                onFilterChange={handleFilterChange}
                scenarioCount={filteredScenarios.length}
                totalCount={scenarios.length}
                availableLanguages={availableLanguages}
            />

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                {filteredScenarios.map((scenario) => (
                    <ScenarioCard
                        key={scenario.id}
                        scenario={scenario}
                        onEdit={handleEdit}
                        onDelete={handleDelete}
                        onStatusChange={handleStatusChange}
                        onViewDetails={setViewingScenario}
                    />
                ))}
            </div>

            {scenarios.length > 0 && filteredScenarios.length === 0 && (
                <div className="text-center py-12 text-gray-500">
                    <h3 className="text-lg font-medium">No scenarios match your filters</h3>
                    <p className="mt-1">Try adjusting your search or filter settings.</p>
                </div>
            )}

            {viewingScenario && (
                <ScenarioDetailModal
                    scenario={viewingScenario}
                    onClose={() => setViewingScenario(null)} // Pass the handler to close the modal
                />
            )}
        </div>
    );
};

export default ScenariosManagement;
