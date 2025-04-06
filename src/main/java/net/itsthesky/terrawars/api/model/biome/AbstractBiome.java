package net.itsthesky.terrawars.api.model.biome;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.itsthesky.terrawars.api.model.ability.IAbility;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;

import java.util.List;

@Getter
@AllArgsConstructor
public class AbstractBiome implements IBiome {

    private final String id;
    private final String name;
    private final List<TextColor> scheme;
    private final List<IAbility> availableAbilities;
    private final DyeColor color;
    private final List<String> description;
    private final Material mainBlock;

}
