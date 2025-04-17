package net.itsthesky.terrawars.api.model.game;

import net.itsthesky.terrawars.api.model.biome.IBiome;
import net.itsthesky.terrawars.api.model.upgrade.AbstractUpgrade;
import net.itsthesky.terrawars.api.model.upgrade.ITeamUpgrade;
import net.itsthesky.terrawars.core.impl.game.GameBiomeNode;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface IGameTeam extends IGameHolder {

    @NotNull Set<IGamePlayer> getPlayers();

    @NotNull IBiome getBiome();

    @NotNull IGameNexus getNexus();

    @NotNull List<TextColor> getColorScheme();

    @NotNull Location getSpawnLocation();

    @NotNull UUID getId();

    boolean tryAddPlayer(@NotNull IGamePlayer player);

    @NotNull Location getTeamChestLocation();

    @Nullable IGamePlayer getPlayer(@NotNull Player player);

    boolean shouldApplyUpgrade(@NotNull ITeamUpgrade upgrade);

    @NotNull Set<GameBiomeNode> getCapturedNodes();

    int getUpgradeLevel(@NotNull ITeamUpgrade upgrade);

    void increaseUpgradeLevel(ITeamUpgrade upgrade);
}
