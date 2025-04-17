package net.itsthesky.terrawars.api.model.game.generator;

import lombok.Getter;
import net.itsthesky.terrawars.util.Colors;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;

import java.util.List;
import java.util.Set;

@Getter
public enum GameGeneratorType {
    BASE(new GeneratorDrop(Material.IRON_INGOT, 4), new GeneratorDrop(Material.GOLD_INGOT, 24)),
    DIAMOND(Material.DIAMOND_BLOCK, "Diamond Generator", 8, Colors.SKY, new GeneratorDrop(Material.DIAMOND, 30*4)),
    EMERALD(Material.EMERALD_BLOCK, "Emerald Generator", 8, Colors.EMERALD, new GeneratorDrop(Material.EMERALD, 60*4)),
    AMETHYST(Material.AMETHYST_BLOCK, "Amethyst Generator", 4, Colors.VIOLET, new GeneratorDrop(Material.AMETHYST_SHARD, 120*4)),
    ;

    public static final List<GameGeneratorType> CONFIGURABLE_TYPES =
            List.of(DIAMOND, EMERALD, AMETHYST);

    private final Material blockIcon;
    private final String displayName;
    private final int maxEntities;
    private final List<GeneratorDrop> drops;
    private final List<TextColor> scheme;

    GameGeneratorType(Material blockIcon, String displayName,
                      int maxEntities, List<TextColor> scheme, GeneratorDrop... drops) {
        this.blockIcon = blockIcon;
        this.displayName = displayName;
        this.maxEntities = maxEntities;
        this.scheme = scheme;
        this.drops = List.of(drops);
    }

    GameGeneratorType(GeneratorDrop... drops) {
        this(Material.BARRIER, "Base Type", 64, Colors.SLATE, drops);
    }

    public boolean hasPhysicalDisplay() {
        return this != BASE;
    }
}
