package net.itsthesky.terrawars.core.impl.ability.badlands;

import net.itsthesky.terrawars.api.model.ability.PassiveAbility;
import net.itsthesky.terrawars.api.model.game.IGame;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.core.impl.game.Game;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class EarthyCamouflageAbility extends PassiveAbility {

    private static final float DAMAGE_REDUCTION = 0.75f; // 75% damage reduction
    private static final int DAMAGE_REDUCTION_COOLDOWN = 30; // 30 seconds

    // Valid badlands/mesa biomes
    private static final Set<Biome> BADLANDS_BIOMES = Set.of(
            Biome.BADLANDS, Biome.ERODED_BADLANDS, Biome.WOODED_BADLANDS
    );

    public EarthyCamouflageAbility() {
        super("badlands_earthy_camouflage", Material.ORANGE_CONCRETE_POWDER, "Earthy Camouflage",
                List.of(
                        "Move faster (Speed II) in your biome.",
                        "Every hit (within cooldowns) taken in",
                        "your biome reduces damage by <shade-amber:500>75%</shade-amber>."
                ), DAMAGE_REDUCTION_COOLDOWN);
    }

    @Override
    protected PassiveAbilityListener createListener(@NotNull IGamePlayer player, @NotNull IGame game) {
        return new CamouflageListener(player, game);
    }

    private class CamouflageListener implements PassiveAbilityListener {
        private final UUID playerUuid;
        private final IGame game;
        private boolean isInBiome = false;

        public CamouflageListener(IGamePlayer player, IGame game) {
            this.playerUuid = player.getPlayer().getUniqueId();
            this.game = game;
        }

        @EventHandler
        public void onPlayerMove(PlayerMoveEvent event) {
            final Player player = event.getPlayer();
            if (!player.getUniqueId().equals(playerUuid)) return;

            final IGamePlayer gamePlayer = game.findGamePlayer(player);
            if (gamePlayer == null) return;

            final boolean wasInBiome = isInBiome;
            isInBiome = isInBadlandsBiome(player.getLocation(), gamePlayer);

            // If player just entered their biome
            if (!wasInBiome && isInBiome) {
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.SPEED,
                        Integer.MAX_VALUE,
                        1, // Speed II
                        false,
                        false,
                        true
                ));
                player.playSound(player.getLocation(), Sound.BLOCK_SAND_STEP, 0.5f, 1.0f);
                ((Game) game).getChatService().sendMessage(player, IChatService.MessageSeverity.INFO,
                        "Earthy Camouflage activated! You feel faster in your natural environment.");
            }
            // If player just left their biome
            else if (wasInBiome && !isInBiome) {
                player.removePotionEffect(PotionEffectType.SPEED);
                ((Game) game).getChatService().sendMessage(player, IChatService.MessageSeverity.INFO,
                        "Earthy Camouflage deactivated. You've left your biome territory.");
            }
        }

        @EventHandler
        public void onPlayerDamage(EntityDamageEvent event) {
            if (!(event.getEntity() instanceof Player player)) return;
            if (!player.getUniqueId().equals(playerUuid)) return;

            final IGamePlayer gamePlayer = game.findGamePlayer(player);
            if (gamePlayer == null) return;

            // Check if player is in their biome
            if (!isInBadlandsBiome(player.getLocation(), gamePlayer)) return;

            // Check if damage reduction is on cooldown
            if (isOnCooldown(gamePlayer))
                return;

            // Apply damage reduction
            final double originalDamage = event.getDamage();
            final double reducedDamage = originalDamage * (1 - DAMAGE_REDUCTION);
            event.setDamage(reducedDamage);

            // Set cooldown
            startCooldown(gamePlayer);

            // Visual and sound effects
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_SAND_BREAK, 1.0f, 0.5f);

            // Notify player
            ((Game) game).getChatService().sendMessage(player, IChatService.MessageSeverity.SUCCESS,
                    "Your Earthy Camouflage absorbed <accent>" +
                            String.format("%.1f", originalDamage - reducedDamage) + "/" +
                            String.format("%.1f", originalDamage) +
                            "<text> damage!");
        }

        private boolean isInBadlandsBiome(Location location, IGamePlayer gamePlayer) {
            // First check if they're in their own team's territory
            if (gamePlayer.getTeam() != null &&
                    gamePlayer.getTeam().getBiome().getId().equals("badlands")) {
                return true;
            }

            // Alternatively check actual world biome (for natural badlands)
            return BADLANDS_BIOMES.contains(location.getBlock().getBiome());
        }
    }

    @Override
    public void onDeselect(@NotNull IGamePlayer player) {
        player.getPlayer().removePotionEffect(PotionEffectType.SPEED);
    }
}