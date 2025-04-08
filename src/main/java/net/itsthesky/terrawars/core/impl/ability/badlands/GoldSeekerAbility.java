package net.itsthesky.terrawars.core.impl.ability.badlands;

import net.itsthesky.terrawars.api.model.ability.PassiveAbility;
import net.itsthesky.terrawars.api.model.game.IGame;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.core.impl.game.Game;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class GoldSeekerAbility extends PassiveAbility {

    private static final int BASE_REDUCTION = 15; // Base 15% damage reduction
    private static final int MAX_GOLD_COUNTED = 10; // Maximum gold ingots counted
    private static final double REDUCTION_PER_GOLD = 2.0; // 2% damage reduction per gold ingot

    public GoldSeekerAbility() {
        super("badlands_gold_seeker", Material.GOLD_INGOT, "Gold Seeker",
                List.of(
                        "Reduce incoming damage based on gold",
                        "in your inventory: <shade-yellow:500>15%</shade-yellow> base +",
                        "<shade-yellow:500>2%</shade-yellow> per gold ingot (max 10 ingots).",
                        "Maximum reduction: <shade-yellow:500>35%</shade-yellow>."
                ), 0); // No explicit cooldown
    }

    @Override
    protected PassiveAbilityListener createListener(@NotNull IGamePlayer player, @NotNull IGame game) {
        return new GoldSeekerListener(player, game);
    }

    private class GoldSeekerListener implements PassiveAbilityListener {
        private final UUID playerUuid;
        private final Game game;

        public GoldSeekerListener(IGamePlayer player, IGame game) {
            this.playerUuid = player.getPlayer().getUniqueId();
            this.game = (Game) game;
        }

        @EventHandler
        public void onPlayerDamage(EntityDamageEvent event) {
            if (!(event.getEntity() instanceof Player player)) return;
            if (!player.getUniqueId().equals(playerUuid)) return;

            final IGamePlayer gamePlayer = game.findGamePlayer(player);
            if (gamePlayer == null) return;

            // Count gold ingots in inventory
            final int goldCount = countGoldIngots(player.getInventory());
            if (goldCount == 0) return; // No gold, no reduction

            // Calculate damage reduction percentage
            final int effectiveGold = Math.min(goldCount, MAX_GOLD_COUNTED);
            final double reductionPercentage = BASE_REDUCTION + (effectiveGold * REDUCTION_PER_GOLD);
            
            // Apply damage reduction
            final double originalDamage = event.getDamage();
            final double reducedDamage = originalDamage * (1 - (reductionPercentage / 100.0));
            event.setDamage(reducedDamage);
            
            // Visual feedback
            player.getWorld().playSound(player.getLocation(), 
                    Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.5f, 1.5f);
            
            // Notify player
            game.getChatService().sendMessage(player, IChatService.MessageSeverity.INFO, 
                    "Gold Seeker absorbed <accent>" + 
                    String.format("%.1f", originalDamage - reducedDamage) + 
                    "<text> damage with <accent>" + effectiveGold + 
                    "<text> gold! (<accent>" + String.format("%.1f", reductionPercentage) + "%<text> reduction)");
        }
        
        private int countGoldIngots(PlayerInventory inventory) {
            int count = 0;
            for (ItemStack item : inventory.getContents()) {
                if (item != null && item.getType() == Material.GOLD_INGOT) {
                    count += item.getAmount();
                }
            }
            return count;
        }
    }
}