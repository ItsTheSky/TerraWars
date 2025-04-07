package net.itsthesky.terrawars.core.impl.ability.end;

import net.itsthesky.terrawars.api.model.ability.ActiveAbility;
import net.itsthesky.terrawars.api.model.game.IGame;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.core.impl.game.Game;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DragonBreathAbility extends ActiveAbility {

    private static final double RADIUS = 3.0;
    private static final int MAX_TARGETS = 3;
    private static final int BLIND_DURATION = 3 * 20; // 3 seconds in ticks
    private static final int SLOW_DURATION = 1 * 20; // 1 seconds in ticks
    private static final int SLOW_AMPLIFIER = 1; // Slowness III

    public DragonBreathAbility() {
        super("end_dragon_breath", Material.DRAGON_BREATH, "Dragon Breath", List.of(
                "Unleash a powerful breath attack",
                "blinding and slowing enemies towards you",
                "in a 3 block radius. Max 3 targets."
        ), 60);
    }

    @Override
    protected boolean execute(@NotNull IGamePlayer gamePlayer, @NotNull IGame rawGame) {
        final Game game = (Game) rawGame;
        final Player player = gamePlayer.getPlayer();
        final Location playerLocation = player.getLocation();
        final Vector direction = playerLocation.getDirection();

        if (!player.isOnline()) {
            return false;
        }

        // Find nearby entities in the player's line of sight
        final List<LivingEntity> targets = findTargets(player, gamePlayer);

        if (targets.isEmpty()) {
            game.getChatService().sendMessage(player, IChatService.MessageSeverity.WARNING,
                    "No targets in range for Dragon Breath!");
            return false;
        }

        // Play dragon breath sound and effects
        player.getWorld().playSound(playerLocation, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.2f);

        // Create breath particle effect
        createBreathEffect(playerLocation, direction);

        // Apply effects to targets
        int affectedCount = 0;
        for (LivingEntity target : targets) {
            if (affectedCount >= MAX_TARGETS) {
                break;
            }

            // Apply blindness and slowness
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, BLIND_DURATION, 0));
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, SLOW_DURATION, SLOW_AMPLIFIER));

            // Create particles on the target
            target.getWorld().spawnParticle(
                    Particle.DRAGON_BREATH,
                    target.getLocation().add(0, 1, 0),
                    20, 0.5, 0.5, 0.5, 0.05
            );

            // If it's a player, send them a message
            if (target instanceof Player targetPlayer) {
                final var targetGamePlayer = game.findGamePlayer(targetPlayer);

                if (targetGamePlayer != null) {
                    game.getChatService().sendMessage(targetPlayer, IChatService.MessageSeverity.ERROR,
                            "You've been hit by <accent>" + player.getName() + "<text>'s Dragon Breath!");
                }
            }

            affectedCount++;
        }

        // Success message to the user
        game.getChatService().sendMessage(player, IChatService.MessageSeverity.SUCCESS,
                "Your Dragon Breath hit <accent>" + affectedCount + "<text> " +
                        (affectedCount == 1 ? "enemy" : "enemies") + "!");

        return true;
    }

    private List<LivingEntity> findTargets(Player player, IGamePlayer gamePlayer) {
        final List<LivingEntity> potentialTargets = new ArrayList<>();
        final Location playerLocation = player.getLocation();
        final Vector direction = playerLocation.getDirection();

        // Get all entities in radius
        final List<Entity> nearbyEntities = player.getNearbyEntities(RADIUS, RADIUS, RADIUS);

        for (Entity entity : nearbyEntities) {
            // Skip non-living entities
            if (!(entity instanceof LivingEntity livingEntity)) {
                continue;
            }

            // Skip allies
            if (entity instanceof Player targetPlayer) {
                final var targetGamePlayer = gamePlayer.getGame().findGamePlayer(targetPlayer);

                // Skip if the player is on the same team
                if (targetGamePlayer != null && targetGamePlayer.getTeam() == gamePlayer.getTeam()) {
                    continue;
                }
            }

            // Check if entity is roughly in front of the player (in a cone)
            Vector toEntity = entity.getLocation().toVector().subtract(playerLocation.toVector());

            if (toEntity.length() <= RADIUS) {
                // Normalize both vectors for the angle calculation
                toEntity = toEntity.normalize();
                Vector playerDir = direction.clone().normalize();

                // Calculate the angle between the player's look direction and the entity
                double angle = Math.toDegrees(Math.acos(toEntity.dot(playerDir)));

                // If the entity is within a 60-degree cone in front of the player
                if (angle <= 60) {
                    potentialTargets.add(livingEntity);
                }
            }
        }

        // Sort by distance to player (closest first)
        potentialTargets.sort(Comparator.comparingDouble(entity ->
                entity.getLocation().distanceSquared(playerLocation)));

        return potentialTargets;
    }

    private void createBreathEffect(Location startLocation, Vector direction) {
        final Vector normalizedDir = direction.clone().normalize();
        final Location particleLocation = startLocation.clone().add(0, 1.5, 0); // Start at eye level

        // Create a dragon breath effect that extends outward from the player
        for (double i = 0; i < RADIUS; i += 0.25) {
            // Add some randomization to create a cone effect
            for (int j = 0; j < 3; j++) {
                final Vector offset = normalizedDir.clone().multiply(i);

                // Add slight randomization
                offset.add(new Vector(
                        (Math.random() - 0.5) * i * 0.5,
                        (Math.random() - 0.5) * i * 0.5,
                        (Math.random() - 0.5) * i * 0.5
                ));

                final Location loc = particleLocation.clone().add(offset);

                // Spawn dragon breath particles
                loc.getWorld().spawnParticle(
                        Particle.DRAGON_BREATH,
                        loc,
                        3, 0.1, 0.1, 0.1, 0.01
                );
            }
        }

        // Add some dust particles for effect
        Particle.DustOptions dustOptions = new Particle.DustOptions(
                Color.fromRGB(128, 0, 128), // Purple color
                1.5f // Size
        );

        for (double i = 0; i < RADIUS; i += 0.5) {
            final Vector offset = normalizedDir.clone().multiply(i);
            final Location loc = particleLocation.clone().add(offset);

            loc.getWorld().spawnParticle(
                    Particle.DUST,
                    loc,
                    5, 0.2, 0.2, 0.2, 0,
                    dustOptions
            );
        }
    }
}