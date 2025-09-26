import React from "react";

interface SearchInputProps {
    id?: string;
    value: string;
    onChange: (value: any) => void;
    placeholder?: string;
}

const SearchInput: React.FC<SearchInputProps> = ({id, value, onChange, placeholder}) => {
    return (
        <input
            id={id}
            type="text"
            placeholder={placeholder}
            className="w-full p-2 border rounded-lg"
            value={value}
            onChange={(e) => onChange(e.target.value)}
        />
    );
};

export default SearchInput;