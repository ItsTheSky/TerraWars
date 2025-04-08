package net.itsthesky.terrawars.core.impl.ability.tundra;

import net.itsthesky.terrawars.api.model.ability.ActiveAbility;
import net.itsthesky.terrawars.api.model.game.IGame;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.core.impl.game.Game;
import net.itsthesky.terrawars.util.BukkitUtils;
import net.itsthesky.terrawars.util.Keys;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class IceBridgeAbility extends ActiveAbility {

    private static final int BRIDGE_LENGTH = 10;
    private static final int BRIDGE_WIDTH = 2;
    private static final int DURATION_SECONDS = 15;
    private static final Material BRIDGE_MATERIAL = Material.BLUE_ICE;

    private final Map<UUID, BridgeInstance> activeBridges = new HashMap<>();

    public IceBridgeAbility() {
        super("tundra_ice_bridge", Material.BLUE_ICE, "Ice Bridge",
                List.of(
                        "Creates a bridge of ice 2 blocks wide",
                        "and up to 10 blocks long in the direction",
                        "you're looking. Lasts for 15 seconds."
                ), 90);
    }

    @Override
    protected boolean execute(@NotNull IGamePlayer player, @NotNull IGame rawGame) {
        final Game game = (Game) rawGame;
        final UUID playerId = player.getPlayer().getUniqueId();
        
        // Remove existing bridge if any
        if (activeBridges.containsKey(playerId)) {
            activeBridges.get(playerId).remove();
            activeBridges.remove(playerId);
        }
        
        // Get player direction and starting position
        final Vector direction = player.getPlayer().getLocation().getDirection().setY(0).normalize();
        final var startLocation = player.getPlayer().getLocation().clone().add(direction.clone().multiply(1.5));
        startLocation.setY(startLocation.getBlockY() - 1); // Place bridge at feet level
        
        // Create the bridge
        final List<Block> bridgeBlocks = new ArrayList<>();
        
        // Calculate the bridge midpoint
        final Vector right = direction.clone().crossProduct(new Vector(0, 1, 0)).normalize();
        
        for (int i = 0; i < BRIDGE_LENGTH; i++) {
            final var bridgePoint = startLocation.clone().add(direction.clone().multiply(i));
            
            for (int w = 0; w < BRIDGE_WIDTH; w++) {
                final var offsetPoint = bridgePoint.clone().add(right.clone().multiply(w - (BRIDGE_WIDTH-1) * 0.5));
                final Block block = offsetPoint.getBlock();
                
                // Skip if block is not air or is already a bridge block
                if (block.getType() != Material.AIR && block.getType() != Material.WATER)
                    continue;
                
                // Store original block type for restoration
                BukkitUtils.editBlockPdc(block, pdc -> {
                    pdc.set(Keys.GAME_PLACED_BLOCK_KEY, PersistentDataType.STRING, playerId.toString());
                });
                
                // Set to ice
                block.setType(BRIDGE_MATERIAL);
                bridgeBlocks.add(block);
            }
        }
        
        // Effects
        player.getPlayer().getWorld().playSound(player.getPlayer().getLocation(), 
                Sound.BLOCK_GLASS_PLACE, 1.0f, 1.0f);
        
        for (Block block : bridgeBlocks) {
            block.getWorld().spawnParticle(
                    Particle.SNOWFLAKE,
                    block.getLocation().add(0.5, 1, 0.5),
                    3, 0.2, 0.1, 0.2, 0
            );
        }
        
        // Create and store bridge instance
        final BridgeInstance bridge = new BridgeInstance(bridgeBlocks, game, player);
        activeBridges.put(playerId, bridge);
        
        game.getChatService().sendMessage(player.getPlayer(), IChatService.MessageSeverity.SUCCESS,
                "Ice Bridge created! It will last for <accent>15 seconds</accent>.");
        
        return true;
    }
    
    private static class BridgeInstance {
        private final List<Block> blocks;
        private final BukkitTask removalTask;
        
        public BridgeInstance(List<Block> blocks, Game game, IGamePlayer player) {
            this.blocks = blocks;
            
            // Schedule bridge removal
            this.removalTask = BukkitUtils.runTaskLater(() -> {
                remove();
                if (player.isOnline()) {
                    game.getChatService().sendMessage(player.getPlayer(), IChatService.MessageSeverity.INFO,
                            "Your Ice Bridge has melted away.");
                }
            }, DURATION_SECONDS * 20L);
        }
        
        public void remove() {
            // Remove the bridge blocks
            for (Block block : blocks) {
                if (block.getType() == BRIDGE_MATERIAL) {
                    block.setType(Material.AIR);
                    
                    // Display a melting effect
                    block.getWorld().spawnParticle(
                            Particle.DRIPPING_WATER,
                            block.getLocation().add(0.5, 0.5, 0.5),
                            5, 0.4, 0.4, 0.4, 0
                    );
                }
            }
            
            // Cancel the task if it's still running
            if (removalTask != null && !removalTask.isCancelled()) {
                removalTask.cancel();
            }
        }
    }
}