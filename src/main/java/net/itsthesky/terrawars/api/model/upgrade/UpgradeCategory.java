package net.itsthesky.terrawars.api.model.upgrade;

import lombok.Getter;
import org.bukkit.Material;

import java.util.List;

@Getter
public enum UpgradeCategory {
    EMBER("Ember", List.of("The dawn of civilization, where","the first sparks of knowledge are kindled"),
            0, Material.ORANGE_STAINED_GLASS_PANE,
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWYwZmE0MTgwMGNjOTkyMzEwN2Y2MjZjYjllOTY5ZWFjMGNiMGUxOTA0ZGZhODAwMzFhZTU4MWZmNzQxYmYyZCJ9fX0="),
    CRYSTAL("Crystal", List.of("As knowledge grows, societies learn", "to harness and refine natural resources"),
            1, Material.WHITE_STAINED_GLASS_PANE,
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTY2MzZiYTY5ODhjZTliNDBkZGM3NDlhMDljZTBmYjkzOWFmNTI2MDA1OTk1YzE4ZDMyM2FjOTY2MjVmMGQ2ZCJ9fX0="),
    CELESTIAL("Celestial", List.of("Civilizations reach beyond their","world, drawing power from cosmic forces"),
            2, Material.LIGHT_BLUE_STAINED_GLASS_PANE,
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzVmZjI1MzIwYTg0MjUwOWI3ZWU1YTIzNjczZjY4NTI5MWM0NjcyYjgzNGQ5YjU3N2U4NjJhOTIwOTgzYzEwYiJ9fX0="),
    QUANTUM("Quantum", List.of("The pinnacle of civilization, where","the boundaries of reality are pushed"),
            3, Material.PURPLE_STAINED_GLASS_PANE,
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzNhOGU0MDJkYWQxYjdkYWQ5YWFlNmY0MDE1OTMyMTgzNDI5Y2U4N2JiYmVjZWQzMTE5MDI2ZjgyOTYzMzZjMiJ9fX0="),
    ;

    private final String name;
    private final List<String> description;
    private final int requiredCapturedNodes;
    private final Material paneMaterial;
    private final String iconTexture;

    UpgradeCategory(String name, List<String> description, int requiredCapturedNodes, Material paneMaterial, String iconTexture) {
        this.name = name;
        this.description = description;
        this.requiredCapturedNodes = requiredCapturedNodes;
        this.paneMaterial = paneMaterial;
        this.iconTexture = iconTexture;
    }
}
