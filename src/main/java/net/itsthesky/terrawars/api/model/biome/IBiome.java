package net.itsthesky.terrawars.api.model.biome;

import net.itsthesky.terrawars.api.model.ability.IAbility;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IBiome {

    @NotNull String getId();

    @NotNull String getName();

    @NotNull List<TextColor> getScheme();

    @NotNull List<IAbility> getAvailableAbilities();

    @NotNull TextColor getColor();

    @NotNull List<String> getDescription();

    @NotNull Material getMainBlock();

    @NotNull Material getWoolBlock();

}
