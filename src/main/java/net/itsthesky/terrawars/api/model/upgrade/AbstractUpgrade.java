package net.itsthesky.terrawars.api.model.upgrade;

import lombok.Getter;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.api.model.game.IGameTeam;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public abstract class AbstractUpgrade implements ITeamUpgrade {

    protected final String upgradeId;
    protected final UpgradeCategory category;
    protected final String name;
    protected final List<String> description;
    protected final int maxLevel;
    protected final Material icon;
    protected final Map<Integer, Map<Material, Integer>> costs;
    protected final Map<ITeamUpgrade, Integer> requiredUpgrades;

    protected AbstractUpgrade(String id, UpgradeCategory category, String name, List<String> description, int maxLevel, Material icon) {
        this.upgradeId = id;
        this.category = category;
        this.name = name;
        this.description = description;
        this.maxLevel = maxLevel;
        this.icon = icon;

        this.costs = new HashMap<>();
        this.requiredUpgrades = new HashMap<>();
    }

    public AbstractUpgrade addCost(int level, Material material, int amount) {
        this.costs.computeIfAbsent(level, k -> new HashMap<>()).put(material, amount);
        return this;
    }

    public AbstractUpgrade addRequiredUpgrade(ITeamUpgrade upgrade, int minLevel) {
        this.requiredUpgrades.put(upgrade, minLevel);
        return this;
    }

    public AbstractUpgrade addRequiredUpgrade(ITeamUpgrade upgrade) {
        return addRequiredUpgrade(upgrade, upgrade.getMaxLevel());
    }

    @Override
    public @NotNull Map<Material, Integer> getCosts(@NotNull IGameTeam team, int level) {
        return costs.getOrDefault(level, Collections.emptyMap());
    }

    @Override
    public void applyUpgrade(@NotNull IGamePlayer source, @NotNull IGameTeam team, int level) {
        team.increaseUpgradeLevel(this);
    }
}
