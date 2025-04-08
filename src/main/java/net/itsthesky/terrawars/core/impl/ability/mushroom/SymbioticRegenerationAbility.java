package net.itsthesky.terrawars.core.impl.ability.mushroom;

import net.itsthesky.terrawars.api.model.ability.PassiveAbility;
import net.itsthesky.terrawars.api.model.game.IGame;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.core.impl.game.Game;
import net.itsthesky.terrawars.util.BukkitUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SymbioticRegenerationAbility extends PassiveAbility {

    private static final double REGEN_AMOUNT = 1.0; // Half a heart per second
    private static final int REGEN_INTERVAL = 20; // Tick interval (1 second)
    
    private final Map<UUID, BukkitTask> regenTasks = new HashMap<>();
    private final Map<UUID, Boolean> inBiomeStatus = new HashMap<>();
    
    // Valid mushroom biomes
    private static final Set<Biome> MUSHROOM_BIOMES = Set.of(
            Biome.MUSHROOM_FIELDS
    );

    public SymbioticRegenerationAbility() {
        super("mushroom_symbiotic_regeneration", Material.RED_MUSHROOM, "Symbiotic Regeneration",
                List.of(
                        "Passively regenerates health while",
                        "in your team's biome territory.",
                        "<shade-red:500>+0.5♥</shade-red> every second."
                ), 0); // No cooldown needed
    }

    @Override
    protected PassiveAbilityListener createListener(@NotNull IGamePlayer player, @NotNull IGame game) {
        return new RegenerationListener(player, game);
    }

    private class RegenerationListener implements PassiveAbilityListener {
        private final UUID playerUuid;
        private final IGame game;

        public RegenerationListener(IGamePlayer player, IGame game) {
            this.playerUuid = player.getPlayer().getUniqueId();
            this.game = game;
            
            // Initialize status
            inBiomeStatus.put(playerUuid, false);
            
            // Start regeneration task
            startRegenTask(player);
        }

        @EventHandler
        public void onPlayerMove(PlayerMoveEvent event) {
            final Player player = event.getPlayer();
            if (!player.getUniqueId().equals(playerUuid)) return;

            final IGamePlayer gamePlayer = game.findGamePlayer(player);
            if (gamePlayer == null) return;

            final boolean wasInBiome = inBiomeStatus.getOrDefault(playerUuid, false);
            final boolean isInBiome = isInMushroomBiome(player.getLocation(), gamePlayer);
            
            // Update status if changed
            if (wasInBiome != isInBiome) {
                inBiomeStatus.put(playerUuid, isInBiome);
                
                // Notify player
                if (isInBiome) {
                    ((Game) game).getChatService().sendMessage(player, IChatService.MessageSeverity.INFO,
                            "Symbiotic Regeneration activated! You'll regenerate <shade-red:500>0.5♥</shade-red> per second.");
                    
                    // Visual effect
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_FUNGUS_PLACE, 0.5f, 1.2f);
                } else {
                    ((Game) game).getChatService().sendMessage(player, IChatService.MessageSeverity.INFO,
                            "Symbiotic Regeneration deactivated. You've left your biome territory.");
                }
            }
        }
        
        private void startRegenTask(IGamePlayer gamePlayer) {
            final UUID uuid = playerUuid;
            
            // Cancel existing task if any
            if (regenTasks.containsKey(uuid)) {
                regenTasks.get(uuid).cancel();
            }
            
            // Create new regeneration task
            BukkitTask task = BukkitUtils.runTaskTimer(() -> {
                final Player player = gamePlayer.getPlayer();
                if (!player.isOnline()) {
                    regenTasks.get(uuid).cancel();
                    regenTasks.remove(uuid);
                    return;
                }
                
                // Only regenerate if in mushroom biome
                if (inBiomeStatus.getOrDefault(uuid, false) && player.getHealth() < player.getMaxHealth()) {
                    // Apply regeneration
                    double newHealth = Math.min(player.getHealth() + REGEN_AMOUNT, player.getMaxHealth());
                    player.setHealth(newHealth);
                    
                    // Visual effect (occasionally)
                    if (Math.random() < 0.3) {
                        player.getWorld().spawnParticle(
                                Particle.HEART,
                                player.getLocation().add(0, 1, 0),
                                1, 0.3, 0.3, 0.3, 0
                        );
                    }
                }
            }, 0, REGEN_INTERVAL);
            
            regenTasks.put(uuid, task);
        }

        private boolean isInMushroomBiome(Location location, IGamePlayer gamePlayer) {
            // First check if they're in their own team's territory
            if (gamePlayer.getTeam() != null &&
                    gamePlayer.getTeam().getBiome().getId().equals("mushroom")) {
                return true;
            }

            // Alternatively check actual world biome (for natural mushroom fields)
            return MUSHROOM_BIOMES.contains(location.getBlock().getBiome());
        }
    }
    
    @Override
    public void onDeselect(@NotNull IGamePlayer player) {
        super.onDeselect(player);
        
        // Clean up when ability is deselected
        UUID uuid = player.getPlayer().getUniqueId();
        if (regenTasks.containsKey(uuid)) {
            regenTasks.get(uuid).cancel();
            regenTasks.remove(uuid);
        }
        inBiomeStatus.remove(uuid);
    }
}