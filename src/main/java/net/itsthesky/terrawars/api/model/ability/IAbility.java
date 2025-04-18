package net.itsthesky.terrawars.api.model.ability;

import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IAbility {

    /**
     * Get the unique ID of the ability.
     * @return The unique ID of the ability.
     */
    String getId();

    /**
     * Get the (raw) name of the ability.
     * @return The name of the ability.
     */
    String getDisplayName();

    /**
     * Get the (raw) description of the ability.
     * @return The description of the ability.
     */
    List<String> getDescription();

    /**
     * Get the material displayed for the ability.
     * @return The material of the ability.
     */
    Material getIcon();

    int getCooldownSeconds();

    default String getCooldownString() {
        int minutes = getCooldownSeconds() / 60;
        int seconds = getCooldownSeconds() % 60;
        StringBuilder sb = new StringBuilder();
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0) sb.append(seconds).append("s");
        return sb.toString();
    }

    AbilityType getType();

    boolean isOnCooldown(IGamePlayer player);

    int getRemainingCooldown(IGamePlayer player);

    void startCooldown(IGamePlayer player);

    void removeCooldown(IGamePlayer player);

    @NotNull ItemStack buildHotBarItem(IGamePlayer player);

    default void onSelect(@NotNull IGamePlayer player) {};

    default void onDeselect(@NotNull IGamePlayer player) {};

}
