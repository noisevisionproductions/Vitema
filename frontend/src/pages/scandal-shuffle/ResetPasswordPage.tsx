import React, {useEffect, useState} from 'react';
import {supabase} from "../../config/supabase";

export default function ResetPasswordPage() {
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState<string | null>(null);
    const [isValidSession, setIsValidSession] = useState(false);

    useEffect(() => {
        // Check if user has a valid recovery session
        const checkSession = async () => {
            const {data: {session}} = await supabase.auth.getSession();

            if (session) {
                setIsValidSession(true);
            } else {
                setError('Invalid or expired reset link. Please request a new password reset.');
            }
        };

        checkSession().catch(console.error);

        // Listen for auth state changes during password recovery
        const {data: {subscription}} = supabase.auth.onAuthStateChange((event) => {
            if (event === 'PASSWORD_RECOVERY') {
                setIsValidSession(true);
                setError(null);
            }
        });

        return () => {
            subscription.unsubscribe();
        };
    }, []);

    const tryOpenApp = () => {
        // Try multiple methods to open the app
        const appScheme = 'scandalshufflemobile://auth/password-reset-success';

        // Method 1: Direct navigation
        window.location.href = appScheme;

        // Method 2: Fallback with iframe (for some browsers)
        setTimeout(() => {
            const iframe = document.createElement('iframe');
            iframe.style.display = 'none';
            iframe.src = appScheme;
            document.body.appendChild(iframe);

            setTimeout(() => {
                document.body.removeChild(iframe);
            }, 1000);
        }, 500);
    };

    const handleResetPassword = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!isValidSession) {
            setError('Invalid session. Please request a new password reset.');
            return;
        }

        setError(null);
        setSuccess(null);

        // Validation
        if (password.length < 6) {
            setError('Password must be at least 6 characters long.');
            return;
        }
        if (password !== confirmPassword) {
            setError('Passwords do not match.');
            return;
        }

        setLoading(true);
        try {
            const {error} = await supabase.auth.updateUser({
                password: password
            });

            if (error) {
                console.error('Password reset error:', error);
                setError(error.message || 'Failed to reset password. Please try again.');
                return;
            }

            setSuccess('Password successfully changed! You can now log in to the mobile app with your new password.');

        } catch (err: any) {
            console.error('Unexpected password reset error:', err);
            setError(err.message || 'An unexpected error occurred. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    if (!isValidSession && !error) {
        return (
            <div className="flex items-center justify-center h-screen">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
            </div>
        );
    }

    return (
        <div style={{
            maxWidth: '400px',
            margin: '50px auto',
            padding: '20px',
            border: '1px solid #ccc',
            borderRadius: '8px',
            fontFamily: 'Arial, sans-serif'
        }}>
            <h2 style={{textAlign: 'center', marginBottom: '20px'}}>Set New Password</h2>

            {!isValidSession ? (
                <div style={{textAlign: 'center'}}>
                    <p style={{color: 'red', marginBottom: '20px'}}>{error}</p>
                    <p style={{color: '#666', fontSize: '14px'}}>
                        Please return to the app and request a new password reset.
                    </p>
                </div>
            ) : (
                <form onSubmit={handleResetPassword}>
                    <div style={{marginBottom: '15px'}}>
                        <label>New Password</label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                            style={{width: '100%', padding: '8px', marginTop: '5px'}}
                        />
                    </div>
                    <div style={{marginBottom: '15px'}}>
                        <label>Confirm New Password</label>
                        <input
                            type="password"
                            value={confirmPassword}
                            onChange={(e) => setConfirmPassword(e.target.value)}
                            required
                            style={{width: '100%', padding: '8px', marginTop: '5px'}}
                        />
                    </div>

                    {error && <p style={{color: 'red'}}>{error}</p>}

                    {success && (
                        <div style={{marginBottom: '20px'}}>
                            <p style={{color: 'green', marginBottom: '15px'}}>{success}</p>
                            <button
                                type="button"
                                onClick={tryOpenApp}
                                style={{
                                    width: '100%',
                                    padding: '10px',
                                    background: '#28a745',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '4px',
                                    marginBottom: '10px'
                                }}
                            >
                                Open Mobile App
                            </button>
                            <p style={{fontSize: '12px', color: '#666', textAlign: 'center'}}>
                                If the app doesn't open automatically, please open it manually from your device.
                            </p>
                        </div>
                    )}

                    {!success && (
                        <button
                            type="submit"
                            disabled={loading}
                            style={{
                                width: '100%',
                                padding: '10px',
                                background: loading ? '#ccc' : '#007bff',
                                color: 'white',
                                border: 'none',
                                borderRadius: '4px',
                                cursor: loading ? 'not-allowed' : 'pointer'
                            }}
                        >
                            {loading ? 'Saving...' : 'Save New Password'}
                        </button>
                    )}
                </form>
            )}
        </div>
    );
}