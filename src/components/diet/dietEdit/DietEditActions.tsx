import React from 'react';
import { Trash2 } from 'lucide-react';

interface DietEditActionsProps {
    loading: boolean;
    deleteConfirm: boolean;
    onDelete: () => void;
    onClose: () => void;
    onSave: () => void;
    hasDays: boolean;
}

const DietEditActions: React.FC<DietEditActionsProps> = ({
                                                             loading,
                                                             deleteConfirm,
                                                             onDelete,
                                                             onClose,
                                                             onSave,
                                                             hasDays
                                                         }) => {
    return (
        <div className="flex justify-between items-center pt-4 border-t">
            <button
                onClick={onDelete}
                disabled={loading}
                className={`flex items-center gap-2 px-4 py-2 rounded-lg 
                    ${deleteConfirm
                    ? 'bg-red-600 text-white hover:bg-red-700'
                    : 'text-red-600 hover:text-red-700'}`}
            >
                <Trash2 className="h-5 w-5"/>
                {deleteConfirm ? 'Potwierdź usunięcie' : 'Usuń dietę'}
            </button>

            <div className="flex gap-4">
                <button
                    onClick={onClose}
                    disabled={loading}
                    className="px-4 py-2 border rounded-lg hover:bg-gray-50"
                >
                    Anuluj
                </button>
                <button
                    onClick={onSave}
                    disabled={loading || !hasDays}
                    className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
                >
                    {loading ? 'Zapisywanie...' : 'Zapisz zmiany'}
                </button>
            </div>
        </div>
    );
};

export default DietEditActions;