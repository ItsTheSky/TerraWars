package net.itsthesky.terrawars.core.impl.ability.mushroom;

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

public class FungalGrowthAbility extends ActiveAbility {

    private static final int WALL_HEIGHT = 4;
    private static final int WALL_WIDTH = 4;
    private static final int DURATION_SECONDS = 5;
    private static final Material WALL_MATERIAL = Material.RED_MUSHROOM_BLOCK;

    private final Map<UUID, WallInstance> activeWalls = new HashMap<>();

    public FungalGrowthAbility() {
        super("mushroom_fungal_growth", Material.RED_MUSHROOM_BLOCK, "Fungal Growth",
                List.of(
                        "Instantly creates a 4x4 wall behind you",
                        "made of mushroom blocks. The wall remains",
                        "for 5 seconds before disappearing."
                ), 60);
    }

    @Override
    protected boolean execute(@NotNull IGamePlayer player, @NotNull IGame rawGame) {
        final Game game = (Game) rawGame;
        final UUID playerId = player.getPlayer().getUniqueId();
        
        // Remove existing wall if any
        if (activeWalls.containsKey(playerId)) {
            activeWalls.get(playerId).remove();
            activeWalls.remove(playerId);
        }
        
        // Get player direction and starting position
        final Vector direction = player.getPlayer().getLocation().getDirection().setY(0).normalize().multiply(-1); // Behind player
        final var startLocation = player.getPlayer().getLocation().clone().add(direction.clone().multiply(2));
        
        // Get perpendicular vector for wall width
        final Vector right = direction.clone().crossProduct(new Vector(0, 1, 0)).normalize();
        
        // Create the wall
        final List<Block> wallBlocks = new ArrayList<>();
        
        for (int h = 0; h < WALL_HEIGHT; h++) {
            for (int w = 0; w < WALL_WIDTH; w++) {
                final var offset = w - (WALL_WIDTH / 2) + 0.5;
                final var wallPoint = startLocation.clone().add(right.clone().multiply(offset)).add(0, h, 0);
                final Block block = wallPoint.getBlock();
                
                // Skip if block is not air
                if (!block.isEmpty() && block.getType() != Material.WATER)
                    continue;
                
                // Store original block type for restoration
                BukkitUtils.editBlockPdc(block, pdc -> {
                    pdc.set(Keys.GAME_PLACED_BLOCK_KEY, PersistentDataType.STRING, playerId.toString());
                });
                
                // Set to mushroom block
                block.setType(WALL_MATERIAL);
                wallBlocks.add(block);
            }
        }
        
        // Effects
        player.getPlayer().getWorld().playSound(player.getPlayer().getLocation(), 
                Sound.BLOCK_FUNGUS_BREAK, 1.0f, 0.8f);
        
        for (Block block : wallBlocks) {
            block.getWorld().spawnParticle(
                    Particle.CRIMSON_SPORE,
                    block.getLocation().add(0.5, 0.5, 0.5),
                    5, 0.4, 0.4, 0.4, 0
            );
        }
        
        // Create and store wall instance
        final WallInstance wall = new WallInstance(wallBlocks, game, player);
        activeWalls.put(playerId, wall);
        
        game.getChatService().sendMessage(player.getPlayer(), IChatService.MessageSeverity.SUCCESS,
                "Fungal Growth wall created! It will last for <accent>5 seconds</accent>.");
        
        return true;
    }
    
    private class WallInstance {
        private final List<Block> blocks;
        private final BukkitTask removalTask;
        
        public WallInstance(List<Block> blocks, Game game, IGamePlayer player) {
            this.blocks = blocks;
            
            // Schedule wall removal
            this.removalTask = BukkitUtils.runTaskLater(() -> {
                remove();
                if (player.isOnline()) {
                    game.getChatService().sendMessage(player.getPlayer(), IChatService.MessageSeverity.INFO,
                            "Your Fungal Growth wall has disappeared.");
                }
            }, DURATION_SECONDS * 20L);
        }
        
        public void remove() {
            // Remove the wall blocks
            for (Block block : blocks) {
                if (block.getType() == WALL_MATERIAL) {
                    block.setType(Material.AIR);
                    
                    // Display a dissolve effect
                    block.getWorld().spawnParticle(
                            Particle.CRIMSON_SPORE,
                            block.getLocation().add(0.5, 0.5, 0.5),
                            10, 0.4, 0.4, 0.4, 0.01
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