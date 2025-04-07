package net.itsthesky.terrawars.core.impl.game;

import com.github.stefvanschie.inventoryframework.util.UUIDTagType;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.Getter;
import net.itsthesky.terrawars.TerraWars;
import net.itsthesky.terrawars.api.model.ability.AbilityType;
import net.itsthesky.terrawars.api.model.ability.ActiveAbility;
import net.itsthesky.terrawars.api.model.ability.PassiveAbility;
import net.itsthesky.terrawars.api.model.game.IGame;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.api.model.game.IGameTeam;
import net.itsthesky.terrawars.api.services.IBiomeService;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.api.services.base.IServiceProvider;
import net.itsthesky.terrawars.api.services.base.Inject;
import net.itsthesky.terrawars.core.config.GameConfig;
import net.itsthesky.terrawars.core.events.game.GameStateChangeEvent;
import net.itsthesky.terrawars.util.BukkitUtils;
import net.itsthesky.terrawars.util.Checks;
import net.itsthesky.terrawars.util.Colors;
import net.itsthesky.terrawars.util.Keys;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class Game implements IGame {

    @Inject private IChatService chatService;
    @Inject private IBiomeService biomeService;
    private final IServiceProvider serviceProvider;

    private final Map<Location, Block> placedBlocks;
    private final GameConfig config;
    private final List<GameTeam> teams;
    private final Set<GamePlayer> waitingPlayers;
    private final Set<GameGenerator> generators;
    private final UUID id;
    private final int maxPlayers;

    private GameState state;
    private BukkitTask startCountdownTask;

    public Game(@NotNull IServiceProvider serviceProvider,
                @NotNull GameConfig config) {
        serviceProvider.inject(this);
        this.serviceProvider = serviceProvider;
        this.config = config;

        BukkitUtils.registerListener(new GameListener());

        this.id = UUID.randomUUID();
        this.state = GameState.WAITING;
        this.teams = new ArrayList<>();
        this.waitingPlayers = new HashSet<>();
        this.placedBlocks = new HashMap<>();
        this.generators = new HashSet<>();

        this.maxPlayers = this.config.getGameSize().getPlayerPerTeam() * 4;
    }

    //region Getters

    @Override
    public @NotNull Set<IGamePlayer> getWaitingPlayers() {
        if (state != GameState.WAITING)
            throw new IllegalStateException("Cannot get waiting players when not in WAITING state.");

        return Set.copyOf(waitingPlayers);
    }

    @Override
    public @NotNull GameSize getSize() {
        return config.getGameSize();
    }

    @Override
    public @NotNull World getWorld() {
        return this.config.getWorld();
    }

    @Override
    public @NotNull Location getLobbyLocation() {
        return this.config.getLobby();
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

        player.teleport(getLobbyLocation());
        player.getInventory().clear();

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
    public void cleanupGame() {
        for (GameTeam team : teams)
            team.cleanup();

        for (Block block : placedBlocks.values()) {
            if (block.getLocation().getWorld() != getWorld())
                continue;

            block.setType(Material.AIR);
        }

        for (var generator : generators)
            generator.cleanup();
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
                broadcastMessage(IChatService.MessageSeverity.SUCCESS, "Game started!");
                startCountdownTask.cancel();
                startCountdownTask = null;
                setupStartedGame();
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
                        Duration.ofSeconds(0), Duration.ofMillis(1100), Duration.ofSeconds(0));
            }

            broadcastMessage(severity, "Game starting in <accent>" + count + " second" + (count > 1 ? "s" : "") + "<text> ...");
        }, 0, 20);
    }

    public void setupStartedGame() {
        if (this.startCountdownTask != null) {
            this.startCountdownTask.cancel();
            this.startCountdownTask = null;
        }

        this.teams.clear();
        for (final var cfg : config.getTeams()) {
            final var team = new GameTeam(cfg, this);
            teams.add(team);
        }

        final var players = new ArrayList<>(waitingPlayers);
        Collections.shuffle(players);

        final var remainingPlayers = new ArrayList<>(players);

        while (!remainingPlayers.isEmpty()) {
            for (GameTeam team : teams) {
                if (remainingPlayers.isEmpty())
                    break;
                final var player = remainingPlayers.removeFirst();
                if (!team.tryAddPlayer(player))
                    remainingPlayers.add(player);
            }
        }

        for (GameTeam team : teams) {
            for (IGamePlayer player : team.getPlayers()) {
                if (player.isOnline()) {
                    player.getPlayer().teleport(team.getConfig().getSpawnLocation().add(0, 1, 0).toCenterLocation());
                    player.setupHotbar(true);
                    player.refreshArmor();
                }
            }
        }

        // Setup generators
        for (var generator : config.getGenerators()) {
            final var gameGenerator = new GameGenerator(this, generator);
            generators.add(gameGenerator);
        }

        for (var team : teams) {
            final var generator = new GameGenerator(this, team.getConfig().getGeneratorLocation());
            generators.add(generator);
        }

        setState(GameState.RUNNING);
    }

    public void cancelCountdownIfNeeded() {
        if (startCountdownTask != null) {
            startCountdownTask.cancel();
            startCountdownTask = null;
        }
    }

    //region Game Listener!

    public class GameListener implements Listener {

        @EventHandler
        public void onAsyncChat(AsyncChatEvent event) {
            if (state == GameState.WAITING) {
                event.message(chatService.format("<accent><player><base> <b>→</b> <text><message>",
                        Colors.SLATE, TagResolver.resolver(
                                Placeholder.component("player", event.getPlayer().displayName()),
                                Placeholder.component("message", event.message()))
                        ));
                return;
            }

            final var player = event.getPlayer();
            if (player == null)
                return;

            final var gamePlayer = findGamePlayer(player);
            if (gamePlayer == null)
                return;

            final var team = gamePlayer.getTeam();
            if (team == null)
                return;

            event.message(chatService.format("<accent><player><base> [<team>]<base> <b>→</b> <text><message>",
                    team.getColorScheme(), TagResolver.resolver(
                            Placeholder.component("player", player.displayName()),
                            Placeholder.component("message", event.message())),
                            Placeholder.parsed("team", team.getBiome() == null ? "none yet" : team.getBiome().toString())
                    ));
        }

        // Protection Handler (place/break blocks)
        @EventHandler(priority = EventPriority.HIGH)
        public void onPlayerPlace(BlockPlaceEvent event) {
            if (state != GameState.RUNNING || event.isCancelled())
                return;

            final var player = event.getPlayer();
            final var gamePlayer = findGamePlayer(player);
            if (gamePlayer == null)
                return;

            final var team = gamePlayer.getTeam();
            if (team == null)
                return;

            final var block = event.getBlock();
            if (block.getLocation().getWorld() != getWorld()) {
                event.setCancelled(true);
                return;
            }

            BukkitUtils.editBlockPdc(block, pdc ->
                    pdc.set(Keys.GAME_PLACED_BLOCK_KEY, PersistentDataType.STRING, player.getUniqueId().toString()));
            placedBlocks.put(block.getLocation(), block);
        }

        @EventHandler(priority = EventPriority.HIGH)
        public void onPlayerBreak(@NotNull BlockBreakEvent event) {
            if (state != GameState.RUNNING || event.isCancelled() || event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) // creative players can break blocks
                return;

            final var player = event.getPlayer();
            final var gamePlayer = findGamePlayer(player);
            if (gamePlayer == null)
                return;

            final var block = event.getBlock();
            if (block.getLocation().getWorld() != getWorld()) {
                event.setCancelled(true);
                return;
            }

            final var pdc = BukkitUtils.getBlockPdc(block);
            if (pdc == null || !pdc.has(Keys.GAME_PLACED_BLOCK_KEY, PersistentDataType.STRING))
                event.setCancelled(true);

            placedBlocks.remove(block.getLocation());
        }

        // Handler for abilities
        @EventHandler(priority = EventPriority.HIGH)
        public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
            final var player = event.getPlayer();
            if (state != GameState.RUNNING)
                return;
            final var gamePlayer = findGamePlayer(player);
            if (gamePlayer == null)
                return;

            final var heldItem = event.getItem();
            if (gamePlayer.getSelectedAbility() == null || heldItem == null || heldItem.getType() == Material.AIR)
                return;

            final var abilityKey = heldItem.getPersistentDataContainer().get(Keys.ABILITY_KEY, PersistentDataType.STRING);
            if (abilityKey == null || !abilityKey.equals(gamePlayer.getSelectedAbility().getId()))
                return;

            final var ability = gamePlayer.getSelectedAbility();
            if (ability.getType() == AbilityType.ACTIVE && ability instanceof final ActiveAbility activeAbility) {
                if (activeAbility.use(gamePlayer, Game.this)) {
                    gamePlayer.setupHotbar(false);
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // Prevent items from generators to stacks
        @EventHandler(priority = EventPriority.HIGH)
        public void onItemStack(@NotNull ItemMergeEvent event) {
            final var entity = event.getEntity();
            if (!entity.getPersistentDataContainer().has(Keys.GENERATOR_ITEM_KEY, UUIDTagType.INSTANCE))
                return;

            event.setCancelled(true);
        }
    }

    //endregion

    public <T> T getService(Class<T> serviceClass) {
        return serviceProvider.getService(serviceClass);
    }
}
