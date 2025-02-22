// src/api/axios.ts
import axios from 'axios';
import { auth } from './firebase';

const api = axios.create({
    baseURL: 'http://localhost:8080/api',
    headers: {
        'Content-Type': 'application/json'
    }
});

// Dodaj interceptor dla zapytań
api.interceptors.request.use(async (config) => {
    const user = auth.currentUser;
    if (user) {
        const token = await user.getIdToken();
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
}, (error) => {
    return Promise.reject(error);
});

// Dodaj interceptor dla odpowiedzi
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (axios.isAxiosError(error)) {
            console.error('Axios error:', error.response?.data);
        }
        return Promise.reject(error);
    }
);

export default api;