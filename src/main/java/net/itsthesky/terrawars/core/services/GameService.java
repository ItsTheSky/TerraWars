package net.itsthesky.terrawars.core.services;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.arguments.WorldArgument;
import net.itsthesky.terrawars.api.model.biome.IBiome;
import net.itsthesky.terrawars.api.model.game.IGame;
import net.itsthesky.terrawars.api.model.game.IGameTeam;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.api.services.ICommandService;
import net.itsthesky.terrawars.api.services.IGameService;
import net.itsthesky.terrawars.api.services.base.IService;
import net.itsthesky.terrawars.api.services.base.IServiceProvider;
import net.itsthesky.terrawars.api.services.base.Inject;
import net.itsthesky.terrawars.api.services.base.Service;
import net.itsthesky.terrawars.core.impl.game.Game;
import net.itsthesky.terrawars.util.Checks;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GameService implements IGameService, IService {

    @Inject private ICommandService commandService;
    @Inject private IChatService chatService;
    @Inject private IServiceProvider serviceProvider;

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
                .withSubcommand(new CommandAPICommand("create")
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
        );
    }
}
