import React from 'react';
import {Plus} from 'lucide-react';
import {CardsTabProps} from '../../../../types/scandallShuffle/scenario-creation';
import CardItem from "./item/CardItem";

/**
 * Cards management tab for scenario creation
 * Updated to use CardData type from RN app
 */
const CardsTab: React.FC<CardsTabProps> = ({
                                               cards,
                                               error,
                                               onAddCard,
                                               onUpdateCard,
                                               onDeleteCard,
                                               editingCardId,
                                               onSetEditingCardId
                                           }) => {
    const handleSaveOrCancel = () => {
        onSetEditingCardId(null);
    };

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="bg-white rounded-lg shadow p-6">
                <div className="flex justify-between items-center mb-4">
                    <div>
                        <h3 className="text-lg font-medium text-gray-900">Game Cards</h3>
                        <p className="text-sm text-gray-500 mt-1">
                            Create clues and red herrings for your scenario. Players will discover these during the
                            game.
                        </p>
                    </div>
                    <button
                        onClick={onAddCard}
                        className="bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 transition-colors flex items-center gap-2"
                    >
                        <Plus className="w-4 h-4"/>
                        Add Card
                    </button>
                </div>

                {error && (
                    <div className="bg-red-50 border border-red-200 rounded-lg p-3 mb-4">
                        <p className="text-red-600 text-sm">{error}</p>
                    </div>
                )}

                <div className="text-sm text-gray-600 bg-gray-50 rounded-lg p-4">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                            <h4 className="font-medium text-green-700 mb-2 flex items-center">
                                <div className="w-3 h-3 bg-green-500 rounded-full mr-2"></div>
                                Relevant Cards (Clues)
                            </h4>
                            <p>These cards contain information that helps solve the mystery. They should guide players
                                toward the solution.</p>
                        </div>
                        <div>
                            <h4 className="font-medium text-red-700 mb-2 flex items-center">
                                <div className="w-3 h-3 bg-red-500 rounded-full mr-2"></div>
                                Irrelevant Cards (Red Herrings)
                            </h4>
                            <p>These cards are designed to mislead players. They contain false information or
                                distractions.</p>
                        </div>
                    </div>
                    <div className="mt-4 p-3 bg-yellow-50 border border-yellow-200 rounded">
                        <p className="text-yellow-800 text-xs">
                            <strong>Requirements:</strong> You need at least 15 cards total, with a good mix of relevant
                            and irrelevant cards.
                            Minimum 4 cards per player.
                        </p>
                    </div>
                </div>
            </div>

            {/* Cards List */}
            {cards.length === 0 ? (
                <div className="bg-white rounded-lg shadow p-12 text-center">
                    <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                        <Plus className="w-8 h-8 text-gray-400"/>
                    </div>
                    <h3 className="text-lg font-medium text-gray-900 mb-2">No cards added yet</h3>
                    <p className="text-gray-500 mb-6">
                        Start by adding your first game card. You'll need at least 15 cards to create a scenario.
                    </p>
                    <button
                        onClick={onAddCard}
                        className="bg-green-600 text-white px-6 py-3 rounded-lg hover:bg-green-700 transition-colors"
                    >
                        Add First Card
                    </button>
                </div>
            ) : (
                <div className="space-y-4">
                    {/* Summary Stats */}
                    <div className="bg-white rounded-lg shadow p-4">
                        <div className="grid grid-cols-3 gap-4 text-center">
                            <div>
                                <div className="text-2xl font-bold text-gray-900">{cards.length}</div>
                                <div className="text-sm text-gray-500">Total Cards</div>
                            </div>
                            <div>
                                <div className="text-2xl font-bold text-green-600">
                                    {cards.filter(card => card.isRelevant).length}
                                </div>
                                <div className="text-sm text-gray-500">Relevant</div>
                            </div>
                            <div>
                                <div className="text-2xl font-bold text-red-600">
                                    {cards.filter(card => !card.isRelevant).length}
                                </div>
                                <div className="text-sm text-gray-500">Red Herrings</div>
                            </div>
                        </div>
                    </div>

                    {/* Cards Grid */}
                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                        {cards.map((card, index) => (
                            <CardItem
                                key={card.id}
                                card={card}
                                allCards={cards}
                                index={index}
                                isEditing={editingCardId === card.id}
                                onEdit={() => onSetEditingCardId(card.id)}
                                onCancel={handleSaveOrCancel}
                                onDelete={() => onDeleteCard(card.id)}
                                onUpdate={onUpdateCard}
                            />
                        ))}
                    </div>

                    {/* Add More Card Button */}
                    <div className="flex justify-center pt-4">
                        <button
                            onClick={onAddCard}
                            className="bg-green-600 text-white px-6 py-3 rounded-lg hover:bg-green-700 transition-colors flex items-center gap-2"
                        >
                            <Plus className="w-4 h-4"/>
                            Add Another Card
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default CardsTab;