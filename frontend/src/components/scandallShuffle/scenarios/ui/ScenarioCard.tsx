import React from 'react';
import {Scenario} from "../../../../types/scandallShuffle/database";
import {Clock, Edit, Star, Trash2, Users} from 'lucide-react';
import {ScenarioStatus} from "../types";

interface ScenarioCardProps {
    scenario: Scenario;
    onEdit: (scenario: Scenario) => void;
    onDelete: (scenario: Scenario) => void;
    onStatusChange: (scenario: Scenario, newStatus: ScenarioStatus) => void;
    onViewDetails: (scenario: Scenario) => void;
}

// Helper to get styling for different difficulties
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

// Helper to get styling for different statuses
const getStatusColor = (status: ScenarioStatus) => {
    switch (status) {
        case 'approved':
            return 'bg-green-100 text-green-800';
        case 'pending':
            return 'bg-yellow-100 text-yellow-800';
        case 'rejected':
            return 'bg-red-100 text-red-800';
        default:
            return 'bg-gray-100 text-gray-800';
    }
};

const ScenarioCard: React.FC<ScenarioCardProps> = ({scenario, onEdit, onDelete, onStatusChange, onViewDetails}) => {
    return (
        <div
            className="bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow flex flex-col cursor-pointer"
            onClick={() => onViewDetails(scenario)}
        >
            {scenario.image_url && (
                <img
                    src={scenario.image_url}
                    alt={scenario.name}
                    className="w-full h-32 object-cover rounded-t-lg"
                />
            )}
            <div className="p-4 flex flex-col flex-grow">
                <div className="flex justify-between items-start mb-2">
                    <h3 className="text-md font-semibold text-gray-900 line-clamp-2 leading-tight">
                        {scenario.name}
                    </h3>
                    <span
                        className={`px-2 py-0.5 rounded-full text-xs font-medium whitespace-nowrap ${getDifficultyColor(scenario.difficulty)}`}>
                        {scenario.difficulty}
                    </span>
                </div>

                <p className="text-gray-600 text-xs mb-3 line-clamp-2 flex-grow">
                    {scenario.description || 'No description available.'}
                </p>

                <div className="flex items-center justify-between text-xs text-gray-500 mb-4 border-t pt-3">
                    <div className="flex items-center gap-1" title="Players">
                        <Users className="h-4 w-4"/>
                        <span>{scenario.suggested_players}-{scenario.max_players}</span>
                    </div>
                    <div className="flex items-center gap-1" title="Duration">
                        <Clock className="h-4 w-4"/>
                        <span>{scenario.duration_minutes}min</span>
                    </div>
                    <div className="flex items-center gap-1" title="Average Rating">
                        <Star className="h-4 w-4 text-yellow-400"/>
                        <span>{scenario.average_rating?.toFixed(1) ?? 'N/A'}</span>
                    </div>
                </div>

                {/* Status Management Section */}
                <div className="mt-auto space-y-2">
                    <div className="flex items-center justify-between">
                         <span
                             className={`px-2 py-0.5 rounded-full text-xs font-medium ${getStatusColor(scenario.status)}`}>
                            {scenario.status}
                        </span>
                        <select
                            value={scenario.status}
                            onChange={(e) => onStatusChange(scenario, e.target.value as ScenarioStatus)}
                            className="text-xs border-gray-300 rounded-md shadow-sm focus:border-indigo-300 focus:ring focus:ring-indigo-200 focus:ring-opacity-50"
                            onClick={(e) => e.stopPropagation()}
                        >
                            <option value="pending">Pending</option>
                            <option value="approved">Approved</option>
                            <option value="rejected">Rejected</option>
                        </select>
                    </div>
                    <div className="flex justify-end gap-2 border-t pt-2">
                        <button
                            onClick={(e) => {
                                e.stopPropagation();
                                onEdit(scenario);
                            }}
                            className="p-2 text-gray-500 hover:text-indigo-600 hover:bg-indigo-50 rounded-full"
                            title="Edit scenario"
                        >
                            <Edit className="h-4 w-4"/>
                        </button>
                        <button
                            onClick={(e) => {
                                e.stopPropagation();
                                onDelete(scenario);
                            }}
                            className="p-2 text-gray-500 hover:text-red-600 hover:bg-red-50 rounded-full"
                            title="Delete scenario"
                        >
                            <Trash2 className="h-4 w-4"/>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ScenarioCard;