package net.itsthesky.terrawars.core.impl.game;

import lombok.Getter;
import net.itsthesky.terrawars.TerraWars;
import net.itsthesky.terrawars.api.model.game.IGame;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.api.model.game.IGameTeam;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.api.services.base.IServiceProvider;
import net.itsthesky.terrawars.api.services.base.Inject;
import net.itsthesky.terrawars.core.events.game.GameStateChangeEvent;
import net.itsthesky.terrawars.util.BukkitUtils;
import net.itsthesky.terrawars.util.Checks;
import net.itsthesky.terrawars.util.Colors;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class Game implements IGame {

    @Inject private IChatService chatService;
    private final IServiceProvider serviceProvider;

    private final Set<GameTeam> teams;
    private final Set<GamePlayer> waitingPlayers;
    private final UUID id;
    private final World world;
    private final Location lobbyLocation;
    private final int maxPlayers;
    private final GameSize size;

    private GameState state;
    private BukkitTask startCountdownTask;

    public Game(@NotNull IServiceProvider serviceProvider,
                @NotNull World world, @NotNull GameSize size,
                @NotNull Location lobbyLocation) {
        serviceProvider.inject(this);
        this.serviceProvider = serviceProvider;

        this.id = UUID.randomUUID();
        this.state = GameState.WAITING;
        this.teams = new HashSet<>();
        this.waitingPlayers = new HashSet<>();

        this.world = world;
        this.size = size;
        this.lobbyLocation = lobbyLocation;

        this.maxPlayers = size.getPlayerPerTeam() * 4;
    }

    //region Getters


    @Override
    public @NotNull Set<IGamePlayer> getWaitingPlayers() {
        if (state != GameState.WAITING)
            throw new IllegalStateException("Cannot get waiting players when not in WAITING state.");

        return Set.copyOf(waitingPlayers);
    }

    @Override
    public @NotNull Set<IGameTeam> getTeams() {
        if (state != GameState.RUNNING)
            throw new IllegalStateException("Cannot get teams when not in RUNNING state.");

        return Set.copyOf(teams);
    }

    //endregion

    @Override
    public void setState(@NotNull GameState state) {
        Checks.notNull(state, "Game state cannot be null");
        if (this.state == GameState.RUNNING && state != GameState.ENDED)
            throw new IllegalStateException("Cannot change game state from RUNNING to " + state + " without ending the game first.");

        final var old = this.state;
        this.state = state;

        switch (this.state) {
            case STARTING -> startStartCountdown();
        }

        BukkitUtils.callEvent(new GameStateChangeEvent(this, old, state));
    }

    @Override
    public boolean tryAddPlayer(@NotNull Player player) {
        Checks.notNull(player, "Player cannot be null");

        if (state != GameState.WAITING)
            throw new IllegalStateException("Cannot add player to game when not in WAITING state.");

        if (waitingPlayers.size() >= maxPlayers)
            return false;

        if (waitingPlayers.stream().anyMatch(p -> p.getOfflinePlayer().getUniqueId().equals(player.getUniqueId())))
            return false;

        final var gamePlayer = new GamePlayer(player, this);
        waitingPlayers.add(gamePlayer);
        player.teleport(lobbyLocation);

        broadcastMessage(IChatService.MessageSeverity.INFO,
                "<base>" + player.getName() + "<text> joined the game. <accent>[<text>" + waitingPlayers.size() + "<accent>/<text>" + maxPlayers + "<accent>]");

        // TODO: remove this so it actually only starts when there's enough players :)
        if (waitingPlayers.size() >= maxPlayers || true)
            setState(GameState.STARTING);
        return true;
    }

    @Override
    public void removePlayer(@NotNull Player player) {
        Checks.notNull(player, "Player cannot be null");
        Checks.isTrue(state != GameState.RUNNING, "Cannot remove player from game when in RUNNING state.");

        if (state == GameState.WAITING) {
            waitingPlayers.removeIf(p -> p.getOfflinePlayer().getUniqueId().equals(player.getUniqueId()));
            broadcastMessage(IChatService.MessageSeverity.INFO,
                    "<base>" + player.getName() + "<text> left the game. <accent>[<text>" + waitingPlayers.size() + "<accent>/<text>" + maxPlayers + "<accent>]");
            cancelCountdownIfNeeded();
        }
    }

    @Override
    public void broadcastMessage(IChatService.@NotNull MessageSeverity severity, @NotNull String message,
                                 @Nullable IGameTeam specificTeam, @Nullable OfflinePlayer sender) {
        final var targets = new ArrayList<Audience>();
        final var builder = new IChatService.MessageBuilder()
                .severity(severity)
                .message(message);
        if (sender != null && sender.isOnline())
            builder.source(sender.getPlayer());

        if (state == GameState.RUNNING) {
            if (specificTeam != null) {
                for (IGamePlayer player : specificTeam.getPlayers()) {
                    if (player.isOnline())
                        targets.add(player.getPlayer());
                }

                builder.scheme(specificTeam.getColorScheme());
            } else {
                for (GameTeam team : teams) {
                    for (IGamePlayer player : team.getPlayers()) {
                        if (player.isOnline())
                            targets.add(player.getPlayer());
                    }
                }
            }
        } else {
            // game is not in a "team" state, we can just send the message to all players
            waitingPlayers.forEach(player -> {
                if (player.isOnline())
                    targets.add(player.getPlayer());
            });
        }

        builder.audience(Audience.audience(targets));
        chatService.sendMessage(builder);
    }

    @Override
    public void broadcastTitle(@NotNull String title, @NotNull String subtitle,
                               Duration fadeIn, Duration stay, Duration fadeOut,
                               @NotNull List<TextColor> scheme,
                               @Nullable IGameTeam specificTeam, @Nullable OfflinePlayer sender) {
        final var targets = new ArrayList<Audience>();
        if (sender != null && sender.isOnline())
            targets.add(sender.getPlayer());

        if (state == GameState.RUNNING) {
            if (specificTeam != null) {
                for (IGamePlayer player : specificTeam.getPlayers()) {
                    if (player.isOnline())
                        targets.add(player.getPlayer());
                }
            } else {
                for (GameTeam team : teams) {
                    for (IGamePlayer player : team.getPlayers()) {
                        if (player.isOnline())
                            targets.add(player.getPlayer());
                    }
                }
            }
        } else {
            waitingPlayers.forEach(player -> {
                if (player.isOnline())
                    targets.add(player.getPlayer());
            });
        }

        chatService.sendTitle(new IChatService.TitleBuilder()
                .title(title)
                .subtitle(subtitle)
                .fadeIn(fadeIn)
                .stay(stay)
                .fadeOut(fadeOut)
                .scheme(scheme)
                .audience(Audience.audience(targets)));
    }

    public void startStartCountdown() {
        if (startCountdownTask != null)
            startCountdownTask.cancel();

        final var countdown = new AtomicInteger(10);
        startCountdownTask = Bukkit.getScheduler().runTaskTimer(TerraWars.instance(), () -> {
            if (countdown.get() <= 0) {
                broadcastMessage(IChatService.MessageSeverity.SUCCESS, "Game started! ☻");
                startCountdownTask.cancel();
                startCountdownTask = null;
                return;
            }

            final int count = countdown.getAndDecrement();
            final var severity = switch (count) {
                case 1, 2, 3 -> IChatService.MessageSeverity.ERROR;
                case 4, 5 -> IChatService.MessageSeverity.WARNING;
                default -> IChatService.MessageSeverity.INFO;
            };

            if (count <= 5) {
                broadcastTitle("Game starting in <accent>" + count + " second" + (count > 1 ? "s" : ""),
                        "<base>Get ready!", Colors.AMBER,
                        Duration.ofSeconds(0), Duration.ofSeconds(1), Duration.ofSeconds(0));
            }

            broadcastMessage(severity, "Game starting in <accent>" + count + " second" + (count > 1 ? "s" : "") + "<text> ...");
        }, 0, 20);
    }

    public void cancelCountdownIfNeeded() {
        if (startCountdownTask != null) {
            startCountdownTask.cancel();
            startCountdownTask = null;
        }
    }
}
