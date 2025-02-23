import React, {useEffect, useState} from "react";
import {CategorizedShoppingListItem, Diet, ShoppingListV3} from "../../../../../types";
import {Calendar, Info} from "lucide-react";
import {formatTimestamp} from "../../../../../utils/dateFormatters";
import {useConfirmation} from "../../../../../hooks/useConfirmation";
import ConfirmationDialog from "../../../../common/ConfirmationDialog";
import {getCategoryLabel} from "../../../../../utils/productUtils";
import CategoryItemEditor from "../../CategoryItemEditor";
import AddProductButton from "./AddProductButton";
import { useShoppingList} from "../../../../../hooks/useShoppingList";

interface DietShoppingListEditorProps {
    diet: Diet;
    shoppingList: ShoppingListV3 | null;
    onUpdate: (updatedDiet: Diet) => Promise<void>;
}

const DietShoppingListEditor: React.FC<DietShoppingListEditorProps> = ({
                                                                           shoppingList: initialShoppingList
                                                                       }) => {
    const [shoppingList, setShoppingList] = useState(initialShoppingList);
    const { updateItems, removeItem, addItem } = useShoppingList(shoppingList?.dietId || '');

    const {
        isConfirmationOpen,
        confirmationData,
        openConfirmation,
        closeConfirmation
    } = useConfirmation<{ categoryId: string; itemIndex: number }>();

    if (!shoppingList) {
        return (
            <div className="text-center py-8 text-gray-500">
                Brak listy zakupów dla tej diety
            </div>
        );
    }

    useEffect(() => {
        setShoppingList(initialShoppingList);
    }, [initialShoppingList]);

    const handleEditItem = async (
        categoryId: string,
        index: number,
        updatedItem: CategorizedShoppingListItem
    ) => {
        if (!shoppingList) return;

        try {
            const newItems = {...shoppingList.items};
            newItems[categoryId][index] = updatedItem;
            await updateItems(newItems);
        } catch (error) {
            console.error('Error updating item:', error);
        }
    };

    const handleDeleteItem = async (categoryId: string, index: number) => {
        try {
            await removeItem(categoryId, index);
            closeConfirmation();
        } catch (error) {
            console.error('Error deleting item:', error);
        }
    };

    const handleAddItem = async (categoryId: string, newItem: CategorizedShoppingListItem) => {
        try {
            await addItem(categoryId, newItem);
        } catch (error) {
            console.error('Error adding item:', error);
        }
    };

    return (
        <div className="space-y-6">
            <div className="bg-white p-4 rounded-lg">
                <div className="flex items-center justify-between mb-6">
                    <h3 className="text-lg font-medium">Lista zakupów</h3>
                    <div className="flex items-center gap-2 text-sm text-gray-500">
                        <Calendar className="h-4 w-4"/>
                        {formatTimestamp(shoppingList.startDate)} - {formatTimestamp(shoppingList.endDate)}
                    </div>
                </div>

                <div className="mb-4 p-3 bg-blue-50 rounded-lg flex items-start gap-2">
                    <Info className="h-5 w-5 text-blue-500 flex-shrink-0 mt-0.5"/>
                    <p className="text-sm text-blue-700">
                        Okres listy zakupów jest automatycznie synchronizowany z datami diety.
                        Zmiana daty rozpoczęcia diety zaktualizuje również daty listy zakupów.
                    </p>
                </div>

                <div className="space-y-6">
                    {Object.entries(shoppingList.items).map(([categoryId, items]) => (
                        <div key={categoryId} className="space-y-2">
                            <h4 className="font-medium text-gray-700">
                                {getCategoryLabel(categoryId)}
                            </h4>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-2">
                                {items.map((item, index) => (
                                    <CategoryItemEditor
                                        key={index}
                                        item={item}
                                        onEdit={(updatedItem) => handleEditItem(categoryId, index, updatedItem)}
                                        onDelete={async () => openConfirmation({categoryId, itemIndex: index})}
                                    />
                                ))}
                            </div>
                            <AddProductButton
                                categoryId={categoryId}
                                onAdd={(newItem) => handleAddItem(categoryId, newItem)}
                            />
                        </div>
                    ))}
                </div>
            </div>

            <ConfirmationDialog
                isOpen={isConfirmationOpen}
                onClose={closeConfirmation}
                onConfirm={() => confirmationData &&
                    handleDeleteItem(confirmationData.categoryId, confirmationData.itemIndex)}
                title="Usuń produkt"
                description="Czy na pewno chcesz usunąć ten produkt z listy zakupów?"
                confirmLabel="Usuń"
                cancelLabel="Anuluj"
                variant="destructive"
            />
        </div>
    );
};

export default DietShoppingListEditor;