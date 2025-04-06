package net.itsthesky.terrawars.util;

import com.github.stefvanschie.inventoryframework.util.UUIDTagType;
import org.bukkit.NamespacedKey;

/**
 * Constants for the {@link org.bukkit.NamespacedKey keys} used across TerraWars.
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

    //endregion
}
