package net.itsthesky.terrawars.core.impl.game;

import lombok.Getter;
import lombok.Setter;
import net.itsthesky.terrawars.api.model.biome.IBiome;
import net.itsthesky.terrawars.api.model.game.IGameHolder;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.api.model.game.IGameTeam;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.util.BukkitUtils;
import net.itsthesky.terrawars.util.Colors;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class GameBiomeNode implements IGameHolder {

    public enum NodeWithinState {
        NO_PLAYERS,
        ONE_PLAYER,
        MULTIPLE_PLAYERS
    }

    private static final float CAPTURE_RADIUS = 2f; // in blocks
    private static final int CAPTURE_TIME = 10; // in seconds

    private final @NotNull Game game;
    private final @NotNull Location location;

    // data
    private @NotNull BukkitTask circleParticleTask;
    private @NotNull List<IGamePlayer> playersInRange;
    private @Nullable IGameTeam owningTeam;
    private @Nullable IGamePlayer capturingPlayer;

    public GameBiomeNode(@NotNull Game game,
                         @NotNull Location nodeLocation) {
        this.game = game;
        this.location = nodeLocation.add(0, 0.75f, 0);
        this.owningTeam = null;
        this.playersInRange = new ArrayList<>();

        this.circleParticleTask = createParticleTask();
        paste("neutral.schem");
    }

    public void changeOwningTeam(@Nullable IGameTeam team) {
        if (team == null) {
            paste("neutral.schem");
            if (this.owningTeam != null)
                this.owningTeam.getCapturedNodes().remove(this);

            this.owningTeam = null;
        } else {
            paste(team.getBiome().getSchematicName());

            if (this.owningTeam != null)
                this.owningTeam.getCapturedNodes().remove(this);

            this.owningTeam = team;
            this.owningTeam.getCapturedNodes().add(this);
        }
    }

    private void paste(String name) {
        game.getSchemService().pasteSchematic(name, location, false);
    }

    private NodeWithinState scanPlayerInRange() {
        playersInRange.clear();

        for (IGamePlayer player : game.getPlayers()) {
            if (player.getPlayer().getLocation().distance(location) <= CAPTURE_RADIUS) {
                playersInRange.add(player);
            }
        }

        if (playersInRange.isEmpty()) {
            capturingPlayer = null;
            return NodeWithinState.NO_PLAYERS;
        } else if (playersInRange.size() == 1) {
            if (capturingPlayer == null || capturingPlayer != playersInRange.getFirst()) {
                capturingPlayer = playersInRange.getFirst();
                lastCaptureTimerMs = System.currentTimeMillis();
                captureProgress = 0;
            }
            return NodeWithinState.ONE_PLAYER;
        } else {
            capturingPlayer = null;
            return NodeWithinState.MULTIPLE_PLAYERS;
        }
    }

    private long lastCaptureTimerMs = 0;
    private int captureProgress = 0;
    public BukkitTask createParticleTask() {
        return BukkitUtils.runTaskTimer(() -> {
            final var state = scanPlayerInRange();

            var color = owningTeam != null
                    ? owningTeam.getBiome().getColor() : Colors.GRAY.get(Colors.SHADE_300);
            if (state == NodeWithinState.NO_PLAYERS)
            {
                color = Colors.GRAY.get(Colors.SHADE_500);
            }
            else if (state == NodeWithinState.ONE_PLAYER)
            {
                color = Colors.LIME.get(Colors.SHADE_500);
                if (owningTeam == null || !capturingPlayer.getTeam().equals(owningTeam)) {
                    if (System.currentTimeMillis() - lastCaptureTimerMs >= 1000) { // only fire this event every second
                        lastCaptureTimerMs = System.currentTimeMillis();
                        game.getChatService().sendTitle(new IChatService.TitleBuilder()
                                .audience(Objects.requireNonNull(capturingPlayer).getPlayer())
                                .title("<accent>Capturing Node ...")
                                .subtitle("<text>Time : <shade-lime:600>" + (CAPTURE_TIME - captureProgress) + "s")
                                .scheme(Colors.LIME)
                                .time(0, 1500, 0));

                        if (captureProgress >= CAPTURE_TIME) {
                            captureNode();
                        } else {
                            captureProgress++;
                        }
                    }
                }
            }
            else if (state == NodeWithinState.MULTIPLE_PLAYERS)
            {
                color = Colors.RED.get(Colors.SHADE_500);
            }

            createParticleCircle(location, CAPTURE_RADIUS, 75,
                    BukkitUtils.convertColor(color));
        }, 10, 5);
    }

    private void captureNode() {
        if (capturingPlayer != null) {
            final var biome = capturingPlayer.getTeam().getBiome();
            game.getChatService().sendTitle(new IChatService.TitleBuilder()
                    .audience(Objects.requireNonNull(capturingPlayer).getPlayer())
                    .title("<accent>Node Captured !")
                    .subtitle("<text>Biome : <shade-lime:600>" + biome.getName())
                    .scheme(biome.getScheme())
                    .time(0, 700, 300));
            game.broadcastMessage(IChatService.MessageSeverity.WARNING,
                    "<text>Biome <shade-lime:600>" + biome.getName() +
                            "<text> has been captured by <shade-lime:600>" +
                            capturingPlayer.getPlayer().getName());
            changeOwningTeam(capturingPlayer.getTeam());
        }
    }

    private void createParticleCircle(Location center, double radius,
                                      int particleCount, Color color) {
        World world = center.getWorld();
        double angleIncrement = 2 * Math.PI / particleCount;

        for (int i = 0; i < particleCount; i++) {
            double angle = i * angleIncrement;

            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);

            Location particleLocation = center.clone().add(x, 0, z);
            Particle.DustOptions dustOptions = new Particle.DustOptions(color, 0.8f);
            world.spawnParticle(Particle.DUST, particleLocation, 1,
                    0, 0, 0, 0, dustOptions);
        }
    }

    public void cleanup() {
        if (circleParticleTask != null)
            circleParticleTask.cancel();
    }
}
