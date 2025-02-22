import React from "react";
import {useForm} from 'react-hook-form';

interface ChangelogFormData {
    title: string;
    description: string;
    type: 'feature' | 'fix' | 'improvement';
}

interface ChangelogFormProps {
    onSubmit: (data: ChangelogFormData) => Promise<void>;
    isSubmitting: boolean;
}

const ChangelogForm: React.FC<ChangelogFormProps> = ({onSubmit, isSubmitting}) => {
    const {register, handleSubmit, reset, formState: {errors}} = useForm<ChangelogFormData>();

    const onFormSubmit = async (data: ChangelogFormData) => {
        await onSubmit(data);
        reset();
    };

    return (
        <form onSubmit={handleSubmit(onFormSubmit)} className="space-y-4">
            <div>
                <label className="block text-sm font-medium text-gray-700">
                    Tytuł zmiany
                </label>
                <input
                    type="text"
                    {...register('title', {required: 'Tytuł jest wymagany'})}
                    className="mt-1 px-2 py-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                />
                {errors.title && (
                    <p className="mt-1 text-sm text-red-600">
                        {errors.title.message}
                    </p>
                )}
            </div>

            <div>
                <label className="block text-sm font-medium text-gray-700">
                    Typ zmiany
                </label>
                <select
                    {...register('type', {required: 'Typ jest wymagany'})}
                    className="mt-1 px-2 py-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                >
                    <option value="feature">
                        Nowa funkcja
                    </option>
                    <option value="fix">
                        Poprawka
                    </option>
                    <option value="improvement">
                        Ulepszenie
                    </option>
                </select>
            </div>

            <div>
                <label className="block text-sm font-medium text-gray-700">
                    Opis zmiany
                </label>
                <textarea
                    {...register('description', {required: 'Opis jest wymagany'})}
                    rows={6}
                    className="mt-1 px-2 py-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                />
                {errors.description && (
                    <p className="mt-1 text-sm text-red-600">
                        {errors.description.message}
                    </p>
                )}
            </div>

            <button
                type="submit"
                disabled={isSubmitting}
                className="w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
            >
                {isSubmitting ? 'Dodawanie...' : 'Dodaj wpis'}
            </button>
        </form>
    );
};

export default ChangelogForm;