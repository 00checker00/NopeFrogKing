package de.nopefrogking

import de.nopefrogking.utils.FontIcon
import ktx.i18n.BundleLine

enum class Item(val icon: FontIcon, val title: BundleLine) {
    Flask(FontIcon.Item_Flask, I18N.item_flask),
    Orb(FontIcon.Item_Orb, I18N.item_orb),
    Umbrella(FontIcon.Item_Umbrella, I18N.item_umbrella),
    Storm(FontIcon.Item_Storm, I18N.item_storm),
    ;
}