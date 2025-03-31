package net.itsthesky.terrawars.core.services;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandAPICommand;
import net.itsthesky.terrawars.TerraWars;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.api.services.ICommandService;
import net.itsthesky.terrawars.api.services.base.IService;
import net.itsthesky.terrawars.api.services.base.Inject;
import net.itsthesky.terrawars.api.services.base.Service;
import net.itsthesky.terrawars.util.Checks;
import org.jetbrains.annotations.NotNull;

@Service
public class CommandService implements ICommandService, IService {

    @Inject
    private IChatService chatService;

    public CommandService(@NotNull TerraWars plugin) {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(plugin)
                .setNamespace("terrawars"));
        CommandAPI.onEnable();
    }

    @Override
    public void registerCommand(@NotNull CommandAPICommand command) {
        Checks.notNull(command, "Command cannot be null");

        command.register("terrawars");
    }

    @Override
    public void init() {
        registerCommand(new CommandAPICommand("terrawars")
                .withPermission("terrawars.command")
                .executesPlayer((sender, args) -> {
                    for (final var sevirity : IChatService.MessageSeverity.values()) {
                        chatService.sendMessage(sender, sevirity, "This is " + sevirity.name() + " message :)");
                    }
                }));
    }
}
