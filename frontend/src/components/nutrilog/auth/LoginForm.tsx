// src/components/shared/auth/LoginForm.tsx

import React, {useState} from "react";
import {useAuth} from "../../../contexts/AuthContext";
import {useNavigate} from "react-router-dom";
import {EnvelopeIcon, LockClosedIcon} from "@heroicons/react/24/outline";
import InputField from "../../shared/ui/InputField";
import {APPLICATION_CONFIGS, ApplicationType} from "../../../types/application";

const LoginForm = () => {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [selectedApp, setSelectedApp] = useState<ApplicationType>(ApplicationType.NUTRILOG);
    const [error, setError] = useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const {loginWithApplication} = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsSubmitting(true);
        setError(null);

        try {
            await loginWithApplication(email, password, selectedApp);

            // Navigate based on selected application
            const targetRoute = selectedApp === ApplicationType.NUTRILOG
                ? '/dashboard'
                : '/scandal-shuffle/dashboard';

            navigate(targetRoute);
        } catch (error) {
            console.error('Login failed:', error);
            setError("Nieprawidłowy email lub hasło");
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <form onSubmit={handleSubmit} className="space-y-6">
            {error && (
                <div className="bg-red-50 border border-red-200 text-red-600 rounded-lg p-3 text-sm">
                    {error}
                </div>
            )}

            {/* Application Selection */}
            <div>
                <label className="block text-sm font-medium text-gray-700 mb-3">
                    Wybierz aplikację
                </label>
                <div className="grid grid-cols-2 gap-3">
                    {Object.values(APPLICATION_CONFIGS).map((config) => (
                        <button
                            key={config.type}
                            type="button"
                            onClick={() => setSelectedApp(config.type)}
                            className={`p-3 rounded-lg border-2 transition-all text-center ${
                                selectedApp === config.type
                                    ? 'border-primary bg-primary/5 text-primary'
                                    : 'border-gray-200 hover:border-gray-300'
                            }`}
                        >
                            <div className="font-medium text-sm">{config.name}</div>
                            <div className="text-xs text-gray-500 mt-1">
                                {config.authMethod === 'firebase' ? 'Firebase' : 'Supabase'}
                            </div>
                        </button>
                    ))}
                </div>
            </div>

            <InputField
                id="email"
                label="Email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                icon={<EnvelopeIcon className="h-5 w-5 text-gray-400"/>}
                placeholder="twoj@email.com"
                required
            />

            <InputField
                id="password"
                label="Hasło"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                icon={<LockClosedIcon className="h-5 w-5 text-gray-400"/>}
                placeholder="••••••••"
                required
            />

            <button
                type="submit"
                disabled={isSubmitting}
                className="w-full flex justify-center py-3 px-4 border border-transparent rounded-lg shadow-sm text-sm font-medium text-white bg-primary hover:bg-primary-dark focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary transition-colors disabled:opacity-50"
            >
                {isSubmitting ? (
                    <span className="flex items-center">
                       <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" fill="none" viewBox="0 0 24 24">
                           <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor"
                                   strokeWidth="4"/>
                           <path className="opacity-75" fill="currentColor"
                                 d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"/>
                       </svg>
                       Logowanie...
                   </span>
                ) : (
                    `Zaloguj się do ${APPLICATION_CONFIGS[selectedApp].name}`
                )}
            </button>
        </form>
    );
};

export default LoginForm;