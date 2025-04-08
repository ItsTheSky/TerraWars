package net.itsthesky.terrawars.core.impl.biome;

import net.itsthesky.terrawars.api.model.biome.AbstractBiome;
import net.itsthesky.terrawars.core.impl.ability.end.DragonBreathAbility;
import net.itsthesky.terrawars.core.impl.ability.end.EtherealJumpAbility;
import net.itsthesky.terrawars.core.impl.ability.end.RiftTeleportationAbility;
import net.itsthesky.terrawars.util.Colors;
import net.minecraft.world.entity.npc.VillagerType;
import org.bukkit.Material;
import org.bukkit.entity.Villager;

import java.util.List;

public class EndBiome extends AbstractBiome {

    public EndBiome() {
        super("end", "End", Colors.PURPLE,
                List.of( new EtherealJumpAbility(), new DragonBreathAbility(), new RiftTeleportationAbility()),
                Colors.PURPLE.get(Colors.SHADE_600), List.of(
                        "The <base>End<text> biome is a mysterious biome,",
                        "characterized by its floating islands and",
                        "unique flora and fauna."
                ), Material.END_STONE, Material.PURPLE_WOOL,
                Villager.Type.DESERT);
    }

}
