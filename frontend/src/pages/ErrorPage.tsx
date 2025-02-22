import React, { useState } from 'react';

const ErrorPage: React.FC = () => {
    const [showInstructions, setShowInstructions] = useState(false);

    const instructions = [
        {
            browser: 'Chrome',
            steps: [
                'Kliknij menu (trzy kropki) w prawym górnym rogu',
                'Wybierz "Ustawienia"',
                'Przejdź do "Prywatność i bezpieczeństwo"',
                'Kliknij "Wyczyść dane przeglądania"',
                'Wybierz "Pliki cookie i inne dane witryn"',
                'Kliknij "Wyczyść dane"'
            ]
        },
        {
            browser: 'Firefox',
            steps: [
                'Kliknij menu w prawym górnym rogu',
                'Wybierz "Ustawienia"',
                'Przejdź do "Prywatność i bezpieczeństwo"',
                'W sekcji "Ciasteczka i dane stron" kliknij "Wyczyść dane"'
            ]
        },
        {
            browser: 'Safari',
            steps: [
                'Otwórz menu Safari',
                'Wybierz "Preferencje"',
                'Przejdź do zakładki "Prywatność"',
                'Kliknij "Zarządzaj danymi witryn"',
                'Wybierz witrynę i kliknij "Usuń" lub "Usuń wszystko"'
            ]
        }
    ];

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50 p-4">
            <div className="max-w-lg w-full space-y-8 bg-white rounded-lg shadow p-8">
                <div className="text-center">
                    <h2 className="text-2xl font-bold text-gray-900 mb-4">
                        Wystąpił problem z autoryzacją
                    </h2>
                    <p className="text-gray-600 mb-6">
                        Aby rozwiązać ten problem, należy wyczyścić pliki cookie przeglądarki i odświeżyć stronę.
                    </p>

                    <div className="space-y-4">
                        <button
                            onClick={() => setShowInstructions(!showInstructions)}
                            className="w-full px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
                        >
                            {showInstructions ? 'Ukryj instrukcje' : 'Pokaż instrukcje czyszczenia plików cookie'}
                        </button>

                        {showInstructions && (
                            <div className="mt-6 text-left">
                                <div className="space-y-6">
                                    {instructions.map((browser) => (
                                        <div key={browser.browser} className="bg-gray-50 p-4 rounded-lg">
                                            <h3 className="font-semibold mb-2">{browser.browser}</h3>
                                            <ol className="list-decimal list-inside space-y-1">
                                                {browser.steps.map((step, index) => (
                                                    <li key={index} className="text-gray-600 text-sm">
                                                        {step}
                                                    </li>
                                                ))}
                                            </ol>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        )}

                        <button
                            onClick={() => window.location.reload()}
                            className="w-full px-4 py-2 border border-gray-300 rounded hover:bg-gray-50"
                        >
                            Odśwież stronę
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ErrorPage;