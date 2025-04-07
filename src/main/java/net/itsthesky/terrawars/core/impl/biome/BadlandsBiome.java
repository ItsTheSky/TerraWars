package net.itsthesky.terrawars.core.impl.biome;

import net.itsthesky.terrawars.api.model.biome.AbstractBiome;
import net.itsthesky.terrawars.core.impl.ability.mushroom.GroundStrikeAbility;
import net.itsthesky.terrawars.util.Colors;
import org.bukkit.DyeColor;
import org.bukkit.Material;

import java.util.List;

public class BadlandsBiome extends AbstractBiome {

    public BadlandsBiome() {
        super("badlands", "Badlands", Colors.ORANGE,
                List.of(new GroundStrikeAbility()),
                Colors.ORANGE.get(Colors.SHADE_600), List.of(
                        "The <base>Badlands<text> biome is a dry biome,",
                        "characterized by its arid climate and",
                        "rocky terrain."
                ), Material.RED_SANDSTONE, Material.BROWN_WOOL);
    }

}
