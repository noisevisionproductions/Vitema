package com.noisevisionsoftware.nutrilog.service;

import com.noisevisionsoftware.nutrilog.exception.NotFoundException;
import com.noisevisionsoftware.nutrilog.model.shopping.CategorizedShoppingListItem;
import com.noisevisionsoftware.nutrilog.model.shopping.ShoppingList;
import com.noisevisionsoftware.nutrilog.repository.ShoppingListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShoppingListService {
    private final ShoppingListRepository shoppingListRepository;

    public ShoppingList getShoppingListByDietId(String dietId) {
        return shoppingListRepository.findByDietId(dietId)
                .orElse(null);
    }

    public void saveShoppingList(ShoppingList shoppingList) {
        shoppingListRepository.save(shoppingList);
    }

    public ShoppingList updateShoppingListItems(String id, Map<String, List<CategorizedShoppingListItem>> items) {
        ShoppingList shoppingList = shoppingListRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shopping list not found: " + id));

        shoppingList.setItems(items);
        shoppingListRepository.save(shoppingList);
        return shoppingList;
    }

    public void removeItemFromCategory(String id, String categoryId, int itemIndex) {
        ShoppingList shoppingList = shoppingListRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shopping list not found: " + id));

        Map<String, List<CategorizedShoppingListItem>> items = shoppingList.getItems();
        if (items.containsKey(categoryId)) {
            List<CategorizedShoppingListItem> categoryItems = items.get(categoryId);
            if (itemIndex >= 0 && itemIndex < categoryItems.size()) {
                categoryItems.remove(itemIndex);
                if (categoryItems.isEmpty()) {
                    items.remove(categoryId);
                }
                shoppingListRepository.save(shoppingList);
            }
        }
    }

    public ShoppingList addItemToCategory(String id, String categoryId, CategorizedShoppingListItem item) {
        ShoppingList shoppingList = shoppingListRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shopping list not found: " + id));

        Map<String, List<CategorizedShoppingListItem>> items = shoppingList.getItems();
        items.computeIfAbsent(categoryId, k -> new ArrayList<>()).add(item);

        shoppingListRepository.save(shoppingList);
        return shoppingList;
    }
}