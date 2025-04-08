package net.itsthesky.terrawars.core.impl.ability.tundra;

import net.itsthesky.terrawars.api.model.ability.ActiveAbility;
import net.itsthesky.terrawars.api.model.game.IGame;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.core.impl.game.Game;
import net.itsthesky.terrawars.util.BukkitUtils;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FrostyArmorAbility extends ActiveAbility {
    
    private static final float DAMAGE_REDUCTION = 0.85f; // 85% damage reduction
    private static final int DURATION_SECONDS = 10;
    private static final int SLOWNESS_DURATION = 3 * 20; // 3 seconds in ticks
    private static final int SLOWNESS_AMPLIFIER = 3; // Slowness IV
    
    private final Map<UUID, BukkitTask> activeArmors = new HashMap<>();
    private final Map<UUID, EntityDamageListener> listeners = new HashMap<>();
    
    public FrostyArmorAbility() {
        super("tundra_frosty_armor", Material.PACKED_ICE, "Frosty Armor", 
              List.of(
                  "Grants a special ice armor that reduces",
                  "damage by <shade-blue:500>85%</shade-blue> for 10 seconds.",
                  "Attackers are inflicted with Slowness IV",
                  "for 3 seconds when they hit you."
              ), 60);
    }
    
    @Override
    protected boolean execute(@NotNull IGamePlayer player, @NotNull IGame rawGame) {
        final Game game = (Game) rawGame;
        final Player bukkitPlayer = player.getPlayer();
        final UUID playerId = bukkitPlayer.getUniqueId();
        
        // Cancel existing armor if active
        if (activeArmors.containsKey(playerId)) {
            activeArmors.get(playerId).cancel();
            activeArmors.remove(playerId);
            
            if (listeners.containsKey(playerId)) {
                BukkitUtils.unregisterListener(listeners.get(playerId));
                listeners.remove(playerId);
            }
        }
        
        // Create the damage listener
        final EntityDamageListener listener = new EntityDamageListener(playerId, game);
        BukkitUtils.registerListener(listener);
        listeners.put(playerId, listener);
        
        // Visual and sound effects
        bukkitPlayer.getWorld().playSound(bukkitPlayer.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 0.5f);
        bukkitPlayer.getWorld().spawnParticle(
                Particle.SNOWFLAKE,
                bukkitPlayer.getLocation().add(0, 1, 0),
                50, 0.5, 0.8, 0.5, 0.05
        );
        
        // Notify player
        game.getChatService().sendMessage(bukkitPlayer, IChatService.MessageSeverity.SUCCESS,
                "Frosty Armor activated! <shade-blue:500>85%</shade-blue> damage reduction for <accent>10 seconds<text>.");
        startCooldown(player);
        
        // Schedule armor removal
        final BukkitTask task = BukkitUtils.runTaskLater(() -> {
            if (listeners.containsKey(playerId)) {
                BukkitUtils.unregisterListener(listeners.get(playerId));
                listeners.remove(playerId);
            }
            activeArmors.remove(playerId);
            
            if (bukkitPlayer.isOnline()) {
                bukkitPlayer.getWorld().playSound(bukkitPlayer.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.5f, 1.5f);
                game.getChatService().sendMessage(bukkitPlayer, IChatService.MessageSeverity.INFO,
                        "Your Frosty Armor has worn off.");
            }
        }, DURATION_SECONDS * 20L);
        
        activeArmors.put(playerId, task);
        return true;
    }
    
    private static class EntityDamageListener implements Listener {
        private final UUID playerId;
        private final Game game;
        
        public EntityDamageListener(UUID playerId, Game game) {
            this.playerId = playerId;
            this.game = game;
        }
        
        @EventHandler
        public void onEntityDamage(EntityDamageByEntityEvent event) {
            // Check if the player with armor is being damaged
            if (!(event.getEntity() instanceof Player player) || !player.getUniqueId().equals(playerId)) 
                return;
            
            // Apply damage reduction
            event.setDamage(event.getDamage() * (1 - DAMAGE_REDUCTION));
            
            // Apply slowness to attacker if it's a player
            Entity damager = event.getDamager();
            if (damager instanceof Player attacker) {
                // Make sure attacker is not on the same team
                final IGamePlayer attackerPlayer = game.findGamePlayer(attacker);
                final IGamePlayer victimPlayer = game.findGamePlayer(player);
                
                if (attackerPlayer != null && victimPlayer != null && 
                    attackerPlayer.getTeam() != victimPlayer.getTeam()) {
                    
                    // Apply slowness
                    attacker.addPotionEffect(new PotionEffect(
                        PotionEffectType.SLOWNESS,
                        SLOWNESS_DURATION,
                        SLOWNESS_AMPLIFIER,
                        false,
                        true,
                        true
                    ));
                    
                    // Frost effect on the attacker
                    attacker.getWorld().spawnParticle(
                            Particle.SNOWFLAKE,
                            attacker.getLocation().add(0, 1, 0),
                            20, 0.5, 0.5, 0.5, 0.01
                    );
                    
                    // Sound effect
                    attacker.getWorld().playSound(attacker.getLocation(), 
                            Sound.BLOCK_GLASS_BREAK, 0.6f, 1.2f);
                    
                    game.getChatService().sendMessage(attacker, IChatService.MessageSeverity.ERROR,
                            "You've been slowed by <accent>" + player.getName() + "<text>'s Frosty Armor!");
                }
            }
            
            // Visual feedback
            player.getWorld().spawnParticle(
                    Particle.SNOWFLAKE,
                    player.getLocation().add(0, 1, 0),
                    10, 0.5, 0.8, 0.5, 0.01
            );
        }
    }
}