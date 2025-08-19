import React, {createContext, useContext, useState} from 'react';
import {ApplicationType, APPLICATION_CONFIGS, ApplicationConfig} from '../types/application';

interface ApplicationContextType {
    currentApplication: ApplicationType | null;
    applicationConfig: ApplicationConfig | null;
    setApplication: (app: ApplicationType) => void;
    clearApplication: () => void;
}

const ApplicationContext = createContext<ApplicationContextType | null>(null);

export const useApplication = () => {
    const context = useContext(ApplicationContext);
    if (!context) {
        throw new Error('useApplication must be used within ApplicationProvider');
    }
    return context;
};

export const ApplicationProvider: React.FC<{ children: React.ReactNode }> = ({children}) => {
    const [currentApplication, setCurrentApplication] = useState<ApplicationType | null>(() => {
        // Initialize from localStorage
        const saved = localStorage.getItem('selectedApplication');
        return saved ? (saved as ApplicationType) : null;
    });

    const setApplication = (app: ApplicationType) => {
        setCurrentApplication(app);
        localStorage.setItem('selectedApplication', app);
    };

    const clearApplication = () => {
        setCurrentApplication(null);
        localStorage.removeItem('selectedApplication');
    };

    const applicationConfig = currentApplication ? APPLICATION_CONFIGS[currentApplication] : null;

    const value = {
        currentApplication,
        applicationConfig,
        setApplication,
        clearApplication
    };

    return (
        <ApplicationContext.Provider value={value}>
            {children}
        </ApplicationContext.Provider>
    );
};