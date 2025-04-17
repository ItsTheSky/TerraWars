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
     * The delay, in 1/4 of second, between each spawn of this drop. For instance, a delay of 4
     * means this drop will appear every 1 second.
     * <br>
     * <code>1 = 0.25 second = 5 ticks</code>
     */
    private final int roundDelay;

}
