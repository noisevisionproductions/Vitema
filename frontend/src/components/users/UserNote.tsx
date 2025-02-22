import React, {useEffect, useState} from "react";
import {Check, Pencil, X} from "lucide-react";

interface UserNoteProps {
    note?: string;
    onSave: (note: string) => Promise<void>;
}

const UserNote: React.FC<UserNoteProps> = ({note, onSave}) => {
    const [isEditing, setIsEditing] = useState(false);
    const [editedNote, setEditedNote] = useState(note || '');
    const [isLoading, setIsLoading] = useState(false);

    const handleSave = async () => {
        setIsLoading(true);
        try {
            await onSave(editedNote);
            setIsEditing(false);
        } catch (error) {
            setEditedNote(note || '');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        setEditedNote(note || '')
    }, [note]);

    const handleCancel = () => {
        setEditedNote(note || '');
        setIsEditing(false);
    };

    if (isEditing) {
        return (
            <div className="flex items-center gap-2">
                <input
                    type="text"
                    value={editedNote}
                    onChange={(e) => setEditedNote(e.target.value)}
                    className="flex-1 px-2 py-1 text-sm border rounded"
                    placeholder="Dodaj notatkę..."
                    disabled={isLoading}
                />
                <button
                    onClick={handleSave}
                    disabled={isLoading}
                    className="p-1 text-green-600 hover:text-green-800"
                >
                    <Check className="w-4 h-4"/>
                </button>
                <button
                    onClick={handleCancel}
                    disabled={isLoading}
                    className="p-1 text-red-700 hover:text-red-800"
                >
                    <X className="w-4 h-4"/>
                </button>
            </div>
        );
    }

    return (

        <div className="flex items-center gap-2">
            <span className="text-sm text-gray-600">
                {note || 'Brak notatki'}
            </span>
            <button
                onClick={() => setIsEditing(true)}
                className="p-1 text-gray-400 hover:text-gray-600"
            >
                <Pencil className="w-4 h-4"/>
            </button>
        </div>
    );
};

export default UserNote;