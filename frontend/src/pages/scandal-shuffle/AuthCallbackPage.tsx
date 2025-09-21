import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { supabase } from '../../config/supabase';
import ResetPasswordPage from './ResetPasswordPage';
import EmailVerifiedPage from './EmailVerifiedPage';
import LoadingSpinner from '../../components/shared/common/LoadingSpinner';

/**
 * Handles different types of auth callbacks from Supabase
 */
export default function AuthCallbackPage() {
    const [searchParams] = useSearchParams();
    const [callbackType, setCallbackType] = useState<'recovery' | 'signup' | 'unknown' | null>(null);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        const handleAuthCallback = async () => {
            try {
                // Extract the type from URL parameters
                const type = searchParams.get('type');

                if (type === 'recovery') {
                    // This is a password recovery callback
                    setCallbackType('recovery');
                } else if (type === 'signup') {
                    // This is email verification after signup
                    setCallbackType('signup');
                } else {
                    // Try to handle auth state change for other cases
                    const { data, error } = await supabase.auth.getSession();

                    if (error) {
                        console.error('Auth callback error:', error);
                        navigate('/error');
                        return;
                    }

                    if (data.session) {
                        // User is authenticated, likely email verification
                        setCallbackType('signup');
                    } else {
                        setCallbackType('unknown');
                    }
                }
            } catch (error) {
                console.error('Error handling auth callback:', error);
                navigate('/error');
            } finally {
                setLoading(false);
            }
        };

        handleAuthCallback().catch(console.error);
    }, [searchParams, navigate]);

    if (loading) {
        return (
            <div className="flex items-center justify-center h-screen">
                <LoadingSpinner size="lg" />
            </div>
        );
    }

    // Render appropriate component based on callback type
    switch (callbackType) {
        case 'recovery':
            return <ResetPasswordPage />;
        case 'signup':
            return <EmailVerifiedPage />;
        default:
            return (
                <div className="flex items-center justify-center h-screen">
                    <div className="text-center">
                        <h1 className="text-xl font-semibold mb-4">Authentication Complete</h1>
                        <p className="text-gray-600 mb-4">You can now close this window.</p>
                        <button
                            onClick={() => navigate('/')}
                            className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
                        >
                            Return to Home
                        </button>
                    </div>
                </div>
            );
    }
}