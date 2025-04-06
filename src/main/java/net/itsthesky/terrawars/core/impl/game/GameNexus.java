package net.itsthesky.terrawars.core.impl.game;

import com.github.stefvanschie.inventoryframework.util.UUIDTagType;
import net.itsthesky.terrawars.api.model.game.IGameNexus;
import net.itsthesky.terrawars.api.model.game.IGameTeam;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.util.BukkitUtils;
import net.itsthesky.terrawars.util.Keys;
import net.itsthesky.terrawars.util.StringUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Display;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GameNexus implements IGameNexus {

    private final NexusStats stats;
    private final Location location;
    private final GameTeam team;
    private final int maxLevel;

    private final BukkitTask regenTask;

    private NexusCrystal crystal;

    private int level;
    private long lastDamage;

    public GameNexus(@NotNull GameTeam gameTeam) {
        this.location = gameTeam.getConfig().getNexusLocation();
        this.team = gameTeam;

        this.stats = new NexusStats(500, 500, 2, 30);
        this.maxLevel = 3;

        this.level = 1;

        this.crystal = new NexusCrystal();

        BukkitUtils.registerListener(new NexusListener());
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
    public int getMaxLevel() {
        return maxLevel;
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
        this.crystal.destroy();
        if (regenTask != null)
            regenTask.cancel();
    }

    public class NexusCrystal {

        private EnderCrystal crystal;
        private TextDisplay display;

        public NexusCrystal() {
            location.getWorld().spawn(location.add(0, 1, 0), EnderCrystal.class, crystal -> {
                this.crystal = crystal;

                this.crystal.setInvulnerable(true);
                this.crystal.setCustomNameVisible(false);
                this.crystal.setShowingBottom(false);
                this.crystal.getPersistentDataContainer().set(Keys.NEXUS_TEAM_KEY, UUIDTagType.INSTANCE, team.getId());
            });
            location.getWorld().spawn(location.add(0, 2, 0), TextDisplay.class, display -> {
                this.display = display;

                this.display.setBillboard(Display.Billboard.CENTER);
                this.display.setAlignment(TextDisplay.TextAlignment.CENTER);
                this.display.setShadowed(false);
                updateTextDisplay();
            });
        }

        private void updateTextDisplay() {
            // make a cool progress for the hearts using ❤
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
                    "<accent>✦ <text>" + /* getTeam().getBiome().getName() */ "XXX" + " Nexus <shade-slate:500>- <base>Level " + getLevel() + " <accent>✦",
                    hearts,
                    healthInfos
            );

            final var chatService = getGame().getChatService();

            this.display.text(chatService.joinNewLine(lines.stream()
                    .map(line -> chatService.format(line, getTeam().getColorScheme()))
                    .toList()));
        }

        private void destroy() {
            // kill both entities
            if (this.crystal != null) this.crystal.remove();
            if (this.display != null) this.display.remove();
            ;
        }
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

            final var gamePlayer = getGame().findGamePlayer(player);
            if (gamePlayer == null)
                return;

            if (gamePlayer.getTeam().getId().equals(teamId)) { // Same team, just gives a warning!
                getGame().getChatService().sendMessage(player, IChatService.MessageSeverity.ERROR,
                        "You cannot damage your <accent>own nexus<text>!");
            } else {
                lastDamage = System.currentTimeMillis();

                final var dealt = event.getFinalDamage();
                final var newHealth = (int) Math.round(getStats().getHealth() - dealt);
                if (newHealth <= 0) {
                    getGame().getChatService().sendMessage(player, IChatService.MessageSeverity.INFO,
                            "You destroyed the <accent>" + getTeam() + "<text> nexus!");
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

    }
}
