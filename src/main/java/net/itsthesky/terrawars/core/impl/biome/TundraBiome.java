package net.itsthesky.terrawars.core.impl.biome;

import net.itsthesky.terrawars.api.model.biome.AbstractBiome;
import net.itsthesky.terrawars.core.impl.ability.tundra.IglooAbility;
import net.itsthesky.terrawars.util.Colors;
import org.bukkit.DyeColor;
import org.bukkit.Material;

import java.util.List;

public class TundraBiome extends AbstractBiome {

    public TundraBiome() {
        super("tundra", "Tundra", Colors.SKY,
                List.of(new IglooAbility()),
                DyeColor.CYAN, List.of(
                        "The <base>Tundra<text> is a cold biome,",
                        "characterized by its low temperatures",
                        "and limited vegetation."
                ), Material.ICE);
    }

}
