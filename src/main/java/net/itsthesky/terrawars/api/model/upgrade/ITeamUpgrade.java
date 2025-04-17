package net.itsthesky.terrawars.api.model.upgrade;

import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.api.model.game.IGameTeam;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ITeamUpgrade {

    @NotNull String getUpgradeId();

    @NotNull UpgradeCategory getCategory();

    @NotNull String getName();

    @NotNull List<String> getDescription();

    @NotNull List<String> buildDescription(@NotNull IGameTeam team, int level);

    @NotNull Material getIcon();

    void applyUpgrade(@NotNull IGamePlayer source, @NotNull IGameTeam team, int level);

    int getMaxLevel();

    @NotNull Map<Material, Integer> getCosts(@NotNull IGameTeam team, int level);

    @NotNull Map<ITeamUpgrade, Integer> getRequiredUpgrades();
}
