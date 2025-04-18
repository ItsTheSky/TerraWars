package net.itsthesky.terrawars.core.impl.upgrade.ember;

import net.itsthesky.terrawars.api.model.game.IGameTeam;
import net.itsthesky.terrawars.api.model.game.generator.GameGeneratorType;
import net.itsthesky.terrawars.api.model.upgrade.AbstractUpgrade;
import net.itsthesky.terrawars.api.model.upgrade.UpgradeCategory;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GeneratorSpeedUpgrade extends AbstractUpgrade {

    public record GeneratorDropModifier(int ironRound, int goldRound) { }

    ;
    public static final Map<Integer, GeneratorDropModifier> LEVEL_GENERATION = Map.of( // base is 20, for one second between each drops

            0, new GeneratorDropModifier(GameGeneratorType.BASE.getDrops().get(0).getRoundDelay(),
                    GameGeneratorType.BASE.getDrops().get(1).getRoundDelay()),

            1, new GeneratorDropModifier(4, 22),
            2, new GeneratorDropModifier(3, 20),
            3, new GeneratorDropModifier(2, 16)
    );

    public GeneratorSpeedUpgrade() {
        super("generator", UpgradeCategory.EMBER,
                "Generator Speed", List.of("Increase the base generation", "speed for iron and gold."),
                3, Material.GOLD_INGOT);

        addCost(1, Material.IRON_INGOT, 32);
        addCost(1, Material.GOLD_INGOT, 16);
        addCost(1, Material.DIAMOND, 4);

        addCost(2, Material.IRON_INGOT, 48);
        addCost(2, Material.GOLD_INGOT, 32);
        addCost(2, Material.DIAMOND, 8);

        addCost(3, Material.IRON_INGOT, 64);
        addCost(3, Material.GOLD_INGOT, 64);
        addCost(3, Material.DIAMOND, 16);
        addCost(3, Material.AMETHYST_SHARD, 2);
    }

    @Override
    public @NotNull List<String> buildDescription(@NotNull IGameTeam team, int level) {
        final var lore = new ArrayList<String>();
        final var nextLevel = level + 1;

        final var currentGeneration = LEVEL_GENERATION.get(level);
        final var nextGeneration = LEVEL_GENERATION.get(nextLevel);

        lore.add("<shade-slate:800>- <shade-slate:600>Iron Generation: <shade-amber:500>" +
                String.format("%.1f", currentGeneration.ironRound / 4.0) + "s <shade-slate:600>→ <shade-lime:500>" +
                String.format("%.1f", nextGeneration.ironRound / 4.0) + "s</shade-slate:800>");
        lore.add("<shade-slate:800>- <shade-slate:600>Gold Generation: <shade-amber:500>" +
                String.format("%.1f", currentGeneration.goldRound / 4.0) + "s <shade-slate:600>→ <shade-lime:500>" +
                String.format("%.1f", nextGeneration.goldRound / 4.0) + "s</shade-slate:800>");

        return lore;
    }
}
