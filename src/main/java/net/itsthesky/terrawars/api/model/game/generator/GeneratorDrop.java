package net.itsthesky.terrawars.api.model.game.generator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;

/**
 * Represents a drop from a game generator.
 */
@Getter
@Setter
@AllArgsConstructor
public class GeneratorDrop {

    /**
     * The material of the drop.
     */
    private final Material material;

    /**
     * The delay between each spawn of this drop. For instance, a delay of 5
     * means this drop will appear every 5 "seconds" (= rounds).
     * <br>
     * 1 would be at every round.
     */
    private final int roundDelay;

}
