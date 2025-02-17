import React from 'react';
import SearchInput from '../common/SearchInput';
import {User} from '../../types/user';
import {XCircle} from 'lucide-react';

export type SortOption = 'newest' | 'oldest' | 'name';

interface DietFilterProps {
    activeUsers: User[];
    selectedUserId: string | null;
    onUserSelect: (userId: string | null) => void;
    searchQuery: string;
    onSearchChange: (query: string) => void;
    onReset: () => void;
    sortBy: SortOption;
    onSortChange: (sort: SortOption) => void;
}

const DietFilter: React.FC<DietFilterProps> = ({
                                                   activeUsers,
                                                   selectedUserId,
                                                   onUserSelect,
                                                   searchQuery,
                                                   onSearchChange,
                                                   onReset,
                                                   sortBy,
                                                   onSortChange
                                               }) => {
    const hasActiveFilters = selectedUserId !== null || searchQuery !== '' || sortBy !== 'newest';

    return (
        <div className="bg-white p-4 rounded-lg shadow-sm space-y-4">
            <div className="flex items-center gap-4">
                <div className="flex-1">
                    <SearchInput
                        value={searchQuery}
                        onChange={onSearchChange}
                        placeholder="Szukaj diet..."
                    />
                </div>
                <div className="flex items-center gap-2">
                    <select
                        value={sortBy}
                        onChange={(e) => onSortChange(e.target.value as SortOption)}
                        className="px-3 py-2 border rounded-lg text-sm focus:ring-2 focus:ring-blue-500"
                    >
                        <option value="newest">
                            Najnowsze
                        </option>
                        <option value="oldest">
                            Najstarsze
                        </option>
                        <option value="name">
                            Nazwa pliku
                        </option>
                    </select>
                    {hasActiveFilters && (
                        <button
                            onClick={onReset}
                            className="flex items-center gap-2 px-3 py-2 text-sm text-red-600 hover:text-red-700 hover:bg-red-50 rounded-lg transition-colors"
                        >
                            <XCircle className="w-4 h-4"/>
                            Wyczyść filtry
                        </button>
                    )}
                </div>
            </div>

            <div className="flex flex-wrap gap-2">
                <button
                    onClick={() => onUserSelect(null)}
                    className={`px-3 py-1 rounded-full text-sm ${
                        selectedUserId === null
                            ? 'bg-blue-100 text-blue-800'
                            : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                    }`}
                >
                    Wszyscy
                </button>
                {activeUsers.map((user) => (
                    <button
                        key={user.id}
                        onClick={() => onUserSelect(user.id)}
                        className={`px-3 py-1 rounded-full text-sm ${
                            selectedUserId === user.id
                                ? 'bg-blue-100 text-blue-800'
                                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                        }`}
                    >
                        {user.email}
                    </button>
                ))}
            </div>
        </div>
    );
};

export default DietFilter;