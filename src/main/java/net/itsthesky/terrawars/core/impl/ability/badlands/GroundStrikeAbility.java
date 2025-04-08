package net.itsthesky.terrawars.core.impl.ability.badlands;

import net.itsthesky.terrawars.api.model.ability.ActiveAbility;
import net.itsthesky.terrawars.api.model.game.IGame;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.core.impl.game.Game;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GroundStrikeAbility extends ActiveAbility {

    public GroundStrikeAbility() {
        super("badlands_ground_strike", Material.DIRT, "Ground Strike",
                List.of(
                        "Push any enemies around you away",
                        "by <shade-brown:500>5 blocks</shade-brown>."
                ), 20);
    }

    @Override
    protected boolean execute(@NotNull IGamePlayer player, @NotNull IGame game) {
        // Validation
        if (!player.isOnline())
            return false;

        final var bukkitPlayer = player.getPlayer();
        final var location = bukkitPlayer.getLocation();
        final var world = location.getWorld();
        final var team = player.getTeam();

        // Constants for the ability
        final double RADIUS = 5.0;
        final double PUSH_FORCE = 1.5;
        final double VERTICAL_FORCE = 0.5;

        // Visual and sound effects at the source
        world.playSound(location, Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 1.0f, 0.5f);
        world.spawnParticle(Particle.BLOCK, location, 50, 2, 0.5, 2, 0.1, Material.DIRT.createBlockData());

        // Find entities in radius and push them
        final var entities = world.getNearbyEntities(location, RADIUS, RADIUS, RADIUS);
        int affectedEntities = 0;

        for (Entity entity : entities) {
            // Skip self and allies
            if (entity.equals(bukkitPlayer))
                continue;

            if (entity instanceof Player targetPlayer) {
                final var targetGamePlayer = game.findGamePlayer(targetPlayer);
                // Skip allies
                if (targetGamePlayer != null && targetGamePlayer.getTeam() == team)
                    continue;
            }

            if (entity instanceof LivingEntity) {
                // Calculate direction away from player
                final var entityLocation = entity.getLocation();
                final var direction = entityLocation.toVector().subtract(location.toVector());

                // Only push if not already too far
                if (direction.lengthSquared() > 0.1) {
                    direction.normalize();
                    direction.multiply(PUSH_FORCE);
                    direction.setY(VERTICAL_FORCE); // Add some upward push

                    entity.setVelocity(direction);
                    affectedEntities++;

                    // Visual effect on pushed entity
                    world.spawnParticle(Particle.BLOCK, entityLocation, 10, 0.3, 0.3, 0.3, 0.1,
                            Material.DIRT.createBlockData());
                    world.playSound(entityLocation, Sound.BLOCK_GRAVEL_BREAK, 0.6f, 1.0f);
                }
            }
        }

        // Ground effect - cracked pattern around player
        for (int i = 0; i < 12; i++) {
            final double angle = Math.toRadians(i * 30);
            for (int distance = 1; distance <= 3; distance++) {
                final var x = location.getX() + Math.cos(angle) * distance;
                final var z = location.getZ() + Math.sin(angle) * distance;
                final var effectLoc = new Location(world, x, location.getY(), z);

                world.spawnParticle(Particle.BLOCK, effectLoc, 5, 0.2, 0, 0.2, 0, Material.DIRT.createBlockData());
            }
        }

        // Success message
        if (affectedEntities > 0) {
            ((Game) game).getChatService().sendMessage(bukkitPlayer, IChatService.MessageSeverity.SUCCESS,
                    "You pushed <accent>" + affectedEntities + "<text> " +
                            (affectedEntities == 1 ? "enemy" : "enemies") + " away with your Ground Strike!");
        } else {
            ((Game) game).getChatService().sendMessage(bukkitPlayer, IChatService.MessageSeverity.INFO,
                    "Your Ground Strike didn't affect any enemies.");
        }

        return true;
    }
}
