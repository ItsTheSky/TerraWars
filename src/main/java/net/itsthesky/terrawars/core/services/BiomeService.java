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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BiomeService implements IBiomeService {

    private final Map<String, IBiome> biomes = new HashMap<>();

    public BiomeService() {
        registerBiome(new BadlandsBiome());
        registerBiome(new EndBiome());
        registerBiome(new MushroomBiome());
        registerBiome(new TundraBiome());
    }

    private void registerBiome(IBiome biome) {
        biomes.put(biome.getId(), biome);
    }

    @Override
    public @NotNull List<IBiome> getAvailableBiomes() {
        return List.copyOf(biomes.values());
    }

    @Override
    public @Nullable IBiome getBiome(@NotNull String id) {
        return biomes.get(id);
    }

}
