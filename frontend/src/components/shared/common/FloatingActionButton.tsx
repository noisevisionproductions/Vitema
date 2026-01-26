import React, {ReactNode} from "react";

interface FloatingActionButtonProps {
    icon?: ReactNode;
    label: string;
    onClick: () => void;
    variant?: "primary" | "secondary";
    disabled?: boolean;
    isLoading?: boolean;
    loadingLabel?: string;
    loadingIcon?: ReactNode;
    className?: string;
}

export const FloatingActionButton: React.FC<FloatingActionButtonProps> = ({
                                                                              icon,
                                                                              label,
                                                                              onClick,
                                                                              variant = "secondary",
                                                                              disabled = false,
                                                                              isLoading = false,
                                                                              loadingLabel,
                                                                              loadingIcon,
                                                                              className = ""
                                                                          }) => {
    const baseClasses = "px-5 py-3 shadow-lg rounded-full disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2";
    const variantClasses = {
        primary: "bg-blue-600 text-white hover:bg-blue-700",
        secondary: "bg-white border hover:bg-gray-50"
    };

    return (
        <button
            onClick={onClick}
            disabled={disabled || isLoading}
            className={`${baseClasses} ${variantClasses[variant]} ${className}`}
        >
            {isLoading ? (
                <>
                    {loadingIcon}
                    <span>{loadingLabel || label}</span>
                </>
            ) : (
                <>
                    {icon && icon}
                    <span>{label}</span>
                </>
            )}
        </button>
    );
};

interface FloatingActionButtonGroupProps {
    children: ReactNode;
    position?: "bottom-right" | "bottom-left" | "bottom-center";
}

export const FloatingActionButtonGroup: React.FC<FloatingActionButtonGroupProps> = ({
                                                                                        children,
                                                                                        position = "bottom-right"
                                                                                    }) => {
    const positionClasses = {
        "bottom-right": "bottom-6 right-6",
        "bottom-left": "bottom-6 left-6",
        "bottom-center": "bottom-6 left-1/2 transform -translate-x-1/2"
    };

    return (
        <div className={`fixed ${positionClasses[position]} flex gap-3 z-10`}>
            {children}
        </div>
    );
};