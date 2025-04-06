package net.itsthesky.terrawars.core.impl.biome;

import net.itsthesky.terrawars.api.model.biome.AbstractBiome;
import net.itsthesky.terrawars.util.Colors;
import org.bukkit.DyeColor;
import org.bukkit.Material;

import java.util.List;

public class MushroomBiome extends AbstractBiome {

    public MushroomBiome() {
        super("mushroom", "Mushroom", Colors.ROSE,
                List.of(),
                DyeColor.RED, List.of(
                        "The <base>Mushroom<text> biome is a unique biome,",
                        "characterized by its large mushrooms and",
                        "vibrant colors."
                ), Material.RED_MUSHROOM_BLOCK);
    }

}
