package net.itsthesky.terrawars.core.services;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
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
import net.itsthesky.terrawars.core.impl.ability.snow.IglooAbility;
import net.itsthesky.terrawars.core.impl.game.Game;
import net.itsthesky.terrawars.util.Checks;
import net.itsthesky.terrawars.util.Colors;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                            gamePlayer.setSelectedAbility(new IglooAbility());
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
                        .withSubcommand(new CommandAPICommand("smaple_game")
                                .executesPlayer((player, args) -> {
                                    // we'll basically replicate the game creation process here, so
                                    // 1. create a new game
                                    // 2. make the player joins it

                                    final var game = createGame(player.getWorld(), player.getLocation(), IGame.GameSize.SOLO, Set.of());
                                    game.tryAddPlayer(player);
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

                            final var gui = new ChestGui(6, "Game Configuration");
                            final var pane = new StaticPane(0, 0, 9, 6);
                            pane.addItem(baseGuiControlsService.createChatInputControl("Enter the unique game name used in configurations.", name -> name.length() > 5,
                                    new IBaseGuiControlsService.InputControlData<>(
                                            Material.PAPER,
                                            null, null,
                                            "Game Name",
                                            List.of("The unique Game Name requested for the game.", "Another line to try it out!"),
                                            (name, inputData) -> {
                                                chatService.sendMessage(player, IChatService.MessageSeverity.WARNING,
                                                        "Game name set to <accent>" + name + "<text>!");
                                                inputData.setCurrentValue(name);
                                            },
                                            (evt, inputData) -> {
                                                chatService.sendMessage(player, IChatService.MessageSeverity.WARNING,
                                                        "Cancel requested.");
                                            },
                                            (evt, inputData) -> {
                                                chatService.sendMessage(player, IChatService.MessageSeverity.WARNING,
                                                        "Reset requested.");
                                                inputData.setCurrentValue(null);
                                            }
                                    )), 0, 0);
                            pane.addItem(baseGuiControlsService.createComboBoxInputControl(Arrays.stream(IGame.GameSize.values()).toList(),
                                    new IBaseGuiControlsService.InputControlData<>(
                                            Material.NOTE_BLOCK,
                                            null, IGame.GameSize.SOLO,
                                            "Game Size",
                                            List.of("Yay, the game size! Very funny <red>right?"),
                                            (name, inputData) -> inputData.setCurrentValue(name),
                                            (evt, inputData) -> { },
                                            (evt, inputData) -> inputData.setCurrentValue(null)
                                    ), size -> switch (size) {
                                        case SOLO -> "Solo (1v1v1v1)";
                                        case DUO -> "Duo (2v2v2v2)";
                                        case SQUAD -> "Squad (4v4v4v4)";
                                    }), 1, 0);
                            pane.addItem(baseGuiControlsService.createLocationInputControl("Move to the desired location you mother fucker!", new IBaseGuiControlsService.InputControlData<>(
                                    Material.BEACON,
                                    null, null,
                                    "Lobby Location",
                                    List.of("The location of the lobby.", "Another line to try it out!"),
                                    (location, inputData) -> {
                                        chatService.sendMessage(player, IChatService.MessageSeverity.WARNING,
                                                "Lobby location set to <accent>" + location + "<text>!");
                                        inputData.setCurrentValue(location);
                                    },
                                    (evt, inputData) -> {
                                        chatService.sendMessage(player, IChatService.MessageSeverity.WARNING,
                                                "Cancel requested.");
                                    },
                                    (evt, inputData) -> {
                                        chatService.sendMessage(player, IChatService.MessageSeverity.WARNING,
                                                "Reset requested.");
                                        inputData.setCurrentValue(null);
                                    }
                            )), 2, 0);
                            pane.addItem(baseGuiControlsService.createBackButton(evt -> {
                                player.closeInventory();
                            }), 0, 5);

                            gui.addPane(pane);
                            gui.show(player);
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
