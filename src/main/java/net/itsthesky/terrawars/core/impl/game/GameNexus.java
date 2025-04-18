package net.itsthesky.terrawars.core.impl.game;

import com.github.stefvanschie.inventoryframework.util.UUIDTagType;
import lombok.Getter;
import net.itsthesky.terrawars.api.model.game.IGameNexus;
import net.itsthesky.terrawars.api.model.game.IGameTeam;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.util.BukkitUtils;
import net.itsthesky.terrawars.util.Keys;
import net.itsthesky.terrawars.util.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GameNexus implements IGameNexus {

    private static final int NEXUS_PROTECTION_RANGE = 3; // in blocks

    private final NexusStats stats;
    private final Location location;
    private final GameTeam team;

    private final BukkitTask regenTask;
    private final NexusListener listener;

    private NexusCrystal crystal;

    @Getter
    private boolean isDestroyed;
    private int level;
    private long lastDamage;

    public GameNexus(@NotNull GameTeam gameTeam) {
        this.location = gameTeam.getConfig().getNexusLocation();
        this.team = gameTeam;

        this.isDestroyed = false;

        this.stats = new NexusStats(500, 500,
                5, 60);
        this.level = 1;

        this.crystal = new NexusCrystal();

        BukkitUtils.registerListener(this.listener = new NexusListener());
        regenTask = BukkitUtils.runTaskTimer(() -> {
            if (System.currentTimeMillis() - lastDamage > stats.getRegenDelay() * 1000L) {
                if (stats.getHealth() < stats.getMaxHealth()) {
                    stats.setHealth(stats.getHealth() + stats.getRegenPerSec());
                    if (stats.getHealth() > stats.getMaxHealth())
                        stats.setHealth(stats.getMaxHealth());

                    crystal.updateTextDisplay();
                    getGame().getWorld().playSound(
                            getLocation(),
                            Sound.BLOCK_BREWING_STAND_BREW,
                            0.2f, 1f
                    );
                    getGame().getWorld().spawnParticle(
                            Particle.HEART,
                            getLocation().clone().add(0, -1, 0),
                            15,
                            0.5, 0.5, 0.5,
                            0
                    );
                }
            }
        }, 20, 20);
    }

    @Override
    public @NotNull IGameTeam getTeam() {
        return team;
    }

    @Override
    public @NotNull Location getLocation() {
        return location;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public @NotNull NexusStats getStats() {
        return stats;
    }

    @Override
    public Game getGame() {
        return team.getGame();
    }

    public void cleanup() {
        this.crystal.cleanup();
        if (this.regenTask != null) this.regenTask.cancel();
        if (this.listener != null) BukkitUtils.unregisterListener(this.listener);
    }

    public void destroyNexus() {
        this.isDestroyed = true;
        this.crystal.destroy();
        this.regenTask.cancel();
        BukkitUtils.unregisterListener(this.listener);

        getGame().getWorld().playSound(
                getLocation(),
                Sound.ENTITY_ENDER_DRAGON_DEATH,
                1f, 1f
        );
        getGame().getWorld().spawnParticle(
                Particle.EXPLOSION,
                getLocation(),
                15,
                0.5, 0.5, 0.5,
                0
        );
    }

    public class NexusCrystal {

        private EnderCrystal crystal;
        private TextDisplay display;
        private BlockDisplay destroyedDisplay;

        public NexusCrystal() {
            location.getWorld().spawn(location.clone().add(0, 1, 0), EnderCrystal.class, crystal -> {
                this.crystal = crystal;

                this.crystal.setInvulnerable(false);
                this.crystal.setCustomNameVisible(false);
                this.crystal.setShowingBottom(false);
                this.crystal.getPersistentDataContainer().set(Keys.NEXUS_TEAM_KEY, UUIDTagType.INSTANCE, team.getId());
            });
            location.getWorld().spawn(location.clone().add(0, 3, 0), TextDisplay.class, display -> {
                this.display = display;

                this.display.setBillboard(Display.Billboard.CENTER);
                this.display.setAlignment(TextDisplay.TextAlignment.CENTER);
                this.display.setShadowed(false);
                updateTextDisplay();
            });
        }

        private void updateTextDisplay() {
            if (!isDestroyed) {
                final var filledColor = "<shade-red:500>";
                final var emptyColor = "<shade-slate:500>";
                final var maxHearts = 10;
                final var progress = StringUtils.generateProgressBar(
                        stats.getHealth(),
                        stats.getMaxHealth(),
                        maxHearts,
                        filledColor + "❤",
                        emptyColor + "❤"
                );
                final var percentage = Math.round((float) stats.getHealth() / stats.getMaxHealth() * 100);
                final var hearts = progress + " <shade-slate:500>- <shade-red:200>" + percentage + "%";

                final var healthInfos = "<shade-red:200>" + stats.getHealth() + "/" + stats.getMaxHealth() + " <shade-red:500>❤ <shade-slate:500>- <shade-rose:500>" + stats.getRegenPerSec() + " ❤/s";

                final List<String> lines = List.of(
                        "<accent>✦ <text>" + getTeam().getBiome().getName() + " Nexus <shade-slate:500>- <base>Level " + getLevel() + " <accent>✦",
                        hearts,
                        healthInfos
                );

                final var chatService = getGame().getChatService();

                this.display.text(chatService.joinNewLine(lines.stream()
                        .map(line -> chatService.format(line, getTeam().getColorScheme()))
                        .toList()));
            } else {
                final var lines = List.of(
                        "<shade-slate:500>✦ <accent>" + getTeam().getBiome().getName() + " Nexus <shade-slate:500>- <base>Level " + getLevel() + " <shade-slate:500>✦",
                        "<shade-red:500>\uD83D\uDC80 <shade-slate:500>- <shade-red:200>DESTROYED <shade-slate:500>- <shade-red:500>\uD83D\uDC80"
                );

                final var chatService = getGame().getChatService();
                this.display.text(chatService.joinNewLine(lines.stream()
                        .map(line -> chatService.format(line, getTeam().getColorScheme()))
                        .toList()));
            }
        }

        private void cleanup() {
            if (this.crystal != null) this.crystal.remove();
            if (this.display != null) this.display.remove();
            if (this.destroyedDisplay != null) this.destroyedDisplay.remove();
        }

        private void destroy() {
            if (this.crystal != null) {
                this.crystal.remove();
                this.crystal = null;
            }

            this.destroyedDisplay = location.getWorld().spawn(location.clone().add(0, 1, 0), BlockDisplay.class, display -> {
                this.destroyedDisplay = display;

                this.destroyedDisplay.setBlock(Material.BEDROCK.createBlockData());
            });

            updateTextDisplay();
        }
    }

    public void updateDisplay() {
        if (this.crystal != null) this.crystal.updateTextDisplay();
    }

    public class NexusListener implements Listener {

        @EventHandler
        public void onNexusDamage(@NotNull EntityDamageByEntityEvent event) {
            final var entity = event.getEntity();
            if (!(entity instanceof final EnderCrystal crystal))
                return;

            final var teamId = crystal.getPersistentDataContainer().get(Keys.NEXUS_TEAM_KEY, UUIDTagType.INSTANCE);
            if (!team.getId().equals(teamId))
                return;

            event.setCancelled(true);
            final var sourceEntity = event.getDamageSource().getCausingEntity();
            if (sourceEntity == null)
                return;
            if (!(sourceEntity instanceof final Player player))
                return;
            if (event.getFinalDamage() <= 1.5d || System.currentTimeMillis() - lastDamage < 12)
                return;

            final var gamePlayer = getGame().findGamePlayer(player);
            if (gamePlayer == null)
                return;

            if (gamePlayer.getTeam().getId().equals(teamId)) {
                getGame().getChatService().sendMessage(player, IChatService.MessageSeverity.ERROR,
                        "You cannot damage your <accent>own nexus<text>!");
            } else {
                lastDamage = System.currentTimeMillis();

                final var dealt = event.getFinalDamage();
                final var newHealth = (int) Math.round(getStats().getHealth() - dealt);
                if (newHealth <= 0) {
                    getGame().getChatService().sendMessage(player, IChatService.MessageSeverity.INFO,
                            "You destroyed the <accent>" + getTeam().getBiome().getName() + "<text> nexus!");
                    getGame().broadcastMessage(getTeam().getColorScheme(),
                            "The <accent>" + getTeam().getBiome().getName() + "<text> nexus has been destroyed by <accent>" + player.getName() + "<text>!");
                    destroyNexus();
                } else {
                    getGame().getWorld().playSound(
                            getLocation(),
                            Sound.ENTITY_PHANTOM_HURT,
                            1f, 1f
                    );
                    getStats().setHealth(newHealth);
                    GameNexus.this.crystal.updateTextDisplay();
                }
            }
        }

        @EventHandler
        public void onBlockPlace(@NotNull BlockPlaceEvent event) {
            final var block = event.getBlock();
            if (block.getLocation().distance(location) > NEXUS_PROTECTION_RANGE)
                return;

            final var player = event.getPlayer();
            final var gamePlayer = getGame().findGamePlayer(player);
            if (gamePlayer == null)
                return;

            if (gamePlayer.getTeam().getId().equals(team.getId())) {
                event.setCancelled(true);
                getGame().getChatService().sendMessage(player, IChatService.MessageSeverity.ERROR,
                        "You cannot build near your <accent>own nexus<text>!");
            } else {
                event.setCancelled(true);
                getGame().getChatService().sendMessage(player, IChatService.MessageSeverity.ERROR,
                        "You cannot build near the <accent>" + getTeam() + "<text> nexus!");
            }
        }

    }
}
