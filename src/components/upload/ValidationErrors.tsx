import React from 'react';
import { AlertCircle, AlertTriangle, X } from 'lucide-react';
import {Dialog} from "@headlessui/react";

interface ValidationError {
    row: number;
    column: string;
    message: string;
    type?: 'warning' | 'error';
}

interface ValidationErrorsProps {
    errors: ValidationError[];
    warnings: ValidationError[];
    onClose: () => void;
    onProceed?: () => void;
}

const ValidationErrors: React.FC<ValidationErrorsProps> = ({
                                                               errors,
                                                               warnings,
                                                               onClose
                                                           }) => {
    return (
        <Dialog
            open={true}
            onClose={onClose}
            className="relative z-50"
        >
            <div className="fixed inset-0 bg-black/30" aria-hidden="true" />

            <div className="fixed inset-0 flex items-center justify-center p-4">
                <Dialog.Panel
                    className="bg-white p-6 rounded-lg shadow-sm space-y-4"
                    aria-describedby="validation-description"
                >
                    <div className="flex justify-between items-center">
                        <Dialog.Title className="text-lg font-medium">
                            Wyniki walidacji pliku
                        </Dialog.Title>
                        <button
                            onClick={onClose}
                            className="text-gray-400 hover:text-gray-600"
                            aria-label="Zamknij okno walidacji"
                        >
                            <X className="h-5 w-5" />
                        </button>
                    </div>

                    <div id="validation-description" className="sr-only">
                        Lista błędów i ostrzeżeń znalezionych podczas walidacji pliku
                    </div>

                    {errors.length > 0 && (
                        <div className="space-y-2" role="alert">
                            <div className="flex items-center gap-2 text-red-600">
                                <AlertCircle className="h-5 w-5" aria-hidden="true" />
                                <span className="font-medium">Błędy:</span>
                            </div>
                            <ul className="ml-7 text-red-600 list-disc">
                                {errors.map((error, index) => (
                                    <li key={index}>
                                        {error.row > 0 ? `Wiersz ${error.row}: ` : ''}{error.message}
                                    </li>
                                ))}
                            </ul>
                        </div>
                    )}

                    {warnings.length > 0 && (
                        <div className="space-y-2" role="alert">
                            <div className="flex items-center gap-2 text-yellow-600">
                                <AlertTriangle className="h-5 w-5" aria-hidden="true" />
                                <span className="font-medium">Ostrzeżenia:</span>
                            </div>
                            <ul className="ml-7 text-yellow-600 list-disc">
                                {warnings.map((warning, index) => (
                                    <li key={index}>
                                        {warning.row > 0 ? `Wiersz ${warning.row}: ` : ''}{warning.message}
                                    </li>
                                ))}
                            </ul>
                        </div>
                    )}
                </Dialog.Panel>
            </div>
        </Dialog>
    );
};

export default ValidationErrors;