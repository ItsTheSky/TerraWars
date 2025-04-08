package net.itsthesky.terrawars.core.impl.ability.end;

import lombok.Getter;
import net.itsthesky.terrawars.api.model.ability.PassiveAbility;
import net.itsthesky.terrawars.api.model.game.IGame;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.core.impl.game.Game;
import net.itsthesky.terrawars.util.BukkitUtils;
import net.itsthesky.terrawars.util.Colors;
import net.itsthesky.terrawars.util.ItemBuilder;
import net.itsthesky.terrawars.util.Keys;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class RiftTeleportationAbility extends PassiveAbility {

    private static final int MAX_PORTALS = 2;
    private static final int PORTAL_PARTICLE_INTERVAL = 5; // ticks
    private static final double PORTAL_HEIGHT_OFFSET = 0.5;
    private static final int MINIMAL_PORTAL_DISTANCE = 5;
    private static final int MAXIMAL_PORTAL_DISTANCE = 30;

    private static final NamespacedKey RIFT_GUN_KEY = new NamespacedKey(Keys.NAMESPACE, "rift_gun");

    private final Map<UUID, List<PortalInstance>> playerPortals = new HashMap<>();

    public RiftTeleportationAbility() {
        super("end_rift_teleportation", Material.ENDER_PEARL, "Rift Teleportation",
                List.of(
                        "Adds a Rift Gun to your inventory that",
                        "creates linked portals to teleport between.",
                        "Right-click to place portals, right-click a",
                        "portal to teleport, drop the gun to remove portals."
                ), 60);
    }

    @Override
    public void onSelect(@NotNull IGamePlayer player) {
        super.onSelect(player);

        // Give player the Rift Gun item
        if (player.isOnline()) {
            giveRiftGun(player.getPlayer());
        }
    }

    @Override
    public void onDeselect(@NotNull IGamePlayer player) {
        super.onDeselect(player);

        // Remove Rift Gun and portals
        if (player.isOnline()) {
            removeRiftGun(player.getPlayer());
            removeAllPortals(player.getPlayer().getUniqueId());
        }
    }

    private void giveRiftGun(Player player) {
        // Check if player already has a Rift Gun
        if (hasRiftGun(player)) return;

        // Create Rift Gun item
        final ItemStack riftGun = new ItemBuilder(Material.ENDER_EYE)
                .name("<accent><b>✦</b> <base>Rift Gun <accent><b>✦</b>")
                .lore(Colors.PURPLE, List.of(
                        "<base>• <i><text>Right-click to create portals.",
                        "<base>• <i><text>Right-click on a portal to teleport.",
                        "<base>• <i><text>Drop to remove all portals."
                ))
                .setCustomData(RIFT_GUN_KEY, PersistentDataType.BOOLEAN, true)
                .cleanLore()
                .glow()
                .getItem();

        player.getInventory().addItem(riftGun);
    }

    private boolean hasRiftGun(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isRiftGun(item)) {
                return true;
            }
        }
        return false;
    }

    private boolean isRiftGun(ItemStack item) {
        if (item == null) return false;
        return item.getItemMeta() != null &&
                item.getItemMeta().getPersistentDataContainer().has(RIFT_GUN_KEY, PersistentDataType.BOOLEAN);
    }

    private void removeRiftGun(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isRiftGun(item)) {
                player.getInventory().remove(item);
            }
        }
    }

    private void removeAllPortals(UUID playerId) {
        List<PortalInstance> portals = playerPortals.get(playerId);
        if (portals != null) {
            for (PortalInstance portal : portals) {
                portal.destroy();
            }
            playerPortals.remove(playerId);
        }
    }

    private static class PortalInstance {
        private final Location location;
        @Getter
        private final ArmorStand armorStand;
        private final BukkitTask particleTask;

        public PortalInstance(Location location) {
            this.location = location.clone();

            // Create armor stand for visual representation
            this.armorStand = (ArmorStand) location.getWorld().spawnEntity(
                    location.clone().add(0, -PORTAL_HEIGHT_OFFSET, 0),
                    EntityType.ARMOR_STAND);

            // Configure armor stand
            this.armorStand.setInvisible(true);
            this.armorStand.setInvulnerable(true);
            this.armorStand.setGravity(false);
            this.armorStand.setBasePlate(false);
            this.armorStand.setMarker(true);
            this.armorStand.setCustomNameVisible(true);
            this.armorStand.customName(BukkitUtils.chat().format("<accent>☄ <base>Rift Portal <accent>☄", Colors.PURPLE));

            // Add purple armor to make it more visible
            final ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
            final LeatherArmorMeta meta = (LeatherArmorMeta) chestplate.getItemMeta();
            meta.setColor(Color.fromRGB(128, 0, 128)); // Purple
            chestplate.setItemMeta(meta);
            this.armorStand.getEquipment().setChestplate(chestplate);

            // Create particle effect
            this.particleTask = BukkitUtils.runTaskTimer(() -> {
                location.getWorld().spawnParticle(
                        Particle.PORTAL,
                        location.clone().add(0, 1, 0),
                        10, 0.5, 1, 0.5, 0.01
                );
                location.getWorld().spawnParticle(
                        Particle.END_ROD,
                        location.clone().add(0, 1.5, 0),
                        3, 0.2, 0.5, 0.2, 0.01
                );
            }, 0, PORTAL_PARTICLE_INTERVAL);
        }

        public void destroy() {
            armorStand.remove();
            particleTask.cancel();
        }

        public Location getLocation() {
            return location.clone();
        }

    }

    @Override
    protected PassiveAbilityListener createListener(@NotNull IGamePlayer player, @NotNull IGame game) {
        return new RiftGunListener(player, (Game) game);
    }

    private class RiftGunListener implements PassiveAbilityListener {
        private final UUID playerUuid;
        private final Game game;
        private final IGamePlayer gamePlayer;

        public RiftGunListener(IGamePlayer player, Game game) {
            this.playerUuid = player.getPlayer().getUniqueId();
            this.game = game;
            this.gamePlayer = player;
        }

        @EventHandler
        public void onPlayerInteract(PlayerInteractEvent event) {
            final Player player = event.getPlayer();
            if (!player.getUniqueId().equals(playerUuid))
                return;

            final ItemStack item = event.getItem();
            if (item == null || !isRiftGun(item)) return;

            // Cancel the event to prevent normal item usage
            event.setCancelled(true);
            if (event.getAction() != Action.RIGHT_CLICK_AIR)
                return;

            // Check if player is in their biome for portal creation
            if (gamePlayer.getTeam() == null ||
                    !gamePlayer.getTeam().getBiome().getId().equals("end")) {
                game.getChatService().sendMessage(player, IChatService.MessageSeverity.ERROR,
                        "You can only create portals in your own biome!");
                return;
            }

            // Initialize portal list if needed
            List<PortalInstance> portals = playerPortals.computeIfAbsent(playerUuid, k -> new ArrayList<>());

            // Check if player clicked on a portal
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                // Check for portals nearby
                for (List<PortalInstance> playerPortalList : playerPortals.values()) {
                    for (PortalInstance portal : playerPortalList) {
                        // Check if player clicked on or near a portal
                        if (portal.getLocation().distance(player.getLocation()) < 2.0) {
                            // Find the other portal of this player
                            PortalInstance targetPortal = null;
                            for (PortalInstance playerPortal : portals) {
                                if (playerPortal != portal) {
                                    targetPortal = playerPortal;
                                    break;
                                }
                            }

                            // If there's a target portal, teleport
                            if (targetPortal != null) {
                                if (isOnCooldown(gamePlayer)) {
                                    game.getChatService().sendMessage(player, IChatService.MessageSeverity.ERROR,
                                            "You need to wait before teleporting through the rift again!");
                                    return;
                                }
                                // Teleport player
                                final Location destination = targetPortal.getLocation().clone()
                                        .setDirection(player.getLocation().getDirection());

                                player.teleport(destination);

                                // Effects
                                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                                player.getWorld().spawnParticle(
                                        Particle.REVERSE_PORTAL,
                                        player.getLocation(),
                                        30, 0.5, 1, 0.5, 0.05
                                );

                                game.getChatService().sendMessage(player, IChatService.MessageSeverity.SUCCESS,
                                        "Teleported through the rift!");
                                startCooldown(gamePlayer);
                                return;
                            } else {
                                game.getChatService().sendMessage(player, IChatService.MessageSeverity.ERROR,
                                        "You need a second portal to teleport!");
                                return;
                            }
                        }
                    }
                }
            }

            // If not clicking on portal, create a new one (if we don't have max already)
            if (portals.size() < MAX_PORTALS) {
                // Place portal at player's location
                final Location portalLoc = player.getLocation().toBlockLocation().add(0.5, 0, 0.5);
                if (portals.size() == 1) {
                    final double distance = portals.getFirst().getLocation().distance(portalLoc);
                    if (distance > MAXIMAL_PORTAL_DISTANCE) {
                        game.getChatService().sendMessage(player, IChatService.MessageSeverity.ERROR,
                                "The portals are too far apart! Maximum distance is <accent>" + MAXIMAL_PORTAL_DISTANCE + " blocks.");
                        return;
                    } else if (distance < MINIMAL_PORTAL_DISTANCE) {
                        game.getChatService().sendMessage(player, IChatService.MessageSeverity.ERROR,
                                "The portals are too close! Minimum distance is <accent>" + MINIMAL_PORTAL_DISTANCE + " blocks.");
                        return;
                    }
                }

                // Create portal
                final PortalInstance portal = new PortalInstance(portalLoc);
                portals.add(portal);

                // Effects
                player.playSound(portalLoc, Sound.BLOCK_PORTAL_TRIGGER, 0.5f, 1.5f);

                // Message
                if (portals.size() == 1) {
                    game.getChatService().sendMessage(player, IChatService.MessageSeverity.SUCCESS,
                            "First rift portal created! Place a second one to complete the link.");
                } else {
                    game.getChatService().sendMessage(player, IChatService.MessageSeverity.SUCCESS,
                            "Second rift portal created! The link is now active.");
                }
            } else {
                game.getChatService().sendMessage(player, IChatService.MessageSeverity.WARNING,
                        "You already have two portals! Right-click near a portal to teleport or drop the Rift Gun to remove them.");
            }
        }

        @EventHandler
        public void onItemDrop(PlayerDropItemEvent event) {
            final Player player = event.getPlayer();
            if (!player.getUniqueId().equals(playerUuid))
                return;

            final ItemStack item = event.getItemDrop().getItemStack();
            if (!isRiftGun(item))
                return;

            // Cancel the drop
            event.setCancelled(true);

            // Remove all portals
            removeAllPortals(playerUuid);

            // Feedback
            player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.5f, 2.0f);
            game.getChatService().sendMessage(player, IChatService.MessageSeverity.SUCCESS,
                    "All rift portals have been removed.");
        }
    }
}