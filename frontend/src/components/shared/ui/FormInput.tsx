import React from 'react';
import {Input} from './Input';
import {cn} from '../../../utils/cs';
import {ValidationStatus} from "../../../types/scandallShuffle/scenario-creation";

interface FormInputProps extends React.InputHTMLAttributes<HTMLInputElement> {
    error?: string;
    label?: string;
    validationStatus?: ValidationStatus;
    validationMessage?: string | null;
    validationIcon?: React.ReactNode;
}

/**
 * Form input wrapper with error handling
 */
const FormInput: React.FC<FormInputProps> = ({
                                                 error,
                                                 label,
                                                 className,
                                                 id,
                                                 validationStatus,
                                                 validationMessage,
                                                 validationIcon,
                                                 ...props
                                             }) => {

    const getBorderColor = () => {
        // Final form submission error has the highest priority
        if (error) {
            return 'border-red-500 focus:border-red-500 focus:ring-red-500';
        }
        // Real-time validation status colors
        switch (validationStatus) {
            case 'available':
                return 'border-green-500 focus:border-green-500 focus:ring-green-500';
            case 'unavailable':
            case 'error':
            case 'too-short':
                return 'border-red-500 focus:border-red-500 focus:ring-red-500';
            default:
                return 'border-gray-300 focus:border-green-500 focus:ring-green-500';
        }
    };

    const getMessageColor = () => {
        switch (validationStatus) {
            case 'available':
                return 'text-green-600';
            case 'unavailable':
            case 'error':
            case 'too-short':
                return 'text-red-600';
            default:
                return 'text-gray-500';
        }
    };

    return (
        <div>
            {label && (
                <label htmlFor={id} className="block text-sm font-medium text-gray-700 mb-1">
                    {label}
                </label>
            )}
            <div className="relative">
                <Input
                    id={id}
                    className={cn(
                        getBorderColor(),
                        validationIcon ? 'pr-10' : '',
                        className
                    )}
                    {...props}
                />
                {validationIcon && (
                    <div className="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
                        {validationIcon}
                    </div>
                )}
            </div>
            {validationMessage && (
                <p className={`mt-1 text-sm ${getMessageColor()}`}>{validationMessage}</p>
            )}
            {!validationMessage && error && (
                <p className="mt-1 text-sm text-red-600">{error}</p>
            )}
        </div>
    );
};

export default FormInput;