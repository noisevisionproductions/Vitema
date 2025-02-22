import {onAuthStateChanged, signInWithEmailAndPassword, signOut, User as FirebaseUser} from 'firebase/auth';
import {doc, getDoc} from 'firebase/firestore';
import React, {createContext, useContext, useEffect, useState} from "react";
import {auth, db} from '../config/firebase';
import {User} from '../types/user';
import api from "../config/axios";
import axios from 'axios';

interface AuthContextType {
    currentUser: FirebaseUser | null;
    userData: User | null;
    loading: boolean;
    login: (email: string, password: string) => Promise<User>;
    logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({children}) => {
    const [currentUser, setCurrentUser] = useState<FirebaseUser | null>(null);
    const [userData, setUserData] = useState<User | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        return onAuthStateChanged(auth, async (user) => {
            setCurrentUser(user);
            if (user) {
                const userDoc = await getDoc(doc(db, 'users', user.uid));
                if (userDoc.exists()) {
                    setUserData(userDoc.data() as User);
                }
            } else {
                setUserData(null);
            }
            setLoading(false);
        });
    }, []);

    const login = async (email: string, password: string) => {
        try {
            const credential = await signInWithEmailAndPassword(auth, email, password);
            const token = await credential.user.getIdToken();

            const response = await api.post('/auth/login',
                {email, password},
                {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                }
            );

            const userData = response.data;
            setUserData(userData);
            return userData;
        } catch (error) {
            console.error('Authentication failed:', error);
            await logout();

            if (axios.isAxiosError(error)) {
                throw new Error(error.response?.data?.message || 'Błąd uwierzytelniania');
            }

            throw error;
        }
    };

    const logout = async () => {
        try {
            await signOut(auth);
            setCurrentUser(null);
            setUserData(null);
        } catch (error) {
            console.error('Logout error:', error);
            throw error;
        }
    };

    const value = {
        currentUser,
        userData,
        loading,
        login,
        logout
    };

    return (
        <AuthContext.Provider value={value}>
            {!loading && children}
        </AuthContext.Provider>
    );
};