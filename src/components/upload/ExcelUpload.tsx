import React, {useState} from "react";
import {User} from "../../types/user";
import {toast} from "sonner";
import UserSelector from "./UserSelector";
import FileUploadZone from "./FileUploadZone";
import {ExcelParserService} from "../../services/ExcelParserService";
import {FirebaseService} from "../../services/FirebaseService";
import {Timestamp} from 'firebase/firestore';
import {ExcelValidationService} from "../../services/ExcelValidationService";
import ValidationErrors from "./ValidationErrors";

interface ValidationError {
    row: number;
    column: string;
    message: string;
    type?: 'warning' | 'error';
}

const ExcelUpload: React.FC = () => {
    const [selectedUser, setSelectedUser] = useState<User | null>(null);
    const [file, setFile] = useState<File | null>(null);
    const [isProcessing, setIsProcessing] = useState(false);
    const [validationErrors, setValidationErrors] = useState<ValidationError[]>([]);
    const [validationWarnings, setValidationWarnings] = useState<ValidationError[]>([]);

    const handleFileSelect = async (file: File | null) => {
        setFile(null);
        setValidationErrors([]);
        setValidationWarnings([]);

        if (!selectedUser) {
            toast.error('Najpierw wybierz użytkownika');
            return;
        }

        if (file) {
            const validationResult = await ExcelValidationService.validateDietExcel(file, selectedUser.id);

            if (!validationResult.isValid) {
                setValidationErrors(validationResult.errors);
                setValidationWarnings(validationResult.warnings || []);
            } else {
                setFile(file);
                if (validationResult.warnings?.length > 0) {
                    setValidationWarnings(validationResult.warnings);
                }
            }
        }
    };

    const handleUpload = async () => {
        if (!selectedUser || !file) {
            toast.error('Wybierz użytkownika i plik przed wysłaniem');
            return;
        }

        setIsProcessing(true);
        try {
            const parsedData = await ExcelParserService.parseDietExcel(file);
            const fileUrl = await FirebaseService.uploadExcelFile(file, selectedUser.id);

            const dietId = await FirebaseService.saveDiet(
                parsedData,
                selectedUser.id,
                {
                    fileName: file.name,
                    fileUrl
                }
            );

            if (parsedData.shoppingList && parsedData.shoppingList.length > 0) {
                const startDate = parsedData.days[0]?.date.replace(',', '');
                const endDate = parsedData.days[parsedData.days.length - 1]?.date?.replace(',', '');

                await FirebaseService.saveShoppingList({
                    userId: selectedUser.id,
                    dietId,
                    items: parsedData.shoppingList,
                    createdAt: Timestamp.fromDate(new Date()),
                    startDate,
                    endDate
                });
            }

            toast.success('Dieta została pomyślnie zapisana');
            setFile(null);
            setValidationErrors([]);
            setValidationWarnings([]);
        } catch (error) {
            console.error('Error saving diet:', error);
            toast.error('Wystąpił błąd podczas zapisywania diety');
        } finally {
            setIsProcessing(false);
        }
    };

    const handleErrorsClose = () => {
        setValidationErrors([]);
        setValidationWarnings([]);
    };

    return (
        <div className="space-y-8">
            {(validationErrors.length > 0 || validationWarnings.length > 0) && (
                <ValidationErrors
                    errors={validationErrors}
                    warnings={validationWarnings}
                    onClose={handleErrorsClose}
                    onProceed={validationErrors.length === 0 ? handleUpload : undefined}
                />
            )}

            <div className="bg-white p-6 rounded-lg shadow-sm">
                <h3 className="text-lg font-medium mb-4">Wybierz użytkownika</h3>
                <UserSelector
                    selectedUser={selectedUser}
                    onUserSelect={setSelectedUser}
                />
            </div>

            <div className="bg-white p-6 rounded-lg shadow-sm">
                <h3 className="text-lg font-medium mb-4">Upload pliku Excel</h3>
                <FileUploadZone
                    file={file}
                    onFileSelect={handleFileSelect}
                    disabled={!selectedUser}
                />
            </div>

            <div className="flex justify-end">
                <button
                    onClick={handleUpload}
                    disabled={!selectedUser || !file || validationErrors.length > 0 || isProcessing}
                    className={`px-4 py-2 rounded-lg ${
                        !selectedUser || !file || validationErrors.length > 0 || isProcessing
                            ? 'bg-gray-300 cursor-not-allowed'
                            : 'bg-blue-500 hover:bg-blue-600 text-white'
                    }`}
                >
                    {isProcessing ? 'Przetwarzanie...' : 'Wyślij'}
                </button>
            </div>
        </div>
    );
};

export default ExcelUpload;