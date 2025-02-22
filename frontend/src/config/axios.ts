import axios from 'axios';
import {auth} from './firebase';
import {toast} from "sonner";

const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
    headers: {
        'Content-Type': 'application/json'
    }
});

api.interceptors.request.use(async (config) => {
    const user = auth.currentUser;
    if (user) {
        const token = await user.getIdToken();
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            // Można tutaj dodać logikę wylogowania
            toast.error('Sesja wygasła. Zaloguj się ponownie.');
        } else if (error.response?.status === 403) {
            toast.error('Brak uprawnień do wykonania tej operacji.');
        } else {
            toast.error(error.response?.data?.message || 'Wystąpił błąd');
        }
        return Promise.reject(error);
    }
);

export default api;