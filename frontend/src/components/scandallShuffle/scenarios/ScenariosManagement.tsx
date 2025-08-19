import React, {useEffect, useState} from 'react';
import {BookOpen, Plus, Edit, Trash2, Users, Clock, Star} from 'lucide-react';
import {ScandalShuffleApiService} from "../../../services/scandallShuffle/ApiService";
import {Scenario} from "../../../types/scandallShuffle/database";
import LoadingSpinner from '../../shared/common/LoadingSpinner';
import {toast} from '../../../utils/toast';
import SectionHeader from "../../shared/common/SectionHeader";

const ScenariosManagement: React.FC = () => {
    const [scenarios, setScenarios] = useState<Scenario[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchScenarios().catch(console.error);
    }, []);

    const fetchScenarios = async () => {
        try {
            const scenariosData = await ScandalShuffleApiService.getAllScenarios();
            setScenarios(scenariosData);
        } catch (error) {
            console.error('Error fetching scenarios:', error);
            toast.error('Błąd podczas pobierania scenariuszy');
        } finally {
            setLoading(false);
        }
    };

    const getDifficultyColor = (difficulty: string) => {
        switch (difficulty.toLowerCase()) {
            case 'easy':
                return 'bg-green-100 text-green-800';
            case 'medium':
                return 'bg-yellow-100 text-yellow-800';
            case 'hard':
                return 'bg-red-100 text-red-800';
            default:
                return 'bg-gray-100 text-gray-800';
        }
    };

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
                <button
                    className="bg-primary text-white px-4 py-2 rounded-lg hover:bg-primary-dark transition-colors flex items-center gap-2">
                    <Plus className="h-4 w-4"/>
                    Add Scenario
                </button>
            </div>

            {/* Scenarios Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {scenarios.map((scenario) => (
                    <div key={scenario.id} className="bg-white rounded-lg shadow hover:shadow-md transition-shadow">
                        {scenario.image_url && (
                            <img
                                src={scenario.image_url}
                                alt={scenario.name}
                                className="w-full h-48 object-cover rounded-t-lg"
                            />
                        )}
                        <div className="p-6">
                            <div className="flex justify-between items-start mb-3">
                                <h3 className="text-lg font-semibold text-gray-900 line-clamp-2">
                                    {scenario.name}
                                </h3>
                                <span
                                    className={`px-2 py-1 rounded-full text-xs font-medium ${getDifficultyColor(scenario.difficulty)}`}>
                                    {scenario.difficulty}
                                </span>
                            </div>

                            {scenario.description && (
                                <p className="text-gray-600 text-sm mb-4 line-clamp-3">
                                    {scenario.description}
                                </p>
                            )}

                            <div className="flex items-center justify-between text-sm text-gray-500 mb-4">
                                <div className="flex items-center gap-1">
                                    <Users className="h-4 w-4"/>
                                    <span>{scenario.suggested_players}-{scenario.max_players}</span>
                                </div>
                                <div className="flex items-center gap-1">
                                    <Clock className="h-4 w-4"/>
                                    <span>{scenario.duration_minutes}min</span>
                                </div>
                                {scenario.average_rating && (
                                    <div className="flex items-center gap-1">
                                        <Star className="h-4 w-4 text-yellow-400"/>
                                        <span>{scenario.average_rating.toFixed(1)}</span>
                                    </div>
                                )}
                            </div>

                            <div className="flex justify-between items-center">
                                <span className="text-xs text-gray-500">
                                    Ocen: {scenario.total_ratings}
                                </span>
                                <div className="flex gap-2">
                                    <button
                                        className="p-2 text-gray-600 hover:text-indigo-600 hover:bg-indigo-50 rounded">
                                        <Edit className="h-4 w-4"/>
                                    </button>
                                    <button className="p-2 text-gray-600 hover:text-red-600 hover:bg-red-50 rounded">
                                        <Trash2 className="h-4 w-4"/>
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                ))}
            </div>

            {scenarios.length === 0 && (
                <div className="text-center py-12">
                    <BookOpen className="mx-auto h-12 w-12 text-gray-400"/>
                    <h3 className="mt-2 text-sm font-medium text-gray-900">
                        No scenarios
                    </h3>
                    <p className="mt-1 text-sm text-gray-500">
                        Start by adding your first scenario to the game.
                    </p>
                    <button
                        className="mt-4 bg-primary text-white px-4 py-2 rounded-lg hover:bg-primary-dark transition-colors flex items-center gap-2 mx-auto">
                        <Plus className="h-4 w-4"/>
                        Add first scenario
                    </button>
                </div>
            )}
        </div>
    );
};

export default ScenariosManagement;