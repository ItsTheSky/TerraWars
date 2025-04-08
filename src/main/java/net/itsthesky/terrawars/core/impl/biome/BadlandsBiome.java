package net.itsthesky.terrawars.core.impl.biome;

import net.itsthesky.terrawars.api.model.biome.AbstractBiome;
import net.itsthesky.terrawars.core.impl.ability.badlands.EarthyCamouflageAbility;
import net.itsthesky.terrawars.core.impl.ability.badlands.GoldSeekerAbility;
import net.itsthesky.terrawars.core.impl.ability.badlands.GroundStrikeAbility;
import net.itsthesky.terrawars.util.Colors;
import net.minecraft.world.entity.npc.VillagerType;
import org.bukkit.Material;

import java.util.List;

public class BadlandsBiome extends AbstractBiome {

    public BadlandsBiome() {
        super("badlands", "Badlands", Colors.ORANGE,
                List.of(new GroundStrikeAbility(), new GoldSeekerAbility(), new EarthyCamouflageAbility()),
                Colors.ORANGE.get(Colors.SHADE_600), List.of(
                        "The <base>Badlands<text> biome is a dry biome,",
                        "characterized by its arid climate and",
                        "rocky terrain."
                ), Material.RED_SANDSTONE, Material.BROWN_WOOL,
                VillagerType.SAVANNA);
    }

}
