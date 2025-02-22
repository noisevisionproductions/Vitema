import { AxiosError } from 'axios';

export class ApiError extends Error {
    constructor(
        message: string,
        public status?: number,
        public code?: string
    ) {
        super(message);
        this.name = 'ApiError';
    }
}

export const handleAxiosError = (error: unknown): never => {
    if (error instanceof AxiosError) {
        const message = error.response?.data?.error || 'Wystąpił błąd podczas komunikacji z serwerem';
        throw new ApiError(message, error.response?.status);
    }
    throw new ApiError('Nieznany błąd podczas komunikacji z serwerem');
};