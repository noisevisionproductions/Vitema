import React from 'react';
import FormInput from '../../../shared/ui/FormInput';
import {BasicInfoTabProps} from '../../../../types/scandallShuffle/scenario-creation';
import ImageUploader from "./ImageUploader";
import {CheckCircle, Loader, Users, XCircle} from "lucide-react";

/**
 * Basic information tab for scenario creation.
 * Now displays player configuration automatically based on card count.
 */
const BasicInfoTab: React.FC<BasicInfoTabProps> = ({
                                                       formData,
                                                       errors,
                                                       difficultyOptions,
                                                       updateField,
                                                       nameValidationStatus,
                                                       nameValidationMessage,
                                                       cardCount
                                                   }) => {
    // Calculate max players dynamically based on the formula
    const calculatedMaxPlayers = Math.floor((cardCount - 1) / 3);
    const maxPlayers = calculatedMaxPlayers > 0 ? calculatedMaxPlayers : 0;

    const getNameValidationIcon = () => {
        switch (nameValidationStatus) {
            case 'checking':
                return <Loader className="h-5 w-5 text-gray-400 animate-spin"/>;
            case 'available':
                return <CheckCircle className="h-5 w-5 text-green-500"/>;
            case 'unavailable':
            case 'error':
                return <XCircle className="h-5 w-5 text-red-500"/>;
            default:
                return null;
        }
    };

    return (
        <div className="space-y-8">
            {/* Main Info Card */}
            <div className="bg-white rounded-xl shadow-sm p-8 border border-gray-200">
                <div className="mb-6">
                    <h3 className="text-xl font-semibold text-gray-900 mb-2">Basic Information</h3>
                    <p className="text-gray-600">
                        Provide the essential details about your scenario that players will see.
                    </p>
                </div>

                <div className="space-y-6">
                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                        <div className="lg:col-span-2">
                            <FormInput
                                id="name"
                                label="Scenario Name *"
                                value={formData.name || ''}
                                onChange={(e) => updateField('name', e.target.value)}
                                placeholder="Enter an engaging scenario name"
                                error={errors.name}
                                className="text-lg font-medium"
                                validationStatus={nameValidationStatus}
                                validationMessage={nameValidationMessage}
                                validationIcon={getNameValidationIcon()}
                            />
                        </div>

                        <div className="lg:col-span-2">
                            <label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-2">
                                Description *
                            </label>
                            <textarea
                                id="description"
                                value={formData.description || ''}
                                onChange={(e) => updateField('description', e.target.value)}
                                placeholder="Describe the scenario setting, context, and what players can expect..."
                                rows={4}
                                className={`w-full px-4 py-3 border rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent transition-colors resize-none ${
                                    errors.description ? 'border-red-500' : 'border-gray-300'
                                }`}
                            />
                            {errors.description && (
                                <p className="mt-2 text-sm text-red-600">{errors.description}</p>
                            )}
                        </div>

                        <div className="lg:col-span-2">
                            <label htmlFor="firstClue" className="block text-sm font-medium text-gray-700 mb-2">
                                First Clue *
                            </label>
                            <textarea
                                id="firstClue"
                                value={formData.firstClue || ''}
                                onChange={(e) => updateField('firstClue', e.target.value)}
                                placeholder="Provide the initial clue that players will receive to start the investigation..."
                                rows={3}
                                className={`w-full px-4 py-3 border rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent transition-colors resize-none ${
                                    errors.firstClue ? 'border-red-500' : 'border-gray-300'
                                }`}
                            />
                            {errors.firstClue && (
                                <p className="mt-2 text-sm text-red-600">{errors.firstClue}</p>
                            )}
                        </div>

                        <div>
                            <label htmlFor="difficulty" className="block text-sm font-medium text-gray-700 mb-2">
                                Difficulty Level
                            </label>
                            <select
                                id="difficulty"
                                value={formData.difficulty || 'medium'}
                                onChange={(e) => updateField('difficulty', e.target.value)}
                                className="w-full px-4 py-3 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent bg-white"
                            >
                                {difficultyOptions.map(option => (
                                    <option key={option.value} value={option.value}>
                                        {option.label}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div>
                            <FormInput
                                id="duration"
                                label="Duration (minutes) *"
                                type="number"
                                min="15"
                                max="300"
                                value={formData.duration || ''}
                                onChange={(e) => updateField('duration', parseInt(e.target.value) || 15)}
                                error={errors.duration}
                                placeholder="45"
                            />
                        </div>
                    </div>
                </div>
            </div>

            {/* Player Settings Card - Updated to be automatic */}
            <div className="bg-white rounded-xl shadow-sm p-8 border border-gray-200">
                <div className="mb-4">
                    <h3 className="text-xl font-semibold text-gray-900 mb-2">Player Configuration</h3>
                    <p className="text-gray-600">
                        The maximum number of players is calculated automatically based on the number of cards.
                    </p>
                </div>
                <div className="bg-blue-50 border border-blue-200 rounded-lg p-6 text-center">
                    <div className="flex items-center justify-center gap-3 text-blue-800 mb-2">
                        <Users className="w-8 h-8"/>
                        <span className="text-5xl font-bold">{maxPlayers}</span>
                    </div>
                    <p className="font-medium text-blue-900">Maximum Players</p>
                    <p className="text-xs text-blue-700 mt-2">
                        Based on your <strong>{cardCount}</strong> cards, this scenario supports up to {maxPlayers} players.
                    </p>
                    <p className="text-xs text-blue-700 mt-1">
                        (Formula: 1 starting card + 3 cards per player)
                    </p>
                </div>
            </div>

            {/* Solution Card */}
            <div className="bg-white rounded-xl shadow-sm p-8 border border-gray-200">
                <div className="mb-6">
                    <h3 className="text-xl font-semibold text-gray-900 mb-2">Solution & Resolution</h3>
                    <p className="text-gray-600">
                        Provide the complete solution that will be revealed at the end of the game.
                    </p>
                </div>

                <div>
                    <label htmlFor="solution" className="block text-sm font-medium text-gray-700 mb-2">
                        Solution *
                    </label>
                    <textarea
                        id="solution"
                        value={formData.solution || ''}
                        onChange={(e) => updateField('solution', e.target.value)}
                        placeholder="Describe the complete solution, including key evidence, reasoning, and how players should arrive at the answer..."
                        rows={4}
                        className={`w-full px-4 py-3 border rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent transition-colors resize-none ${
                            errors.solution ? 'border-red-500' : 'border-gray-300'
                        }`}
                    />
                    {errors.solution && (
                        <p className="mt-2 text-sm text-red-600">{errors.solution}</p>
                    )}
                </div>
            </div>

            {/* Image Upload Card - Optional */}
            <div className="bg-white rounded-xl shadow-sm p-8 border border-gray-200">
                <div className="mb-6">
                    <h3 className="text-xl font-semibold text-gray-900 mb-2">Scenario Image</h3>
                    <p className="text-gray-600">
                        Add an optional image to make your scenario more engaging.
                    </p>
                </div>

                <ImageUploader
                    label=""
                    value={formData.imageUri}
                    onImageSelect={(uri) => updateField('imageUri', uri)}
                />
            </div>
        </div>
    );
};

export default BasicInfoTab;

