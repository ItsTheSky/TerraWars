package net.itsthesky.terrawars.core.services;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.StringArgument;
import net.itsthesky.terrawars.api.model.game.IGame;
import net.itsthesky.terrawars.api.model.game.IGameTeam;
import net.itsthesky.terrawars.api.services.*;
import net.itsthesky.terrawars.api.services.base.IService;
import net.itsthesky.terrawars.api.services.base.IServiceProvider;
import net.itsthesky.terrawars.api.services.base.Inject;
import net.itsthesky.terrawars.api.services.base.Service;
import net.itsthesky.terrawars.core.config.GameConfig;
import net.itsthesky.terrawars.core.config.GameTeamConfig;
import net.itsthesky.terrawars.core.gui.ShopKeeperGui;
import net.itsthesky.terrawars.core.impl.ability.tundra.IglooAbility;
import net.itsthesky.terrawars.core.impl.game.Game;
import net.itsthesky.terrawars.util.Checks;
import net.itsthesky.terrawars.util.Colors;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GameService implements IGameService, IService {

    @Inject
    private ICommandService commandService;
    @Inject
    private IChatService chatService;
    @Inject
    private IServiceProvider serviceProvider;
    @Inject
    private IConfigService configService;
    @Inject
    private IBaseGuiControlsService baseGuiControlsService;
    @Inject
    private IGameEditorGuiProvider gameEditorGuiProvider;

    private final Map<UUID, IGame> games = new HashMap<>();

    @Override
    public @NotNull Set<IGame> getGames() {
        return Set.copyOf(games.values());
    }

    @Override
    public @Nullable IGame getGame(@NotNull UUID uuid) {
        Checks.notNull(uuid, "UUID cannot be null");

        return games.get(uuid);
    }

    @Override
    public @NotNull IGame createGame(@NotNull GameConfig config) {
        Checks.notNull(config, "Config cannot be null");

        final var game = new Game(serviceProvider, config);
        games.put(game.getId(), game);
        game.setState(IGame.GameState.WAITING);
        // game.setAvailableBiomes(availableBiomes);

        return game;
    }

    @Override
    public @Nullable IGame getPlayerGame(@NotNull UUID playerId) {
        Checks.notNull(playerId, "Player ID cannot be null");

        return games.values().stream()
                .filter(game -> game.findGamePlayer(playerId) != null)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void init() {
        commandService.registerCommand(new CommandAPICommand("games")
                .withSubcommand(new CommandAPICommand("select_ability")
                        .executesPlayer((player, args) -> {
                            final var game = getPlayerGame(player.getUniqueId());
                            if (game == null) {
                                chatService.sendMessage(player, IChatService.MessageSeverity.ERROR, "You are not in a game!");
                                return;
                            }

                            if (game.getState() != IGame.GameState.RUNNING) {
                                chatService.sendMessage(player, IChatService.MessageSeverity.ERROR, "You can only select an ability in a running game!");
                                return;
                            }

                            final var gamePlayer = game.findGamePlayer(player);
                            final var abilities = gamePlayer.getTeam().getBiome().getAvailableAbilities();
                            final var firstAbility = abilities.stream().findFirst();
                            if (firstAbility.isEmpty()) {
                                chatService.sendMessage(player, IChatService.MessageSeverity.ERROR, "No abilities available for your team!");
                                return;
                            }

                            gamePlayer.setSelectedAbility(firstAbility.get());
                        }))
                .withSubcommand(new CommandAPICommand("list")
                        .executesPlayer((player, args) -> {
                            if (games.isEmpty()) {
                                chatService.sendMessage(player, IChatService.MessageSeverity.INFO, "No <accent>games<text> available!");
                                return;
                            }

                            chatService.sendMessage(player, IChatService.MessageSeverity.NEUTRAL, "<accent>" + games.size() + "<text> game(s) available:");
                            for (final var game : games.values()) {
                                if (!game.isRunning()) {
                                    chatService.sendMessage(player, IChatService.MessageSeverity.NEUTRAL, "<accent>- <base><click:copy_to_clipboard:'" + game.getId() + "'>" + game.getId() + "</click> (" + game.getState().name() + ") - " + game.getState().name() + " - " + game.getWaitingPlayers().size() + "/" + game.getMaxPlayers() + " players waitings");
                                } else {
                                    chatService.sendMessage(player, IChatService.MessageSeverity.SUCCESS, "<accent>- <base><click:copy_to_clipboard:'" + game.getId() + "'>" + game.getId() + "</click> (" + game.getState().name() + ") - " + game.getState().name() + " - "
                                            + game.getTeams().stream().map(IGameTeam::getPlayers).collect(Collectors.toSet()).size() + "<text> players playing");
                                }
                            }
                        }))
                .withSubcommand(new CommandAPICommand("sample_game")
                        .executesPlayer((player, args) -> {
                            final var game = createGame(configService.load(GameConfig.class, "games" + File.separator + "sample_game.json"));
                            for (final var otherPlayer : Bukkit.getOnlinePlayers())
                                game.tryAddPlayer(otherPlayer);
                            ((Game) game).setupStartedGame();
                        }))
                .withSubcommand(new CommandAPICommand("shop")
                        .executesPlayer((player, args) -> {
                            final var game = getPlayerGame(player.getUniqueId());
                            if (game == null) {
                                chatService.sendMessage(player, IChatService.MessageSeverity.ERROR, "You are not in a game!");
                                return;
                            }

                            if (game.getState() != IGame.GameState.RUNNING) {
                                chatService.sendMessage(player, IChatService.MessageSeverity.ERROR, "You can only select an ability in a running game!");
                                return;
                            }

                            final var gamePlayer = game.findGamePlayer(player);
                            final var gui = new ShopKeeperGui(game, gamePlayer);
                            gui.show(player);
                        }))
                .withSubcommand(new CommandAPICommand("join")
                        .withArguments(List.of(new StringArgument("game_id")))
                        .executesPlayer((player, args) -> {
                            final var gameId = UUID.fromString((String) Objects.requireNonNull(args.get("game_id")));
                            final var game = games.get(gameId);

                            if (game == null) {
                                chatService.sendMessage(player, IChatService.MessageSeverity.ERROR, "Game with ID <base>" + gameId + "<text> not found!");
                                return;
                            }

                            if (!game.tryAddPlayer(player)) {
                                chatService.sendMessage(player, IChatService.MessageSeverity.ERROR, "Game is full or you are already in the game!");
                                return;
                            }

                            chatService.sendMessage(player, IChatService.MessageSeverity.SUCCESS, "You joined the game with ID <base>" + gameId + "<text>!");
                        }))
                .withSubcommand(new CommandAPICommand("edit")
                        .withArguments(new StringArgument("name"))
                        .executesPlayer((player, args) -> {
                            final String configName = (String) args.get("name");

                            // Check if file exists
                            final File configFile = configService.getFile("games", configName + ".json");
                            if (!configFile.exists()) {
                                chatService.sendMessage(player, IChatService.MessageSeverity.ERROR,
                                        "Game configuration <accent>" + configName + "<text> does not exist!");
                                return;
                            }

                            // Load the game configuration
                            GameConfig gameConfig;
                            try {
                                gameConfig = configService.load(GameConfig.class, "games" + File.separator + configName + ".json");
                            } catch (Exception e) {
                                chatService.sendMessage(player, IChatService.MessageSeverity.ERROR,
                                        "Failed to load game configuration: <base>" + e.getMessage());
                                e.printStackTrace();
                                return;
                            }

                            gameConfig.setSaveRunnable(() -> {
                                try {
                                    configService.save(gameConfig, "games" + File.separator + configName + ".json");
                                } catch (Exception e) {
                                    chatService.sendMessage(player, IChatService.MessageSeverity.ERROR,
                                            "Failed to create/save game configuration: <base>" + e.getMessage());
                                    e.printStackTrace();
                                }
                            });

                            // Show the GUI
                            final var newGui = gameEditorGuiProvider.createGameEditorGui(gameConfig);
                            newGui.show(player);
                        }))
                .withSubcommand(new CommandAPICommand("create")
                        .withArguments(new StringArgument("name"))
                        .executesPlayer((player, args) -> {
                            final String configName = (String) args.get("name");

                            // Check if file already exists
                            final File configFile = configService.getFile("games", configName + ".json");
                            if (configFile.exists()) {
                                chatService.sendMessage(player, IChatService.MessageSeverity.ERROR,
                                        "A game configuration with name <accent>" + configName + "<text> already exists! Use the <base>/games edit <accent>" + configName + "<text> command to edit it.");
                                return;
                            }

                            // Create new GameConfig with default values
                            final GameConfig gameConfig = new GameConfig();
                            for (int i = 0; i < 4; i++)
                                gameConfig.getTeams().add(new GameTeamConfig());

                            gameConfig.setSaveRunnable(() -> {
                                try {
                                    configService.save(gameConfig, "games" + File.separator + configName + ".json");
                                } catch (Exception e) {
                                    chatService.sendMessage(player, IChatService.MessageSeverity.ERROR,
                                            "Failed to create/save game configuration: <base>" + e.getMessage());
                                    e.printStackTrace();
                                }
                            });

                            final var newGui = gameEditorGuiProvider.createGameEditorGui(gameConfig);
                            newGui.show(player);
                        }))
                .withSubcommand(new CommandAPICommand("load")
                        .withArguments(new StringArgument("name"))
                        .executesPlayer((player, args) -> {
                            final String configName = (String) args.get("name");

                            // Check if file exists
                            final File configFile = configService.getFile("games", configName + ".json");
                            if (!configFile.exists()) {
                                chatService.sendMessage(player, IChatService.MessageSeverity.ERROR,
                                        "Game configuration <accent>" + configName + "<text> does not exist!");
                                return;
                            }

                            try {
                                final GameConfig gameConfig = configService.load(GameConfig.class, "games" + File.separator + configName + ".json");
                                final var game = createGame(gameConfig);

                                chatService.sendMessage(player, IChatService.MessageSeverity.SUCCESS,
                                        "Game created successfully! ID: <accent>" + game.getId());

                                // Show title to player
                                chatService.sendTitle(new IChatService.TitleBuilder()
                                        .audience(player)
                                        .title("Game Created")
                                        .subtitle("ID: " + game.getId())
                                        .scheme(Colors.GREEN)
                                        .fadeIn(Duration.ofMillis(500))
                                        .stay(Duration.ofSeconds(3))
                                        .fadeOut(Duration.ofMillis(500))
                                );

                            } catch (Exception e) {
                                chatService.sendMessage(player, IChatService.MessageSeverity.ERROR,
                                        "Failed to load game configuration: <base>" + e.getMessage());
                                e.printStackTrace();
                            }
                        }))
        );
    }

    private String[] getAvailableConfigs() {
        final File gamesDir = configService.getFile("games");
        if (!gamesDir.exists() || !gamesDir.isDirectory()) {
            return new String[0];
        }

        return Arrays.stream(Objects.requireNonNull(gamesDir.listFiles()))
                .filter(file -> file.isFile() && file.getName().endsWith(".json"))
                .map(file -> file.getName().replace(".json", ""))
                .toArray(String[]::new);
    }

    @Override
    public void destroy() {
        for (IGame game : games.values())
            game.cleanupGame();
    }
}
