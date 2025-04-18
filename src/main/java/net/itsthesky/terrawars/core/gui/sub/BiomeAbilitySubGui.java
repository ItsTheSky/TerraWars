package net.itsthesky.terrawars.core.gui.sub;

import net.itsthesky.terrawars.api.gui.AbstractGUI;
import net.itsthesky.terrawars.api.gui.GUI;
import net.itsthesky.terrawars.api.model.ability.AbilityType;
import net.itsthesky.terrawars.api.model.biome.IBiome;
import net.itsthesky.terrawars.util.BukkitUtils;
import net.itsthesky.terrawars.util.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class BiomeAbilitySubGui extends AbstractGUI {

    private final IBiome biome;
    public BiomeAbilitySubGui(@Nullable AbstractGUI parent, @NotNull IBiome biome) {
        super(parent, BukkitUtils.chat().format(
                "<accent><b>→</b> <base>" + biome.getName() + " Abilities",
                biome.getScheme()
        ), 3);

        this.biome = biome;

        setItems(ItemBuilder::fill, e -> e.setCancelled(true), getBorders());
        createBackButton(18);

        populateAbilities();
    }

    public void populateAbilities() {
        int slot = 9;
        for (final var ability : biome.getAvailableAbilities()) {
            slot ++;

            final var lore = new ArrayList<String>();
            lore.add("");

            // Add ability type
            if (ability.getType().equals(AbilityType.PASSIVE))
                lore.add("<shade-violet:700><b>▷</b> <shade-violet:900>PASSIVE ABILITY");
            else
                lore.add("<shade-indigo:700><b>▷</b> <shade-indigo:900>ACTIVE ABILITY");

            lore.add("");

            // Add description
            for (final var line : ability.getDescription())
                lore.add("<text><i>" + line);

            lore.add("");

            // Add cooldown for active abilities
            if (ability.getType().equals(AbilityType.ACTIVE)) {
                lore.add("<accent><b>⌚</b> <base>Cooldown: " + ability.getCooldownString());
            }

            final var item = new ItemBuilder(ability.getIcon())
                    .name("<accent><b>»</b> <base>" + ability.getDisplayName(), biome.getScheme())
                    .lore(biome.getScheme(), lore);

            setItem(slot, item::getItem, e -> e.setCancelled(true));
        }
    }

    @Override
    public @NotNull GUI createCopy(@NotNull Player player) {
        return new BiomeAbilitySubGui(getParent(), biome);
    }
}
