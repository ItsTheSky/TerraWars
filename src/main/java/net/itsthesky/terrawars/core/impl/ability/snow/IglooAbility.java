package net.itsthesky.terrawars.core.impl.ability.snow;

import lombok.Getter;
import net.itsthesky.terrawars.TerraWars;
import net.itsthesky.terrawars.api.model.ability.ActiveAbility;
import net.itsthesky.terrawars.api.model.game.IGame;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.api.model.game.IGameTeam;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.core.impl.game.Game;
import net.itsthesky.terrawars.util.BukkitUtils;
import net.itsthesky.terrawars.util.Checks;
import net.minecraft.network.protocol.game.ClientboundCooldownPacket;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class IglooAbility extends ActiveAbility {

    private static final int IGLOO_RADIUS = 3;
    private static final int IGLOO_HEIGHT = 3;
    private static final int IGLOO_DURATION_SECONDS = 10;
    private static final int REGEN_AMOUNT = 4;
    private static final double EXPULSION_FORCE = 1.5;
    private static final Material IGLOO_BLOCK = Material.ICE;

    private final Map<UUID, IglooData> playerIgloos = new HashMap<>();

    public IglooAbility() {
        super("toundra_igloo", Material.ICE, "Igloo Bunker",
                List.of(
                        "Create a temporary 3x3x3 ice dome around you,",
                        "while expelling enemies out. Will heal you for",
                        "<shade-red:500>2 ♥</shade-red> per seconds while you're inside.",
                        "",
                        "<shade-emerald:200>The igloo lasts <shade-emerald:500>10 seconds</shade-red>!"
                ), 60);
    }

    private record IglooData(
            Set<Location> blocks,
            BukkitTask regenTask,
            BukkitTask removeTask,
            Game game
    ) {}

    @Override
    protected boolean execute(@NotNull IGamePlayer gamePlayer, @NotNull IGame rawGame) {
        Checks.notNull(gamePlayer, "gamePlayer");
        Checks.notNull(rawGame, "game");
        final var game = (Game) rawGame;
        final var player = gamePlayer.getPlayer();

        if (!player.isOnline())
            return false;

        final var team = gamePlayer.getTeam();
        final var center = player.getLocation();

        // Create the igloo structure
        final var iglooBlocks = createIgloo(center);
        if (iglooBlocks.isEmpty()) {
            game.getChatService().sendMessage(player, IChatService.MessageSeverity.ERROR,
                    "You cannot create an igloo here, not enough space!");
            return false;
        }

        // Play creation sound and visual effects
        // playSoundEffects(center.getWorld(), center);

        // Expel enemies from the igloo
        expelEnemies(center, gamePlayer, team);

        final var regenTask = scheduleHealthRegeneration(center, gamePlayer, team);
        final var removalTask = scheduleIglooRemoval(player.getUniqueId(), iglooBlocks, regenTask);

        // Show success message
        //showSuccessMessage(player);
        game.getChatService().sendMessage(player, IChatService.MessageSeverity.INFO,
                "Igloo created! You will heal for <shade-emerald:500>2 ♥</shade-emerald> per second while inside.");

        // Store the igloo blocks for this player
        playerIgloos.put(player.getUniqueId(),
                new IglooData(iglooBlocks, regenTask, removalTask, game));

        return true;
    }

    /**
     * Expels enemy players and entities from the igloo area.
     *
     * @param center The center of the igloo
     * @param team The team that owns the igloo
     */
    private void expelEnemies(Location center, IGamePlayer gamePlayer, IGameTeam team) {
        final var world = center.getWorld();
        final var centerX = center.getX();
        final var centerY = center.getY();
        final var centerZ = center.getZ();

        // Get all entities within the igloo radius
        final var entities = world.getNearbyEntities(center, IGLOO_RADIUS, IGLOO_HEIGHT, IGLOO_RADIUS);

        for (Entity entity : entities) {
            if (entity instanceof Player player) {
                // Skip allies
                if (gamePlayer != null && gamePlayer.getTeam() == team)
                    continue;
            }

            if (entity instanceof LivingEntity) {
                // Calculate direction away from center
                final var entityLoc = entity.getLocation();
                final var dirX = entityLoc.getX() - centerX;
                final var dirZ = entityLoc.getZ() - centerZ;

                // Normalize and apply force
                final var length = Math.sqrt(dirX * dirX + dirZ * dirZ);
                if (length > 0) {
                    final var normalizedX = dirX / length;
                    final var normalizedZ = dirZ / length;

                    final var velocity = new Vector(
                            normalizedX * EXPULSION_FORCE,
                            0.5, // Slight upward force
                            normalizedZ * EXPULSION_FORCE
                    );

                    entity.setVelocity(velocity);

                    // Play effect on the entity
                    if (entity instanceof Player) {
                        entity.getWorld().playSound(entity.getLocation(), Sound.BLOCK_SNOW_BREAK, 1.0f, 0.5f);
                    }
                }
            }
        }
    }

    /**
     * Creates an igloo structure around the specified location.
     *
     * @param center The center location of the igloo
     * @return A set of locations where blocks were placed, empty if creation failed
     */
    private Set<Location> createIgloo(Location center) {
        final var world = center.getWorld();
        final var placedBlocks = new HashSet<Location>();
        final var centerX = center.getBlockX();
        final var centerY = center.getBlockY();
        final var centerZ = center.getBlockZ();

        try {
            // Check if there's enough space for the igloo
            if (!hasEnoughSpace(center)) {
                return Collections.emptySet();
            }

            // Create floor
            for (int x = centerX - IGLOO_RADIUS; x <= centerX + IGLOO_RADIUS; x++) {
                for (int z = centerZ - IGLOO_RADIUS; z <= centerZ + IGLOO_RADIUS; z++) {
                    // Skip the center 3x3 area to create entrance
                    if (x > centerX - 2 && x < centerX + 2 && z > centerZ - 2 && z < centerZ + 2) {
                        continue;
                    }

                    final var loc = new Location(world, x, centerY, z);
                    final var block = loc.getBlock();

                    if (block.getType() == Material.AIR) {
                        block.setType(IGLOO_BLOCK);
                        placedBlocks.add(loc);
                    }
                }
            }

            // Create walls
            for (int y = centerY + 1; y < centerY + IGLOO_HEIGHT; y++) {
                for (int x = centerX - IGLOO_RADIUS; x <= centerX + IGLOO_RADIUS; x++) {
                    for (int z = centerZ - IGLOO_RADIUS; z <= centerZ + IGLOO_RADIUS; z++) {
                        // Only place blocks on the perimeter
                        if (x == centerX - IGLOO_RADIUS || x == centerX + IGLOO_RADIUS ||
                                z == centerZ - IGLOO_RADIUS || z == centerZ + IGLOO_RADIUS) {

                            final var loc = new Location(world, x, y, z);
                            final var block = loc.getBlock();

                            if (block.getType() == Material.AIR) {
                                block.setType(IGLOO_BLOCK);
                                placedBlocks.add(loc);
                            }
                        }
                    }
                }
            }

            // Create ceiling
            for (int x = centerX - IGLOO_RADIUS; x <= centerX + IGLOO_RADIUS; x++) {
                for (int z = centerZ - IGLOO_RADIUS; z <= centerZ + IGLOO_RADIUS; z++) {
                    final var loc = new Location(world, x, centerY + IGLOO_HEIGHT, z);
                    final var block = loc.getBlock();

                    if (block.getType() == Material.AIR) {
                        block.setType(IGLOO_BLOCK);
                        placedBlocks.add(loc);
                    }
                }
            }

            return placedBlocks;
        } catch (Exception e) {
            // If any error occurs, ensure we don't leave a partial structure
            for (Location loc : placedBlocks) {
                loc.getBlock().setType(Material.AIR);
            }

            TerraWars.instance().getLogger().warning("Failed to create igloo: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptySet();
        }
    }

    /**
     * Checks if the area has enough space to create an igloo.
     *
     * @param center The center location
     * @return true if there's enough space, false otherwise
     */
    private boolean hasEnoughSpace(Location center) {
        final var world = center.getWorld();
        final var centerX = center.getBlockX();
        final var centerY = center.getBlockY();
        final var centerZ = center.getBlockZ();

        // Count the number of solid blocks in the area
        int solidBlocks = 0;
        int totalBlocks = 0;

        for (int y = centerY; y <= centerY + IGLOO_HEIGHT; y++) {
            for (int x = centerX - IGLOO_RADIUS; x <= centerX + IGLOO_RADIUS; x++) {
                for (int z = centerZ - IGLOO_RADIUS; z <= centerZ + IGLOO_RADIUS; z++) {
                    totalBlocks++;

                    final var block = world.getBlockAt(x, y, z);
                    if (block.getType().isSolid()) {
                        solidBlocks++;
                    }
                }
            }
        }

        // If more than 30% of the area is already solid, don't build
        return (double) solidBlocks / totalBlocks < 0.3;
    }

    /**
     * Schedules the removal of the igloo after the duration.
     *
     * @param playerUuid The UUID of the player who created the igloo
     * @param iglooBlocks The blocks making up the igloo
     * @param regenTask The regeneration task to cancel
     */
    private BukkitTask scheduleIglooRemoval(UUID playerUuid, Set<Location> iglooBlocks, BukkitTask regenTask) {
        final var countdown = new AtomicInteger(IGLOO_DURATION_SECONDS);

        return BukkitUtils.runTaskTimer(() -> {
            final var secondsLeft = countdown.getAndDecrement();

            if (secondsLeft <= 0) {
                // Stop regeneration
                regenTask.cancel();

                // Remove the igloo blocks
                for (Location loc : iglooBlocks) {
                    if (loc.getBlock().getType() == IGLOO_BLOCK) {
                        // Play break effect
                        loc.getWorld().playSound(loc, Sound.BLOCK_SNOW_BREAK, 0.3f, 1.0f);

                        // Remove the block
                        loc.getBlock().setType(Material.AIR);
                    }
                }

                // Remove from tracked igloos
                playerIgloos.remove(playerUuid);
            }
        }, 20, 20); // Run every second
    }

    /**
     * Schedules a task to regenerate health for allies inside the igloo.
     *
     * @param center The center of the igloo
     * @param team The team that owns the igloo
     * @return The BukkitTask handling regeneration
     */
    private BukkitTask scheduleHealthRegeneration(Location center, IGamePlayer gamePlayer, IGameTeam team) {
        final var world = center.getWorld();

        return BukkitUtils.runTaskTimer(() -> {
            // Get all players within the igloo radius
            final var entities = world.getNearbyEntities(center, IGLOO_RADIUS, IGLOO_HEIGHT, IGLOO_RADIUS);

            for (Entity entity : entities) {
                if (entity instanceof Player player) {
                    // Only heal allies
                    if (gamePlayer != null && gamePlayer.getTeam() == team) {
                        // Apply regeneration
                        final var maxHealth = player.getMaxHealth();
                        final var newHealth = Math.min(player.getHealth() + REGEN_AMOUNT, maxHealth);
                        player.setHealth(newHealth);

                        // Show healing effect
                        world.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2.0f);
                    }
                }
            }
        }, 40, 40); // 40 ticks = 2 seconds, runs 5 times during the 10 seconds
    }
}
