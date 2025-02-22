import React, {useEffect, useState} from "react";
import {useChangeLog} from "../../hooks/useChangeLog";
import LoadingSpinner from "../common/LoadingSpinner";
import {PlusCircle} from "lucide-react";
import ChangelogForm from "./ChangelogForm";
import ChangelogList from "./ChangelogList";

const Changelog: React.FC = () => {
    const {entries, loading, addEntry, hasUnread, markAsRead} = useChangeLog();
    const [isAddingEntry, setIsAddingEntry] = useState(false);
    const [isSubmitting, setIsSubmitting] = useState(false);

    useEffect(() => {
        if (hasUnread) {
            markAsRead().catch(console.error);
        }
    }, [hasUnread]);

    const handleSubmit = async (data: any) => {
        setIsSubmitting(true);
        try {
            await addEntry(data);
            setIsAddingEntry(false);
        } finally {
            setIsSubmitting(false);
        }
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center h-64">
                <LoadingSpinner/>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h2 className="text-2xl font-bold">
                    Historia zmian
                </h2>
                <button
                    onClick={() => setIsAddingEntry(!isAddingEntry)}
                    className="flex items-center gap-2 px-4 py-2 text-blue-600 gover:bg-blue-50 rounded-lg transition-colors"
                >
                    <PlusCircle className="w-5 h-5"/>
                    {isAddingEntry ? 'Anuluj' : 'Dodaj wpis'}
                </button>
            </div>

            {isAddingEntry && (
                <div className="bg-white p-6 rounded-lg shadow-sm">
                    <ChangelogForm onSubmit={handleSubmit} isSubmitting={isSubmitting}/>
                </div>
            )}

            <ChangelogList entries={entries}/>
        </div>
    );
};

export default Changelog;