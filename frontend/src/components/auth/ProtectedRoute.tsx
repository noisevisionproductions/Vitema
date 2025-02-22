import {useAuth} from "../../contexts/AuthContext";
import React from "react";
import {Navigate} from "react-router-dom";
import {UserRole} from "../../types/user";

const ProtectedRoute = ({children}: { children: React.ReactNode }) => {
    const {currentUser, userData, loading} = useAuth();

    if (loading) {
        return <div className="flex items-center justify-center h-screen">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900" />
        </div>
    }

    if (!currentUser || !userData) {
        return <Navigate to="/login"/>;
    }

    if (userData.role !== UserRole.ADMIN) {
        return <Navigate to="/unauthorized" />;
    }

    return children;
};

export default ProtectedRoute;