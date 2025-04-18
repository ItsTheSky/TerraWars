package net.itsthesky.terrawars.core.impl.biome;

import net.itsthesky.terrawars.api.model.biome.AbstractBiome;
import net.itsthesky.terrawars.core.impl.ability.mushroom.FungalGrowthAbility;
import net.itsthesky.terrawars.core.impl.ability.mushroom.SporeShieldAbility;
import net.itsthesky.terrawars.core.impl.ability.mushroom.SymbioticRegenerationAbility;
import net.itsthesky.terrawars.util.Colors;
import net.minecraft.world.entity.npc.VillagerType;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Villager;

import java.util.List;

public class MushroomBiome extends AbstractBiome {

    public MushroomBiome() {
        super("mushroom", "Mushroom", Colors.ROSE,
                List.of(new SporeShieldAbility(), new SymbioticRegenerationAbility(), new FungalGrowthAbility()),
                Colors.ROSE.get(Colors.SHADE_600), List.of(
                        "The <base>Mushroom<text> biome is a unique biome,",
                        "characterized by its large mushrooms and",
                        "vibrant colors."
                ), Material.RED_MUSHROOM_BLOCK, Material.RED_WOOL,
                Villager.Type.SWAMP);
    }

}
