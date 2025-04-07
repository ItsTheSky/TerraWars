package net.itsthesky.terrawars.core.impl.ability.end;

import net.itsthesky.terrawars.api.model.ability.PassiveAbility;
import net.itsthesky.terrawars.api.model.game.IGame;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.core.impl.game.Game;
import net.itsthesky.terrawars.util.BukkitUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class DoubleJumpAbility extends PassiveAbility {

    private static final double JUMP_POWER = 0.65;
    private static final double FORWARD_BOOST = 0.75;

    public DoubleJumpAbility() {
        super("end_double_jump", Material.FEATHER, "Etheral Jump",
                List.of(
                        "Press space while in the air to perform",
                        "a second jump, propelling yourself",
                        "higher and further."
                ), 30);
    }

    @Override
    public void onSelect(@NotNull IGamePlayer player) {
        if (!player.isOnline()) return;
        
        final var bukkitPlayer = player.getPlayer();

        bukkitPlayer.setAllowFlight(true);
        bukkitPlayer.setFlying(false);
        
        // Inform the player
        ((Game) player.getGame()).getChatService().sendMessage(
                bukkitPlayer,
                IChatService.MessageSeverity.INFO,
                "Double Jump ability activated! Press space in mid-air to double jump."
        );
    }

    @Override
    public void onCooldownEnd(@NotNull IGamePlayer player) {
        if (!player.isOnline())
            return;

        player.getPlayer().setAllowFlight(true);
    }

    @Override
    public void onDeselect(@NotNull IGamePlayer player) {
        if (!player.isOnline()) return;
        
        final var bukkitPlayer = player.getPlayer();

        if (bukkitPlayer.getGameMode() == GameMode.SURVIVAL || 
            bukkitPlayer.getGameMode() == GameMode.ADVENTURE) {
            bukkitPlayer.setAllowFlight(false);
            bukkitPlayer.setFlying(false);
        }
    }

    @Override
    protected PassiveAbilityListener createListener(@NotNull IGamePlayer player, @NotNull IGame game) {
        return new DoubleJumpListener(player, game);
    }
    
    private class DoubleJumpListener implements PassiveAbilityListener {
        private final UUID playerUuid;
        private final IGame game;
        
        public DoubleJumpListener(IGamePlayer player, IGame game) {
            this.playerUuid = player.getPlayer().getUniqueId();
            this.game = game;
        }
        
        @EventHandler
        public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
            final Player player = event.getPlayer();
            
            // Check if this is the player with the ability
            if (!player.getUniqueId().equals(playerUuid)) {
                return;
            }
            
            // Find the game player
            final IGamePlayer gamePlayer = game.findGamePlayer(player);
            if (gamePlayer == null) {
                return;
            }
            
            // Cancel the flight event - we don't want them to actually fly
            event.setCancelled(true);
            
            // Check if the ability is on cooldown
            if (isOnCooldown(gamePlayer)) {
                ((Game) game).getChatService().sendMessage(
                    player, 
                    IChatService.MessageSeverity.ERROR,
                    "Double Jump is on cooldown: <accent>" + getRemainingCooldown(gamePlayer) + "s"
                );
                return;
            }
            
            // Get the player's looking direction for the forward boost
            final Vector velocity = player.getLocation().getDirection().multiply(FORWARD_BOOST);
            velocity.setY(JUMP_POWER);
            player.setVelocity(velocity);

            player.setFlying(false);
            player.setAllowFlight(false);

            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0f, 1.5f);
            startCooldown(gamePlayer);
        }
    }
}