package net.itsthesky.terrawars.core.services;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.arguments.WorldArgument;
import net.itsthesky.terrawars.api.model.biome.IBiome;
import net.itsthesky.terrawars.api.model.game.IGame;
import net.itsthesky.terrawars.api.model.game.IGameTeam;
import net.itsthesky.terrawars.api.services.*;
import net.itsthesky.terrawars.api.services.base.IService;
import net.itsthesky.terrawars.api.services.base.IServiceProvider;
import net.itsthesky.terrawars.api.services.base.Inject;
import net.itsthesky.terrawars.api.services.base.Service;
import net.itsthesky.terrawars.core.config.GameConfig;
import net.itsthesky.terrawars.core.config.GameTeamConfig;
import net.itsthesky.terrawars.core.impl.game.Game;
import net.itsthesky.terrawars.util.Checks;
import net.itsthesky.terrawars.util.Colors;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class GameService implements IGameService, IService {

    @Inject private ICommandService commandService;
    @Inject private IChatService chatService;
    @Inject private IServiceProvider serviceProvider;
    @Inject private IConfigService configService;
    @Inject private IConfigInterfaceService configInterfaceService;

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
    public @NotNull IGame createGame(@NotNull World world,
                                     @NotNull Location lobby,
                                     IGame.@NotNull GameSize size,
                                     @NotNull Set<IBiome> availableBiomes) {
        Checks.notNull(world, "World cannot be null");
        Checks.notNull(lobby, "Lobby location cannot be null");
        Checks.notNull(size, "Game size cannot be null");
        Checks.notNull(availableBiomes, "Available biomes cannot be null");

        // Checks.isTrue(availableBiomes.size() > 0, "Available biomes cannot be empty");
        Checks.isTrue(availableBiomes.size() <= 4, "Available biomes cannot be more than 4");

        final var game = new Game(serviceProvider, world, size, lobby);
        games.put(game.getId(), game);
        game.setState(IGame.GameState.WAITING);
        // game.setAvailableBiomes(availableBiomes);

        return game;
    }

    @Override
    public void init() {
        commandService.registerCommand(new CommandAPICommand("games")
                .withSubcommand(new CommandAPICommand("old_create")
                        .withArguments(List.of(
                                new WorldArgument("game_world"),
                                new MultiLiteralArgument("game_size", "SOLO", "DUO", "SQUAD")
                        ))
                        .executesPlayer((player, args) -> {
                            final var world = (World) args.get("game_world");
                            final var size = IGame.GameSize.valueOf(((String) Objects.requireNonNull(args.get("game_size"))).toUpperCase(Locale.ROOT));

                            assert world != null;
                            final var game = createGame(world, player.getLocation(), size, Set.of());
                            chatService.sendMessage(player, IChatService.MessageSeverity.SUCCESS, "Game created with size " + size.name() + " in world " + world.getName() + " at location " + player.getLocation() + " ID: " + game.getId());
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
                                    chatService.sendMessage(player, IChatService.MessageSeverity.NEUTRAL, "<accent>- <base><click:copy_to_clipboard:'"+ game.getId() +"'>" + game.getId() + "</click> (" + game.getState().name() + ") - " + game.getState().name() + " - " + game.getWaitingPlayers().size() + "/" + game.getMaxPlayers() + " players waitings");
                                } else {
                                    chatService.sendMessage(player, IChatService.MessageSeverity.SUCCESS, "<accent>- <base><click:copy_to_clipboard:'"+ game.getId() +"'>" + game.getId() + "</click> (" + game.getState().name() + ") - " + game.getState().name() + " - "
                                            + game.getTeams().stream().map(IGameTeam::getPlayers).collect(Collectors.toSet()).size() + "<text> players playing");
                                }
                            }
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
                .withSubcommand(new CommandAPICommand("create")
                        .withArguments(new StringArgument("name"))
                        .executesPlayer((player, args) -> {
                            final String configName = (String) args.get("name");

                            // Check if file already exists
                            final File configFile = configService.getFile("games", configName + ".json");
                            if (configFile.exists()) {
                                chatService.sendMessage(player, IChatService.MessageSeverity.ERROR,
                                        "A game configuration with name <accent>" + configName + "<text> already exists!");
                                return;
                            }

                            // Create new GameConfig with default values
                            final GameConfig gameConfig = new GameConfig();

                            // Initialize with 4 teams
                            List<GameTeamConfig> teams = new ArrayList<>(4);
                            for (int i = 0; i < 4; i++)
                                teams.add(new GameTeamConfig());
                            gameConfig.setTeams(teams);

                            // Open the configuration GUI
                            chatService.sendMessage(player, IChatService.MessageSeverity.INFO,
                                    "Creating a new game configuration: <accent>" + configName);

                            configInterfaceService.openConfigGui(player, gameConfig, "Game Config: " + configName, (config) -> {
                                // When saved, write to file
                                try {
                                    // Create directories if needed
                                    final File gamesDir = configService.getFile("games");
                                    if (!gamesDir.exists()) {
                                        gamesDir.mkdirs();
                                    }

                                    configService.save(config, "games" + File.separator + configName + ".json");

                                    chatService.sendMessage(player, IChatService.MessageSeverity.SUCCESS,
                                            "Game configuration <accent>" + configName + "<text> saved successfully! You may now load it with /games load <accent>" + configName);
                                } catch (Exception e) {
                                    chatService.sendMessage(player, IChatService.MessageSeverity.ERROR,
                                            "Failed to save game configuration: <base>" + e.getMessage());
                                    e.printStackTrace();
                                }
                            });
                        })
                .withSubcommand(new CommandAPICommand("load")
                        .withArguments(new StringArgument("name")))
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
                                // Load the config
                                final GameConfig gameConfig = configService.load(GameConfig.class, "games" + File.separator + configName + ".json");

                                // Create the game
                                final var game = createGame(
                                        gameConfig.getWorld(),
                                        gameConfig.getLobby(),
                                        gameConfig.getGameSize(),
                                        Set.of()
                                );

                                chatService.sendMessage(player, IChatService.MessageSeverity.SUCCESS,
                                        "Game created successfully! ID: <accent>" + game.getId());

                                // Show title to player
                                chatService.sendTitle(new IChatService.TitleBuilder()
                                        .audience(player)
                                        .title("Game Created")
                                        .subtitle("ID: " + game.getId())
                                        .scheme(Colors.GREEN)
                                        .fadeIn(java.time.Duration.ofMillis(500))
                                        .stay(java.time.Duration.ofSeconds(3))
                                        .fadeOut(java.time.Duration.ofMillis(500))
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
}
