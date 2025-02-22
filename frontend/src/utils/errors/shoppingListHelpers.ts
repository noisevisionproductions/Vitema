import { ShoppingListItemV2} from "../../types/diet";

export interface GroupedShoppingList {
    [category: string]: string[];
}

export const groupShoppingListByCategory = (items: ShoppingListItemV2[]): GroupedShoppingList => {
    return items.reduce((groups, item) => {
        const category = item.category || 'Inne';
        if (!groups[category]) {
            groups[category] = [];
        }
        groups[category].push(item.name);
        return groups;
    }, {} as GroupedShoppingList);
};