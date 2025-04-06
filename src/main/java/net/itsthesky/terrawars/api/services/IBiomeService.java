package net.itsthesky.terrawars.api.services;

import net.itsthesky.terrawars.api.model.biome.IBiome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IBiomeService {

    @NotNull List<IBiome> getAvailableBiomes();

    @Nullable IBiome getBiome(@NotNull String id);

}
