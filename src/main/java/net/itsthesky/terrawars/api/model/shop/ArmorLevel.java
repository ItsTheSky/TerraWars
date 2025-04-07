package net.itsthesky.terrawars.api.model.shop;

import lombok.Getter;
import org.bukkit.Material;

@Getter
public enum ArmorLevel implements Comparable<ArmorLevel> {
    LEATHER,
    CHAINMAIL(Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS),
    IRON(Material.IRON_LEGGINGS, Material.IRON_BOOTS),
    DIAMOND(Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS),
    NETHERITE(Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS),
    ;

    private final Material leggings;
    private final Material boots;

    ArmorLevel() {
        this(Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS);
    }

    ArmorLevel(Material leggings, Material boots) {
        this.leggings = leggings;
        this.boots = boots;
    }

    public boolean isHigherThan(ArmorLevel other) {
        return this.compareTo(other) > 0;
    }

    public boolean isLowerThan(ArmorLevel other) {
        return this.compareTo(other) < 0;
    }

    public boolean isEqualTo(ArmorLevel other) {
        return this.compareTo(other) == 0;
    }
}
