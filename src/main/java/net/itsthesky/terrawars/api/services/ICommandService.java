package net.itsthesky.terrawars.api.services;

import dev.jorel.commandapi.CommandAPICommand;
import org.jetbrains.annotations.NotNull;

public interface ICommandService {

    void registerCommand(@NotNull CommandAPICommand command);

    void registerSubCommand(@NotNull CommandAPICommand command);
}