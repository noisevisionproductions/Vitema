import React, {useState} from "react";
import {CardItemProps} from "../../../../../types/scandallShuffle/scenario-creation";
import {Edit, Eye, EyeOff, Trash2} from "lucide-react";
import {toast} from "../../../../../utils/toast";

const CardItem: React.FC<CardItemProps> = ({
                                               card,
                                               allCards,
                                               isEditing,
                                               onEdit,
                                               onCancel,
                                               onDelete,
                                               onUpdate
                                           }) => {
    const [error, setError] = useState<string | null>(null);

    const handleDoneEditing = () => {
        const trimmedContent = card.content.trim();

        if (!trimmedContent) {
            const errorMessage = 'Card content cannot be empty.';
            setError(errorMessage);
            toast.error(errorMessage);
            return;
        }

        const isDuplicate = allCards.some(
            otherCard =>
                otherCard.id !== card.id &&
                otherCard.content.trim().toLowerCase() === trimmedContent.toLowerCase()
        );

        if (isDuplicate) {
            const errorMessage = 'This card content is already used in another card.';
            setError(errorMessage);
            toast.error(errorMessage);
            return;
        }

        setError(null);
        onCancel();
    };

    const toggleRelevance = () => {
        onUpdate(card.id, {isRelevant: !card.isRelevant});
    };

    if (isEditing) {
        return (
            <div className="bg-white rounded-lg shadow border-2 border-green-200 p-6">
                <div className="space-y-4">

                    <h3 className="text-lg font-semibold text-gray-800 border-b pb-2">
                        Editing: {card.title}
                    </h3>

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Card Content *
                        </label>
                        <textarea
                            value={card.content}
                            onChange={(e) => {
                                if (error) setError(null);
                                onUpdate(card.id, {content: e.target.value});
                            }}
                            rows={4}
                            className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 resize-none
                                ${error ? 'border-red-500 ring-red-300' : 'border-gray-300 focus:ring-green-500'}
                            `}
                            placeholder="Enter the information this card reveals..."
                        />
                        {error ? (
                            <p className="mt-1 text-xs text-red-600">{error}</p>
                        ) : (
                            <p className="mt-1 text-xs text-gray-500">
                                {card.content.length}/500 characters
                            </p>
                        )}
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            Card Type
                        </label>
                        <div className="space-y-2">
                            <button
                                type="button"
                                onClick={() => onUpdate(card.id, {isRelevant: true, type: 'clue'})}
                                className={`w-full flex items-center justify-center py-3 px-4 rounded-md border transition-colors ${
                                    card.isRelevant
                                        ? 'bg-green-50 border-green-300 text-green-700'
                                        : 'bg-gray-50 border-gray-300 text-gray-500 hover:bg-gray-100'
                                }`}
                            >
                                <div className="w-3 h-3 bg-green-500 rounded-full mr-2"></div>
                                Relevant (Clue) - Helps solve the mystery
                            </button>
                            <button
                                type="button"
                                onClick={() => onUpdate(card.id, {isRelevant: false, type: 'red_herring'})}
                                className={`w-full flex items-center justify-center py-3 px-4 rounded-md border transition-colors ${
                                    !card.isRelevant
                                        ? 'bg-red-50 border-red-300 text-red-700'
                                        : 'bg-gray-50 border-gray-300 text-gray-500 hover:bg-gray-100'
                                }`}
                            >
                                <div className="w-3 h-3 bg-red-500 rounded-full mr-2"></div>
                                Irrelevant (Red Herring) - Misleading information
                            </button>
                        </div>
                    </div>

                    <div className="flex justify-end gap-2 pt-4 border-t">
                        <button
                            onClick={handleDoneEditing}
                            className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 transition-colors"
                        >
                            Done
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="bg-white rounded-lg shadow hover:shadow-md transition-shadow border border-gray-200">
            <div className="p-6">
                <div className="flex justify-between items-start mb-3">
                    <div className="flex items-center gap-2">
                        {/* ✅ ZMIANA 2: Usunięto redundantny numer "Card {index + 1}", */}
                        {/* ponieważ `card.title` już go zawiera. */}
                        <span className={`px-2 py-1 rounded-full text-xs font-medium border flex items-center gap-1 ${
                            card.isRelevant
                                ? 'bg-green-100 text-green-800 border-green-200'
                                : 'bg-red-100 text-red-800 border-red-200'
                        }`}>
                            <div className={`w-2 h-2 rounded-full ${
                                card.isRelevant ? 'bg-green-500' : 'bg-red-500'
                            }`}></div>
                            {card.isRelevant ? 'Relevant' : 'Red Herring'}
                        </span>
                    </div>
                    <div className="flex gap-1">
                        <button
                            onClick={toggleRelevance}
                            className="p-1.5 text-gray-400 hover:text-gray-600 rounded transition-colors"
                            title={`Mark as ${card.isRelevant ? 'irrelevant' : 'relevant'}`}
                        >
                            {card.isRelevant ? <EyeOff className="w-4 h-4"/> : <Eye className="w-4 h-4"/>}
                        </button>
                        <button
                            onClick={onEdit}
                            className="p-1.5 text-gray-400 hover:text-blue-600 rounded transition-colors"
                            title="Edit card"
                        >
                            <Edit className="w-4 h-4"/>
                        </button>
                        <button
                            onClick={onDelete}
                            className="p-1.5 text-gray-400 hover:text-red-600 rounded transition-colors"
                            title="Delete card"
                        >
                            <Trash2 className="w-4 h-4"/>
                        </button>
                    </div>
                </div>
                <h4 className="font-medium text-gray-900 mb-2 line-clamp-2">{card.title ||
                    <span className="text-gray-400">Untitled Card</span>}</h4>
                <p className="text-gray-600 text-sm leading-relaxed line-clamp-4">{card.content ||
                    <span className="text-gray-400">No content yet...</span>}</p>
                <div className="mt-3 pt-3 border-t border-gray-100">
                    <div className="flex items-center justify-between text-xs text-gray-500">
                        <span>{card.content.length} characters</span>
                        <span className="capitalize">{card.type || (card.isRelevant ? 'clue' : 'red_herring')}</span>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CardItem;