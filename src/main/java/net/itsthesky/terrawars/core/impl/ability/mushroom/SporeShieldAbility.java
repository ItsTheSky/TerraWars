package net.itsthesky.terrawars.core.impl.ability.mushroom;

import net.itsthesky.terrawars.api.model.ability.PassiveAbility;
import net.itsthesky.terrawars.api.model.game.IGame;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.core.impl.game.Game;
import net.itsthesky.terrawars.util.BukkitUtils;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SporeShieldAbility extends PassiveAbility {

    private static final float DAMAGE_REDUCTION = 0.15f; // 15% damage reduction
    private static final float DAMAGE_REFLECTION = 1.5f; // 1.5 hearts of reflection damage
    private static final int INVISIBILITY_DURATION = 5 * 20; // 5 seconds in ticks
    private static final int COOLDOWN_SECONDS = 30;

    public SporeShieldAbility() {
        super("mushroom_spore_shield", Material.RED_MUSHROOM, "Spore Shield",
                List.of(
                        "When taking damage, generate a cloud of",
                        "spores that reduces damage by <shade-red:500>15%</shade-red>,",
                        "damages attackers, and grants <shade-gray:500>5 seconds of invisibility</shade-gray>."
                ), COOLDOWN_SECONDS);
    }

    @Override
    protected PassiveAbilityListener createListener(@NotNull IGamePlayer player, @NotNull IGame game) {
        return new SporeShieldListener(player, (Game) game);
    }

    private class SporeShieldListener implements PassiveAbilityListener {
        private final UUID playerUuid;
        private final Game game;

        public SporeShieldListener(IGamePlayer player, Game game) {
            this.playerUuid = player.getPlayer().getUniqueId();
            this.game = game;
        }

        @EventHandler
        public void onPlayerDamage(EntityDamageEvent event) {
            if (!(event.getEntity() instanceof Player player)) return;
            if (!player.getUniqueId().equals(playerUuid)) return;
            
            final IGamePlayer gamePlayer = game.findGamePlayer(player);
            if (gamePlayer == null) return;
            
            // Check cooldown
            if (isOnCooldown(gamePlayer)) {
                return;
            }
            
            // Apply damage reduction
            final double originalDamage = event.getDamage();
            final double reducedDamage = originalDamage * (1 - DAMAGE_REDUCTION);
            event.setDamage(reducedDamage);
            
            // Create spore cloud effect
            player.getWorld().spawnParticle(
                    Particle.CRIMSON_SPORE,
                    player.getLocation().add(0, 1, 0),
                    50, 1.0, 1.0, 1.0, 0.1
            );
            
            // Apply invisibility
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.INVISIBILITY,
                    INVISIBILITY_DURATION,
                    0, // level 1
                    false, // no ambient particles
                    false, // no particles
                    true // show icon in inventory
            ));
            
            // Sound effect
            player.getWorld().playSound(
                    player.getLocation(),
                    Sound.BLOCK_FUNGUS_BREAK,
                    1.0f, 0.8f
            );
            
            // Reflect damage for melee attacks
            if (event instanceof EntityDamageByEntityEvent damageByEntityEvent) {
                if (damageByEntityEvent.getDamager() instanceof Player attacker) {
                    // Make sure attacker is not on the same team
                    final IGamePlayer attackerPlayer = game.findGamePlayer(attacker);
                    if (attackerPlayer != null && attackerPlayer.getTeam() != gamePlayer.getTeam()) {
                        // Deal reflection damage to attacker
                        attacker.damage(DAMAGE_REFLECTION, player);
                        
                        // Spore effect on attacker
                        attacker.getWorld().spawnParticle(
                                Particle.CRIMSON_SPORE,
                                attacker.getLocation().add(0, 1, 0),
                                20, 0.5, 0.5, 0.5, 0.05
                        );
                        
                        game.getChatService().sendMessage(attacker, IChatService.MessageSeverity.ERROR,
                                "You were damaged by <accent>" + player.getName() + "<text>'s Spore Shield!");
                    }
                }
            }
            
            // Notify player
            game.getChatService().sendMessage(player, IChatService.MessageSeverity.SUCCESS,
                    "Spore Shield activated! Damage reduced by <accent>" + 
                    String.format("%.1f", originalDamage - reducedDamage) + 
                    "<text> and you're invisible for <accent>5 seconds<text>!");
            
            // Start cooldown
            startCooldown(gamePlayer);
        }
    }
}