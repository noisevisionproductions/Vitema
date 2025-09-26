import React, {useEffect, useState} from 'react';
import {Scenario} from '../../../../types/scandallShuffle/database';
import {CardData, QuestionData} from '../../../../types/scandallShuffle/scenario-creation';
import {ScenarioApiService} from '../../../../services/scandallShuffle/ScenarioApiService';
import LoadingSpinner from '../../../shared/common/LoadingSpinner';
import {BookText, CheckCircle2, Clapperboard, HelpCircle, Lightbulb, User, Users, X, XCircle} from 'lucide-react';

interface ScenarioDetailModalProps {
    scenario: Scenario;
    onClose: () => void;
}

// A small component to render each detail item cleanly
const DetailItem: React.FC<{ icon: React.ReactNode; label: string; value: React.ReactNode }> = ({
                                                                                                    icon,
                                                                                                    label,
                                                                                                    value
                                                                                                }) => (
    <div className="flex items-start text-sm">
        <div className="flex-shrink-0 w-6 h-6 text-gray-500">{icon}</div>
        <div className="ml-2">
            <span className="font-semibold text-gray-800">{label}:</span>
            <span className="ml-1 text-gray-600">{value}</span>
        </div>
    </div>
);


const ScenarioDetailModal: React.FC<ScenarioDetailModalProps> = ({scenario, onClose}) => {
    const [cards, setCards] = useState<CardData[]>([]);
    const [questions, setQuestions] = useState<QuestionData[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchDetails = async () => {
            setLoading(true);
            setError(null);
            try {
                const [cardsData, questionsData] = await Promise.all([
                    ScenarioApiService.getCardsForScenario(scenario.id),
                    ScenarioApiService.getQuestionsForScenario(scenario.id),
                ]);
                setCards(cardsData);
                setQuestions(questionsData);
            } catch (err) {
                console.error('Failed to fetch scenario details:', err);
                setError('Could not load scenario details. Please try again later.');
            } finally {
                setLoading(false);
            }
        };

        fetchDetails().catch(console.error);
    }, [scenario.id]);

    return (
        // Modal Backdrop
        <div
            onClick={onClose}
            className="fixed inset-0 bg-black bg-opacity-60 flex justify-center items-center z-50 p-4 transition-opacity duration-300"
        >
            {/* Modal Content */}
            <div
                onClick={(e) => e.stopPropagation()}
                className="bg-gray-50 rounded-lg shadow-2xl w-full max-w-4xl h-full max-h-[90vh] flex flex-col"
            >
                {/* Header */}
                <div className="flex justify-between items-center p-4 border-b bg-white rounded-t-lg">
                    <h2 className="text-xl font-bold text-gray-800">{scenario.name}</h2>
                    <button onClick={onClose}
                            className="p-2 text-gray-500 hover:text-gray-800 hover:bg-gray-100 rounded-full">
                        <X size={24}/>
                    </button>
                </div>

                {/* Body with Scrolling */}
                <div className="flex-grow overflow-y-auto p-6 space-y-6">
                    {loading ? (
                        <div className="flex justify-center items-center h-full"><LoadingSpinner size="lg"/></div>
                    ) : error ? (
                        <div className="text-center text-red-500">{error}</div>
                    ) : (
                        <>
                            {/* Main Details Section */}
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 p-4 bg-white rounded-lg border">
                                {scenario.image_url &&
                                    <img
                                        src={scenario.image_url}
                                        alt={scenario.name}
                                        className="w-full max-h-64 object-contain rounded-md bg-gray-100"
                                    />
                                }
                                <div className={`space-y-3 ${scenario.image_url ? '' : 'md:col-span-2'}`}>
                                    <p className="text-gray-700">{scenario.description}</p>
                                    <div className="grid grid-cols-2 gap-3 pt-3 border-t">
                                        <DetailItem icon={<Users/>} label="Players"
                                                    value={`1-${scenario.max_players}`}/>
                                        <DetailItem icon={<User/>} label="Difficulty" value={scenario.difficulty}/>
                                        <DetailItem icon={<Clapperboard/>} label="Initial Clue"
                                                    value={scenario.initial_clue}/>
                                        <DetailItem icon={<Lightbulb/>} label="Solution" value={scenario.solution}/>
                                    </div>
                                </div>
                            </div>

                            {/* Cards Section */}
                            <div>
                                <h3 className="text-lg font-semibold mb-3 flex items-center"><BookText
                                    className="mr-2"/> Game Cards ({cards.length})</h3>
                                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                                    {cards.map((card, index) => (
                                        <div key={card.id || index} className="p-3 bg-white border rounded-md text-sm">
                                            <div className="flex justify-between items-center mb-1">
                                                <p className="font-semibold text-gray-800">Card {index + 1}</p>
                                                <span
                                                    className={`px-2 py-0.5 rounded-full text-xs font-medium ${card.isRelevant ? 'bg-blue-100 text-blue-800' : 'bg-orange-100 text-orange-800'}`}>
                                                    {card.isRelevant ? 'Clue' : 'Red Herring'}
                                                </span>
                                            </div>
                                            <p className="text-gray-600">{card.content}</p>
                                        </div>
                                    ))}
                                </div>
                            </div>

                            {/* Quiz Section */}
                            <div>
                                <h3 className="text-lg font-semibold mb-3 flex items-center"><HelpCircle
                                    className="mr-2"/> Quiz Questions ({questions.length})</h3>
                                <div className="space-y-4">
                                    {questions.map((q, index) => (
                                        <div key={q.id || index} className="p-4 bg-white border rounded-md">
                                            <p className="font-semibold mb-2">{index + 1}. {q.question}</p>
                                            <ul className="space-y-1 text-sm">
                                                {q.options.map((option, optIndex) => (
                                                    <li key={optIndex}
                                                        className={`flex items-center p-1 rounded ${optIndex === q.correctAnswer ? 'text-green-700 font-bold' : 'text-gray-700'}`}>
                                                        {optIndex === q.correctAnswer
                                                            ? <CheckCircle2
                                                                className="w-4 h-4 mr-2 text-green-500 flex-shrink-0"/>
                                                            : <XCircle
                                                                className="w-4 h-4 mr-2 text-gray-300 flex-shrink-0"/>
                                                        }
                                                        {option}
                                                    </li>
                                                ))}
                                            </ul>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        </>
                    )}
                </div>
            </div>
        </div>
    );
};

export default ScenarioDetailModal;