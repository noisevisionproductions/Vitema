import React, {useRef} from 'react';
import {UploadCloud, X} from 'lucide-react';

interface ImageUploaderProps {
    label: string;
    value?: string;
    onImageSelect: (uri?: string) => void;
}

const ImageUploader: React.FC<ImageUploaderProps> = ({label, value, onImageSelect}) => {
    const fileInputRef = useRef<HTMLInputElement>(null);

    const handleFileSelect = (event: React.ChangeEvent<HTMLInputElement>) => {
        const file = event.target.files?.[0];
        if (file) {
            const localUri = URL.createObjectURL(file);
            onImageSelect(localUri);
        }
    };

    const removeImage = (e: React.MouseEvent) => {
        e.stopPropagation();
        onImageSelect(undefined);
    };

    return (
        <div>
            {label && <label className="block text-sm font-medium text-gray-700 mb-2">{label}</label>}

            {!value ? (
                <div
                    onClick={() => fileInputRef.current?.click()}
                    className="
                        mt-1 flex justify-center items-center
                        w-full h-48 /* Maintain a consistent height */
                        rounded-md border-2 border-dashed border-gray-300
                        cursor-pointer group
                        transition-colors hover:border-green-500 hover:bg-green-50
                    "
                >
                    <div className="space-y-1 text-center">
                        <UploadCloud className="mx-auto h-12 w-12 text-gray-400"/>
                        <div className="flex text-sm text-gray-600">
                            <p className="font-medium text-green-600 group-hover:text-green-700">
                                <span>Upload a file</span>
                            </p>
                        </div>
                        <p className="text-xs text-gray-500">PNG, JPG up to 10MB</p>
                    </div>
                </div>
            ) : (
                <div className="mt-1 flex justify-center">
                    <div
                        onClick={() => fileInputRef.current?.click()}
                        className="
                            relative w-64 h-64 /* Fixed square size for the preview */
                            rounded-md group cursor-pointer
                        "
                    >
                        <img
                            src={value}
                            alt="Scenario preview"
                            className="w-full h-full object-cover rounded-md shadow-md" // object-cover prevents stretching
                        />
                        <div
                            className="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-60 transition-all flex items-center justify-center rounded-md">
                            <button
                                onClick={removeImage}
                                className="p-2 bg-red-600 text-white rounded-full opacity-0 group-hover:opacity-100 transition-opacity"
                                title="Remove image"
                            >
                                <X className="h-5 w-5"/>
                            </button>
                        </div>
                    </div>
                </div>
            )}

            <input
                ref={fileInputRef}
                id="file-upload"
                name="file-upload"
                type="file"
                accept="image/png, image/jpeg"
                className="hidden"
                onChange={handleFileSelect}
            />
        </div>
    );
};

export default ImageUploader;

