package net.itsthesky.terrawars.api.model.game.generator;

import lombok.Getter;
import net.itsthesky.terrawars.util.Colors;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;

import java.util.*;

@Getter
public enum GameGeneratorType {
    BASE(new GeneratorDrop(Material.IRON_INGOT, 1), new GeneratorDrop(Material.GOLD_INGOT, 6)),
    DIAMOND(Material.DIAMOND_BLOCK, "Diamond Generator", 8, Colors.SKY, new GeneratorDrop(Material.DIAMOND, 30)),
    EMERALD(Material.EMERALD_BLOCK, "Emerald Generator", 8, Colors.EMERALD, new GeneratorDrop(Material.EMERALD, 60)),
    AMETHYST(Material.AMETHYST_BLOCK, "Amethyst Generator", 4, Colors.VIOLET, new GeneratorDrop(Material.AMETHYST_SHARD, 120)),
    ;

    public static final List<GameGeneratorType> CONFIGURABLE_TYPES =
            List.of(DIAMOND, EMERALD, AMETHYST);

    private final Material blockIcon;
    private final String displayName;
    private final int maxEntities;
    private final Set<GeneratorDrop> drops;
    private final List<TextColor> scheme;

    GameGeneratorType(Material blockIcon, String displayName,
                      int maxEntities, List<TextColor> scheme, GeneratorDrop... drops) {
        this.blockIcon = blockIcon;
        this.displayName = displayName;
        this.maxEntities = maxEntities;
        this.scheme = scheme;
        this.drops = Set.of(drops);
    }

    GameGeneratorType(GeneratorDrop... drops) {
        this(Material.BARRIER, "Base Type", 64, Colors.SLATE, drops);
    }

    public boolean hasPhysicalDisplay() {
        return this != BASE;
    }
}
