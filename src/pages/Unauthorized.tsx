import React from "react";
import {useNavigate} from "react-router-dom";

const Unauthorized = () => {
    const navigate = useNavigate();

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50">
            <div className="max-w-md w-full text-center space-y-8 p-8 bg-white rounded-lg shadow">
                <h1 className="text-4xl font-bold text-red-600">403</h1>
                <h2 className="text-2xl font-semibold text-gray-900">Brak dostępu</h2>
                <p className="text-gray-600">
                    Nie posiadasz uprawnień do wyświetlania tej strony.
                </p>
                <button
                    onClick={() => navigate('/login')}
                    className="mt-4 px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
                >
                    Wróć do logowania
                </button>
            </div>

        </div>
    );
};

export default Unauthorized;