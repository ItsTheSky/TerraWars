package net.itsthesky.terrawars.api.model.biome;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.itsthesky.terrawars.api.model.ability.IAbility;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.entity.Villager;

import java.util.List;

@Getter
@AllArgsConstructor
public class AbstractBiome implements IBiome {

    private final String id;
    private final String name;
    private final List<TextColor> scheme;
    private final List<IAbility> availableAbilities;
    private final TextColor color;
    private final List<String> description;
    private final Material mainBlock;
    private final Material woolBlock;
    private final Villager.Type villagerType;

}
