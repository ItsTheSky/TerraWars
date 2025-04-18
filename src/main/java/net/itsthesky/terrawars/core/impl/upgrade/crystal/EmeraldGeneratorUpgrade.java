package net.itsthesky.terrawars.core.impl.upgrade.crystal;

import net.itsthesky.terrawars.api.model.game.IGameTeam;
import net.itsthesky.terrawars.api.model.upgrade.AbstractUpgrade;
import net.itsthesky.terrawars.api.model.upgrade.UpgradeCategory;
import net.itsthesky.terrawars.core.impl.upgrade.TeamUpgrades;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EmeraldGeneratorUpgrade extends AbstractUpgrade {

    public static final Map<Integer, Integer> LEVEL_GENERATION = Map.of(
            1, 128,
            2, 96,
            3, 64
    );

    public EmeraldGeneratorUpgrade() {
        super("emerald_generator", UpgradeCategory.CRYSTAL, "Emerald Generator", List.of(
                "Make your base generator generate", "emerald. More levels means more", "emeralds per second."
        ), 3, Material.EMERALD);

        addCost(1, Material.DIAMOND, 12);
        addCost(1, Material.EMERALD, 8);

        addCost(2, Material.DIAMOND, 16);
        addCost(2, Material.EMERALD, 12);
        addCost(2, Material.AMETHYST_SHARD, 1);

        addCost(2, Material.DIAMOND, 20);
        addCost(2, Material.EMERALD, 16);
        addCost(2, Material.AMETHYST_SHARD, 2);

        addRequiredUpgrade(TeamUpgrades.GENERATOR_SPEED, 3);
    }

    @Override
    public @NotNull List<String> buildDescription(@NotNull IGameTeam team, int level) {
        final var lore = new ArrayList<String>();
        final var nextLevel = level + 1;

        final var currentGeneration = LEVEL_GENERATION.get(level);
        final var nextGeneration = LEVEL_GENERATION.get(nextLevel);

        if (level == 0) { // not yet unlocked
            lore.add("<shade-slate:800>- <shade-slate:600>Unlocks <shade-lime:500>Emerald Generation<shade-slate:600> (<shade-amber:500>1 per " +
                    String.format("%.1f", nextGeneration / 4.0) + "s<shade-slate:600>)");
        } else {
            lore.add("<shade-slate:800>- <shade-slate:600>Emerald Generation: <shade-amber:500>" +
                    String.format("%.1f", currentGeneration / 4.0) + "s <shade-slate:600>→ <shade-lime:500>" +
                    String.format("%.1f", nextGeneration / 4.0) + "s</shade-slate:800>");
        }

        return lore;
    }
}
