import React from 'react';
import { Globe } from 'lucide-react';
import { SupportedLanguage, SUPPORTED_LANGUAGES } from '../../../../types/scandallShuffle/language';

interface LanguageSelectorProps {
    value: SupportedLanguage;
    onChange: (language: SupportedLanguage) => void;
    error?: string;
    disabled?: boolean;
}

/**
 * Language selector component for scenario creation
 */
const LanguageSelector: React.FC<LanguageSelectorProps> = ({
                                                               value,
                                                               onChange,
                                                               error,
                                                               disabled = false
                                                           }) => {
    return (
        <div>
            <label htmlFor="language" className="block text-sm font-medium text-gray-700 mb-2">
                <div className="flex items-center gap-2">
                    <Globe className="w-4 h-4" />
                    Language *
                </div>
            </label>
            <select
                id="language"
                value={value}
                onChange={(e) => onChange(e.target.value as SupportedLanguage)}
                disabled={disabled}
                className={`w-full px-4 py-3 border rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent bg-white transition-colors ${
                    error ? 'border-red-500' : 'border-gray-300'
                } ${disabled ? 'opacity-50 cursor-not-allowed' : ''}`}
            >
                {SUPPORTED_LANGUAGES.map(lang => (
                    <option key={lang.code} value={lang.code}>
                        {lang.flag} {lang.name}
                    </option>
                ))}
            </select>
            {error && (
                <p className="mt-2 text-sm text-red-600">{error}</p>
            )}
            <p className="mt-1 text-xs text-gray-500">
                Select the primary language for this scenario's content
            </p>
        </div>
    );
};

export default LanguageSelector;