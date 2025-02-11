package com.noisevisionsoftware.szytadieta.domain.model.shopping

enum class ProductCategory(
    val id: String,
    val displayName: String,
    val color: String,
    val icon: String
) {
    DAIRY("dairy", "Nabiał", "#AED6F1", "milk"),
    MEAT_FISH("meat-fish", "Ryby i Mięso", "#F5B7B1", "fish"),
    VEGETABLES("vegetables", "Warzywa", "#A9DFBF", "carrot"),
    FRUITS("fruits", "Owoce", "#F9E79F", "apple"),
    DRY_GOODS("dry-goods", "Produkty suche", "#F5CBA7", "wheat"),
    SPICES("spices", "Przyprawy", "#E8DAEF", "soup"),
    OILS("oils", "Oleje i tłuszcze", "#FAD7A0", "droplet"),
    NUTS("nuts", "Orzechy i nasiona", "#D5D8DC", "nut"),
    BEVERAGES("beverages", "Napoje", "#A3E4D7", "beer"),
    CANNED("canned", "Produkty konserwowe", "#D7BDE2", "box"),
    FROZEN("frozen", "Mrożonki", "#85C1E9", "snowflake"),
    SNACKS("snacks", "Przekąski", "#F8C471", "cookie"),
    OTHER("other", "Inne", "#CCD1D1", "package");

    companion object {
        fun fromId(id: String): ProductCategory =
            entries.find { it.id == id } ?: OTHER
    }
}