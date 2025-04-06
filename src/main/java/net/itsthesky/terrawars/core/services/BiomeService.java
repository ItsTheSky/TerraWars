package net.itsthesky.terrawars.core.services;

import net.itsthesky.terrawars.api.model.biome.IBiome;
import net.itsthesky.terrawars.api.services.IBiomeService;
import net.itsthesky.terrawars.api.services.base.Service;
import net.itsthesky.terrawars.core.impl.biome.BadlandsBiome;
import net.itsthesky.terrawars.core.impl.biome.EndBiome;
import net.itsthesky.terrawars.core.impl.biome.MushroomBiome;
import net.itsthesky.terrawars.core.impl.biome.TundraBiome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Service
public class BiomeService implements IBiomeService {

    private final List<IBiome> biomes = List.of(
            new TundraBiome(),
            new MushroomBiome(),
            new EndBiome(),
            new BadlandsBiome()
    );

    @Override
    public @NotNull List<IBiome> getAvailableBiomes() {
        return biomes;
    }

    @Override
    public @Nullable IBiome getBiome(@NotNull String id) {
        for (IBiome biome : biomes)
            if (biome.getId().equalsIgnoreCase(id))
                return biome;
        return null;
    }

}
