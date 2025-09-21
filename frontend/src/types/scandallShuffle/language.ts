export type SupportedLanguage = 'en' | 'pl' | 'uk';

export interface LanguageOption {
    code: SupportedLanguage;
    name: string;
    flag: string;
}

export const SUPPORTED_LANGUAGES: LanguageOption[] = [
    { code: 'en', name: 'English', flag: '🇺🇸' },
    { code: 'pl', name: 'Polski', flag: '🇵🇱' },
    { code: 'uk', name: 'Українська', flag: '🇺🇦' }
];

export const DEFAULT_LANGUAGE: SupportedLanguage = 'en';