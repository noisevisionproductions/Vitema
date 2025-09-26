import React from 'react';
import SearchInput from "../../../shared/common/SearchInput";
import { FilterState, StatusFilter } from "../types";

interface ScenarioFiltersProps {
    filters: FilterState;
    onFilterChange: (filterName: string, value: string) => void;
    scenarioCount: number;
    totalCount: number;
    availableLanguages: string[];
}

const ScenarioFilters: React.FC<ScenarioFiltersProps> = ({
                                                             filters,
                                                             onFilterChange,
                                                             scenarioCount,
                                                             totalCount,
                                                             availableLanguages
                                                         }) => {
    const statusOptions: StatusFilter[] = ['all', 'pending', 'approved', 'rejected'];

    return (
        <div className="p-4 bg-white rounded-lg shadow-sm space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 items-end">
                {/* Search Input */}
                <div className="md:col-span-2">
                    <label htmlFor="search-input" className="block text-sm font-medium text-gray-700 mb-1">
                        Search
                    </label>
                    <SearchInput
                        id="search-input"
                        placeholder="Search by name or description..."
                        value={filters.search}
                        onChange={(value) => onFilterChange('search', value)}
                    />
                </div>

                {/* Language Filter */}
                <div>
                    <label htmlFor="language-filter" className="block text-sm font-medium text-gray-700 mb-1">
                        Language
                    </label>
                    <select
                        id="language-filter"
                        value={filters.language}
                        onChange={(e) => onFilterChange('language', e.target.value)}
                        className="w-full p-2 border border-gray-300 rounded-lg bg-white shadow-sm"
                    >
                        <option value="all">All Languages</option>
                        {availableLanguages.map(lang => (
                            <option key={lang} value={lang}>{lang.toUpperCase()}</option>
                        ))}
                    </select>
                </div>
            </div>

            {/* Status Filter */}
            <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Status</label>
                <div className="flex flex-wrap gap-2">
                    {statusOptions.map(status => (
                        <button
                            key={status}
                            onClick={() => onFilterChange('status', status)}
                            className={`px-3 py-1 rounded-full text-sm font-medium transition-colors ${
                                filters.status === status
                                    ? 'bg-indigo-600 text-white'
                                    : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                            }`}
                        >
                            {status.charAt(0).toUpperCase() + status.slice(1)}
                        </button>
                    ))}
                </div>
            </div>

            <div className="text-sm text-gray-500 pt-2 border-t">
                Showing <strong>{scenarioCount}</strong> of <strong>{totalCount}</strong> scenarios.
            </div>
        </div>
    );
};

export default ScenarioFilters;