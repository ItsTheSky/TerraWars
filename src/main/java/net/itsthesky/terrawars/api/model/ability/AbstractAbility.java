package net.itsthesky.terrawars.api.model.ability;

import lombok.Getter;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.core.impl.game.GamePlayer;
import net.itsthesky.terrawars.util.BukkitUtils;
import net.itsthesky.terrawars.util.Colors;
import net.itsthesky.terrawars.util.ItemBuilder;
import net.itsthesky.terrawars.util.Keys;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
public abstract class AbstractAbility implements IAbility {
    private final String id;
    private final String displayName;
    private final Material icon;
    private final List<String> description;
    private final int cooldownSeconds;
    private final AbilityType type;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    protected AbstractAbility(String id, Material icon, String displayName, List<String> description,
                              int cooldownSeconds, AbilityType type) {
        this.id = id;
        this.icon = icon;
        this.displayName = displayName;
        this.description = description;
        this.cooldownSeconds = cooldownSeconds;
        this.type = type;
    }

    @Override
    public boolean isOnCooldown(IGamePlayer player) {
        UUID playerId = player.getPlayer().getUniqueId();
        if (!cooldowns.containsKey(playerId))
            return false;
        long cooldownEnd = cooldowns.get(playerId);
        return System.currentTimeMillis() < cooldownEnd;
    }

    @Override
    public int getRemainingCooldown(IGamePlayer player) {
        UUID playerId = player.getPlayer().getUniqueId();
        if (!cooldowns.containsKey(playerId))
            return 0;

        long cooldownEnd = cooldowns.get(playerId);
        long remainingMillis = cooldownEnd - System.currentTimeMillis();
        return (int) Math.max(0, remainingMillis / 1000);
    }

    @Override
    public void startCooldown(IGamePlayer player) {
        UUID playerId = player.getPlayer().getUniqueId();
        long cooldownEnd = System.currentTimeMillis()
                + (cooldownSeconds * 1000L);
        cooldowns.put(playerId, cooldownEnd);
        BukkitUtils.runTaskTimer(task -> {
            if (!player.isOnline()) {
                cooldowns.remove(playerId);
                return;
            }

            if (System.currentTimeMillis() >= cooldownEnd)
            {
                cooldowns.remove(playerId);
                task.cancel();
            }

            player.refreshHotbar();
        }, 0, 20L);
    }

    @Override
    public @NotNull ItemStack buildShopItem(Player player) {
        final var lore = new ArrayList<String>();
        // TODO: make a proper description

        return new ItemBuilder(getIcon())
                .cleanLore()
                .lore(Colors.INDIGO, lore)
                .name(getDisplayName())
                .getItem();
    }

    @Override
    public @NotNull ItemStack buildHotBarItem(IGamePlayer player) {
        final var lore = new ArrayList<String>();
        final Material material;

        if (isOnCooldown(player)) {
            material = Material.BARRIER;
            int remaining = getRemainingCooldown(player);
            lore.add("[red]<accent>⌚ <text>Cooldown: <base>" + remaining + "s");
        } else {
            material = getIcon();
            if (getType() == AbilityType.PASSIVE) {
                lore.add("[green]<accent>✔ <text>Ready to use!");
            } else {
                lore.add("[green]<accent>✔ <base>Ready to use!<text> Right-click or drop your weapon.");
            }
        }

        final var builder = new ItemBuilder(material)
                .cleanLore()
                .noMovement()
                .setCustomData(Keys.ABILITY_KEY, PersistentDataType.STRING, getId())
                .lore(Colors.INDIGO, lore)
                .name(getDisplayName());

        return builder.getItem();
    }
}