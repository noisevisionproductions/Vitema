import {CheckCircle, Edit, Trash2} from "lucide-react";
import {QuestionItemProps} from "../../../../../types/scandallShuffle/scenario-creation";
import React, {useState} from "react";
import {toast} from "../../../../../utils/toast";

/**
 * Individual question component for editing and display
 * Updated to use QuestionData type from RN app
 */
const QuestionItem: React.FC<QuestionItemProps> = ({
                                                       question,
                                                       index,
                                                       isEditing,
                                                       onEdit,
                                                       onCancel,
                                                       onDelete,
                                                       onUpdate
                                                   }) => {
    const [errors, setErrors] = useState<{ question?: boolean; options?: boolean[] }>({});

    const handleDoneEditing = () => {
        const newErrors: { question?: boolean; options?: boolean[] } = {};
        const isQuestionEmpty = !question.question.trim();
        const optionErrors = question.options.map(opt => !opt.trim());
        const hasEmptyOptions = optionErrors.some(err => err);

        if (isQuestionEmpty) {
            newErrors.question = true;
        }
        if (hasEmptyOptions) {
            newErrors.options = optionErrors;
        }

        setErrors(newErrors);

        if (isQuestionEmpty || hasEmptyOptions) {
            toast.error("Please fill in the question and all answer options.");
            return;
        }

        onCancel();
    };

    const updateOption = (optionIndex: number, value: string) => {
        if (errors.options?.[optionIndex]) {
            setErrors(prev => ({
                ...prev,
                options: prev.options?.map((err, i) => (i === optionIndex ? false : err))
            }));
        }
        const newOptions = [...question.options];
        newOptions[optionIndex] = value;
        onUpdate(question.id, {options: newOptions});
    };

    const removeOption = (optionIndex: number) => {
        if (question.options.length > 2) {
            const newOptions = question.options.filter((_, i) => i !== optionIndex);
            const newCorrectAnswer = question.correctAnswer >= optionIndex
                ? Math.max(0, question.correctAnswer - 1)
                : question.correctAnswer;

            onUpdate(question.id, {
                options: newOptions,
                correctAnswer: newCorrectAnswer
            });
        }
    };

    const getDifficultyColor = (difficulty: string) => {
        switch (difficulty) {
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

    if (isEditing) {
        return (
            <div className="bg-white rounded-lg shadow border-2 border-green-200 p-6">
                <div className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Question *</label>
                        <textarea
                            value={question.question}
                            onChange={(e) => {
                                if (errors.question) setErrors(prev => ({...prev, question: false}));
                                onUpdate(question.id, {question: e.target.value});
                            }}
                            rows={2}
                            className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 resize-none
                                ${errors.question ? 'border-red-500 ring-red-300' : 'border-gray-300 focus:ring-green-500'}
                            `}
                            placeholder="Enter your question..."
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Difficulty</label>
                        <select
                            value={question.difficulty}
                            onChange={(e) => onUpdate(question.id, {difficulty: e.target.value as any})}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500"
                        >
                            <option value="easy">Easy</option>
                            <option value="medium">Medium</option>
                            <option value="hard">Hard</option>
                        </select>
                    </div>

                    <div>
                        <div className="flex justify-between items-center mb-2">
                            <label className="block text-sm font-medium text-gray-700">Answer Options *</label>
                        </div>
                        <div className="space-y-3">
                            {question.options.map((option, optionIndex) => (
                                <div key={optionIndex} className="flex items-center gap-3">
                                    <button
                                        type="button"
                                        onClick={() => onUpdate(question.id, {correctAnswer: optionIndex})}
                                        className={`flex-shrink-0 w-6 h-6 rounded-full border-2 flex items-center justify-center transition-colors ${question.correctAnswer === optionIndex
                                            ? 'bg-green-500 border-green-500'
                                            : 'bg-transparent border-gray-300 hover:border-green-400'
                                        } `}
                                        title="Mark as correct answer"
                                    >
                                        {question.correctAnswer === optionIndex &&
                                            <CheckCircle className="w-4 h-4 text-white"/>}
                                    </button>
                                    <div className="flex-1 relative">
                                        <input
                                            type="text"
                                            value={option}
                                            onChange={(e) => updateOption(optionIndex, e.target.value)}
                                            className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 
                                                ${errors.options?.[optionIndex] ? 'border-red-500 ring-red-300' : 'border-gray-300 focus:ring-green-500'}
                                            `}
                                            placeholder={`Option ${String.fromCharCode(65 + optionIndex)}`}
                                        />
                                    </div>
                                    {question.options.length > 2 && (
                                        <button type="button" onClick={() => removeOption(optionIndex)} className="...">
                                            <Trash2 className="w-4 h-4"/>
                                        </button>
                                    )}
                                </div>
                            ))}
                        </div>
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Explanation (optional)</label>
                        <textarea
                            value={question.explanation || ''}
                            onChange={(e) => onUpdate(question.id, {explanation: e.target.value})}
                            rows={3}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md ..."
                            placeholder="Explain why this is the correct answer..."
                        />
                    </div>

                    <div className="flex justify-end gap-2 pt-4 border-t">
                        <button onClick={handleDoneEditing}
                                className="px-4 py-2 bg-green-600 text-white rounded-md ...">
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
                        <span className="text-sm font-medium text-gray-500">Question {index + 1}</span>
                        <span
                            className={`px-2 py-1 rounded-full text-xs font-medium ${getDifficultyColor(question.difficulty)}`}>
                            {question.difficulty}
                        </span>
                    </div>
                    <div className="flex gap-1">
                        <button
                            onClick={onEdit}
                            className="p-1.5 text-gray-400 hover:text-blue-600 rounded transition-colors"
                            title="Edit question"
                        >
                            <Edit className="w-4 h-4"/>
                        </button>
                        <button
                            onClick={onDelete}
                            className="p-1.5 text-gray-400 hover:text-red-600 rounded transition-colors"
                            title="Delete question"
                        >
                            <Trash2 className="w-4 h-4"/>
                        </button>
                    </div>
                </div>

                <h4 className="font-medium text-gray-900 mb-3 leading-relaxed">{question.question}</h4>

                <div className="space-y-2 mb-3">
                    {question.options.map((option, optionIndex) => (
                        <div
                            key={optionIndex}
                            className={`flex items-center gap-3 p-3 rounded-md transition-colors ${
                                optionIndex === question.correctAnswer
                                    ? 'bg-green-50 border border-green-200'
                                    : 'bg-gray-50 border border-gray-200'
                            }`}
                        >
                            <div
                                className={`w-5 h-5 rounded-full border-2 flex items-center justify-center flex-shrink-0 ${
                                    optionIndex === question.correctAnswer
                                        ? 'border-green-500 bg-green-500'
                                        : 'border-gray-300'
                                }`}>
                                <span className="text-xs font-medium text-white">
                                    {optionIndex === question.correctAnswer && '✓'}
                                </span>
                            </div>
                            <span className={`text-sm flex-1 ${
                                optionIndex === question.correctAnswer
                                    ? 'text-green-800 font-medium'
                                    : 'text-gray-600'
                            }`}>
                                <span className="font-medium mr-2">{String.fromCharCode(65 + optionIndex)}.</span>
                                {option}
                            </span>
                        </div>
                    ))}
                </div>

                {question.explanation && (
                    <div className="bg-blue-50 border border-blue-200 rounded-md p-3 mt-3">
                        <div className="flex items-start gap-2">
                            <div className="w-4 h-4 bg-blue-500 rounded-full flex-shrink-0 mt-0.5"></div>
                            <div>
                                <span
                                    className="text-xs font-medium text-blue-800 uppercase tracking-wide">Explanation</span>
                                <p className="text-sm text-blue-700 mt-1 leading-relaxed">{question.explanation}</p>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default QuestionItem;