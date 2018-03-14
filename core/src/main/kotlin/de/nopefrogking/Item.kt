package de.nopefrogking

import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import de.nopefrogking.utils.DefaultSkin
import de.nopefrogking.utils.FontIcon
import de.nopefrogking.utils.NamedAsset
import ktx.i18n.BundleLine

enum class Item(val icon: FontIcon, val drawable: NamedAsset<Drawable>, val title: BundleLine, val price: Int) {
    Flask(FontIcon.Item_Flask, DefaultSkin.ui_item_flask, I18N.item_flask, 15),
    Orb(FontIcon.Item_Orb, DefaultSkin.ui_item_orb, I18N.item_orb, 70),
    Umbrella(FontIcon.Item_Umbrella, DefaultSkin.ui_item_umbrella, I18N.item_umbrella, 70),
    Storm(FontIcon.Item_Storm, DefaultSkin.ui_item_storm, I18N.item_storm, 15),
    ;
}