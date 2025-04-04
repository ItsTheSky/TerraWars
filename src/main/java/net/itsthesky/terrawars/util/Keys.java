package net.itsthesky.terrawars.util;

import org.bukkit.NamespacedKey;

/**
 * Constants for the {@link org.bukkit.NamespacedKey keys} used across TerraWars.
 */
public final class Keys {

    private static final String NAMESPACE = "terrawars";

    //region Item-specific keys

    /**
     * Key used to prevent item movement in inventories.
     * <br>
     * Will holds a {@link org.bukkit.persistence.PersistentDataType#BOOLEAN boolean} value.
     */
    public static final NamespacedKey NO_MOVE_KEY = new NamespacedKey(NAMESPACE, "no_item_movement");

    /**
     * Key used to mark an item as an ability item.
     * <br>
     * Will holds a {@link org.bukkit.persistence.PersistentDataType#STRING} value, representing the ability's ID.
     */
    public static final NamespacedKey ABILITY_KEY = new NamespacedKey(NAMESPACE, "ability_item");

    //endregion
}
