package net.itsthesky.terrawars.core.services;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandAPICommand;
import net.itsthesky.terrawars.TerraWars;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.api.services.ICommandService;
import net.itsthesky.terrawars.api.services.base.IService;
import net.itsthesky.terrawars.api.services.base.Service;
import net.itsthesky.terrawars.util.Checks;
import org.jetbrains.annotations.NotNull;

@Service
public class CommandService implements ICommandService, IService {

    private IChatService chatService;
    private CommandAPICommand mainCommand;

    public CommandService(@NotNull TerraWars plugin) {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(plugin)
                .setNamespace("terrawars"));
        CommandAPI.onEnable();

        mainCommand = new CommandAPICommand("terrawars")
                .withPermission("terrawars.command")
                .withSubcommand(new CommandAPICommand("test_msg")
                        .executesPlayer((sender, args) -> {
                            for (final var sevirity : IChatService.MessageSeverity.values())
                                chatService.sendMessage(sender, sevirity, "This is " + sevirity.name() + " message :)");
                        }));
    }

    @Override
    public void registerCommand(@NotNull CommandAPICommand command) {
        Checks.notNull(command, "Command cannot be null");

        command.register("terrawars");
    }

    @Override
    public void init() {
        registerCommand(mainCommand);
    }

    @Override
    public void registerSubCommand(@NotNull CommandAPICommand command) {
        Checks.notNull(command, "Command cannot be null");

        if (mainCommand == null)
            throw new IllegalStateException("Main command is not registered yet.");

        registerCommand(mainCommand.withSubcommand(command));
    }
}
