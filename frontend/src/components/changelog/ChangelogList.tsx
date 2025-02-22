import React from "react";
import {ChangelogEntry} from "../../types/changeLog";
import {Bug, Lightbulb, Sparkles} from "lucide-react";
import {formatDate} from "../../utils/dateFormatters";

interface ChangelogListProps {
    entries: ChangelogEntry[];
}

const ChangelogList: React.FC<ChangelogListProps> = ({ entries}) => {
    const getTypeIcon = (type: ChangelogEntry['type']) => {
        switch (type) {
            case "feature":
                return <Sparkles className="w-5 h-5 text-blue-500"/>;
            case "fix":
                return <Bug className="w-5 h-5 text-red-500"/>;
            case "improvement":
                return <Lightbulb className="w-5 h-5 text-yellow-500"/>;
        }
    };

    const getTypeLabel = (type: ChangelogEntry['type']) => {
        switch (type) {
            case 'feature':
                return 'Nowa funkcja';
            case "fix":
                return 'Poprawka';
            case "improvement":
                return 'Ulepszenie';
        }
    };

    return (
        <div className="space-y-6">
            {entries.map((entry) => (
                <div key={entry.id} className="bg-white p-4 rounded-lg shadow-sm">
                    <div className="flex items-start gap-4">
                        <div className="p-2 bg-gray-50 rounded-lg">
                            {getTypeIcon(entry.type)}
                        </div>
                        <div className="flex-1">
                            <div className="flex justify-between items-start">
                                <h3 className="text-lg font-medium">
                                    {entry.title}
                                </h3>
                                <span className="text-sm text-gray-500">
                                    {formatDate(entry.createdAt)}
                                </span>
                            </div>
                            <span className="inline-block px-2 py-1 text-xs rounded-full bg-gray-100 text-gray-700 mt-1">
                                {getTypeLabel(entry.type)}
                            </span>
                            <p className="mt-2 text-gray-600 whitespace-pre-line">
                                {entry.description}
                            </p>
                            <p className="mt-2 text-sm text-gray-500">
                                Dodał(a): {entry.author}
                            </p>
                        </div>
                    </div>
                </div>
            ))}
        </div>
    );
};

export default ChangelogList;