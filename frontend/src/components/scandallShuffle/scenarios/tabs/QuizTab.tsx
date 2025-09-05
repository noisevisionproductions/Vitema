import React from 'react';
import {Plus} from 'lucide-react';
import {QuizTabProps} from '../../../../types/scandallShuffle/scenario-creation';
import QuestionItem from "./item/QuestionItem";

/**
 * Quiz management tab for scenario creation
 * Updated to use QuestionData type from RN app
 */
const QuizTab: React.FC<QuizTabProps> = ({
                                             questions,
                                             error,
                                             onAddQuestion,
                                             onUpdateQuestion,
                                             onDeleteQuestion,
                                             editingQuestionId,
                                             onSetEditingQuestionId
                                         }) => {
    const getDifficultyStats = () => {
        return {
            easy: questions.filter(q => q.difficulty === 'easy').length,
            medium: questions.filter(q => q.difficulty === 'medium').length,
            hard: questions.filter(q => q.difficulty === 'hard').length
        };
    };

    const difficultyStats = getDifficultyStats();

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="bg-white rounded-lg shadow p-6">
                <div className="flex justify-between items-center mb-4">
                    <div>
                        <h3 className="text-lg font-medium text-gray-900">Quiz Questions</h3>
                        <p className="text-sm text-gray-500 mt-1">
                            Create questions that test players' understanding of the scenario solution.
                        </p>
                    </div>
                    <button
                        onClick={onAddQuestion}
                        className="bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 transition-colors flex items-center gap-2"
                    >
                        <Plus className="w-4 h-4"/>
                        Add Question
                    </button>
                </div>

                {error && (
                    <div className="bg-red-50 border border-red-200 rounded-lg p-3 mb-4">
                        <p className="text-red-600 text-sm">{error}</p>
                    </div>
                )}
            </div>

            {/* Questions List */}
            {questions.length === 0 ? (
                <div className="bg-white rounded-lg shadow p-12 text-center">
                    <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                        <Plus className="w-8 h-8 text-gray-400"/>
                    </div>
                    <h3 className="text-lg font-medium text-gray-900 mb-2">No questions added yet</h3>
                    <p className="text-gray-500 mb-6">
                        Create quiz questions to test players' understanding of your scenario. You need at least one
                        question.
                    </p>
                    <button
                        onClick={onAddQuestion}
                        className="bg-green-600 text-white px-6 py-3 rounded-lg hover:bg-green-700 transition-colors"
                    >
                        Add First Question
                    </button>
                </div>
            ) : (
                <div className="space-y-4">
                    {/* Summary Stats */}
                    <div className="bg-white rounded-lg shadow p-4">
                        <div className="grid grid-cols-4 gap-4 text-center">
                            <div>
                                <div className="text-2xl font-bold text-gray-900">{questions.length}</div>
                                <div className="text-sm text-gray-500">Total Questions</div>
                            </div>
                            <div>
                                <div className="text-2xl font-bold text-green-600">{difficultyStats.easy}</div>
                                <div className="text-sm text-gray-500">Easy</div>
                            </div>
                            <div>
                                <div className="text-2xl font-bold text-yellow-600">{difficultyStats.medium}</div>
                                <div className="text-sm text-gray-500">Medium</div>
                            </div>
                            <div>
                                <div className="text-2xl font-bold text-red-600">{difficultyStats.hard}</div>
                                <div className="text-sm text-gray-500">Hard</div>
                            </div>
                        </div>

                        {questions.length > 20 && (
                            <div className="mt-4 p-3 bg-yellow-50 border border-yellow-200 rounded-lg">
                                <p className="text-yellow-800 text-sm">
                                    <strong>Warning:</strong> You have {questions.length} questions. Consider reducing
                                    to 20 or fewer for better player experience.
                                </p>
                            </div>
                        )}
                    </div>

                    {/* Questions List */}
                    <div className="space-y-4">
                        {questions.map((question, index) => (
                            <QuestionItem
                                key={question.id}
                                question={question}
                                index={index}
                                isEditing={editingQuestionId === question.id}
                                onEdit={() => onSetEditingQuestionId(question.id)}
                                onCancel={() => onSetEditingQuestionId(null)}
                                onDelete={() => onDeleteQuestion(question.id)}
                                onUpdate={onUpdateQuestion}
                            />
                        ))}
                    </div>

                    {/* Add More Question Button */}
                    <div className="flex justify-center pt-4">
                        <button
                            onClick={onAddQuestion}
                            disabled={questions.length >= 20}
                            className={`px-6 py-3 rounded-lg transition-colors flex items-center gap-2 ${
                                questions.length >= 20
                                    ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                                    : 'bg-green-600 text-white hover:bg-green-700'
                            }`}
                        >
                            <Plus className="w-4 h-4"/>
                            {questions.length >= 20 ? 'Maximum Questions Reached' : 'Add Another Question'}
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default QuizTab;