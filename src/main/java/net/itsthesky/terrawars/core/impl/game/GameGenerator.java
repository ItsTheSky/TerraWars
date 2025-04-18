package net.itsthesky.terrawars.core.impl.game;

import com.github.stefvanschie.inventoryframework.util.UUIDTagType;
import net.itsthesky.terrawars.api.model.game.generator.GameGeneratorType;
import net.itsthesky.terrawars.api.model.game.generator.GeneratorDrop;
import net.itsthesky.terrawars.core.config.GameGeneratorConfig;
import net.itsthesky.terrawars.core.impl.upgrade.crystal.EmeraldGeneratorUpgrade;
import net.itsthesky.terrawars.core.impl.upgrade.ember.GeneratorSpeedUpgrade;
import net.itsthesky.terrawars.core.impl.upgrade.TeamUpgrades;
import net.itsthesky.terrawars.util.BukkitUtils;
import net.itsthesky.terrawars.util.Colors;
import net.itsthesky.terrawars.util.Keys;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class GameGenerator {

    private final UUID uuid;
    private final BukkitTask generatingTask;
    private final GameGeneratorType type;
    private final Game game;
    private final GameTeam team;

    private final Location spawnLocation;
    private TextDisplay textDisplay;
    private BlockDisplay blockDisplay;

    public GameGenerator(@NotNull Game game, @NotNull GameGeneratorConfig config) {
        this.uuid = UUID.randomUUID();

        this.game = game;
        this.type = config.getGeneratorType();
        this.spawnLocation = config.getGeneratorLocation();
        this.spawnLocation.setYaw(0);
        this.spawnLocation.setPitch(0);
        this.team = null;

        this.generatingTask = createGeneratingTask();
        this.createDisplays();
    }

    public GameGenerator(@NotNull Game game, @NotNull GameTeam gameTeam) {
        this.uuid = UUID.randomUUID();
        this.game = game;
        this.team = gameTeam;
        this.type = GameGeneratorType.BASE;

        this.spawnLocation = gameTeam.getConfig().getGeneratorLocation();
        this.generatingTask = createGeneratingTask();
    }

    private void createDisplays() {
        final var textLocation = spawnLocation.clone().toCenterLocation().add(0, 2.5, 0);
        final var blockLocation = textLocation.clone().add(-0.25, 0.75, -0.25);

        this.textDisplay = spawnLocation.getWorld().spawn(textLocation, TextDisplay.class, display -> {
            display.text(Component.text("Loading ..."));
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
            final int entityCount = spawnLocation.getWorld().getNearbyEntities(spawnLocation, 2, 2, 2,
                    entity -> entity.getType().equals(EntityType.ITEM) &&
                            this.uuid.equals(entity.getPersistentDataContainer().getOrDefault(Keys.GENERATOR_ITEM_KEY, UUIDTagType.INSTANCE, null))).size();
            if (entityCount > type.getMaxEntities())
            {
                updateDisplays(true, 0);
                return;
            }

            final var round = roundCount.getAndIncrement();
            final var drops = new ArrayList<>(type.getDrops());
            if (type == GameGeneratorType.BASE && team.getUpgradeLevel(TeamUpgrades.EMERALD_GENERATOR) > 0) {
                drops.add(new GeneratorDrop(Material.EMERALD,
                        EmeraldGeneratorUpgrade.LEVEL_GENERATION.get(
                                team.getUpgradeLevel(TeamUpgrades.EMERALD_GENERATOR)
                        )));
            }

            for (var drop : drops) {
                int roundDelay = drop.getRoundDelay();
                if (type == GameGeneratorType.BASE) {
                    final var generatorLevel = team.getUpgradeLevel(TeamUpgrades.GENERATOR_SPEED);
                    if (generatorLevel != 0) {
                        final var dropModifier = GeneratorSpeedUpgrade.LEVEL_GENERATION.get(generatorLevel);
                        if (drop == GameGeneratorType.BASE.getDrops().get(0)) {
                            roundDelay = dropModifier.ironRound();
                        } else if (drop == GameGeneratorType.BASE.getDrops().get(1)) {
                            roundDelay = dropModifier.goldRound();
                        }
                    }
                }

                if (round % roundDelay == 0) {
                    spawnLocation.getWorld().spawn(spawnLocation, Item.class, item -> {
                        item.setItemStack(new ItemStack(drop.getMaterial()));
                        item.getPersistentDataContainer().set(Keys.GENERATOR_ITEM_KEY, UUIDTagType.INSTANCE, uuid);
                        item.setVelocity(new Vector(0, 0.1, 0));
                    });
                }
            }

            final var firstDrop = type.getDrops().iterator().next();
            final var next = (firstDrop.getRoundDelay() - (round % firstDrop.getRoundDelay())) / 4;
            updateDisplays(false, next);
        }, 20, 5);
    }

    public void cleanup() {
        if (textDisplay != null)
            textDisplay.remove();
        if (blockDisplay != null)
            blockDisplay.remove();
        if (generatingTask != null)
            generatingTask.cancel();
    }

    public void updateDisplays(boolean full, int next) {
        if (textDisplay == null || blockDisplay == null)
            return;

        final var firstLine = game.getChatService().format("<base>✪ <text>" + this.type.getDisplayName() + " <base>✪", this.type.getScheme());
        final var secondLine = full
                ? game.getChatService().format("<accent>Too many items in the generator.", Colors.RED)
                : game.getChatService().format("<text>Next spawn in <base>" + formatTime(next), this.type.getScheme());

        textDisplay.text(game.getChatService().joinNewLine(List.of(firstLine, secondLine)));
    }

    private String formatTime(int seconds) {
        final var minutes = seconds / 60;
        final var secondsLeft = seconds % 60;
        return (minutes > 0 ? minutes + "m " : "") + secondsLeft + "s";
    }

}
