package net.itsthesky.terrawars.core.impl.game;

import net.itsthesky.terrawars.core.gui.ShopKeeperGui;
import net.itsthesky.terrawars.core.gui.TeamUpgradeGui;
import net.itsthesky.terrawars.util.BukkitUtils;
import org.bukkit.Location;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class GameShopEntity {

    public static enum ShopEntityType {
        SHOPKEEPER,
        UPGRADES
    }

    private final GameTeam team;
    private final ShopEntityType type;
    private final Location location;
    private final Listener listener;

    private Villager villager;

    public GameShopEntity(GameTeam team,
                          ShopEntityType type) {
        this.team = team;
        this.type = type;

        this.location = type == ShopEntityType.SHOPKEEPER
                ? team.getConfig().getShopkeeperLocation()
                : team.getConfig().getUpgradesLocation();

        location.getWorld().spawn(location, Villager.class, villager -> {
            villager.setCustomNameVisible(true);
            villager.setVillagerType(team.getBiome().getVillagerType());
            villager.setAI(false);
            villager.setCollidable(false);
            villager.setInvulnerable(true);
            villager.setSilent(true);
            villager.setGravity(true);

            if (type == ShopEntityType.SHOPKEEPER) {
                villager.setProfession(Villager.Profession.LIBRARIAN);
                villager.customName(team.getGame().getChatService()
                        .format("<accent>✦ <base>Shopkeeper <accent>✦", team.getColorScheme()));
            } else if (type == ShopEntityType.UPGRADES) {
                villager.setProfession(Villager.Profession.TOOLSMITH);
                villager.customName(team.getGame().getChatService()
                        .format("<accent>✦ <base>Team Upgrades <accent>✦", team.getColorScheme()));
            }
            this.villager = villager;
        });

        BukkitUtils.registerListener(this.listener = new GameShopListener());
    }

    public void cleanup() {
        if (villager != null)
            villager.remove();
        BukkitUtils.unregisterListener(listener);
    }

    public class GameShopListener implements Listener {

        @EventHandler
        public void onEntityRightClick(@NotNull PlayerInteractEntityEvent event) {
            if (event.getRightClicked() != villager) return;
            final var player = event.getPlayer();
            event.setCancelled(true);

            if (type == ShopEntityType.SHOPKEEPER) {
                final var gui = new ShopKeeperGui(team.getGame(), Objects.requireNonNull(team.getGame().findGamePlayer(player)));
                gui.open(player);
            } else if (type == ShopEntityType.UPGRADES) {
                final var gui = new TeamUpgradeGui(team.getGame(), Objects.requireNonNull(team.getGame().findGamePlayer(player)), null);
                gui.open(player);
            }
        }

    }

}
