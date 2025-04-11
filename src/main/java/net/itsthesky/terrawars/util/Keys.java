package net.itsthesky.terrawars.util;

import com.github.stefvanschie.inventoryframework.util.UUIDTagType;
import org.bukkit.NamespacedKey;

/**
 * Constants for the {@link NamespacedKey keys} used across TerraWars.
 */
public final class Keys {

    public static final String NAMESPACE = "terrawars";

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
     * Will holds a {@link org.bukkit.persistence.PersistentDataType#STRING string} value, representing the ability's ID.
     */
    public static final NamespacedKey ABILITY_KEY = new NamespacedKey(NAMESPACE, "ability_item");

    /**
     * Key used to mark an end crystal as a nexus of a team.
     * <br>
     * Will holds a {@link UUIDTagType uuid} value, representing the team's UUID.
     */
    public static final NamespacedKey NEXUS_TEAM_KEY = new NamespacedKey(NAMESPACE, "nexus_team");

    /**
     * Key used to define a block as a placed block by a player.
     * This mean this block may be destroyed by others, and removed once the game ends.
     * <br>
     * Will holds a {@link UUIDTagType UUID} value, representing the player's UUID.
     */
    public static final NamespacedKey GAME_PLACED_BLOCK_KEY = new NamespacedKey(NAMESPACE, "game_placed_block");

    /**
     * Key used to identify an item as a bought item from the shop.
     * <br>
     * Will holds a {@link org.bukkit.persistence.PersistentDataType#STRING string} value, representing the item's ID.
     */
    public static final NamespacedKey SHOP_ITEM_KEY = new NamespacedKey(NAMESPACE, "shop_item");

    /**
     * Key used to know if a specific item is coming from a {@link net.itsthesky.terrawars.core.impl.game.GameGenerator generator}.
     * <br>
     * Will holds a {@link UUIDTagType UUID} value, representing the generator's ID.
     * <br>
     * <b>WARNING: This key is applied on an {@link org.bukkit.entity.Item item entity}, not an {@link org.bukkit.inventory.ItemStack item stack}!</b>
     */
    public static final NamespacedKey GENERATOR_ITEM_KEY = new NamespacedKey(NAMESPACE, "generator_item");

    /**
     * Key used to mark an item as "destroyed-on-drop", meaning it will be destroyed
     * when dropped on the ground.
     * <br>
     * Will holds a {@link org.bukkit.persistence.PersistentDataType#BOOLEAN boolean} value.
     */
    public static final NamespacedKey DESTROY_ON_DROP_KEY = new NamespacedKey(NAMESPACE, "destroy_on_drop");

    /**
     * Key used to mark an item as a "weapon", so it may be used to
     * call abilities.
     * <br>
     * Will holds a {@link org.bukkit.persistence.PersistentDataType#BOOLEAN boolean} value.
     */
    public static final NamespacedKey WEAPON_KEY = new NamespacedKey(NAMESPACE, "weapon_item");

    public static final NamespacedKey KILLER_KEY = new NamespacedKey(NAMESPACE, "killer_item");

    /**
     * Key used to put custom interaction events on items.
     * <br>
     * Will holds a {@link org.bukkit.persistence.PersistentDataType#LIST list}: list of strings
     */
    public static final NamespacedKey INTERACTION_KEY = new NamespacedKey(NAMESPACE, "interaction_item");

    //endregion

    //region Player-specific keys

    /**
     * Key used to indicate at what level a player's armor is in the game.
     * <br>
     * Will holds a {@link org.bukkit.persistence.PersistentDataType#INTEGER integer}, representing the index of the {@link net.itsthesky.terrawars.api.model.shop.ArmorLevel armor level}.
     */
    public static final NamespacedKey ARMOR_LEVEL_KEY = new NamespacedKey(NAMESPACE, "armor_level");

    //endregion
}
