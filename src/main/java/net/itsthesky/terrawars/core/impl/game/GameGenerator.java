package net.itsthesky.terrawars.core.impl.game;

import net.itsthesky.terrawars.api.model.game.generator.GameGeneratorType;
import net.itsthesky.terrawars.core.config.GameGeneratorConfig;
import net.itsthesky.terrawars.util.BukkitUtils;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

public class GameGenerator {

    private final BukkitTask generatingTask;
    private final GameGeneratorType type;
    private final Game game;

    private final Location spawnLocation;
    private TextDisplay textDisplay;
    private BlockDisplay blockDisplay;

    public GameGenerator(@NotNull Game game, @NotNull GameGeneratorConfig config) {
        this.game = game;
        this.type = config.getGeneratorType();
        this.spawnLocation = config.getGeneratorLocation();
        this.spawnLocation.setYaw(0);
        this.spawnLocation.setPitch(0);

        this.generatingTask = createGeneratingTask();
        this.createDisplays();
    }

    public GameGenerator(@NotNull Game game, @NotNull Location location) {
        this.game = game;
        this.type = GameGeneratorType.BASE;

        this.spawnLocation = location;
        this.generatingTask = createGeneratingTask();
    }

    private void createDisplays() {
        final var textLocation = spawnLocation.clone().toCenterLocation().add(0, 2.5, 0);
        final var blockLocation = textLocation.clone().add(-0.25, 0.5, -0.25);

        this.textDisplay = spawnLocation.getWorld().spawn(textLocation, TextDisplay.class, display -> {
            display.text(game.getChatService().format("<base>✪ <text>" + this.type.getDisplayName() + " <base>✪", this.type.getScheme()));
            display.setBillboard(Display.Billboard.CENTER);
        });

        this.blockDisplay = spawnLocation.getWorld().spawn(blockLocation, BlockDisplay.class, display -> {
            display.setBlock(type.getBlockIcon().createBlockData());
            display.setGravity(false);

            final var transform = display.getTransformation();
            transform.getScale().set(0.5, 0.5, 0.5);
            display.setTransformation(transform);
        });
    }

    private final AtomicInteger roundCount = new AtomicInteger(0);
    private BukkitTask createGeneratingTask() {
        return BukkitUtils.runTaskTimer(() -> {
            final int round = roundCount.getAndIncrement();
            for (var drop : type.getDrops()) {
                if (round % drop.getRoundDelay() != 0)
                    continue;

                spawnLocation.getWorld().dropItem(
                        spawnLocation,
                        new ItemStack(drop.getMaterial())
                );
            }

            if (blockDisplay != null) {
                blockDisplay.setBlock(type.getBlockIcon().createBlockData());
            }
        }, 20, 20);
    }

    public void cleanup() {
        if (textDisplay != null)
            textDisplay.remove();
        if (blockDisplay != null)
            blockDisplay.remove();
        if (generatingTask != null)
            generatingTask.cancel();
    }


}
